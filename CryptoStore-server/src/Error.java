public enum Error {
    // COMMANDS
    UNKNOWN_COMMAND(1, "The command received is not recognised"),
    // AUTH
    CANNOT_AUTH(2, "Client failed authorization to the server"),
    NO_USER(3, "The username doesn't exist"),
    INCORRECT_PASSWORD(4, "The password was incorrect"),
    // FILES
    FILE_NOT_FOUND(5, "The requested filename doesn't exist"),
    FILE_NOT_SENT(6, "The file was not sent"),
    FILE_NOT_RECEIVED(7, "The file was not received"),
    CANNOT_SAVE_FILE(8, "The file could not be saved"),
    ZERO_SIZE(9, "Zero size values for input are not allowed"),
    NEGATIVE_SIZE(10, "Negative values for input are not allowed"),
    // CONNECTION / COMMUNICATION
    CANNOT_CONNECT(11, "Connection between client and server could not be established"),
    CLIENT_DISCONNECTED(12, "The client disconnected"),
    SERER_DISCONNECTED(13, "The server disconnected"),
    SOCKET_CLOSED(14, "The socket is closed"),
    FAILED_TO_WRITE(15, "The packets could not be sent"),
    FAILED_TO_READ(16, "The packets could not be read"),
    COMMUNICATION_FAILED(17, "The communication between client and server has failed"),
    FAILED_TO_CLOSE_STREAMS(18, "Failed to close the streams"),
    // VALIDITY
    INCORRECT_FORM(19, "The input was not of the correct form");

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
