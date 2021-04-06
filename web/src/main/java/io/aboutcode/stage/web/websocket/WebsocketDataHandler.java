package io.aboutcode.stage.web.websocket;

import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * <p>
 * Any method annotated with this will accept websocket messages of the type specified in the
 * parameters. Additionally, if the method specifies a parameter of type {@link
 * io.aboutcode.stage.web.websocket.WebsocketContext} then that context will be injected as
 * well.</p>
 * <p>Return value of the method will be serialized and sent to the calling client as a
 * response.</p>
 * <p>Note that multiple methods can specify the same type as recipient - all of those methods will
 * be invoked for any received message</p>
 */
@Retention(RetentionPolicy.RUNTIME)
@Inherited
@Target({ElementType.METHOD})
public @interface WebsocketDataHandler {
}
