public enum Error {
    // COMMANDS
    UNKNOWN_COMMAND(1, "The command received is not recognised"),
    // AUTH
    CANNOT_AUTH(2, "Client failed authorization to the server"),
    NO_USER(3, "The client username doesn't exist"),
    // FILES
    FILE_NOT_FOUND(4, "The requested filename doesn't exist"),
    FILE_NOT_SENT(5, "The file was not sent"),
    CANNOT_SAVE_FILE(6, "The file could not be saved"),
    ZERO_SIZE(7, "Zero length input not allowed"),
    // CONNECTION / COMMUNICATION
    CANNOT_CONNECT(8, "The server cannot connect to client. Check there is only one instance of the server running"),
    CLIENT_DISCONNECTED(9, "The client is disconnected"),
    SOCKET_CLOSED(10, "The socket is closed"),
    FAILED_TO_WRITE(11, "The packets could not be sent to the client"),
    FAILED_TO_READ(12, "Server failed to read the data sent from client"),
    COMMUNICATION_FAILED(13, "The communication between client and server has failed"),
    FAILED_TO_CLOSE_STREAMS(14, "The server failed to close the streams");

    private final int code;
    private final String description;

    Error(int code, String description) {
        this.code = code;
        this.description = description;
    }

    public String getDescription(String client) {
        return "ERROR " + code + ": " + description + '!' + " (" + client + ')';
    }

    public String getDescription() {
        return "ERROR " + code + ": " + description + '!';
    }

    public void print(String client) {
        System.out.println("ERROR " + code + ": " + description + '!' + " (" + client + ')');
    }

    public void print() {
        System.out.println("ERROR " + code + ": " + description + '!');
    }
}
