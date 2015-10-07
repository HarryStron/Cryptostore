public enum Command {
    DONE(1, "DONE msg"),
    OK(2, "OK msg"),
    READY(3, "READY msg"),
    ERROR(4, "ERROR msg"),
    CLOSE(5, "CLOSE connection"),
    file_from_server(6, "file form server to client"),
    file_from_client(7, "file send from client to server");

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
