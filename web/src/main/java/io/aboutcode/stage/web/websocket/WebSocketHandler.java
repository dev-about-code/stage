package io.aboutcode.stage.web.websocket;

import org.eclipse.jetty.websocket.api.Session;

/**
 * Implementations of this will receive the specified events for any successful websocket connection
 * on the path the handlers is registered on.
 */
public interface WebSocketHandler {
    /**
     * This will be called for every message that is received on the websocket.
     *
     * @param session The session associated with the websocket connection
     * @param message The received message
     */
    void onMessage(Session session, String message);

    /**
     * This is called on successful connection of a client.
     *
     * @param session The session associated with the websocket connection
     */
    void onConnect(Session session);

    /**
     * This is called on disconnection of a client.
     *
     * @param session The session associated with the websocket connection
     * @param status  The status message that resulted in the disconnection
     * @param reason  A string representation of the reason for the disconnection
     */
    void onDisconnect(Session session, int status, String reason);

    /**
     * This is called on any error in the websocket connection
     *
     * @param session The session associated with the websocket connection
     * @param error   The error that was thrown
     */
    void onError(Session session, Throwable error);
}
