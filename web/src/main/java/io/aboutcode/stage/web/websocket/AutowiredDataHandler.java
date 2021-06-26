package io.aboutcode.stage.web.websocket;

import io.aboutcode.stage.web.autowire.exception.AutowiringException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;

/**
 *
 */
final class AutowiredDataHandler {
    private final Object targetObject;
    private final Method method;
    private final AutowiredParameter firstParameter;
    private final AutowiredParameter secondParameter;

    private AutowiredDataHandler(Object targetObject,
                                 Method method,
                                 AutowiredParameter firstParameter,
                                 AutowiredParameter secondParameter) {
        this.targetObject = targetObject;
        this.method = method;
        this.firstParameter = firstParameter;
        this.secondParameter = secondParameter;
        this.method.setAccessible(true);
    }

    /**
     * Creates an {@link AutowiredDataHandler} from the specified parameters if the method is
     * annotated properly with an {@link WebsocketDataHandler} annotation.
     *
     * @param targetObject The object that the method should be executed on when the {@link
     *                     AutowiredDataHandler} is called
     * @param method       The method that should be invoked on the target object
     *
     * @return The {@link AutowiredDataHandler} that can be invoked for a websocket message
     */
    static Optional<AutowiredDataHandler> from(Object targetObject, Method method)
            throws AutowiringException {

        if (method.getAnnotation(WebsocketDataHandler.class) == null) {
            return Optional.empty();
        }

        Parameter[] allParameters = method.getParameters();

        if (allParameters.length > 2 || allParameters.length < 1) {
            throw exception(
                    "Exactly one message parameter and optionally one session parameter allowed per data handler: ",
                    method);
        }

        AutowiredParameter firstParameter = parameter(allParameters[0]);
        AutowiredParameter secondParameter = parameter(
                Stream.of(allParameters)
                      .skip(1)
                      .findFirst()
                      .orElse(null)
        );

        return Optional.of(new AutowiredDataHandler(targetObject,
                                                    method,
                                                    firstParameter,
                                                    secondParameter));
    }

    private static boolean isSessionType(Parameter parameter) {
        return Objects.equals(WebsocketClientSession.class, parameter.getType());
    }

    private static AutowiredParameter parameter(Parameter parameter) {
        if (Objects.isNull(parameter)) {
            return new NoopParameter();
        }

        if (isSessionType(parameter)) {
            return new SessionParameter();
        }

        return new TypedParameter(parameter.getType());
    }

    @SuppressWarnings("SameParameterValue")
    private static AutowiringException exception(String message,
                                                 Method method) {
        throw new AutowiringException(String.format(
                "Could not parse annotations for method '%s' because: %s",
                method.getName(),
                message));
    }

    @Override
    public String toString() {
        return String.format("%s#%s", targetObject.getClass(), method.toString());
    }

    /**
     * Invokes the handler with the specified message and session if this handler can accept this
     * type.
     *
     * @param message The message to handle
     * @param session The session that allows the method to perform operations on its application
     *                session
     *
     * @return The result of the execution
     */
    Optional<Object> invokeHandler(Object message, WebsocketClientSession session)
            throws Exception {
        if (canHandle(message)) {
            try {
                return Optional.ofNullable(method.invoke(targetObject, assignParameters(message, session)));
            } catch (InvocationTargetException e) {
                throw (Exception) e.getCause();
            }
        }
        return Optional.empty();
    }

    // this is really ugly, let's change asap
    private Object[] assignParameters(Object message, WebsocketClientSession session) {
        return Stream.of(firstParameter, secondParameter)
                .<Optional<Object>>map(param -> {
                    if (param.accept(message)) {
                        return Optional.of(message);
                    }

                    if (param.accept(session)) {
                        return Optional.of(session);
                    }

                    return Optional.empty();
                })
                .filter(Optional::isPresent)
                .map(Optional::get)
                .toArray();
    }

    private boolean canHandle(Object message) {
        return Stream.of(firstParameter, secondParameter).anyMatch(param -> param.accept(message));
    }

    private interface AutowiredParameter {
        boolean accept(Object targetObject);
    }

    private static class SessionParameter implements AutowiredParameter {
        @Override
        public boolean accept(Object targetObject) {
            return !Objects.isNull(targetObject) && targetObject instanceof WebsocketClientSession;
        }
    }

    private static class TypedParameter implements AutowiredParameter {
        private final Class<?> type;

        private TypedParameter(Class<?> type) {
            this.type = type;
        }

        @Override
        public boolean accept(Object targetObject) {
            return !Objects.isNull(targetObject) && type.isAssignableFrom(targetObject.getClass());
        }
    }

    private static class NoopParameter implements AutowiredParameter {
        @Override
        public boolean accept(Object targetObject) {
            return false;
        }
    }
}