package io.aboutcode.stage.web.autowire;

import io.aboutcode.stage.web.Route;
import io.aboutcode.stage.web.autowire.auth.AuthorizationRealm;
import io.aboutcode.stage.web.autowire.auth.Authorized;
import io.aboutcode.stage.web.autowire.auth.PermissiveAuthorizationRealm;
import io.aboutcode.stage.web.autowire.exception.AutowiringException;
import io.aboutcode.stage.web.autowire.versioning.Version;
import io.aboutcode.stage.web.request.Request;
import io.aboutcode.stage.web.request.RequestHandler;
import io.aboutcode.stage.web.response.NotFound;
import io.aboutcode.stage.web.response.Response;
import io.aboutcode.stage.web.util.Paths;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Instances of this transform a {@link WebRequestHandler} containing methods that should serve as
 * web endpoints into an executable format. This executable format handles injection of parameters,
 * conversion of (return) types and exception handling in a generic manner.
 */
public final class WebRequestHandlerParser {
    private static final String VERSION_PATH_PARAMETER = "VERSION_PATH";
    private static final String DEFAULT_PATH = "/";
    private final Set<AuthorizationRealm> availableAuthorizationRealms;
    private final AutowiringRequestContext context;

    /**
     * Creates a new parser with all available {@link AuthorizationRealm}s.
     *
     * @param availableAuthorizationRealms All available {@link AuthorizationRealm}s
     * @param context                      The context used for accessing web server scoped
     *                                     functionality
     */
    public WebRequestHandlerParser(Set<AuthorizationRealm> availableAuthorizationRealms,
                                   AutowiringRequestContext context) {
        this.availableAuthorizationRealms = availableAuthorizationRealms;
        this.context = context;
    }

    private static AuthorizationRealm findRealm(Class<? extends AuthorizationRealm> realmType,
                                                Set<AuthorizationRealm> availableAuthorizationRealms) {
        return availableAuthorizationRealms.stream()
                                           .filter(realm -> Objects
                                                   .equals(realmType,
                                                           realm.getClass()))
                                           .findFirst()
                                           .orElseThrow(() -> new AutowiringException(
                                                   "Security realm not found: " + realmType
                                                           .getSimpleName()));
    }

    private String getBasePath(WebRequestHandler handler) {
        return Optional.ofNullable(handler.getClass().getAnnotation(Path.class))
                       .map(Path::value)
                       .orElse("");

    }

    private Optional<AutowirableMethod> parseMethod(WebRequestHandler handler,
                                                    Method method,
                                                    AuthorizationRealm defaultAuthorizationRealm) {
        return AutowirableMethod.from(handler,
                                      method,
                                      defaultAuthorizationRealm,
                                      availableAuthorizationRealms);
    }

    private AuthorizationRealm getAuthorizationRealm(WebRequestHandler handler,
                                                     Set<AuthorizationRealm> availableAuthoriationRealms) {
        return Optional.ofNullable(handler.getClass().getAnnotation(Authorized.class))
                       .map(Authorized::value)
                       .map(realmType -> findRealm(realmType, availableAuthoriationRealms))
                       .orElse(new PermissiveAuthorizationRealm());

    }

