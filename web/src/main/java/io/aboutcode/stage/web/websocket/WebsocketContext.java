package io.aboutcode.stage.web.websocket;


/**
 * This allows a {@link WebsocketDataHandler} to interact with the context of the enclosing {@link
 * WebsocketHandler}.
 */
public interface WebsocketContext {
    /**
     * This allows the {@link WebsocketDataHandler} to publish messages to all clients subscribed to
     * the specified topic.
     *
     * @param topic   The topic to send the message to. If this is null, <em>all</em> clients will
     *                receive the message
     * @param message The message to send
     */
    void publishToSubscribedClients(String topic, Object message);

    /**
     * This allows the {@link WebsocketDataHandler} to publish messages to all clients connected to
     * the websocket (broadcast).
     *
     * @param message The message to send
     */
    void publishToAllClients(Object message);
}
