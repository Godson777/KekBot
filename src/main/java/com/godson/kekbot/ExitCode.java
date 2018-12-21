package com.godson.kekbot;

public enum ExitCode {
    REBOOT(100),
    STOP(101),
    UPDATE(200),
    GENERIC_ERROR(300),
    SHITTY_CONFIG(301),
    DISCONNECTED(302),
    DB_OFFLINE(303),
    UNKNOWN(-1);

    private final int code;

    ExitCode(int code) {
        this.code = code;
    }

    public int getCode() {
        return code;
    }
}
