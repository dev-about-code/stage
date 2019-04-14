package io.aboutcode.stage.web.web.response.renderer;

import com.google.common.net.MediaType;
import io.aboutcode.stage.web.web.request.Request;
import io.aboutcode.stage.web.web.response.Response;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

/**
 * This renderer parses the request's expected media type and attempts to find a matching response
 * renderer. If none is found, the default response renderer is used to return a result.
 */
public final class MappingMediaTypeRenderer implements ResponseRenderer {
   private static final String ACCEPT_TYPE_HEADER = "Accept";
   private final ResponseRenderer defaultRenderer;
   private final Map<MediaType, ResponseRenderer> mediaTypeToResponseHandler = new HashMap<>();

   public MappingMediaTypeRenderer(ResponseRenderer defaultRenderer) {
      this.defaultRenderer = defaultRenderer;
   }

   /**
    * Adds a mapping for the specified response renderer to the specified media types.
    *
    * @param renderer The renderer to map
    * @param types    The types to map the renderer to
    *
    * @return This for fluent interface
    */
   public MappingMediaTypeRenderer map(ResponseRenderer renderer, MediaType... types) {
      Stream
          .of(types)
          .forEach(type -> mediaTypeToResponseHandler.put(type, renderer));
      return this;
   }

   @Override
   public String render(Request request, Response response) {
      return request
          .header(ACCEPT_TYPE_HEADER)
          .flatMap(type -> {
             try {
                return Optional.ofNullable(MediaType.parse(type));
             } catch (Exception e) {
                return Optional.empty();
             }
          })
          .flatMap(type -> Optional.ofNullable(mediaTypeToResponseHandler.get(type)))
          .orElse(defaultRenderer)
          .render(request, response);
   }
}
