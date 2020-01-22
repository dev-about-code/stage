package io.aboutcode.stage.web.serialization;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonSerializer;
import io.aboutcode.stage.web.request.Request;
import io.aboutcode.stage.web.response.Response;
import java.lang.reflect.Type;
import java.util.Collections;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Default converter (serializer and deserializer) for JSON.
 */
public final class JsonWebSerialization implements WebSerialization {
    private final Gson parser;

    public JsonWebSerialization() {
        this(Collections.emptySet());
    }

    public JsonWebSerialization(TypeAdapter... typeAdapters) {
        this(Optional.ofNullable(typeAdapters)
                     .map(adapters -> Stream.of(adapters).collect(Collectors.toSet()))
                     .orElseGet(HashSet::new)
        );
    }


    public JsonWebSerialization(Set<TypeAdapter> typeAdapters) {
        parser = Optional.ofNullable(typeAdapters)
                         .orElseGet(HashSet::new)
                         .stream()
                         .collect(GsonBuilder::new,
                                  (gsonBuilder, typeAdapter) ->
                                          gsonBuilder.registerTypeAdapter(typeAdapter.getType(),
                                                                          typeAdapter),
                                  (left, right) -> {})
                         .disableHtmlEscaping()
                         .create();
    }

    public String serialize(Object data) {
        return parser.toJson(data);
    }

    public <T> T deserialize(String input, Class<T> type) {
        return parser.fromJson(input, type);
    }

    @Override
    public void setContentType(Request request, Response response) {
        response.contentType("application/json");
    }

    public interface TypeAdapter<T> extends JsonSerializer<T>, JsonDeserializer<T> {
        Type getType();
    }
}
