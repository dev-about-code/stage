package io.aboutcode.stage.web.web.response.renderer;

import io.aboutcode.stage.web.web.request.Request;
import io.aboutcode.stage.web.web.response.Response;

/**
 * Implementations of this are used internally by the web framework to render responses to the
 * client.
 */
public interface ResponseRenderer {
   /**
    * Renders the data object of the specified response as a string representation that will be send
    * to the client. Additionally, the renderer should set the <code>Content-Type</code> header of
    * the response to the correct value.
    *
    * @param request  The corresponding request
    * @param response The response object
    *
    * @return The response's data object rendered as string
    */
   String render(Request request, Response response);
}
