package io.aboutcode.stage.web.autowire;

import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Method parameters annotated with this will retrieve its value from the path parameters.
 */
@Retention(RetentionPolicy.RUNTIME)
@Inherited
@Target({ElementType.PARAMETER})
public @interface PathParameter {
    /**
     * Returns the name of the path parameter to assign the value from
     *
     * @return The name of the path parameter to assign the value from
     */
    String value();
}
