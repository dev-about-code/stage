package io.aboutcode.stage.web.web;

import java.util.Optional;

/**
 * A client's session on the web server.
 */
public interface Session {
    /**
     * Optionally retrieves the attribute with the specified name from the session.
     *
     * @param name The name of the attribute to retrieve
     * @param <T>  The expected type of the attribute
     *
     * @return The value of the attribute
     */
    <T> Optional<T> attribute(String name);

    /**
     * Sets the attribute of the specified name to the specified value.
     *
     * @param name  The name of the attribute to set
     * @param value The value to set the attribute to
     */
    void attribute(String name, Object value);
}
