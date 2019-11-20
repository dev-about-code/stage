package io.aboutcode.stage.web.response;

/**
 * Default response for unauthorized access to a resource.
 */
public final class NotAuthorized extends DefaultResponse {
    private NotAuthorized(Object data) {
        super(true, null, data, 403);
    }

    /**
     * Creates a new instance of this response.
     *
     * @return The created instance
     */
    public static Response create() {
        return new NotAuthorized(null);
    }

    /**
     * Creates a new instance of this response with the specified contents.
     *
     * @param data The contents of this response
     *
     * @return The created instance
     */
    public static Response with(Object data) {
        return new NotAuthorized(data);
    }
}
