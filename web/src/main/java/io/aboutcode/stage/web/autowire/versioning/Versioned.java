package io.aboutcode.stage.web.autowire.versioning;

import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Methods or classes annotated with this annotation contain versioned endpoints. Any validly
 * annotated endpoint that has this annotation present, too, will partake in endpoint versioning.
 */
@Retention(RetentionPolicy.RUNTIME)
@Inherited
@Target({ElementType.METHOD})
public @interface Versioned {
    /**
     * Returns the version at which this endpoint was introduced. If null, this endpoint was
     * introduced at inception of the project.
     *
     * @return A version string of the format <code>\d+\.\d+\.\d+</code>. No other format is
     * accepted.
     */
    String introduced() default "";

    /**
     * Returns the version at which this endpoint was deprecated. If null, this endpoint has not
     * been deprecated yet and is still valid.
     *
     * @return A version string of the format <code>\d+\.\d+\.\d+</code>. No other format is
     * accepted.
     */
    String deprecated() default "";
}
