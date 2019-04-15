package io.aboutcode.stage.web.web.request;

import io.aboutcode.stage.web.web.Session;
import java.util.Optional;
import java.util.Set;

/**
 * This represents a HTTP request from a client to the web server. The request will be passed on
 * from each {@link RequestHandler} to the next. Hence, state will be promoted through the chain and
 * can be re-used in subsequent handlers.
 */
public interface Request {
   /**
    * Returns an attribute of the request that has been added to it previously, most likely by a
    * previous {@link RequestHandler}.
    *
    * @param name The name of the attribute to retrieve
    *
    * @return Optionally, the retrieved attribute
    */
   Optional<Object> attribute(String name);

   /**
    * Sets the attribute with the specified name in the request to the specified value.
    *
    * @param name  The name of the attribute to set
    * @param value The value of the attribute to set
    */
   void attribute(String name, Object value);

   /**
    * Returns the parameter from the path of the specified name. Path parameters can be specified by
    * adding a colon to one of the path segments. E.g. <code>/api/user/:id/zip</code>.
    *
    * @param name The name of the parameter <em>without leading colon</em>.
    *
    * @return Optionally, the value for the requested path parameter
    */
   Optional<String> pathParam(String name);

   /**
    * Returns the raw body of the request.
    *
    * @return The raw body of the request or null if none is specified.
    */
   String body();

   /**
    * Returns the value of the request header with the specified name.
    *
    * @param name The name of the header value to retrieve
    *
    * @return Optionally, the value of the header
    */
   Optional<String> header(String name);

   /**
    * Returns the names of all headers that are specified for this request.
    *
    * @return The names of all headers that are available for this request or an empty {@link Set}.
    * Never null.
    */
   Set<String> headers();

   /**
    * Returns the type of the request, e.g. "GET", "POST", etc.
    *
    * @return The type of the request
    */
   RequestType method();

   /**
    * Returns the current session for this requests and creates one if none exists. Sessions may
    * persist longer than a request. Hence, values stored in a session are availabel to subsequent
    * requests, too.
    *
    * @return The session associated with this request
    */
   Session session();

   /**
    * The path the current request was invoked on.
    *
    * @return The path the current request was invoked on
    */
   String path();
}
