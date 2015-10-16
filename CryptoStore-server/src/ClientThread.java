import org.apache.commons.io.IOUtils;

import javax.net.ssl.SSLSocket;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

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
        try {
            super.run();
            transferManager = new TransferManager(clientSocket);

            /** Auth the user **/
            transferManager.writeControl(Command.AUTH);

            int usernameSize = singleByteIn(); //TODO long not int

            greaterThanZero(usernameSize);
            transferManager.writeControl(Command.OK);

            String username = listenForFilename(usernameSize);
            greaterThanZero(username.length());
            transferManager.writeControl(Command.OK);

            int passwordSize = singleByteIn(); //TODO long not int
            greaterThanZero(passwordSize);
            transferManager.writeControl(Command.OK);

            String password = listenForFilename(passwordSize);
            greaterThanZero(password.length());
            clientIsAuthed = JDBCControl.checkUserPassword(username, HashGenerator.getHash(password, JDBCControl.getSalt(username), 100000, 32));

            if (clientIsAuthed) {
                transferManager.writeControl(Command.OK);
            } else {
                throw new Exception();
            }

            waitForClientRequest();

        } catch (Exception e) {
            handleError(Error.CANNOT_AUTH, e);
        }
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
                    greaterThanZero(filenameSize);
                    transferManager.writeControl(Command.OK);

                    String filename = listenForFilename(filenameSize);
                    greaterThanZero(filename.length());
                    transferManager.writeControl(Command.OK);
                    writeToDisk(filename);

                } else if (request == Command.file_from_server.getCode()) {
                    clientPrint("Is trying to retrieve a file.");
                    transferManager.writeControl(Command.OK);

                    int filenameSize = singleByteIn(); //TODO long not int
                    transferManager.writeControl(Command.OK);

                    String filename = listenForFilename(filenameSize);
                    greaterThanZero(filename.length());

                    if (new File(filename).exists()) {
                        transferManager.writeControl(Command.OK);
                        sendToClient(filename);
                    } else {
                        throw new FileNotFoundException(Error.FILE_NOT_FOUND.getDescription());
                    }

                } else if (request == Command.CLOSE.getCode()) {
                    clientPrint("Terminates the connection!");
                    closeConnection();

                } else {
                    throw new IOException(Error.UNKNOWN_COMMAND.getDescription());
                }
            } catch (Exception e) {
                handleError(Error.FILE_NOT_SENT, e);
            }
        }
    }

    private void closeConnection() {
        clientIsConnected = false;
        clientIsAuthed = false;

        try {
            try {
                transferManager.closeStreams();
            } catch (Exception e) {
                System.out.println(e.getMessage());
                throw new IOException();
            }
            clientSocket.close();
            System.out.println("Connection closed!");
        } catch (IOException e) {
            Error.CLIENT_DISCONNECTED.print(clientIP);
        }
    }

    private String listenForFilename(int filenameSize) { //TODO long not int
        String filename = null;

        try {
            filename = IOUtils.toString(transferManager.read(filenameSize).getData(1), "UTF-8");
        } catch (Exception e) {
            handleError(Error.COMMUNICATION_FAILED, e);
        }

        return filename;
    }

    private void writeToDisk(String filename) {
        try {
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

                        transferManager.writeControl(Command.OK);
                    } //if file size is 0 then create an empty file

                    clientPrint(filename + " received!");
                } catch (IOException e) {
                    newFile.delete();
                    throw new Exception(Error.CANNOT_SAVE_FILE.getDescription());
                }
            } catch (FileNotFoundException e) {
                throw new Exception(Error.FILE_NOT_FOUND.getDescription(clientIP));
            }
        } catch (Exception e) {
            handleError(Error.CANNOT_SAVE_FILE, e);
        }
    }

    private void sendToClient(String filename) {
        try {
            clientPrint("Is requesting file: " + filename);

            okOrException();
            Path path = Paths.get(filename);
            byte[] buffer = Files.readAllBytes(path);
            transferManager.writeFileSize(buffer.length);

            okOrException();
            if (buffer.length > 0) {
                transferManager.write(new FileData(buffer));

                okOrException();
            }
            clientPrint("File \'" + filename + "\' sent successfully!");

        } catch (Exception e) {
            handleError(Error.FILE_NOT_SENT, e);
        }
    }

    private void clientPrint(String out) {
        System.out.println("\nClient: " + clientIP);
        System.out.println(out);
    }

    private int singleByteIn() throws Exception {
        try {
            return transferManager.read(0).getData(1)[0];
        } catch (IOException e) {
            throw new Exception(Error.FAILED_TO_READ.getDescription());
        }
    }

    private void greaterThanZero(int num) throws Exception {
        if (num > 0) {
            return;
        } else {
            throw new Exception(Error.ZERO_SIZE.getDescription());
        }
    }

    private void okOrException() throws Exception {
        if (singleByteIn() == Command.OK.getCode()) {
            return;
        } else {
            throw new Exception(Error.COMMUNICATION_FAILED.getDescription());
        }
    }

    private void handleError(Error err1, Exception err2) {
        try {
            transferManager.writeControl(Command.ERROR);
            err1.print(clientIP);
            System.out.println(err2.getMessage());
            closeConnection();
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }
}
