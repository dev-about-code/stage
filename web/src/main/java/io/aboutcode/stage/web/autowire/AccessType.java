package io.aboutcode.stage.web.autowire;

import io.aboutcode.stage.web.Route;
import io.aboutcode.stage.web.request.RequestHandler;
import java.lang.reflect.Method;
import java.util.EnumSet;
import java.util.Optional;

@SuppressWarnings("unused")
public enum AccessType {
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
