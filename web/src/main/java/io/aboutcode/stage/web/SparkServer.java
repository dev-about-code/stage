package io.aboutcode.stage.web;

import com.google.common.base.CharMatcher;
import io.aboutcode.stage.dispatch.Dispatcher;
import io.aboutcode.stage.web.request.Part;
import io.aboutcode.stage.web.request.RequestHandler;
import io.aboutcode.stage.web.request.RequestType;
import io.aboutcode.stage.web.response.InternalServerError;
import io.aboutcode.stage.web.response.Ok;
import io.aboutcode.stage.web.util.HeaderAccess;
import io.aboutcode.stage.web.websocket.WebsocketEndpoint;
import io.aboutcode.stage.web.websocket.io.WebsocketIo;
import java.io.IOException;
import java.io.InputStream;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.servlet.MultipartConfigElement;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import spark.FilterImpl;
import spark.Request;
import spark.Response;
import spark.RouteImpl;
import spark.Service;
import spark.route.HttpMethod;

/**
 * <p>This is the default implementation of the spark http server, used by the {@link
 * WebServerComponent}.</p>
 */
final class SparkServer {

    private static final Logger LOGGER = LoggerFactory.getLogger(SparkServer.class);
    private static final String KEY_RESPONSE = "-RESPONSE-";
    private static final String KEY_ACCEPT_TYPE = "Accept";
    private final int port;
    private final TslConfiguration tslConfiguration;
    private final String staticFilesFolder;
    private final boolean isStaticFolderExternal;
    private final List<Route> routes;
    private final Set<WebsocketEndpoint> websocketEndpoints;
    private final WebsocketIo websocketIo;
    private final Dispatcher<RequestType, ServiceRequestProcessor> SERVICE_PROCESSORS =
            Dispatcher
                    .of(RequestType.AFTER_ALL, filter(HttpMethod.after))
                    .with(RequestType.BEFORE_ALL, filter(HttpMethod.before))
                    .with(RequestType.GET, route(HttpMethod.get))
                    .with(RequestType.POST, route(HttpMethod.post))
                    .with(RequestType.PUT, route(HttpMethod.put))
                    .with(RequestType.DELETE, route(HttpMethod.delete))
                    .with(RequestType.OPTIONS, route(HttpMethod.options))
                    .with(RequestType.PATCH, route(HttpMethod.patch));
    private Service sparkService;

    /**
     * Creates a new component.
     *
     * @param port                   The port to run the server on
     * @param tslConfiguration       The (optional) ssl configuration parameters.
     * @param staticFilesFolder      The folder to serve static files from
     * @param isStaticFolderExternal If true, the folder is considered external and reloaded live
     * @param routes                 The routes this server should be processing
     * @param websocketEndpoints     The endpoints for processing websocket communication
     * @param websocketIo            The io controller for the websocket connection
     */
    SparkServer(int port,
                TslConfiguration tslConfiguration,
                String staticFilesFolder,
                boolean isStaticFolderExternal,
                List<Route> routes,
                Set<WebsocketEndpoint> websocketEndpoints,
                WebsocketIo websocketIo) {
        this.port = port;
        this.tslConfiguration = tslConfiguration;
        this.staticFilesFolder = staticFilesFolder;
        this.isStaticFolderExternal = isStaticFolderExternal;
        this.routes = routes;
        this.websocketEndpoints = websocketEndpoints;
        this.websocketIo = websocketIo;
    }

    private static io.aboutcode.stage.web.request.Request request(Request rawRequest) {
        return new DefaultRequest(rawRequest);
    }

    private static io.aboutcode.stage.web.response.Response getCurrentResponse(
            io.aboutcode.stage.web.request.Request request) {
        return (io.aboutcode.stage.web.response.Response) request
                .attribute(KEY_RESPONSE)
                .orElse(Ok.create());
    }

    private static io.aboutcode.stage.web.response.Response process(
            io.aboutcode.stage.web.request.Request request,
            RequestHandler requestHandler) {
        io.aboutcode.stage.web.response.Response currentResponse = getCurrentResponse(request);

        io.aboutcode.stage.web.response.Response response;
        try {
            response = requestHandler.process(request, currentResponse);
        } catch (Exception e) {
            LOGGER.error("Processing request caused error: {}", e.getMessage(), e);
            response = InternalServerError.with(String.format("Processing request caused error: %s",
                                                              e.getMessage()));
        }

        // this header should be removed by default, let's do it here
        response.header("Server", "");

        return response;
    }

