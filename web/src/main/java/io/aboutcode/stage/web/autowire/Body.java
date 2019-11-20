package io.aboutcode.stage.web.autowire;

import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Method parameters annotated with this will be deserialized as objects directly from the <em>full
 * contents</em> of the body of the request.
 */
@Retention(RetentionPolicy.RUNTIME)
@Inherited
@Target({ElementType.PARAMETER})
public @interface Body {
}
