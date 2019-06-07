package io.aboutcode.stage.web.web.response.renderer;

import com.google.common.net.MediaType;
import io.aboutcode.stage.web.web.request.Request;
import io.aboutcode.stage.web.web.response.HttpHeader;
import io.aboutcode.stage.web.web.response.Response;

/**
 * Renders the responses body using {@link Object#toString()}, setting the header to
 * <code>text/plain</code>
 */
public final class RawResponseRenderer implements ResponseRenderer {
    @Override
    public String render(Request request, Response response) {
        HttpHeader.CONTENT_TYPE.set(response, MediaType.PLAIN_TEXT_UTF_8.toString());
        return response.data() == null ? null : response.data().toString();
    }
}
