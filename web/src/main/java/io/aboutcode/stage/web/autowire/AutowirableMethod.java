package io.aboutcode.stage.web.autowire;

import static io.aboutcode.stage.web.autowire.QueryParameter.DEFAULT_VALUE;

import io.aboutcode.stage.util.TypeInformation;
import io.aboutcode.stage.web.autowire.auth.AuthorizationRealm;
import io.aboutcode.stage.web.autowire.auth.Authorized;
import io.aboutcode.stage.web.autowire.auth.PermissiveAuthorizationRealm;
import io.aboutcode.stage.web.autowire.auth.Unauthorized;
import io.aboutcode.stage.web.autowire.exception.AutowiringException;
import io.aboutcode.stage.web.autowire.exception.IllegalAutowireValueException;
import io.aboutcode.stage.web.autowire.exception.UnauthorizedException;
import io.aboutcode.stage.web.autowire.versioning.Version;
import io.aboutcode.stage.web.autowire.versioning.VersionRange;
import io.aboutcode.stage.web.autowire.versioning.Versioned;
import io.aboutcode.stage.web.request.Request;
import io.aboutcode.stage.web.response.Response;
import io.aboutcode.stage.web.util.Paths;
import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 *
 */
final class AutowirableMethod {
    private final String basePath;
    private final AccessType accessType;
    private final Object targetObject;
    private final Method method;
    private final VersionRange versionRange;
    private final List<AutowiredParameter> parameters;
    private final boolean raw;
    private final AuthorizationRealm authorizationRealm;

    private AutowirableMethod(String basePath, AccessType accessType,
                              Object targetObject,
                              Method method,
                              List<AutowiredParameter> parameters,
                              boolean raw,
                              VersionRange versionRange,
                              AuthorizationRealm authorizationRealm) {
        this.basePath = basePath;
        this.accessType = accessType;
        this.targetObject = targetObject;
        this.method = method;
        this.versionRange = versionRange;
        this.method.setAccessible(true);
        this.parameters = parameters;
        this.raw = raw;
        this.authorizationRealm = authorizationRealm;
    }

    /**
     * Creates an {@link AutowirableMethod} from the specified parameters if the method is annotated
     * properly with an {@link AccessType} annotation.
     *
     * @param basePath                     The path that the path of this method is relative to;
     *                                     usually the path specified on class-level
     * @param targetObject                 The object that the method should be executed on when the
     *                                     {@link AutowirableMethod} is called
     * @param method                       The method that should be invoked on the target object
     * @param defaultAuthorizationRealm    The default realm to use if the method does not define
     *                                     its own realm. Must not be null
     * @param availableAuthorizationRealms All available authorization realms
     *
     * @return Optionally, the {@link AutowirableMethod} that can be invoked for a web request
     */
    static Optional<AutowirableMethod> from(String basePath,
                                            Object targetObject,
                                            Method method,
                                            AuthorizationRealm defaultAuthorizationRealm,
                                            Set<AuthorizationRealm> availableAuthorizationRealms) {
        return AccessType.parse(method)
                         .map(accessType -> {
                             Class<?> returnType = method.getReturnType();
                             List<AutowiredParameter> parameters = Stream.of(method.getParameters())
                                                                         .map(element -> parameter(
                                                                                 method, element))
                                                                         .collect(Collectors
                                                                                          .toList());

                             Annotation[] methodAnnotations = method.getDeclaredAnnotations();
                             boolean isRaw = isRaw(methodAnnotations, method, returnType);

                             VersionRange versionRange = versionRange(methodAnnotations);

                             AuthorizationRealm authorizationRealm = determineAuthorizationRealm(
                                     method,
                                     methodAnnotations,
                                     defaultAuthorizationRealm,
                                     availableAuthorizationRealms);

                             return new AutowirableMethod(basePath,
                                                          accessType,
                                                          targetObject,
                                                          method,
                                                          parameters,
                                                          isRaw,
                                                          versionRange,
                                                          authorizationRealm);
                         });
    }

    private static VersionRange versionRange(Annotation[] methodAnnotations) {
        return Stream.of(methodAnnotations)
                     .filter(annotation -> Objects
                             .equals(annotation.annotationType(), Versioned.class))
                     .findFirst()
                     .map(Versioned.class::cast)
                     .map(versioned -> VersionRange.between(
                             Version.from(versioned.introduced()).orElse(null),
                             Version.from(versioned.deprecated()).orElse(null)
                     ))
                     .orElse(null);
    }

    private static AuthorizationRealm determineAuthorizationRealm(Method method,
                                                                  Annotation[] methodAnnotations,
                                                                  AuthorizationRealm defaultAuthorizationRealm,
                                                                  Set<AuthorizationRealm> availableAuthorizationRealms) {

        validateAuthorizationAnnotations(method, methodAnnotations);

        Optional<AuthorizationRealm> realm =
                Stream.of(methodAnnotations)
                      .filter(annotation -> Objects
                              .equals(annotation.annotationType(), Authorized.class))
                      .findFirst()
                      .map(Authorized.class::cast)
                      .map(Authorized::value)
                      .map(realmType -> findRealm(realmType, availableAuthorizationRealms));

        if (realm.isPresent()) {
            return realm.get();
        }

        if (isUnauthorized(methodAnnotations) || defaultAuthorizationRealm == null) {
            return new PermissiveAuthorizationRealm();
        }

        return defaultAuthorizationRealm;
    }

