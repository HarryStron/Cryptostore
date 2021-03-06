package com.cryptostore.client;

public enum Command {
    OK(1, "OK msg"),
    ERROR(2, "ERROR msg"),
    CLOSE(3, "CLOSE connection"),
    AUTH(4, "AUTH msg"),
    FILE_FROM_SERVER(5, "file form server to client"),
    FILE_FROM_CLIENT(6, "file send from client to server"),
    DELETE(7, "delete file on server"),
    SYNC(8, "get SYNC version"),
    SKIP(9, "skips a step if not required"),
    HEARTBEAT(10, "Heartbeat message"),
    VERSION(11, "Request for sync file version number"),
    NEW_USER(12, "Create a new user");

    private final int code;
    private final String description;

    Command(int code, String description) {
        this.code = code;
        this.description = description;
    }

    public int getCode() {
        return code;
    }

    @Override
    public String toString() {
        return code + ":" + description;
    }
}
