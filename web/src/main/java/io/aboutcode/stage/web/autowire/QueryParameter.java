package io.aboutcode.stage.web.autowire;

import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Method parameters annotated with this will retrieve its value from the query parameters.
 */
@Retention(RetentionPolicy.RUNTIME)
@Inherited
@Target({ElementType.PARAMETER})
public @interface QueryParameter {
    /**
     * Returns the name of the query parameter to assign the value from
     *
     * @return The name of the query parameter to assign the value from
     */
    String value();

    /**
     * If true (the default), the request will fail with an error if the specified query parameter
     * is not found. Otherwise, this assigns null.
     *
     * @return True if the query parameter is mandatory, false otherwise
     */
    boolean mandatory() default true;
}
