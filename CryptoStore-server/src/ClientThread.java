import org.apache.commons.io.IOUtils;

import javax.net.ssl.SSLSocket;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;

public class ClientThread extends Thread {
    private SSLSocket clientSocket;
    private TransferManager transferManager;
    private String clientIP;
    private boolean clientIsConnected;
    private boolean clientIsAuthed;

    public ClientThread(SSLSocket clientSocket) {
        super();
        this.clientSocket = clientSocket;
        clientIsConnected = true;
        clientIsAuthed = false;
        clientIP = clientSocket.getRemoteSocketAddress().toString().substring(1);
        new JDBCControl("jdbc:mysql://mysql.student.sussex.ac.uk:3306/cs391", "cs391", "r127xxhar1");
        clientPrint("Has established a connection!");
    }

    public void run() {
        super.run();

        transferManager = new TransferManager(clientSocket);

        /** Auth the user **/
        try {
            transferManager.writeControl(Command.AUTH);

            int usernameSize = singleByteIn(); //TODO long not int

            if (usernameSize > 0) {
                transferManager.writeControl(Command.OK);

                String username = listenForFilename(usernameSize);

                if (username.length() > 0) {
                    transferManager.writeControl(Command.OK);

                    int passwordSize = singleByteIn(); //TODO long not int

                    if (passwordSize > 0) {
                        transferManager.writeControl(Command.OK);

                        String password = listenForFilename(passwordSize);

                        if (password.length() > 0) {
                            clientIsAuthed = JDBCControl.checkUserPassword(username, HashGenerator.getHash(password, JDBCControl.getSalt(username), 100000, 32));
                            if (clientIsAuthed) {
                                transferManager.writeControl(Command.READY);
                            } else {
                                transferManager.writeControl(Command.ERROR);
                                closeConnection();
                            }
                        } else {
                            transferManager.writeControl(Command.ERROR);
                            Error.EMPTY_FILENAME.print(); //TODO empty password
                        }
                    } else {
                        transferManager.writeControl(Command.ERROR);
                        Error.EMPTY_FILENAME.print(); //TODO empty username
                    }
                } else {
                    transferManager.writeControl(Command.ERROR);
                    Error.EMPTY_FILENAME.print(); //TODO empty username
                }
            } else {
                transferManager.writeControl(Command.ERROR);
                Error.ZERO_SIZE.print();
            }

        } catch (IOException e) {
            Error.CANNOT_AUTH.print();
            closeConnection();
        } catch (InvalidKeySpecException e) {
            Error.CANNOT_AUTH.print();
            closeConnection();
        } catch (NoSuchAlgorithmException e) {
            Error.CANNOT_AUTH.print();
            closeConnection();
        }

        waitForClientRequest();
    }

    private void waitForClientRequest() {
        while (clientIsConnected && clientIsAuthed) {
            /** READ request from client **/
            try {
                int request = singleByteIn();

                if (request == Command.file_from_client.getCode()) {
                    clientPrint("Is trying to send a file.");
                    transferManager.writeControl(Command.OK);

                    int filenameSize = singleByteIn(); //TODO long not int
                    transferManager.writeControl(Command.OK);

                    String filename = listenForFilename(filenameSize);

                    if (!filename.equals("")) {
                        transferManager.writeControl(Command.READY);
                        writeToDisk(filename);

                    } else {
                        transferManager.writeControl(Command.ERROR);
                        Error.EMPTY_FILENAME.print(clientIP);
                    }

                } else if (request == Command.file_from_server.getCode()) {
                    clientPrint("Is trying to retrieve a file.");
                    transferManager.writeControl(Command.OK);

                    int filenameSize = singleByteIn(); //TODO long not int
                    transferManager.writeControl(Command.OK);

                    String filename = listenForFilename(filenameSize);

                    if (!filename.equals("")) {
                        if (new File(filename).exists()) {
                            transferManager.writeControl(Command.READY);
                            sendToClient(filename);
                        } else {
                            transferManager.writeControl(Command.ERROR);
                            Error.FILE_NOT_FOUND.print();
                        }
                    } else {
                        transferManager.writeControl(Command.ERROR);
                        Error.EMPTY_FILENAME.print(clientIP);
                    }
                }

                if (request == Command.CLOSE.getCode()) {
                    clientPrint("Terminates the connection!");
                    closeConnection();
                }
            } catch (IOException e) {
                Error.CLIENT_DISCONECTED.print(clientIP);
                closeConnection();
            }
        }
    }

    private void closeConnection() {
        clientIsConnected = false;
        clientIsAuthed = false;
        try {
            transferManager.closeStreams();
            clientSocket.close();
        } catch (IOException e) {
            Error.CLIENT_DISCONECTED.print(clientIP);
        }
    }

    private String listenForFilename(int filenameSize) { //TODO long not int
        String filename = null;

        try {
            filename = IOUtils.toString(transferManager.read(filenameSize).getData(1), "UTF-8");
        } catch (IOException e) {
            Error.CLIENT_DISCONECTED.print(clientIP);
            closeConnection();
        }

        return filename;
    }

    private void writeToDisk(String filename) {
        if (filename != null) {
            clientPrint("Is sending file: " + filename);

            try {
                File newFile = new File(filename);
                FileOutputStream fileOutputStream = new FileOutputStream(newFile);

                try {
                    int sizeOfFile = singleByteIn(); //TODO change to long

                    transferManager.writeControl(Command.OK);

                    if (sizeOfFile > 0) { //if size is > 0 copy the file to server
                        byte[] buffer = transferManager.read(sizeOfFile).getData(1);

                        fileOutputStream.write(buffer, 0, buffer.length);

                        transferManager.writeControl(Command.DONE);
                    } //if file size is 0 then create an empty file

                    clientPrint(filename + " received!");
                } catch (IOException e) {
                    newFile.delete();
                    Error.CANNOT_SAVE_FILE.print(clientIP);
                }

            } catch (FileNotFoundException e) {
                Error.FILE_NOT_FOUND.print(clientIP);
            }
        } else {
            Error.EMPTY_FILENAME.print(clientIP);
        }
    }

    private void sendToClient(String filename) {
        if (filename != null) {
            clientPrint("Is requesting file: " + filename);
            try {
                if (singleByteIn() == Command.READY.getCode()) {
                    Path path = Paths.get(filename);
                    byte[] buffer = Files.readAllBytes(path);

                    transferManager.writeFileSize(buffer.length);

                    if (singleByteIn() == Command.OK.getCode()) {
                        if (buffer.length > 0) {
                            transferManager.write(new FileData(buffer));

                            if (singleByteIn() == Command.DONE.getCode()) {
                                clientPrint("File \'" + filename + "\' sent successfully!");
                            }
                        } else {
                            clientPrint("File \'" + filename + "\' sent successfully!");
                        }
                    }
                } else {
                    Error.FILE_NOT_SENT.print(clientIP);
                }
            } catch (IOException e) {
                Error.FILE_NOT_SENT.print(clientIP);
            }
        } else {
            Error.EMPTY_FILENAME.print(clientIP);
        }
    }

    private void clientPrint(String out) {
        System.out.println("\nClient: " + clientIP);
        System.out.println(out);
    }

    private int singleByteIn() throws IOException {
        return transferManager.read(0).getData(1)[0];
    }
}
