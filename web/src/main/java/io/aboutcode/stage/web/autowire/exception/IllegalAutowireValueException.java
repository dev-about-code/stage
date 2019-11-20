package io.aboutcode.stage.web.autowire.exception;

public class IllegalAutowireValueException extends AutowiringException {
    public IllegalAutowireValueException(String message) {
        super(message);
    }

    public IllegalAutowireValueException(String message, Throwable cause) {
        super(message, cause);
    }
}
