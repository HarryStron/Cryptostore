package com.cryptostore.client;

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
    CANNOT_COPY_FILE(9, "The file could not be saved"),
    CANNOT_RECEIVE_FILE(10, "The file could not be received from the server"),
    DELETE_FAIL(11, "Deletion of the file has failed"),
    LOCAL_DELETE_FAIL(12, "The file could not be deleted from the local directory"),
    SERVER_DELETE_FAIL(13, "The file could not be deleted on the server"),
    ZERO_SIZE(14, "Zero size values for input are not allowed"),
    NEGATIVE_SIZE(15, "Negative values for input are not allowed"),
    HASHMAP_NOT_FOUND(16, "The filename mapping was not found"),
    // CONNECTION / COMMUNICATION
    CANNOT_CONNECT(17, "Connection between client and server could not be established"),
    CLIENT_DISCONNECTED(18, "The client disconnected"),
    SERVER_DISCONNECTED(19, "The server disconnected"),
    SOCKET_CLOSED(20, "The socket is closed"),
    FAILED_TO_WRITE(21, "The packets could not be sent"),
    FAILED_TO_READ(22, "The packets could not be read"),
    COMMUNICATION_FAILED(23, "The communication between client and server has failed"),
    FAILED_TO_CLOSE_STREAMS(24, "Failed to close the streams"),
    // VALIDITY
    INCORRECT_FORM(25, "The input was not of the correct form"),
    // Encryption
    CANNOT_DECRYPT(26, "The file could not be decrypted"),
    CANNOT_ENCRYPT(27, "The file could not be encrypted"),
    //SYNC
    CANNOT_SYNC(28, "The synchronisation failed");

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
