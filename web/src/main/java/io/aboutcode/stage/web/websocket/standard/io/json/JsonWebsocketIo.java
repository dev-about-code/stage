package io.aboutcode.stage.web.websocket.standard.io.json;

import com.google.common.collect.BiMap;
import com.google.common.collect.ImmutableBiMap;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import io.aboutcode.stage.util.Tuple2;
import io.aboutcode.stage.web.websocket.WebsocketIo;
import java.io.IOException;
import java.util.Map;

/**
 * This serializes and deserializes messages to and from Json format.
 */
public final class JsonWebsocketIo implements WebsocketIo<JsonMessage> {
    private static final Gson gson = new GsonBuilder().create();
    private final BiMap<String, Class<? extends JsonMessage>> messageIdentifierToMessageClass;

    public JsonWebsocketIo(Map<String, Class<? extends JsonMessage>> messages) {
        messageIdentifierToMessageClass = ImmutableBiMap.copyOf(messages);
    }

    private static Tuple2<String, JsonObject> getIdentifier(String message) {
        JsonParser parser = new JsonParser();
        JsonObject jsonObject = parser.parse(message).getAsJsonObject();
        return Tuple2.of(jsonObject.get("type").getAsString(), jsonObject);
    }

    @Override
    public String serialize(JsonMessage element) {
        element.setIdentifier(messageIdentifierToMessageClass.inverse().get(element.getClass()));
        return gson.toJson(element);
    }

    @Override
    public JsonMessage deserialize(String message) throws IOException {
        Tuple2<String, JsonObject> parseResult = getIdentifier(message);
        Class<? extends JsonMessage> messageClass = messageIdentifierToMessageClass
                .get(parseResult.one());
        if (messageClass == null) {
            throw new IOException(
                    String.format("Could not find a matching object type for message: %s",
                                  message));
        } else {
            try {
                return gson.fromJson(parseResult.two(), messageClass);
            } catch (Exception e) {
                throw new IOException(
                        String.format("Could not deserialize type '%s' from message: %s",
                                      messageClass, message), e);
            }
        }
    }
}
