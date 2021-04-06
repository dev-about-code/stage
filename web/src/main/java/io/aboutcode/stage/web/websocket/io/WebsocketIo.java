package io.aboutcode.stage.web.websocket.io;

import java.io.IOException;
import java.util.Optional;

/**
 * Implementations of this provide functionality to serialize and deserialize websocket messages.
 */
public interface WebsocketIo {
    /**
     * Serializes the specified element to a string representation that can be transferred via the
     * websocket.
     *
     * @param element The element to serialize
     *
     * @return The string representation of the object
     *
     * @throws IOException Thrown if the element could not be serialized
     */
    Optional<String> serialize(Object element) throws IOException;

    /**
     * Deserializes the specified message into an object that can be processed by the application.
     *
     * @param message The message to deserialize
     *
     * @return The deserialized object
     *
     * @throws IOException Thrown if the message could not be deserialized
     */
    Optional<Object> deserialize(String message) throws IOException;
}
