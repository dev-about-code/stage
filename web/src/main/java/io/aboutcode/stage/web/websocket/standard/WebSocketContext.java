package io.aboutcode.stage.web.websocket.standard;

/**
 * This allows a {@link WebSocketDataHandler} to interact with the context of the enclosing {@link
 * ModularWebSocketEndpoint}.
 */
public interface WebSocketContext<MessageT extends TypedWebSocketMessage> {
    /**
     * The {@link WebSocketDataHandler} should use this to register any message it is interested in
     * receiving from the websocket. Note that it will only get notified of messages that it is
     * registered for - all other messages will be ommitted.
     *
     * @param messageIdentifier The identifier to register the message under
     * @param targetClass       The type that any message with the identifier will be deserialized
     *                          to
     * @param <TargetT>         The generic type
     */
    <TargetT extends MessageT> void registerMessageType(String messageIdentifier,
                                                        Class<TargetT> targetClass);

    /**
     * This allows the {@link WebSocketDataHandler} to publish messages to all clients connected to
     * the websocket (broadcast).
     *
     * @param topic   The topic to send the message to
     * @param message The message to send
     */
    void publishToAllClients(String topic, MessageT message);
}
