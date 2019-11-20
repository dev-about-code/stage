package io.aboutcode.stage.web.autowire;

import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Inherited
@Target({ElementType.METHOD})
public @interface DELETE {
    /**
     * Returns the path of the endpoint, relative to any {@link Path} value, if present
     *
     * @return The path of the endpoint
     */
    String value();
}
