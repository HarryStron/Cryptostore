package com.cryptostore.server;

public enum Error {
    // COMMANDS
    UNKNOWN_COMMAND(1, "The command received is not recognised"),
    // AUTH
    CANNOT_AUTH(2, "Client failed authorization to the server"),
    NO_USER(3, "The username doesn't exist"),
    USER_EXISTS(4, "The username already exist"),
    USER_LOGGED(5, "The user is logged in from another client"),
    INCORRECT_PASSWORD(6, "The password was incorrect"),
    // FILES
    FILE_NOT_FOUND(7, "The requested filename doesn't exist"),
    FILE_NOT_SENT(8, "The file was not sent"),
    FILE_NOT_RECEIVED(9, "The file was not received"),
    CANNOT_SAVE_FILE(10, "The file could not be saved"),
    CANNOT_COPY_FILE(11, "The file could not be saved"),
    CANNOT_RECEIVE_FILE(12, "The file could not be received from the server"),
    DELETE_FAIL(13, "Deletion of the file has failed"),
    LOCAL_DELETE_FAIL(14, "The file could not be deleted from the local directory"),
    SERVER_DELETE_FAIL(15, "The file could not be deleted on the server"),
    ZERO_SIZE(16, "Zero size values for input are not allowed"),
    NEGATIVE_SIZE(17, "Negative values for input are not allowed"),
    HASHMAP_NOT_FOUND(18, "The filename mapping was not found"),
    // CONNECTION / COMMUNICATION
    CANNOT_CONNECT(19, "Connection between client and server could not be established"),
    CLIENT_DISCONNECTED(20, "The client disconnected"),
    SERER_DISCONNECTED(21, "The server disconnected"),
    SOCKET_CLOSED(22, "The socket is closed"),
    FAILED_TO_WRITE(23, "The packets could not be sent"),
    FAILED_TO_READ(24, "The packets could not be read"),
    COMMUNICATION_FAILED(25, "The communication between client and server has failed"),
    FAILED_TO_CLOSE_STREAMS(26, "Failed to close the streams"),
    // VALIDITY
    INCORRECT_FORM(27, "The input was not of the correct form"),
    // Encryption
    CANNOT_DECRYPT(28, "The file could not be decrypted"),
    CANNOT_ENCRYPT(29, "The file could not be encrypted"),
    //SYNC
    CANNOT_SYNC(30, "The synchronisation failed"),
    //DB
    DB_ERROR(31, "Error while executing queries");

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
