package io.aboutcode.stage.web.websocket.standard;

import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * This represents a websocket session with a client and allows the application to interact with
 * that client.
 *
 * @param <MessageT> The type of message this session handles
 */
public interface WebsocketClientSession<MessageT extends TypedWebsocketMessage> {
    /**
     * Sends the specified message to the client.
     *
     * @param message The message to send
     */
    void send(MessageT message);

    /**
     * Terminates the connection with the client with the specified status code and reason.
     *
     * @param statusCode The status code to be sent (see {@link DefaultCloseStatusCodes})
     * @param reason     The reason string for the close
     */
    void close(int statusCode, String reason);

    /**
     * Subscribes the connected client to the specified topic. It will henceforth receive broadcasts
     * to that topic.
     *
     * @param topic The topic to subscribe the client to
     */
    void subscribeClientToTopic(Object topic);

    /**
     * Registers the specified messageHandler to respond to messages of the specified type sent by
     * the client.
     *
     * @param requestType    The type of message from the client that the handler will process
     * @param messageHandler The handler that will respond to the client's message. If this returns
     *                       null, no response will be returned to the client
     * @param <RequestT>     The type of message the consumer can accept
     * @param <ResponseT>    The type of response the handler produces
     */
    <RequestT extends MessageT, ResponseT extends MessageT> void registerMessageHandler(
            Class<RequestT> requestType, Function<RequestT, ResponseT> messageHandler);

    /**
     * Registers the specified messageConsumer process to messages of the specified type sent by the
     * client.
     *
     * @param requestType     The type of message from the client that the handler will process
     * @param messageConsumer The handler that will process the client's message
     * @param <RequestT>      The type of message the consumer can accept
     */
    <RequestT extends MessageT> void registerMessageConsumer(Class<RequestT> requestType,
                                                             Consumer<RequestT> messageConsumer);

    /**
     * Adds state to the client session. This can be used by a handler to persist information that
     * pertains only to the session.
     *
     * @param identifier The identifier of the state
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

