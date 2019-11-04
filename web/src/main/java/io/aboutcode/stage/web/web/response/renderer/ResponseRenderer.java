package io.aboutcode.stage.web.web.response.renderer;

import io.aboutcode.stage.web.web.request.Request;
import io.aboutcode.stage.web.web.response.Response;

/**
 * Implementations of this are used internally by the web framework to render responses to the client.
 */
public interface ResponseRenderer {
    /**
     * Transforms the data object of the specified response to the representation that will be send to the client.
     * Additionally, the renderer should set the <code>Content-Type</code> header of the response to the correct value.
     *
     * @param request  The corresponding request
     * @param response The response object
     * @return The response's data object transformed into the expected output
     */
    Object render(Request request, Response response);
}
