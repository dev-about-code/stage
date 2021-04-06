package io.aboutcode.stage.web.websocket;

import io.aboutcode.stage.web.autowire.Path;
import io.aboutcode.stage.web.util.Paths;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class AutowiredWebsocketHandler implements WebsocketHandler {
    private static final String DEFAULT_PATH = "/";
    private static final Logger LOGGER = LoggerFactory.getLogger(AutowiredWebsocketHandler.class);
    private final String path;
    private final WebsocketHandler sourceHandler;
    private final List<AutowiredDataHandler> dataHandlers;

    private AutowiredWebsocketHandler(String path, WebsocketHandler sourceHandler,
                                      List<AutowiredDataHandler> dataHandlers) {
        this.path = path;
        this.sourceHandler = sourceHandler;
        this.dataHandlers = dataHandlers;
    }

    public static AutowiredWebsocketHandler from(String basePath, WebsocketHandler handler) {
        String path = getPath(basePath, handler);
        List<AutowiredDataHandler> dataHandlers = Stream.of(handler.getClass().getMethods())
                                                        .map(method -> parseMethod(handler, method))
                                                        .filter(Optional::isPresent)
                                                        .map(Optional::get)
                                                        .collect(Collectors.toList());

        return new AutowiredWebsocketHandler(path,
                                             handler,
                                             dataHandlers);
    }

    private static Optional<AutowiredDataHandler> parseMethod(WebsocketHandler handler,
                                                              Method method) {
        return AutowiredDataHandler.from(handler, method);
    }

    private static String getPath(String basePath, WebsocketHandler handler) {
        String path = Optional.ofNullable(handler.getClass().getAnnotation(Path.class))
                              .map(Path::value)
                              .orElse(DEFAULT_PATH);

        return Paths.concat(basePath, path).orElse(DEFAULT_PATH);
    }

    public String getPath() {
        return path;
    }

    @Override
    public void initialize(WebsocketContext context) {
        sourceHandler.initialize(context);
    }

    @Override
    public void onConnected(WebsocketClientSession session) {
        sourceHandler.onConnected(session);
    }

    @Override
    public void onDisconnected(WebsocketClientSession session) {
        sourceHandler.onDisconnected(session);
    }

    public List<Object> invokeDataHandlers(Object message, WebsocketClientSession session) {
        return dataHandlers.stream()
                           .map(handler -> invoke(handler, message, session))
                           .filter(Optional::isPresent)
                           .map(Optional::get)
                           .collect(Collectors.toList());
    }

    private Optional<Object> invoke(AutowiredDataHandler handler, Object message,
                                    WebsocketClientSession session) {
        try {
            return handler.invokeHandler(message, session);
        } catch (Exception e) {
            LOGGER.warn("Exception invoking handler {}: {}", handler.toString(), e.getMessage(), e);
        }
        return Optional.empty();
    }
}
