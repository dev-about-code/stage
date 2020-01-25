package io.aboutcode.stage.web.autowire.exception;

/**
 * Thrown if a request was not authorized to be executed.
 */
public class UnauthorizedException extends Exception {
    private String path;

    public UnauthorizedException(String path) {
        super(String.format("Access to path '%s' denied for user", path));
        this.path = path;
    }

    public String getPath() {
        return path;
    }
}