    private static String string(Route route) {
        return String.format("%s:%s", route.getType(), route.getPath());
    }

    private ServiceRequestProcessor filter(HttpMethod method) {
        return (service, route) ->
                service.addFilter(method,
                                  new FilterImpl(route.getPath(), "*/*") {
                                      @Override
                                      public void handle(Request rawRequest, Response rawResponse) {
                                          io.aboutcode.stage.web.request.Request request = request(
                                                  rawRequest);
                                          io.aboutcode.stage.web.response.Response response =
                                                  process(request,
                                                          route.getRequestHandler());

                                          apply(rawResponse, request, response);
                                      }
                                  });
    }

    private ServiceRequestProcessor route(HttpMethod method) {
        return (service, route) ->
                service.addRoute(method,
                                 new RouteImpl(route.getPath(), "*/*") {
                                     @Override
                                     public Object handle(Request rawRequest,
                                                          Response rawResponse) {
                                         io.aboutcode.stage.web.request.Request request = request(
                                                 rawRequest);

                                         // has the request been finished before? Then we do not process it
                                         io.aboutcode.stage.web.response.Response response = getCurrentResponse(
                                                 request);
                                         if (!response.finished()) {
                                             // routes always finish a request
                                             response = process(request, route.getRequestHandler());
                                         }
                                         return apply(rawResponse, request, response);
                                     }
                                 });
    }

    private Object apply(Response rawResponse,
                         io.aboutcode.stage.web.request.Request request,
                         io.aboutcode.stage.web.response.Response response) {
        HttpServletResponse servletResponse = rawResponse.raw();

        // if content-type is not set on response, set to requested content-type
        if (Objects.isNull(response.contentType())) {
            request.header(KEY_ACCEPT_TYPE)
                   .map(HeaderAccess::acceptHeader)
                   .flatMap(types -> types.stream().findFirst())
                   .ifPresent(response::contentType);
        }

        // add headers to spark response
        response.headers()
                .forEach(servletResponse::setHeader);

        request.attribute(KEY_RESPONSE, response);

        // set status and return response
        rawResponse.status(response.status());
        // returning empty string as to not trigger a 404 in the Spark framework
        return Optional.ofNullable(response.data()).orElse("");
    }

    private void assign(Service service, Route route) {
        SERVICE_PROCESSORS
                .dispatch(route.getType())
                .orElseThrow(() -> new IllegalArgumentException(
                        String.format("Type %s cannot be processed", route.getType().name())))
                .process(service, route);
    }

    final void start() {
        this.sparkService = Service.ignite().port(port);

        if (tslConfiguration != null) {
            this.sparkService.secure(
                    tslConfiguration.getKeyStoreLocation(),
                    tslConfiguration.getKeyStorePassword(),
                    tslConfiguration.getTrustStoreLocation(),
                    tslConfiguration.getTrustStorePassword(),
                    tslConfiguration.isClientCertificateRequired()
            );
        }

        // set the default exception handler
        sparkService.initExceptionHandler((e) -> LOGGER.error("Error in webserver: {}",
                                                              e.getMessage(),
                                                              e));

        // process static resources
        if (staticFilesFolder != null) {
            if (isStaticFolderExternal) {
                sparkService.externalStaticFileLocation(staticFilesFolder);
            } else {
                sparkService.staticFileLocation(staticFilesFolder);
            }
        }

        websocketEndpoints
                .forEach(endpoint -> {
                    LOGGER.debug("Adding websocket endpoint: {}", endpoint.getPath());
                    endpoint.initialize();
                    sparkService.webSocket(endpoint.getPath(), endpoint);
                });

        //noinspection UnstableApiUsage
        List<Route> sortedRoutes = routes
                .stream()
                .sorted(
                        // the amount of slashes should be the depth
                        Comparator.<Route>comparingInt(
                                element -> CharMatcher.is('/')
                                                      .countIn(element.getPath()))
                                .reversed() // more slashes go first
                )
                .collect(Collectors.toList());

        // find duplicates
        List<String> duplicatePaths = sortedRoutes
                .stream()
                .collect(Collectors.groupingBy(
                        SparkServer::string,
                        Collectors.counting()
                         )
                )
                .entrySet()
                .stream()
                .filter(entry -> entry.getValue() > 1)
                .map(entry -> String
                        .format("Route '%s' declared %d times", entry.getKey(), entry.getValue()))
                .collect(Collectors.toList());

        if (!duplicatePaths.isEmpty()) {
            duplicatePaths
                    .forEach(path -> LOGGER.error("Duplicate declaration of path '{}'", path));
            throw new IllegalStateException("Duplicate path declarations found for web server");
        }

        sortedRoutes.forEach(route -> {
            LOGGER.debug("Adding route: {} -> {}", route.getType(), route.getPath());
            assign(sparkService, route);
        });

        sparkService.init();
    }

