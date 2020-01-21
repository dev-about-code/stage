package io.aboutcode.stage.web.autowire;

import io.aboutcode.stage.web.request.Request;
import io.aboutcode.stage.web.response.Response;
import io.aboutcode.stage.web.serialization.ContentTypeException;

/**
 * Implementation provide callbacks into the web context to provide generic functionality to the
 * autowiring process.
 */
public interface AutowiringRequestContext {
    /**
     * Deserializes the specified input string to the specified type using the web context's default
     * serialization method (e.g. JSON serialization). Note that this method <em>MUST</em> be
     * symmetric with {@link AutowiringRequestContext#serialize(Object)}
     *
     * @param input The input to deserialize
     * @param type  The type to deserialize the input to
     * @param <T>   The generic type of the target type
     *
     * @return The deserialized object
     */
    <T> T deserialize(String input, Class<T> type);

    /**
     * Serializes the specified input into string format using the web context's default
     * serialization method (e.g. JSON serialization).  Note that this method <em>MUST</em> be
     * symmetric with {@link AutowiringRequestContext#deserialize(String, Class)}
     *
     * @param input The input to serialize
     *
     * @return The string representation of the specified object
     */
    String serialize(Object input);

    /**
     * Sets the correct content type on the provided response for the provided request.
     *
     * @param request  The request to set the content type for
     * @param response The response to set the content type on
     *
     * @throws ContentTypeException Thrown if the content type could not be set for some reason
     */
    void setContentType(Request request, Response response) throws ContentTypeException;

    /**
     * Transforms the specified exception into a response that can be returned to the client
     *
     * @param e The exception to serialize
     *
     * @return The exception as a response that can be returned to the client
     */
    Response serialize(Exception e);
}
