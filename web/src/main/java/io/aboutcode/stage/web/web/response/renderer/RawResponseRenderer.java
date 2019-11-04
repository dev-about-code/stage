package io.aboutcode.stage.web.web.response.renderer;

import io.aboutcode.stage.web.web.request.Request;
import io.aboutcode.stage.web.web.response.Response;

/**
 * Instances of this do not perform any transformation and simply pass the object verbatim, <em>NOT</em> setting the
 * response's <code>Content-Type</code> header.
 */
public final class RawResponseRenderer implements ResponseRenderer {
    @Override
    public Object render(Request request, Response response) {
        return response.data();
    }
}
