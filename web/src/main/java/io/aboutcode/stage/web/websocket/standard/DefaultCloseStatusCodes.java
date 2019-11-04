package io.aboutcode.stage.web.websocket.standard;

public enum DefaultCloseStatusCodes {
    NORMAL(1000),
    GOING_AWAY(1001),
    PROTOCOL_ERROR(1002),
    SERVER_ERROR(1011),
    SERVICE_RESTART(1012)
    ;

    private final int code;

    DefaultCloseStatusCodes(int code) {
        this.code = code;
    }

    public int getCode() {
        return code;
    }
}
