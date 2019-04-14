package io.aboutcode.stage.web.web.response.renderer;

import com.google.common.net.MediaType;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import io.aboutcode.stage.web.web.request.Request;
import io.aboutcode.stage.web.web.response.HttpHeader;
import io.aboutcode.stage.web.web.response.Response;

/**
 * Default renderer for JSON responses.
 */
public class JsonResponseRenderer implements ResponseRenderer {
   private final Gson PARSER = new GsonBuilder().create();
   private final MediaType responseType;

   private JsonResponseRenderer(MediaType responseType) {
      this.responseType = responseType;
   }

   /**
    * Creates a new response renderer with a media type of <code>application/json</code>
    *
    * @return The created renderer
    */
   public static JsonResponseRenderer json() {
      return new JsonResponseRenderer(MediaType.JSON_UTF_8);
   }

   /**
    * Creates a new response renderer with a media type of <code>application/javascript</code>
    *
    * @return The created renderer
    */
   public static JsonResponseRenderer jsonp() {
      return new JsonResponseRenderer(MediaType.JAVASCRIPT_UTF_8);
   }

   @Override
   public String render(Request request, Response response) {
      HttpHeader.CONTENT_TYPE.set(response, responseType.toString());
      return response.data() == null ? null : PARSER.toJson(response.data());
   }
}
