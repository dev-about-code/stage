package io.aboutcode.stage.web.web.response;

/**
 * Default "Internal Server Error" response with a status of 501.
 */
public final class InternalServerError extends DefaultResponse {
    private InternalServerError(Object data) {
        super(true, null, data, 501);
    }

    /**
     * Creates a new instance of this response with the specified contents.
     *
     * @param data The contents of this response
     *
     * @return The created instance
     */
    public static Response with(Object data) {
        return new InternalServerError(data);
    }
}
