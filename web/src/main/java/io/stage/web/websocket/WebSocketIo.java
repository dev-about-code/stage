package io.aboutcode.stage.web.websocket;

import java.io.IOException;

/**
 * Implementations of this provide functionality to serialize and deserialize websocket messages.
 *
 * @param <MessageT> The base type of message this can handle
 */
public interface WebSocketIo<MessageT> {
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
   String serialize(Object element) throws IOException;

   /**
    * Deserializes the specified message into an object that can be processed by the application.
    *
    * @param message The message to deserialize
    *
    * @return The deserialized object
    *
    * @throws IOException Thrown if the message could not be deserialized
    */
   MessageT deserialize(String message) throws IOException;

   /**
    * Registers the specified identifier to uniquely identify the specified message type.
    *
    * @param identifier The identifier
    * @param type       The type represented by the identifier
    * @param <TargetT>  Specific type
    */
   <TargetT extends MessageT> void registerMessageType(String identifier, Class<TargetT> type);
}
