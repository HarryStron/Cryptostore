public enum Command {
    OK(1, "OK msg"),
    ERROR(2, "ERROR msg"),
    CLOSE(3, "CLOSE connection"),
    AUTH(4, "AUTH msg"),
    file_from_server(5, "file form server to client"),
    file_from_client(6, "file send from client to server");

    private final int code;
    private String description;

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
