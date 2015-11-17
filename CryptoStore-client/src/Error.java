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
    CANNOT_RECEIVE_FILE(9, "The file could not be received from the server"),
    ZERO_SIZE(10, "Zero size values for input are not allowed"),
    NEGATIVE_SIZE(11, "Negative values for input are not allowed"),
    HASHMAP_NOT_EXISTS(12, "The mapping for the encryption of the filenames doesn't exist"),
    // CONNECTION / COMMUNICATION
    CANNOT_CONNECT(13, "Connection between client and server could not be established"),
    CLIENT_DISCONNECTED(14, "The client disconnected"),
    SERER_DISCONNECTED(15, "The server disconnected"),
    SOCKET_CLOSED(16, "The socket is closed"),
    FAILED_TO_WRITE(17, "The packets could not be sent"),
    FAILED_TO_READ(18, "The packets could not be read"),
    COMMUNICATION_FAILED(19, "The communication between client and server has failed"),
    FAILED_TO_CLOSE_STREAMS(20, "Failed to close the streams"),
    // VALIDITY
    INCORRECT_FORM(21, "The input was not of the correct form"),
    // Encryption
    CANNOT_DECRYPT(22, "The file could not be decrypted"),
    CANNOT_ENCRYPT(23, "The file could not be encrypted");

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
