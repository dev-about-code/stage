package io.aboutcode.stage.lifecycle;

/**
 * An exception thrown by a {@link LifeCycleAware} unit.
 */
public class LifeCycleException extends Exception {
    public LifeCycleException() {
    }

    public LifeCycleException(String message) {
        super(message);
    }

    public LifeCycleException(String message, Throwable cause) {
        super(message, cause);
    }

    public LifeCycleException(Throwable cause) {
        super(cause);
    }
}
