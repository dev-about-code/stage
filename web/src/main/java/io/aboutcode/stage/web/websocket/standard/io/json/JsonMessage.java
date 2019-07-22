package io.aboutcode.stage.web.websocket.standard.io.json;

import com.google.gson.annotations.SerializedName;
import io.aboutcode.stage.web.websocket.standard.TypedWebsocketMessage;

/**
 * A default implementation of {@link TypedWebsocketMessage} that is send in Json format for message
 * serialization and deserialization.
 */
public abstract class JsonMessage implements TypedWebsocketMessage {
    @SerializedName("type")
    private String type;

    @Override
    public final String getIdentifier() {
        return type;
    }

    @Override
    public final void setIdentifier(String identifier) {
        this.type = identifier;
    }
}
