package io.aboutcode.stage.web.response;

import io.aboutcode.stage.web.request.Request;
import io.aboutcode.stage.web.request.RequestHandler;
import java.util.Map;

/**
 * The response for a {@link Request} that will be used to transmit data back to the client.
 */
public interface Response {
    /**
     * If true, the chain of {@link RequestHandler}s will not continue and this response will
     * immediately be send to the client.
     *
     * @return True if the response is finished, false if it should continue being processed
     */
    boolean finished();

    /**
     * Returns all headers that are specified for this response.
     *
     * @return All headers that are specified for this response
     */
    Map<String, String> headers();

    /**
     * Returns the data object currently associated with this response.
     *
     * @return The data object currently associated with this response. May be null.
     */
    Object data();

    /**
     * Returns the current HTTP status code for this response.
     *
     * @return The current HTTP status code for this response
     */
    int status();

    /**
     * Sets the data object for this response.
     *
     * @param data The data object for this response
     *
     * @return This for fluent interface
     */
    Response data(Object data);

    /**
     * Sets the header with the specified name to the specified value for this response.
     *
     * @param name  The name of the header to set
     * @param value The value to set the header to
     *
     * @return This for fluent interface
     */
    Response header(String name, String value);

    /**
     * Sets the <code>Content-Type</code> header of the response to the specified value.
     *
     * @param contentType The content type to set
     *
     * @return This for fluent interface
     */
    Response contentType(String contentType);

    /**
     * Returns the current value of the <code>Content-Type</code> header.
     *
     * @return The current value of the <code>Content-Type</code> header; null if it is not set
     */
    String contentType();
}
