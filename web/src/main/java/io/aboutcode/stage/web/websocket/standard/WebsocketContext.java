package io.aboutcode.stage.web.websocket.standard;

/**
 * This allows a {@link WebsocketDataHandler} to interact with the context of the enclosing {@link
 * io.aboutcode.stage.web.web.WebsocketEndpoint}.
 */
public interface WebsocketContext<MessageT extends TypedWebsocketMessage> {
    /**
     * This allows the {@link WebsocketDataHandler} to publish messages to all clients subscribed to
     * the specified topic.
     *
     * @param topic   The topic to send the message to. If this is null, <em>all</em> clients will
     *                receive the message
     * @param message The message to send
     */
    void publishToSubscribedClients(String topic, MessageT message);

    /**
     * This allows the {@link WebsocketDataHandler} to publish messages to all clients connected to
     * the websocket (broadcast).
     *
     * @param message The message to send
     */
    void publishToAllClients(MessageT message);
}
