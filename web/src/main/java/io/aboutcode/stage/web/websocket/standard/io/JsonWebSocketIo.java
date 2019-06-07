package io.aboutcode.stage.web.websocket.standard.io;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import io.aboutcode.stage.util.Tuple2;
import io.aboutcode.stage.web.websocket.WebSocketIo;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * This serializes and deserializes messages to and from Json format.
 */
public class JsonWebSocketIo implements WebSocketIo<JsonMessage> {
    private static final Gson gson = new GsonBuilder().create();
    private final Map<String, Class<? extends JsonMessage>> messageIdentifierToMessageClass = new HashMap<>();

    private static Tuple2<String, JsonObject> getIdentifier(String message) {
        JsonParser parser = new JsonParser();
        JsonObject jsonObject = parser.parse(message).getAsJsonObject();
        return Tuple2.of(jsonObject.get("type").getAsString(), jsonObject);
    }

    @Override
    public <TargetT extends JsonMessage> void registerMessageType(String identifier,
                                                                  Class<TargetT> type) {
        messageIdentifierToMessageClass.put(identifier, type);
    }

    @Override
    public String serialize(Object element) throws IOException {
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
