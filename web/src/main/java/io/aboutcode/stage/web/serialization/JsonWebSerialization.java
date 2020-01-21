package io.aboutcode.stage.web.serialization;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import io.aboutcode.stage.web.request.Request;
import io.aboutcode.stage.web.response.Response;

/**
 * Default converter (serializer and deserializer) for JSON.
 */
// TODO: make this extensible
public final class JsonWebSerialization implements WebSerialization {
    private final Gson parser = new GsonBuilder()
            .create();

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
}
