package io.aboutcode.stage.web.websocket;

import io.aboutcode.stage.web.websocket.WebsocketRoute;
import java.util.Collections;
import java.util.List;

/**
 * Implementors of this interface will be registered by the WebServerComponent and can partake in
 * request-response Http communication.
 */
public interface WebsocketEndpoint {
    /**
     * Returns the websocket routes that this endpoint wants to register, defaults to an empty
     * list.
     *
     * @return The websocket routes that this endpoint wants to register.
     */
    default List<WebsocketRoute> getWebSocketRoutes() {
        return Collections.emptyList();
    }
}
