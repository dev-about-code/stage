package io.aboutcode.stage.web.websocket;

import io.aboutcode.stage.web.websocket.standard.WebsocketDataHandler;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Represents a route that is backed by a websocket connection.
 */
public final class WebsocketRoute {
    private final String path;
    private final Set<WebsocketDataHandler> webSocketHandlers;

    private WebsocketRoute(String path, Set<WebsocketDataHandler> webSocketHandlers) {
        this.path = path;
        this.webSocketHandlers = webSocketHandlers;
    }

    /**
     * Convenience method to create a list of the specified {@link WebsocketRoute}s.
     *
     * @param routes The web socket routes to create a list for
     *
     * @return The created list, never null
     */
    public static List<WebsocketRoute> list(WebsocketRoute... routes) {
        return Stream.of(routes).collect(Collectors.toList());
    }

    /**
     * Exposes the specified websocket handler at the specified path.
     *
     * @param path     The path at which to expose the specified handler
     * @param handlers The handlers to expose at the specified path
     *
     * @return The created route
     */
    public static WebsocketRoute at(String path, WebsocketDataHandler... handlers) {
        return new WebsocketRoute(path, Stream.of(handlers).collect(Collectors.toSet()));
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
    public Set<WebsocketDataHandler> getWebSocketDataHandlers() {
        return webSocketHandlers;
    }
}
