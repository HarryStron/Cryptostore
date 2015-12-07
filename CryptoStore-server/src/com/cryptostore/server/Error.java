package com.cryptostore.server;

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
    DELETE_FAIL(9, "Deletion of the file has failed"),
    LOCAL_DELETE_FAIL(10, "The file could not be deleted from the local directory"),
    SERVER_DELETE_FAIL(11, "The file could not be deleted on the server"),
    ZERO_SIZE(12, "Zero size values for input are not allowed"),
    NEGATIVE_SIZE(13, "Negative values for input are not allowed"),
    HASHMAP_NOT_FOUND(14, "The filename mapping was not found"),
    // CONNECTION / COMMUNICATION
    CANNOT_CONNECT(15, "Connection between client and server could not be established"),
    CLIENT_DISCONNECTED(16, "The client disconnected"),
    SERER_DISCONNECTED(17, "The server disconnected"),
    SOCKET_CLOSED(18, "The socket is closed"),
    FAILED_TO_WRITE(19, "The packets could not be sent"),
    FAILED_TO_READ(20, "The packets could not be read"),
    COMMUNICATION_FAILED(21, "The communication between client and server has failed"),
    FAILED_TO_CLOSE_STREAMS(22, "Failed to close the streams"),
    // VALIDITY
    INCORRECT_FORM(23, "The input was not of the correct form"),
    // Encryption
    CANNOT_DECRYPT(24, "The file could not be decrypted"),
    CANNOT_ENCRYPT(25, "The file could not be encrypted"),
    //SYNC
    CANNOT_SYNC(26, "The synchronisation failed");

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
