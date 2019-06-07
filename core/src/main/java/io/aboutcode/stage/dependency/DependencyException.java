package io.aboutcode.stage.dependency;

/**
 * The dedicated exception thrown on errors in retrieval and/or definition of inter-module
 * dependencies.
 */
public class DependencyException extends Exception {
    /**
     * Creates a new exception
     *
     * @param message The description of the exception
     */
    public DependencyException(String message) {
        super(message);
    }
}
