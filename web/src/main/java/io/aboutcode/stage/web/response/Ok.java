package io.aboutcode.stage.web.response;

/**
 * Default "OK" response with a status of 200 for a successful processing of a request.
 */
public final class Ok extends DefaultResponse {
    private Ok(Object data) {
        super(false, null, data, 200);
    }

    /**
     * Creates a new instance of this response.
     *
     * @return The created instance
     */
    public static Response create() {
        return new Ok("OK");
    }

    /**
     * Creates a new instance of this response with the specified contents.
     *
     * @param data The contents of this response
     *
     * @return The created instance
     */
    public static Response with(Object data) {
        return new Ok(data);
    }
}
