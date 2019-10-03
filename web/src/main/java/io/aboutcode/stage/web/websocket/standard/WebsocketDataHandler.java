package io.aboutcode.stage.web.websocket.standard;

/**
 * Implementations of this interface can be registered with {@link io.aboutcode.stage.web.websocket.WebsocketRoute}s
 * and will partake in the websocket communication. Each {@link WebsocketDataHandler} usually
 * handles a small part of the total communication. This ensures that the handler stays small and
 * efficient.
 */
public interface WebsocketDataHandler<MessageT extends TypedWebsocketMessage> {
    /**
     * Invoked to prepare the handler before it can accept data communication. This is usually used
     * to load external resources and/or to retain the context that allows the handler to interact
     * with clients.
     *
     * @param context The context that allows interaction with clients
     */
    default void initialize(WebsocketContext<MessageT> context) {}

    /**
     * Invoked on connection of a new client. Note that each client will get connected to the same
     * instance of the handler. Hence the handler should not store any session specific information
     * but should persist this information on the session object instead.
     *
     * @param session The session of the connected client
     */
    default void onConnected(WebsocketClientSession<MessageT> session) {}

    /**
     * Invoked on disconnection of a client. Any cleanup required should be performed in this
     * method.
     *
     * @param session The session of the client
     */
    default void onDisconnected(WebsocketClientSession<MessageT> session) {}
}
