package io.aboutcode.stage.web.websocket.io.noop;

import io.aboutcode.stage.web.websocket.io.WebsocketIo;
import java.util.Optional;

public class NoopWebsocketIo implements WebsocketIo {
    @Override
    public Optional<String> serialize(Object element) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Optional<Object> deserialize(String message) {
        throw new UnsupportedOperationException();
    }
}
