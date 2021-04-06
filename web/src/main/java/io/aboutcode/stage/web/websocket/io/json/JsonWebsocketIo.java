package io.aboutcode.stage.web.websocket.io.json;

import com.google.common.collect.BiMap;
import com.google.common.collect.ImmutableBiMap;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import io.aboutcode.stage.util.Tuple2;
import io.aboutcode.stage.web.websocket.io.WebsocketIo;
import java.io.IOException;
import java.util.Map;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This serializes and deserializes messages to and from Json format.
 */
public final class JsonWebsocketIo implements WebsocketIo {
    private static final Logger LOGGER = LoggerFactory.getLogger(JsonWebsocketIo.class);
    private static final String TYPE_IDENTIFIER = "type";
    private static final Gson gson = new GsonBuilder().create();
    private final BiMap<String, Class<?>> messageIdentifierToMessageClass;

    public JsonWebsocketIo(Map<String, Class<?>> mapping) {
        messageIdentifierToMessageClass = ImmutableBiMap.copyOf(mapping);
    }

    private static Tuple2<String, JsonObject> getIdentifier(String message) {
        JsonParser parser = new JsonParser();
        JsonObject jsonObject = parser.parse(message).getAsJsonObject();
        return Tuple2.of(jsonObject.get(TYPE_IDENTIFIER).getAsString(), jsonObject);
    }

    @Override
    public Optional<String> serialize(Object element) {
        try {
            JsonElement jsonElement = gson.toJsonTree(element);
            if(jsonElement.isJsonObject()) {
                JsonObject jsonObject = jsonElement.getAsJsonObject();
                jsonObject.addProperty(TYPE_IDENTIFIER,
                                       messageIdentifierToMessageClass.inverse()
                                                                      .get(element.getClass()));
                return Optional.of(gson.toJson(jsonObject));
            } else {
                return Optional.of(gson.toJson(jsonElement));
            }
        } catch (Exception e) {
            LOGGER.warn("Could not serialize object {} because: {}", element, e.getMessage(), e);
        }
        return Optional.empty();
    }

    @Override
    public Optional<Object> deserialize(String message) throws IOException {
        try {
            Tuple2<String, JsonObject> parseResult = getIdentifier(message);
            Class<?> messageClass = messageIdentifierToMessageClass.get(parseResult.one());
            if (messageClass == null) {
                LOGGER.debug("Could not find a matching object type for message: {}", message);
            } else {
                return Optional.ofNullable(gson.fromJson(parseResult.two(), messageClass));
            }
        } catch (Exception e) {
            LOGGER.debug("Could not deserialize message {} because: {}", message, e.getMessage(),
                         e);
        }

        return Optional.empty();
    }
}
