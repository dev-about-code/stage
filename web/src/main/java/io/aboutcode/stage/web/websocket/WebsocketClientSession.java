package io.aboutcode.stage.web.websocket;

import io.aboutcode.stage.web.websocket.io.DefaultCloseStatusCodes;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * This represents a websocket session with a client and allows the application to interact with
 * that client.
 */
public interface WebsocketClientSession {
    /**
     * Sends the specified message to the client.
     *
     * @param message The message to send
     */
    void send(Object message);

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

    /**
     * Returns all available headers.
     *
     * @return All available headers for the session connection
     */
    Map<String, List<String>> headers();

    /**
     * Returns all values of the header with the given name.
     *
     * @param name The name of the header to return
     *
     * @return All valuea of the header with the specified name
     */
    List<String> headers(String name);

    /**
     * Returns the value of the header with the given name.
     *
     * @param name The name of the header to return
     *
     * @return Optionally, the value of the header
     */
    Optional<String> header(String name);
}

