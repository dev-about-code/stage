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
    // this is horrible - thanks a lot, Java
    String DEFAULT_VALUE = "---DEFAULT---";

    /**
     * Returns the name of the query parameter to assign the value from
     *
     * @return The name of the query parameter to assign the value from
     */
    String value();


    /**
     * Returns the default value that this parameter should be assigned if it is not set
     *
     * @return The default value that this parameter should be assigned if it is not set. Must be
     * parsable into the target type
     */
    String defaultValue() default DEFAULT_VALUE;

    /**
     * If true (the default), the request will fail with an error if the specified query parameter
     * is not found. Otherwise, this assigns null.
     *
     * @return True if the query parameter is mandatory, false otherwise
     */
    boolean mandatory() default true;
}
