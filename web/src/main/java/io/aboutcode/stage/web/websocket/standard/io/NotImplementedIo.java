package io.aboutcode.stage.web.websocket.standard.io;

import io.aboutcode.stage.web.websocket.WebsocketIo;

public class NotImplementedIo<MessageT> implements WebsocketIo<MessageT> {
    @Override
    public String serialize(MessageT element) {
        throw new UnsupportedOperationException();
    }

    @Override
    public MessageT deserialize(String message) {
        throw new UnsupportedOperationException();
    }
}
