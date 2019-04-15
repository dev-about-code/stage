package io.aboutcode.stage.web.websocket.standard;

/**
 * Implementations of this interface can be registered with the {@link ModularWebSocketEndpoint} and
 * will partake in the websocket communication. Each {@link WebSocketDataHandler} usually handles a
 * small part of the total communication. This ensures that the handler stays small and efficient.
 */
public interface WebSocketDataHandler<MessageT extends TypedWebSocketMessage> {
   /**
    * Invoked to prepare the handler before it can accept data communication. This is usually used
    * to load external resources and/or to register all message types that the handler is willing to
    * accept.
    *
    * @param context The context that allows registration of message types
    */
   void initialize(WebSocketContext<MessageT> context);

   /**
    * Invoked on connection of a new client. Note that each client will get connected to the same
    * instance of the handler. Hence the handler should not store any session specific information
    * but should persist this information on the session object instead.
    *
    * @param session The session of the connected client
    */
   void onConnected(WebSocketClientSession<MessageT> session);

   /**
    * Invoked on disconnection of a client. Any cleanup required should be performed in this
    * method.
    *
    * @param session The session of the client
    */
   void onDisconnected(WebSocketClientSession<MessageT> session);
}
