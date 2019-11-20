package io.aboutcode.stage.web.autowire;

/**
 * <p>
 * Marker interface for components that can serve web endpoints. Any public method annotated with
 * either one of {@link GET}, {@link POST}, {@link OPTIONS}, {@link PATCH}, {@link DELETE} or {@link
 * PUT} will be served. Any returned objects will be serialized with the default serialization
 * mechanism of the web context. Any input type will be deserialized automatically if annotated
 * correctly with {@link Body}, {@link PathParameter} or {@link QueryParameter}.
 * </p>
 * <p>Additionally, annotating a method or class with {@link io.aboutcode.stage.web.autowire.auth.Authorized}
 * and/or {@link io.aboutcode.stage.web.autowire.auth.Unauthorized} along with specifying the
 * correct {@link io.aboutcode.stage.web.autowire.auth.AuthorizationRealm} will automatically
 * attempt authorization for that method.</p>
 * <p>Annotating an implementation of this interface with {@link Path} allows setting of the default
 * path for all methods in the context of the class.</p>
 * <p>
 * Any method parameter of type {@link io.aboutcode.stage.web.request.Request} does not need to
 * be annotated and will provide direct access to the request object.
 * </p>
 * <p>
 * Lastly, annotating a method with {@link Raw} will allow that method to circumvent the normal
 * deserialization process of the returned value and will allow returning a response directly.
 * </p>
 */
public interface WebRequestHandler {
}
