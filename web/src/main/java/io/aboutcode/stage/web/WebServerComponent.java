package io.aboutcode.stage.web;

import io.aboutcode.stage.component.BaseComponent;
import io.aboutcode.stage.dependency.DependencyContext;
import io.aboutcode.stage.dependency.DependencyException;
import io.aboutcode.stage.web.autowire.AutowiringRequestContext;
import io.aboutcode.stage.web.autowire.WebRequestHandler;
import io.aboutcode.stage.web.autowire.WebRequestHandlerParser;
import io.aboutcode.stage.web.autowire.auth.AuthorizationRealm;
import io.aboutcode.stage.web.websocket.AutowiredWebsocketHandler;
import io.aboutcode.stage.web.websocket.WebsocketEndpoint;
import io.aboutcode.stage.web.websocket.WebsocketHandler;
import io.aboutcode.stage.web.websocket.io.WebsocketIo;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * <p>This implements a standard web server component that registers both websocket- and
 * http-endpoints and services them.</p>
 * <p>The component expects implementations of {@link io.aboutcode.stage.web.autowire.WebRequestHandler}
 * and/or {@link io.aboutcode.stage.web.websocket.WebsocketHandler} to be present in the component
 * container. If no such components are present, the server will still start up but will not serve
 * any routes.</p>
 */
final class WebServerComponent extends BaseComponent {
    private final String rootPath;
    private final int port;
    private final String staticFilesFolder;
    private final boolean isStaticFolderExternal;
    private final Set<Class> validEndpoints;
    private final AutowiringRequestContext autowiringRequestContext;
    private WebsocketIo websocketIo;
    private TslConfiguration tslConfiguration;

    private SparkServer sparkServer;

    /**
     * Creates a new component.
     *
     * @param rootPath                 The root path of all endpoints
     * @param port                     The port on which to listen for connections.
     * @param staticFilesFolder        The folder to expose static files from
     * @param isStaticFolderExternal   If true, the static files are reloaded live
     * @param autowiringRequestContext The object that allows access to the applications context
     * @param tslConfiguration         The (optional) ssl configuration to use
     * @param validEndpoints           If not null, the endpoints this server should use. All other
     *                                 types of endpoints are ignored
     * @param websocketIo              The io controller for the websocket connection
     */
    WebServerComponent(String rootPath,
                       int port,
                       String staticFilesFolder,
                       boolean isStaticFolderExternal,
                       AutowiringRequestContext autowiringRequestContext,
                       TslConfiguration tslConfiguration,
                       Set<Class> validEndpoints,
                       WebsocketIo websocketIo) {
        this.rootPath = rootPath;
        this.port = port;
        this.staticFilesFolder = staticFilesFolder;
        this.isStaticFolderExternal = isStaticFolderExternal;
        this.autowiringRequestContext = autowiringRequestContext;
        this.tslConfiguration = tslConfiguration;
        this.validEndpoints = validEndpoints;
        this.websocketIo = websocketIo;
    }

    @Override
    public final void start() {
        sparkServer.start();
    }

    @Override
    public final void stop() {
        sparkServer.stop();
    }

    @Override
    public void resolve(DependencyContext context) throws DependencyException {
        sparkServer = new SparkServer(port,
                                      tslConfiguration,
                                      staticFilesFolder,
                                      isStaticFolderExternal,
                                      routes(context),
                                      websocketEndpoints(context),
                                      websocketIo);
    }

    private Set<WebsocketEndpoint> websocketEndpoints(DependencyContext context)
            throws DependencyException {
        return websocketEndpoints(websocketHandlers(context));
    }

    private Set<WebsocketEndpoint> websocketEndpoints(Set<WebsocketHandler> handlers) {
        return handlers.stream()
                       .map(handler -> AutowiredWebsocketHandler.from(rootPath, handler))
                       .collect(Collectors.groupingBy(AutowiredWebsocketHandler::getPath))
                       .entrySet()
                       .stream()
                       .map(entry -> new WebsocketEndpoint(entry.getKey(), websocketIo,
                                                           entry.getValue()))
                       .collect(Collectors.toSet());
    }

    private Set<WebsocketHandler> websocketHandlers(DependencyContext context)
            throws DependencyException {
        if (validEndpoints == null) {
            return context.retrieveDependencies(WebsocketHandler.class);
        }

        Set<WebsocketHandler> websocketHandlers = new HashSet<>();
        for (Class validType : validEndpoints) {
            //noinspection unchecked
            Set elements = context.retrieveDependencies(validType);
            if (WebsocketHandler.class.isAssignableFrom(validType)) {
                //noinspection unchecked
                websocketHandlers.addAll(elements);
            }
        }
        return websocketHandlers;
    }

    private List<Route> routes(DependencyContext context)
            throws DependencyException {
        if (validEndpoints == null) {
            return routes(context.retrieveDependencies(WebRequestHandler.class), context);
        }

        Set<WebRequestHandler> webRequestHandlers = new HashSet<>();
        for (Class validType : validEndpoints) {
            //noinspection unchecked
            Set elements = context.retrieveDependencies(validType);
            if (WebRequestHandler.class.isAssignableFrom(validType)) {
                //noinspection unchecked
                webRequestHandlers.addAll(elements);
            }
        }
        return routes(webRequestHandlers, context);
    }

    private List<Route> routes(Set<WebRequestHandler> webRequestHandlers,
                               DependencyContext context) throws DependencyException {
        WebRequestHandlerParser parser = new WebRequestHandlerParser(
                context.retrieveDependencies(AuthorizationRealm.class),
                autowiringRequestContext
        );

        return parser.parse(rootPath, webRequestHandlers);
    }
}
