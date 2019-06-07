package io.aboutcode.stage.web.websocket.standard;

/**
 * This is a standard websocket message that provides access to a message type identifier. This
 * identifier is used when deserializing the message to identify the correct type of object to
 * associate with the message.
 */
public interface TypedWebSocketMessage {
    /**
     * Returns the identifier for this message type. The identifiers must be unique over the message
     * communication that is performed on the websocket connection. I.e, the identifier should not
     * be reused for two different messages between client and server.
     *
     * @return The unique identifier of the message
     */
    String getIdentifier();
}
