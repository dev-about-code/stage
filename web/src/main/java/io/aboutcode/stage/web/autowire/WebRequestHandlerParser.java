package io.aboutcode.stage.web.autowire;

import io.aboutcode.stage.web.autowire.auth.AuthorizationRealm;
import io.aboutcode.stage.web.autowire.auth.Authorized;
import io.aboutcode.stage.web.autowire.auth.PermissiveAuthorizationRealm;
import io.aboutcode.stage.web.autowire.exception.AutowiringException;
import io.aboutcode.stage.web.Route;
import io.aboutcode.stage.web.request.Request;
import io.aboutcode.stage.web.request.RequestHandler;
import io.aboutcode.stage.web.response.Response;
import java.lang.reflect.Method;
import java.util.EnumSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Instances of this transform a {@link WebRequestHandler} containing methods that should serve as
 * web endpoints into an executable format. This executable format handles injection of parameters,
 * conversion of (return) types and exception handling in a generic manner.
 */
public final class WebRequestHandlerParser {
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

    private static String getBasePath(WebRequestHandler handler) {
        return Optional.ofNullable(handler.getClass().getAnnotation(Path.class))
                       .map(Path::value)
                       .map(WebRequestHandlerParser::cleanlyCreatePath)
                       .orElse("/");

    }

    private static String cleanlyCreatePath(String inputPath) {
        // prepend and append slash...
        return String.join(inputPath.trim(), "/", "/")
                     // ... then remove double slash at beginning or end
                     .replaceAll("^/?(/.*?)/+$", "$1");
    }

    private static String cleanlyAppendPrefix(String inputPath) {
        // prepend slash...
        return ("/" + inputPath)
                // ... then remove double slash at beginning
                .replaceAll("^/?(/.*)$", "$1");
    }

    private static String sanitize(String inputPath) {
        return inputPath.replaceAll("^/?(/.*?)/*$", "$1");
    }

    private static String fullPath(String basePath, String endpointPath) {
        return sanitize(basePath + cleanlyAppendPrefix(endpointPath));
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

    private RequestHandler requestHandler(WebRequestHandler handler, Method method,
                                          AuthorizationRealm defaultAuthorizationRealm) {
        return new AutowiredRequestHandler(AutowirableMethod.from(handler,
                                                                  method,
                                                                  defaultAuthorizationRealm,
                                                                  availableAuthorizationRealms)
        );
    }

    private Optional<Route> parseMethod(WebRequestHandler handler,
                                        String basePath,
                                        Method method,
                                        AuthorizationRealm defaultAuthorizationRealm) {
        return AccessType.parse(method)
                         .map(accessType -> {
                             String fullPath = fullPath(basePath, accessType.path(method));
                             RequestHandler requestHandler = requestHandler(handler, method,
                                                                            defaultAuthorizationRealm);

                             return accessType.route(fullPath, requestHandler);
                         });
    }

    private AuthorizationRealm getAuthorizationRealm(WebRequestHandler handler,
                                                     Set<AuthorizationRealm> availableAuthoriationRealms) {
        return Optional.ofNullable(handler.getClass().getAnnotation(Authorized.class))
                       .map(Authorized::value)
                       .map(realmType -> findRealm(realmType, availableAuthoriationRealms))
                       .orElse(new PermissiveAuthorizationRealm());

    }

    /**
     * Parses the information on the specified {@link WebRequestHandler} into a list of {@link
     * Route}s that can be served by a webserver.
     *
     * @param handler The handler to analyse
     *
     * @return The list of routes that should be served for the handler
     */
    public List<Route> parse(WebRequestHandler handler) {
        String basePath = getBasePath(handler);
        AuthorizationRealm classAuthorizationRealm = getAuthorizationRealm(handler,
                                                                           availableAuthorizationRealms);

        return Stream.of(handler.getClass().getMethods())
                     .map(method -> parseMethod(handler, basePath, method, classAuthorizationRealm))
                     .filter(Optional::isPresent)
                     .map(Optional::get)
                     .collect(Collectors.toList());

    }

    @SuppressWarnings("unused")
    private enum AccessType {
        GET(GET.class) {
            @Override
            String path(Method method) {
                return method.getAnnotation(GET.class).value();
            }

            @Override
            Route route(String path, RequestHandler requestHandler) {
                return Route.get(path, requestHandler);
            }
        },
        POST(POST.class) {
            @Override
            String path(Method method) {
                return method.getAnnotation(POST.class).value();
            }

            @Override
            Route route(String path, RequestHandler requestHandler) {
                return Route.post(path, requestHandler);
            }
        },
        PATCH(PATCH.class) {
            @Override
            String path(Method method) {
                return method.getAnnotation(PATCH.class).value();
            }

            @Override
            Route route(String path, RequestHandler requestHandler) {
                return Route.patch(path, requestHandler);
            }
        },
        DELETE(DELETE.class) {
            @Override
            String path(Method method) {
                return method.getAnnotation(DELETE.class).value();
            }

            @Override
            Route route(String path, RequestHandler requestHandler) {
                return Route.delete(path, requestHandler);
            }
        },
        PUT(PUT.class) {
            @Override
            String path(Method method) {
                return method.getAnnotation(PUT.class).value();
            }

            @Override
            Route route(String path, RequestHandler requestHandler) {
                return Route.put(path, requestHandler);
            }
        },
        OPTIONS(OPTIONS.class) {
            @Override
            String path(Method method) {
                return method.getAnnotation(OPTIONS.class).value();
            }

            @Override
            Route route(String path, RequestHandler requestHandler) {
                return Route.options(path, requestHandler);
            }
        };

        private Class annotationClass;

        AccessType(Class annotationClass) {
            this.annotationClass = annotationClass;
        }

        static Optional<AccessType> parse(Method method) {
            //noinspection unchecked
            return EnumSet.allOf(AccessType.class)
                          .stream()
                          .filter(accessType -> method.getAnnotation(accessType.annotationClass)
                                                != null)
                          .findFirst();
        }

        abstract String path(Method method);

        abstract Route route(String path, RequestHandler requestHandler);
    }

    private class AutowiredRequestHandler implements RequestHandler {
        private final AutowirableMethod autowirableMethod;

        private AutowiredRequestHandler(AutowirableMethod autowirableMethod) {
            this.autowirableMethod = autowirableMethod;
        }

        @Override
        public Response process(Request request, Response currentResponse) {
            return autowirableMethod.invokeFromRequest(request, context);
        }
    }
}