    /**
     * Parses the information on the specified {@link WebRequestHandler}s into a list of {@link
     * Route}s that can be served by a webserver. All handlers that should be served on the same
     * root path <em>must</em> be included to be able to group {@link io.aboutcode.stage.web.autowire.versioning.Versioned}
     * endpoints together.
     *
     * @param rootPath The root path for all routes created through this parser. Defaults to "/" if
     *                 empty.
     * @param handlers The handlers to analyse
     *
     * @return The list of routes that represent the endpoints defined by the specified handlers
     */
    public List<Route> parse(String rootPath, Set<? extends WebRequestHandler> handlers) {
        final String path;
        if (Objects.isNull(rootPath) || rootPath.trim().isEmpty()) {
            path = DEFAULT_PATH;
        } else {
            path = rootPath;
        }

        return handlers.stream()
                       .map(handler -> {
                           String basePath = getBasePath(handler);
                           AuthorizationRealm classAuthorizationRealm = getAuthorizationRealm(
                                   handler,
                                   availableAuthorizationRealms);

                           return Stream.of(handler.getClass().getMethods())
                                        .map(method -> parseMethod(handler, method,
                                                                   classAuthorizationRealm))
                                        .filter(Optional::isPresent)
                                        .map(Optional::get)
                                        .collect(Collectors.groupingBy(
                                                getEndpointIdentifier(path, basePath)))
                                        .entrySet()
                                        .stream()
                                        .map(entry -> asRoute(entry.getKey(), entry.getValue()))
                                        .collect(Collectors.toList());
                       })
                       .flatMap(Collection::stream)
                       .collect(Collectors.toList());
    }

    private Route asRoute(EndpointIdentifier endpointIdentifier, List<AutowirableMethod> methods) {
        // check that no duplicate versions are used
        for (AutowirableMethod method : methods) {
            for (AutowirableMethod current : methods) {
                if (method != current &&
                    current.getVersionRange().overlaps(method.getVersionRange())) {
                    throw new AutowiringException(
                            String.format(
                                    "Endpoint version for method '%s' overlaps with endpoint version for method '%s'",
                                    current.getTargetObjectType().getSimpleName(),
                                    method.getTargetObjectType().getSimpleName())
                    );
                }
            }
        }

        RequestHandler requestHandler = new AutowiredRequestHandler(methods);

        return endpointIdentifier.accessType.route(endpointIdentifier.path, requestHandler);
    }

    private Function<AutowirableMethod, EndpointIdentifier> getEndpointIdentifier(String rootPath,
                                                                                  String basePath) {
        return method -> new EndpointIdentifier(method.getAccessType(),
                                                getPath(method, rootPath, basePath));
    }

    private String getPath(AutowirableMethod method, String rootPath, String basePath) {
        String versionPath = method.getVersionRange() == null ? "" : VERSION_PATH_PARAMETER;
        return Paths.concat(rootPath, basePath, versionPath, method.getPath()).orElse(DEFAULT_PATH);
    }

    private static class EndpointIdentifier {
        private final AccessType accessType;
        private final String path;

        private EndpointIdentifier(AccessType accessType, String path) {
            this.accessType = accessType;
            this.path = path;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }
            final EndpointIdentifier that = (EndpointIdentifier) o;
            return accessType == that.accessType &&
                   Objects.equals(path, that.path);
        }

        @Override
        public int hashCode() {
            return Objects.hash(accessType, path);
        }
    }

    private class AutowiredRequestHandler implements RequestHandler {
        private final List<AutowirableMethod> methods;

        private AutowiredRequestHandler(List<AutowirableMethod> methods) {
            this.methods = methods;
        }

        @Override
        public Response process(Request request, Response currentResponse) {

            return request.pathParam(VERSION_PATH_PARAMETER)
                          .flatMap(Version::from)
                          .map(version -> withVersion(request, version))
                          .orElse(withoutVersion(request));
        }

        private Response withVersion(Request request, Version version) {
            return methods.stream()
                          .filter(method -> method.getVersionRange() != null)
                          .filter(method -> method.getVersionRange().allows(version))
                          .findFirst()
                          .map(method -> method.invokeFromRequest(request, context))
                          .orElse(notFound(request, version));
        }

        private Response withoutVersion(Request request) {
            return methods.stream()
                          .filter(method -> method.getVersionRange() == null)
                          .findFirst()
                          .map(method -> method.invokeFromRequest(request, context))
                          .orElse(notFound(request));
        }

        private Response notFound(Request request, Version version) {
            return NotFound.with(String.format("Endpoint '%s' not available in API version %s",
                                               request.path(),
                                               version));
        }

        private Response notFound(Request request) {
            return NotFound.with(String.format("Endpoint '%s' not available in API",
                                               request.path()));
        }
    }
}
