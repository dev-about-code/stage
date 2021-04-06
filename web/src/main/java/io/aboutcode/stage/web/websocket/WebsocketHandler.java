package io.aboutcode.stage.web.websocket;

import io.aboutcode.stage.web.autowire.Path;

/**
 * <p>
 * Implementing components can handle websocket communication. Any public method annotated with
 * {@link WebsocketDataHandler} will be invoked for websocket communication.
 * </p>
 * <p>To specify a path for the websocket endpoint, annotate the implemting class with {@link
 * Path}. If this annotation is omitted, the endpoint will be mounted on the root path
 * <code>/</code>. Mutiple endpoints can mount on the same path, they will then all partake in
 * websocket communication.</p>
 */
public interface WebsocketHandler {
    /**
     * Invoked to prepare the handler before it can accept data communication. This is usually used
     * to load external resources and/or to retain the context that allows the handler to interact
     * with clients.
     *
     * @param context The context that allows interaction with clients
     */
    default void initialize(WebsocketContext context) {}

    /**
     * Invoked on connection of a new client. Note that each client might get connected to the same
     * instance of the handler. Hence the handler should not store any session specific information
     * but should persist this information on the session object instead.
     *
     * @param session The session of the connected client
     */
    default void onConnected(WebsocketClientSession session) {}

    /**
     * Invoked on disconnection of a client. Any cleanup required should be performed in this
     * method.
     *
     * @param session The session of the client
     */
    default void onDisconnected(WebsocketClientSession session) {}
}