    private static void validateAuthorizationAnnotations(Method method,
                                                         Annotation[] methodAnnotations) {
        if (Stream.of(methodAnnotations)
                  .filter(annotation ->
                                  Objects.equals(annotation.annotationType(), Authorized.class) ||
                                  Objects.equals(annotation.annotationType(), Unauthorized.class)
                  )
                  .count() > 1) {
            throw exception("Multiple authorization annotations found", method);
        }
    }

    private static boolean isUnauthorized(Annotation[] methodAnnotations) {
        return Stream.of(methodAnnotations)
                     .anyMatch(annotation -> Objects
                             .equals(annotation.annotationType(), Unauthorized.class));
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

    private static boolean isRaw(Annotation[] methodAnnotations, Method method,
                                 Class<?> returnType) {
        Optional<Annotation> raw = Stream.of(methodAnnotations)
                                         .filter(annotation -> Objects
                                                 .equals(annotation.annotationType(), Raw.class))
                                         .findFirst();
        if (raw.isPresent() && !Objects.equals(returnType, Response.class)) {
            throw exception("Raw method does not return Response", method);
        }

        return raw.isPresent();
    }

    private static AutowiredParameter parameter(Method method, Parameter parameter) {
        Class<?> type = parameter.getType();
        Annotation[] annotations = parameter.getAnnotations();

        // if the parameter type is 'Request', we simply use that
        if (Objects.equals(Request.class, type)) {
            return new RequestParameter();
        }

        // otherwise, let's get all relevant annotations only
        List<Annotation> relevantAnnotations = Stream.of(annotations)
                                                     .filter(AutowirableMethod::isAutowireAnnotation)
                                                     .collect(Collectors.toList());

        if (relevantAnnotations.size() > 1) {
            throw exception("Annotations collide", method, parameter);
        }

        if (relevantAnnotations.isEmpty()) {
            throw exception("No annotations found", method, parameter);
        }

        return ParameterAnnotationType.autowire(parameter, relevantAnnotations.get(0));
    }

    private static AutowiringException exception(String message,
                                                 Method method,
                                                 Parameter parameter) {
        throw new AutowiringException(String.format(
                "Could not parse annotations for parameter '%s' on method '%s' because: %s",
                parameter.getName(),
                method.getName(),
                message));
    }

    @SuppressWarnings("SameParameterValue")
    private static AutowiringException exception(String message,
                                                 Method method) {
        throw new AutowiringException(String.format(
                "Could not parse annotations for method '%s' because: %s",
                method.getName(),
                message));
    }

    private static boolean isAutowireAnnotation(Annotation annotation) {
        return ParameterAnnotationType.isMatching(annotation.annotationType());
    }

    /**
     * Returns the access type of this method.
     *
     * @return The access type of this method
     */
    AccessType getAccessType() {
        return accessType;
    }

    /**
     * Returns the version range this method is supported in.
     *
     * @return The version range this method is supported in
     */
    VersionRange getVersionRange() {
        return versionRange;
    }

    /**
     * Returns the path this method is configured to be exposed at. Note that this is relative to
     * any paths configured globally and/or on the containing class.
     *
     * @return The configured path
     */
    String getPath() {
        return Paths.concat(basePath, accessType.path(method)).orElse("/");
    }

    /**
     * Returns the type of the target object for this method.
     *
     * @return The type of the target object
     */
    Class getTargetObjectType() {
        return targetObject.getClass();
    }

    /**
     * Returns the target method to be invoked on the target object for this method.
     *
     * @return The target method to be invoked on the target object
     */
    Method getTargetMethod() {
        return method;
    }

    /**
     * Returns whether the method represented by this object should return a raw value (i.e. the
     * contents of the response should not be serialized and the object be returned as-is)
     *
     * @return True if this is a <em>raw</em> method; false otherwise
     */
    public boolean isRaw() {
        return raw;
    }

    /**
     * Executes the method in the context of the specified request.
     *
     * @param request The request to execute the method for
     * @param context The context that allows the method to perform operations on its application
     *                context
     *
     * @return The result of the execution
     */
    Object invokeFromRequest(Request request, AutowiringRequestContext context) throws Exception {
        if (!authorizationRealm.isAuthorized(request)) {
            throw new UnauthorizedException(request.path());
        }

        try {
            return method.invoke(targetObject,
                                 parameters.stream()
                                           .map(parameter -> parameter
                                                   .retrieveFrom(request, context)).toArray()
            );
        } catch (InvocationTargetException e) {
            throw (Exception) e.getCause();
        }
    }

    @SuppressWarnings("unused")
    private enum ParameterAnnotationType {
        PATH {
            @Override
            AutowiredParameter autowiredParameter(Parameter parameter, Annotation annotation) {
                TypeInformation typeInformation = TypeInformation.from(parameter);
                return new PathParameter(
                        ((io.aboutcode.stage.web.autowire.PathParameter) annotation).value(),
                        typeInformation);
            }

            @Override
            boolean matches(Class<?> annotationType) {
                return Objects.equals(annotationType,
                                      io.aboutcode.stage.web.autowire.PathParameter.class);
            }
        },
        QUERY {
            @Override
            AutowiredParameter autowiredParameter(Parameter parameter, Annotation annotation) {
                io.aboutcode.stage.web.autowire.QueryParameter queryParameter =
                        (io.aboutcode.stage.web.autowire.QueryParameter) annotation;
                TypeInformation typeInformation = TypeInformation.from(parameter);
                return new QueryParameter(
                        queryParameter.value(),
                        queryParameter.defaultValue(),
                        queryParameter.mandatory(),
                        typeInformation);
            }

            @Override
            boolean matches(Class<?> annotationType) {
                return Objects.equals(annotationType,
                                      io.aboutcode.stage.web.autowire.QueryParameter.class);
            }
        },
        BODY {
            @Override
            AutowiredParameter autowiredParameter(Parameter parameter, Annotation annotation) {
                return new BodyParameter(parameter.getType());
            }

            @Override
            boolean matches(Class<?> annotationType) {
                return Objects.equals(annotationType, Body.class);
            }
        };

        static boolean isMatching(Class<?> annotationType) {
            return EnumSet.allOf(ParameterAnnotationType.class)
                          .stream()
                          .anyMatch(element -> element.matches(annotationType));
        }

        static AutowiredParameter autowire(Parameter parameter, Annotation annotation) {
            return EnumSet.allOf(ParameterAnnotationType.class)
                          .stream()
                          .filter(element -> element.matches(annotation.annotationType()))
                          .findFirst()
                          .map(element -> element.autowiredParameter(parameter, annotation))
                          .orElseThrow(IllegalStateException::new);
        }

        abstract boolean matches(Class<?> annotationType);

        abstract AutowiredParameter autowiredParameter(Parameter parameter, Annotation annotation);
    }

    private static class RequestParameter extends AutowiredParameter {
        @Override
        Object retrieveFrom(Request request, AutowiringRequestContext context) {
            return request;
        }
    }

    private static class PathParameter extends AutowiredParameter {
        private final String name;
        private final TypeInformation typeInformation;

        private PathParameter(String name, TypeInformation typeInformation) {
            this.name = name;
            this.typeInformation = typeInformation;
        }

        @Override
        Object retrieveFrom(Request request, AutowiringRequestContext context)
                throws IllegalAutowireValueException {
            try {
                return request.pathParam(name)
                              .map(value -> typeInformation
                                      .convert(Collections.singletonList(value)))
                              .orElseThrow(() -> new IllegalAutowireValueException(
                                      String.format("Parameter '%s' in path missing", name)));
            } catch (IllegalAutowireValueException e) {
                throw e;
            } catch (Exception e) {
                throw new IllegalAutowireValueException(
                        String.format("Converting value for parameter '%s' caused exception: %s",
                                      name, e.getMessage()), e);
            }
        }
    }

    private static class QueryParameter extends AutowiredParameter {
        private final String name;
        private final String defaultValue;
        private final boolean mandatory;
        private final TypeInformation typeInformation;

        private QueryParameter(String name, String defaultValue, boolean mandatory,
                               TypeInformation typeInformation) {
            this.name = name;
            this.defaultValue = defaultValue;
            this.mandatory = mandatory;
            this.typeInformation = typeInformation;
        }

        @Override
        Object retrieveFrom(Request request, AutowiringRequestContext context)
                throws IllegalAutowireValueException {
            List<String> input = request.queryParams(name);
            if (input != null && input.isEmpty()) {
                input = null;
            }

            if (input == null && (mandatory || typeInformation.isPrimitive())) {
                throw new IllegalAutowireValueException(
                        String.format("Mandatory parameter '%s' was not specified", name));
            }

            String defaultValue =
                    Objects.equals(this.defaultValue, DEFAULT_VALUE) ? null : this.defaultValue;

            try {
                input = Optional.ofNullable(input)
                                .orElseGet(() -> Optional.ofNullable(defaultValue)
                                                         .map(Collections::singletonList)
                                                         .orElse(Collections.emptyList())
                                );
                return typeInformation.convert(input);
            } catch (Exception e) {
                throw new IllegalAutowireValueException(
                        String.format("Converting value for parameter '%s' caused exception: %s",
                                      name, e.getMessage()), e);
            }
        }
    }

    private static class BodyParameter extends AutowiredParameter {
        private final Class<?> type;

        private BodyParameter(Class<?> type) {
            this.type = type;
        }

        @Override
        Object retrieveFrom(Request request, AutowiringRequestContext context) {
            return context.deserialize(request.body(), type);
        }
    }

    private static abstract class AutowiredParameter {
        abstract Object retrieveFrom(Request request, AutowiringRequestContext context)
                throws IllegalAutowireValueException;
    }
}