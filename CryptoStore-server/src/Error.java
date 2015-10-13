public enum Error {
    FILE_NOT_FOUND(1, "The requested filename doesn't exist"),
    CLIENT_DISCONECTED(2, "The client is disconnected"),
    PACKETS_NOT_SENT(3, "The packets could not be sent"),
    CANNOT_SAVE_FILE(4, "The file could not be saved"),
    EMPTY_FILENAME(5, "The filename cannot be empty"),
    FILE_NOT_SENT(6, "The file was not sent"),
    CANNOT_CONNECT(7, "The server cannot connect to client. Check there is only one instance of the server running"),
    SOCKET_CLOSED(8, "The socket is closed"),
    CANNOT_READ(9, "The server cannot read from the socket"),
    NO_USER(10, "The client username doesn't exist"),
    WRONG_PASS(11, "The client password was incorrect"),
    CANNOT_WRITE_DB(12, "The entry to the DB could not be made");

    private final int code;
    private final String description;

    Error(int code, String description) {
        this.code = code;
        this.description = description;
    }

    public void print(String client) {
        System.out.println("ERROR " + code + ": " + description + '!' + " (" + client + ')');
    }

    public void print() {
        System.out.println("ERROR " + code + ": " + description + '!');
    }
}
