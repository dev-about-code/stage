package io.aboutcode.stage.web.serialization;

import io.aboutcode.stage.web.request.Request;
import io.aboutcode.stage.web.response.Response;

/**
 * Implementations of this allow serialization and deserialization of objects to and from a string
 * representation.
 */
public interface WebSerialization {
    /**
     * Deserializes the specified input string to the specified type. Note that this method
     * <em>MUST</em> be symmetric with {@link WebSerialization#serialize(Object)}
     *
     * @param input The input to deserialize
     * @param type  The type to deserialize the input to
     * @param <T>   The generic type of the target type
     *
     * @return The deserialized object
     */
    <T> T deserialize(String input, Class<T> type);

    /**
     * Serializes the specified input into string format.  Note that this method <em>MUST</em> be
     * symmetric with {@link WebSerialization#deserialize(String, Class)}
     *
     * @param input The input to serialize
     *
     * @return The string representation of the specified object
     */
    String serialize(Object input);

    /**
     * Sets the correct content type on the provided response for the provided request. The default
     * implementation does not set a content type
     *
     * @param request  The request to set the content type for
     * @param response The response to set the content type on
     *
     * @throws ContentTypeException Thrown if the content type could not be set for some reason
     */
    default void setContentType(Request request, Response response) throws ContentTypeException {

    }
}
