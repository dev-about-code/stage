package io.aboutcode.stage.web.websocket;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Represents a route that is backed by a websocket connection.
 */
public final class WebSocketRoute {
    private final String path;
    private final WebSocketHandler webSocketHandler;

    private WebSocketRoute(String path, WebSocketHandler webSocketHandler) {
        this.path = path;
        this.webSocketHandler = webSocketHandler;
    }

    /**
     * Convenience method to create a list of the specified {@link WebSocketRoute}s.
     *
     * @param routes The web socket routes to create a list for
     *
     * @return The created list, never null
     */
    public static List<WebSocketRoute> list(WebSocketRoute... routes) {
        return Stream.of(routes).collect(Collectors.toList());
    }

    /**
     * Exposes the specified websocket handler at the specified path.
     *
     * @param path    The path at which to expose the specified handler
     * @param handler The handler to expose at the specified path
     *
     * @return The created route
     */
    public static WebSocketRoute at(String path, WebSocketHandler handler) {
        return new WebSocketRoute(path, handler);
    }

    /**
     * Returns the path of this websocket route.
     *
     * @return The path of this websocket route.
     */
    public String getPath() {
        return path;
    }

    /**
     * Returns the handler of this websocket route.
     *
     * @return The handler of this websocket route.
     */
    public WebSocketHandler getWebSocketHandler() {
        return webSocketHandler;
    }
}
