package io.aboutcode.stage.web.websocket.standard;

import java.util.Optional;
import java.util.function.Consumer;

/**
 * This represents a websocket session with a client and allows the application to interact with
 * that client.
 *
 * @param <MessageT> The type of message this session handles
 */
public interface WebSocketClientSession<MessageT extends TypedWebSocketMessage> {
    /**
     * Sends the specified message to the client.
     *
     * @param message The message to send
     */
    void send(MessageT message);

    /**
     * Subscribes the connected client to the specified topic. It will henceforth receive broadcasts
     * to that topic.
     *
     * @param topic The topic to subscribe the client to
     */
    void subscribeClientToTopic(String topic);

    /**
     * Registers the specified requestHandler to respond to messages of the specified type sent by
     * the client.
     *
     * @param targetClass    The type of request from the client that the handler will respond to
     * @param requestHandler The handler that will respond to the client's request
     * @param <TargetT>      The type of message the request handler can accept
     */
    <TargetT extends MessageT> void registerClientRequestHandler(Class<TargetT> targetClass,
                                                                 Consumer<TargetT> requestHandler);

    /**
     * Adds state to the client session. This can be used by a handler to persist information that
     * pertains only to the session.
     *
     * @param identifier The identifier of the message
     * @param value      The data to persist
     */
    void addState(String identifier, Object value);

    /**
     * Returns the - previously - persisted state object.
     *
     * @param identifier The identifier for the data to retrieve
     *
     * @return An optional over the requested data
     */
    Optional<Object> getState(String identifier);

    /**
     * Returns and removes the - previously - persisted state object.
     *
     * @param identifier The identifier for the data to retrieve
     *
     * @return An optional over the requested data
     */
    Optional<Object> removeState(String identifier);
}

