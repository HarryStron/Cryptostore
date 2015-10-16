public enum Error {
    // COMMANDS
    UNKNOWN_COMMAND(1, "The command received is not recognised"),
    // AUTH
    CANNOT_AUTH(2, "Client failed authorization to the server"),
    NO_USER(3, "The username doesn't exist"),
    // FILES
    FILE_NOT_FOUND(4, "The requested filename doesn't exist"),
    FILE_NOT_SENT(5, "The file was not sent"),
    FILE_NOT_RECEIVED(6, "The file was not received"),
    CANNOT_SAVE_FILE(7, "The file could not be saved"),
    ZERO_SIZE(8, "Zero size values for input are not allowed"),
    NEGATIVE_SIZE(9, "Negative values for input are not allowed"),
    // CONNECTION / COMMUNICATION
    CANNOT_CONNECT(10, "Connection between client and server could not be established"),
    CLIENT_DISCONNECTED(11, "The client disconnected"),
    SERER_DISCONNECTED(12, "The server disconnected"),
    SOCKET_CLOSED(13, "The socket is closed"),
    FAILED_TO_WRITE(14, "The packets could not be sent"),
    FAILED_TO_READ(15, "The packets could not be read"),
    COMMUNICATION_FAILED(16, "The communication between client and server has failed"),
    FAILED_TO_CLOSE_STREAMS(17, "Failed to close the streams");

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
