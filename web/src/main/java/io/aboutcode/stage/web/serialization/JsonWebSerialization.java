package io.aboutcode.stage.web.serialization;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

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
}