    final void stop() {
        sparkService.stop();
    }

    /**
     * Helper class for the dispatcher that accepts all needed parameters to be able to process a
     * service request.
     */
    private interface ServiceRequestProcessor {
        void process(Service service, Route route);
    }

    private static class DefaultRequest implements io.aboutcode.stage.web.request.Request {
        private final Request rawRequest;

        private DefaultRequest(Request rawRequest) {
            this.rawRequest = rawRequest;
        }

        @Override
        public Optional<Object> attribute(String name) {
            return Optional.ofNullable(rawRequest.attribute(name));
        }

        @Override
        public void attribute(String name, Object value) {
            rawRequest.attribute(name, value);
        }

        @Override
        public Optional<String> pathParam(String name) {
            return Optional.ofNullable(rawRequest.params(name));
        }

        @Override
        public List<String> queryParams(String name) {
            return Optional
                    .ofNullable(rawRequest.queryParamsValues(name))
                    .map(Stream::of)
                    .orElse(Stream.empty())
                    .collect(Collectors.toList());
        }

        @Override
        public Optional<String> queryParam(String name) {
            return Optional
                    .ofNullable(rawRequest.queryParamsValues(name))
                    .map(Stream::of)
                    .orElse(Stream.empty())
                    .findFirst();
        }

        @Override
        public Set<String> queryParams() {
            return rawRequest.queryParams();
        }

        @Override
        public String body() {
            return rawRequest.body();
        }

        @Override
        public Optional<String> header(String name) {
            return Optional.ofNullable(rawRequest.headers(name));
        }

        @Override
        public Set<String> headers() {
            return rawRequest.headers();
        }

        @Override
        public RequestType method() {
            String methodString = rawRequest.requestMethod().toUpperCase();
            return RequestType.valueOf(methodString);
        }

        @Override
        public Session session() {
            return new Session() {
                @Override
                public <T> Optional<T> attribute(String name) {
                    return Optional.ofNullable(rawRequest.session().attribute(name));
                }

                @Override
                public void attribute(String name, Object value) {
                    rawRequest.session().attribute(name, value);
                }
            };
        }

        @Override
        public String path() {
            return rawRequest.pathInfo();
        }

        @Override
        public Stream<Part> parts() throws IOException {
            rawRequest.attribute("org.eclipse.jetty.multipartConfig",
                                 new MultipartConfigElement((String) null));

            try {
                return rawRequest.raw().getParts().stream().map(WrappingPart::new);
            } catch (ServletException e) {
                throw new IOException(e);
            }
        }
    }

    private static class WrappingPart implements Part {
        private final javax.servlet.http.Part delegate;

        private WrappingPart(javax.servlet.http.Part delegate) {
            this.delegate = delegate;
        }

        @Override
        public InputStream getInputStream() throws IOException {
            return delegate.getInputStream();
        }

        @Override
        public String getContentType() {
            return delegate.getContentType();
        }

        @Override
        public String getName() {
            return delegate.getName();
        }

        @Override
        public String getSubmittedFileName() {
            return delegate.getSubmittedFileName();
        }

        @Override
        public long getSize() {
            return delegate.getSize();
        }
    }
}
