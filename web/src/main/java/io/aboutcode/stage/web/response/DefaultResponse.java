package io.aboutcode.stage.web.response;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * A default implementation of a {@link Response} that can be used to create specific types of
 * responses.
 */
public class DefaultResponse implements Response {
    private static final String CONTENT_TYPE_HEADER = "Content-Type";
    private final Map<String, String> headers = new HashMap<>();
    private final boolean finished;
    private Object data;
    private int status;

    /**
     * Creates a new instance with the specified parameters.
     *
     * @param finished If true, the response is considered finished and subsequent request handlers
     *                 will not be invoked
     * @param headers  The headers to set on the response
     * @param data     The data to add to the response
     * @param status   The HTTP status code for this response
     */
    @SuppressWarnings("WeakerAccess")
    public DefaultResponse(boolean finished,
                           Map<String, String> headers,
                           Object data,
                           int status) {
        this.data = data;
        this.status = status;
        this.finished = finished;

        if (headers != null) {
            this.headers.putAll(headers);
        }
    }

    @Override
    public boolean finished() {
        return finished;
    }

    @Override
    public Map<String, String> headers() {
        return Collections.unmodifiableMap(headers);
    }

    @Override
    public Object data() {
        return data;
    }

    @Override
    public int status() {
        return status;
    }

    @Override
    public Response data(Object data) {
        this.data = data;
        return this;
    }

    @Override
    public Response header(String name, String value) {
        this.headers.put(name, value);
        return this;
    }

    @Override
    public Response contentType(String contentType) {
        this.headers.put(CONTENT_TYPE_HEADER, contentType);
        return this;
    }

    @Override
    public String contentType() {
        return this.headers.get(CONTENT_TYPE_HEADER);
    }
}
