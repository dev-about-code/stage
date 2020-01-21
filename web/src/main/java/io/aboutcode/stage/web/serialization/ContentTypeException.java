package io.aboutcode.stage.web.serialization;

/**
 * Thrown if setting the content type for a response failse for some reason
 */
public class ContentTypeException extends Exception {
    public ContentTypeException(String message) {
        super(message);
    }

    public ContentTypeException(String message, Throwable cause) {
        super(message, cause);
    }
}
