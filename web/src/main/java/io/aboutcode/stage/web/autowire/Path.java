package io.aboutcode.stage.web.autowire;

import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Classes annotated with this will prefix the path of every method served in the web context with
 * the specified value.
 */
@Retention(RetentionPolicy.RUNTIME)
@Inherited
@Target({ElementType.TYPE})
public @interface Path {
    /**
     * Returns the path that every endpoint is defined relative to
     *
     * @return The path that every endpoint is defined relative to
     */
    String value();
}
