package io.aboutcode.stage.web.response;

/**
 * "Resource not found" response with a status of 404.
 */
public final class NotFound extends DefaultResponse {
    private NotFound(Object data) {
        super(true, null, data, 404);
    }

    /**
     * Creates a new instance of this response.
     *
     * @return The created instance
     */
    public static Response create() {
        return new NotFound("Resource not found");
    }

    /**
     * Creates a new instance of this response with the specified contents.
     *
     * @param data The contents of this response
     *
     * @return The created instance
     */
    public static Response with(Object data) {
        return new NotFound(data);
    }
}
