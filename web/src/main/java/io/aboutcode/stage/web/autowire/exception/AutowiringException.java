package io.aboutcode.stage.web.autowire.exception;

public class AutowiringException extends RuntimeException {
    public AutowiringException(String message) {
        super(message);
    }

    public AutowiringException(String message, Throwable cause) {
        super(message, cause);
    }
}
