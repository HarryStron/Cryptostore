public enum Error {
    SERVER_DISCONECTED(1, "The server is disconnected"),
    FILE_NOT_FOUND(2, "The requested filename doesn't exist"),
    PACKETS_NOT_SENT(3, "The packets could not be sent"),
    PACKETS_NOT_RECEIVED(4, "The packets could not be received"),
    EMPTY_FILENAME(5, "The filename cannot be empty"),
    FILE_NOT_SENT(6, "The file was not sent"),
    FILE_NOT_RETRIEVED(7, "The file was not downloaded"),
    CANNOT_CONNECT(8, "The client cannot connect to server"),
    CANNOT_READ(9, "The client cannot read from the socket"),
    CANNOT_AUTH(10, "Client failed authorization to the server");

    private final int code;
    private final String description;

    Error(int code, String description) {
        this.code = code;
        this.description = description;
    }

    public void print() {
        System.out.println("ERROR " + code + ": " + description + '!');
    }
}
