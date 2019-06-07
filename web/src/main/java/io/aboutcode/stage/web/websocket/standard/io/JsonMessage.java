package io.aboutcode.stage.web.websocket.standard.io;

import com.google.gson.annotations.SerializedName;
import io.aboutcode.stage.web.websocket.standard.TypedWebSocketMessage;

/**
 * A default implementation of {@link TypedWebSocketMessage} that is send in Json format for message
 * serialization and deserialization.
 */
public abstract class JsonMessage implements TypedWebSocketMessage {
    @SerializedName("type")
    private String type;

    public JsonMessage(String type) {
        this.type = type;
    }

    public final String getIdentifier() {
        return type;
    }
}
