package com.cryptostore.server;

import org.apache.commons.io.IOUtils;

import javax.net.ssl.SSLSocket;
import java.io.*;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Base64;

public class ClientThread extends Thread {
    private final String HEX_MAP_PATH = "./0000000000.png"; //Must be same as client.FilenameManager
    private SSLSocket clientSocket;
    private String username;
    private TransferManager transferManager;
    private SyncManager syncManager;
    private String clientIP;
    private String connectedUser;
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

            int usernameSize = getSize(); //ERROR
            greaterThanZero(usernameSize);
            transferManager.writeControl(Command.OK);

            username = listenForString(usernameSize);
            greaterThanZero(username.length());

            if (!Validator.validateUsername(username)) {
                throw new Exception(Error.INCORRECT_FORM.getDescription(clientIP));
            }
            if (!JDBCControl.usernameExists(username)) {
                throw new Exception(Error.NO_USER.getDescription(clientIP));
            }
            if (ServerManager.onlineUsers.contains(username)) {
                throw new Exception(Error.USER_LOGGED.getDescription(clientIP));
            }

            transferManager.writeControl(Command.OK);

            int passwordSize = getSize();
            greaterThanZero(passwordSize);
            transferManager.writeControl(Command.OK);

            String password = listenForString(passwordSize);
            greaterThanZero(password.length());
            if (!Validator.validatePassword(password)) {
                throw new Exception(Error.INCORRECT_FORM.getDescription(clientIP));
            }
            transferManager.writeControl(Command.OK);

            String encSalt = JDBCControl.getEncPassSalt(username);
            transferManager.writeFileSize(encSalt.length());
            okOrException();

            transferManager.writeFileName(encSalt);
            okOrException();

            int encPassSize = getSize();
            transferManager.writeControl(Command.OK);

            String encPassword = listenForString(encPassSize);
            if (!Validator.validateEncPassword(encPassword)) {
                throw new Exception(Error.INCORRECT_FORM.getDescription(clientIP));
            }
            if (!JDBCControl.checkEncPass(username, encPassword)) {
                throw new Exception(Error.INCORRECT_PASSWORD.getDescription(clientIP));
            }

            clientIsAuthed = JDBCControl.checkUserPassword(username, HashGenerator.getPBKDF2(password, JDBCControl.getSalt(username)));

            if (clientIsAuthed) {
                transferManager.writeControl(Command.OK);
                connectedUser = username;
                ServerManager.onlineUsers.add(username);
            } else {
                throw new Exception(Error.INCORRECT_PASSWORD.getDescription(clientIP));
            }

            //SETUP SYNC file for user
            syncManager = new SyncManager(connectedUser);
            try {
                syncManager.createFileIfNotExists();
            } catch (Exception e) {
                Error.CANNOT_SAVE_FILE.print();
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
                int request = getCommand();

                if (request == Command.FILE_FROM_CLIENT.getCode()) {
                    clientPrint("Is requesting to send a file.");
                    transferManager.writeControl(Command.OK);

                    String filename = listenForFilename();
                    transferManager.writeControl(Command.OK);

                    writeToDisk(filename);

                    if (!filename.equals(HEX_MAP_PATH.substring(2)) && !filename.equals(syncManager.SYNC_PATH.substring(2))) {
                        syncManager.setVersion(syncManager.getVersion()+1);
                    }
                } else if (request == Command.FILE_FROM_SERVER.getCode()) {
                    clientPrint("Is requesting to retrieve a file.");
                    transferManager.writeControl(Command.OK);

                    String filename = listenForFilename();

                    File requestFile = new File("UserFiles/"+connectedUser+'/'+filename);

                    if (requestFile.exists()) {
                        transferManager.writeControl(Command.OK);
                        sendToClient(filename);
                    } else {
                        throw new FileNotFoundException(Error.FILE_NOT_FOUND.getDescription(clientIP));
                    }

                } else if (request == Command.DELETE.getCode()) {
                    clientPrint("Is requesting to delete a file");
                    transferManager.writeControl(Command.OK);

                    String filename = listenForFilename();

                    File requestFile = new File("UserFiles/"+connectedUser+'/'+filename);

                    if (requestFile.exists()) {
                        transferManager.writeControl(Command.OK);
                        deleteFile(filename);

                        syncManager.updateEntry(filename, null, false);

                        if (!filename.equals(HEX_MAP_PATH.substring(2)) && !filename.equals(syncManager.SYNC_PATH.substring(2))) {
                            syncManager.setVersion(syncManager.getVersion()+1);
                        }
                    } else {
                        throw new FileNotFoundException(Error.FILE_NOT_FOUND.getDescription(clientIP));
                    }

                } else if (request == Command.SYNC.getCode()) {
                    clientPrint("Is requesting to SYNC his files");

                    transferManager.writeFileSize(syncManager.getVersion());
                    if (getCommand() == Command.OK.getCode()) {

                        SyncFile syncFile = syncManager.getSyncFile();

                        transferManager.writeFileSize(syncFile.getFiles().size()); //send the number of files on the server
                        okOrException();

                        for (String s : syncFile.getFiles()) {
                            transferManager.writeFileSize(s.length());
                            okOrException();

                            transferManager.writeFileName(s);
                            if (getCommand() == Command.OK.getCode()) {
                                transferManager.writeFileSize(syncFile.getHashOfFile(s).length());
                                okOrException();

                                transferManager.writeFileName(syncFile.getHashOfFile(s));
                                okOrException();
                            } //If the response is any other i.e. SKIP the hash of that file will be ignored
                        }
                    } //Else response will be SKIP so do nothing

                    System.out.println("SYNC completed!");
                } else if (request == Command.NEW_USER.getCode()) {
                    clientPrint("Is requesting to create a new user");
                    if (JDBCControl.isAdmin(username)) {
                        String salt = Base64.getEncoder().encodeToString(HashGenerator.getSalt());
                        transferManager.writeFileSize(salt.length());
                        okOrException();

                        transferManager.writeFileName(salt);
                        okOrException();

                        registerNewUser(salt);
                    } else {
                        throw new Exception(Error.CANNOT_AUTH.getDescription(clientIP));
                    }
                } else if (request == Command.HEARTBEAT.getCode()) {
                    clientPrint("Sent a heartbeat message");
                    transferManager.writeControl(Command.HEARTBEAT);
                } else if (request == Command.CLOSE.getCode()) {
                    clientPrint("Terminates the connection!");
                    closeConnection();
                } else if (request == Command.VERSION.getCode()) {
                    transferManager.writeFileSize(syncManager.getSyncFile().getVersion());
                    okOrException();
                } else {
                    throw new IOException(Error.UNKNOWN_COMMAND.getDescription(clientIP));
                }
            } catch (Exception e) {
                handleError(Error.FILE_NOT_SENT, e);
            }
        }
    }

    private void closeConnection() {
        clientIsConnected = false;
        clientIsAuthed = false;
        ServerManager.onlineUsers.remove(username);

        try {
            transferManager.closeStreams();
            clientSocket.close();
            System.out.println("Connection closed!");
        } catch (Exception e) {
            Error.CLIENT_DISCONNECTED.print(clientIP);
        }
    }

    private String listenForFilename() throws Exception {
        int filenameSize = getSize();
        greaterThanZero(filenameSize);
        transferManager.writeControl(Command.OK);
        String filename = listenForString(filenameSize);
        greaterThanZero(filename.length());
        if (!Validator.validateFilename(filename)) {
            throw new Exception(Error.INCORRECT_FORM.getDescription(clientIP));
        }
        if (!new File(filename).getCanonicalPath().startsWith(System.getProperty("user.dir"))) {
            throw new Exception(Error.INCORRECT_FORM.getDescription(clientIP));
        }

        return filename;
    }

    private String listenForString(int size) {
        String filename = null;

        try {
            filename = IOUtils.toString(transferManager.read(size).getData(1), "UTF-8");
        } catch (Exception e) {
            handleError(Error.COMMUNICATION_FAILED, e);
        }

        return filename;
    }

    private void writeToDisk(String filename) {
        clientPrint("Is sending file: " + filename);
        try {
            try {
                File newFile = new File("UserFiles/"+connectedUser+'/'+filename.substring(1)); //removes leading '.' of the curr dir
                newFile.getParentFile().mkdirs();
                FileOutputStream fileOutputStream = new FileOutputStream(newFile);

                try {
                    int sizeOfFile = getSize();

                    if (sizeOfFile < 0) {
                        throw new Exception(Error.NEGATIVE_SIZE.getDescription(clientIP));
                    }

                    transferManager.writeControl(Command.OK);

                    if (sizeOfFile > 0) { //if size is > 0 copy the file to server
                        byte[] buffer = transferManager.read(sizeOfFile).getData(1);
                        fileOutputStream.write(buffer, 0, buffer.length);
                        fileOutputStream.close();

                        transferManager.writeControl(Command.OK);
                    } //if file size is 0 then create an empty file

                    syncManager.updateEntry(filename, Files.readAllBytes(Paths.get(newFile.toURI())), true);

                    clientPrint(filename + " received!");

                } catch (IOException e) {
                    newFile.delete();
                    throw new Exception(Error.CANNOT_SAVE_FILE.getDescription(clientIP));
                }
            } catch (FileNotFoundException e) {
                throw new Exception(Error.FILE_NOT_FOUND.getDescription(clientIP));
            }
        } catch (Exception e) {
            handleError(Error.CANNOT_SAVE_FILE, e);
        }
    }

    private void sendToClient(String filename) {
        clientPrint("Is requesting file: " + filename);
        try {
            okOrException();
            Path path = Paths.get("UserFiles/"+connectedUser+'/'+filename);
            byte[] buffer = Files.readAllBytes(path);
            transferManager.writeFileSize(buffer.length);

            okOrException();
            if (buffer.length > 0) {
                transferManager.writeFile(new FileData(buffer));

                okOrException();
            }
            clientPrint("File \'" + filename + "\' sent successfully!");

        } catch (Exception e) {
            handleError(Error.FILE_NOT_SENT, e);
        }
    }

    private void deleteFile (String filename) {
        clientPrint("Is deleting file: " + filename);

        if (new File("UserFiles/"+connectedUser+'/'+filename).delete()) {
            clientPrint("File \'" + filename + "\' deleted successfully!");
        } else {
            Error.DELETE_FAIL.print(clientIP);
        }
    }

    private void registerNewUser(String encSalt) {
        clientPrint("Is registering a user. . .");
        try {
            int usernameSize = getSize();
            greaterThanZero(usernameSize);
            transferManager.writeControl(Command.OK);

            String username = listenForString(usernameSize);
            greaterThanZero(username.length());
            if (!Validator.validateUsername(username)) {
                transferManager.writeControl(Command.ERROR);
                throw new Exception(Error.INCORRECT_FORM.getDescription(clientIP));
            }
            if (JDBCControl.usernameExists(username)) {
                transferManager.writeControl(Command.ERROR);
                throw new Exception(Error.USER_EXISTS.getDescription(clientIP));
            }
            transferManager.writeControl(Command.OK);

            int passSize = getSize();
            greaterThanZero(passSize);
            transferManager.writeControl(Command.OK);

            String pass = listenForString(passSize);
            greaterThanZero(pass.length());
            if (!Validator.validatePassword(pass)) {
                transferManager.writeControl(Command.ERROR);
                throw new Exception(Error.INCORRECT_FORM.getDescription(clientIP));
            }
            transferManager.writeControl(Command.OK);

            int encPassSize = getSize();
            greaterThanZero(encPassSize);
            transferManager.writeControl(Command.OK);

            String encPass = listenForString(encPassSize);
            greaterThanZero(encPass.length());
            if (!Validator.validateEncPassword(encPass)) {
                transferManager.writeControl(Command.ERROR);
                throw new Exception(Error.INCORRECT_FORM.getDescription(clientIP));
            }
            transferManager.writeControl(Command.OK);

            String isAdmin = "0";
            if (getSize()==1) {
                isAdmin = "1";
            }

            byte[] salt = HashGenerator.getSalt();
            pass = HashGenerator.getPBKDF2(pass, salt);
            if (JDBCControl.createNewUser(username, pass, salt, encPass, encSalt, isAdmin)) {
                transferManager.writeControl(Command.OK);
                clientPrint("User successfully registered!");
            } else {
                transferManager.writeControl(Command.ERROR);
                throw new Exception(Error.DB_ERROR.getDescription());
            }

        } catch (Exception e) {
            Error.COMMUNICATION_FAILED.print(clientIP);
        }
    }

    private void clientPrint(String out) {
        System.out.println("\nClient: " + clientIP);
        System.out.println(out);
    }

    private int getCommand() throws Exception {
        try {
            return transferManager.read(0).getData(1)[0];
        } catch (IOException e) {
            throw new Exception(Error.FAILED_TO_READ.getDescription(clientIP));
        }
    }

    private int getSize() throws Exception {
        try {
            byte[] bytes = transferManager.read(0).getData(1);
            ByteBuffer wrapped = ByteBuffer.wrap(bytes);
            return wrapped.getInt();
        } catch (Exception e) {
            throw new Exception(Error.FAILED_TO_READ.getDescription());
        }
    }

    private void greaterThanZero(int num) throws Exception {
        if (num > 0) {
            return;
        } else {
            throw new Exception(Error.ZERO_SIZE.getDescription(clientIP));
        }
    }

    private void okOrException() throws Exception {
        if (getCommand() == Command.OK.getCode()) {
            return;
        } else {
            throw new Exception(Error.COMMUNICATION_FAILED.getDescription(clientIP));
        }
    }

    private void handleError(Error err1, Exception err2) {
        try {
            transferManager.writeControl(Command.ERROR);
            err1.print(clientIP);

            if (err2.getMessage() != null)
                System.out.println(err2.getMessage()+'\n');

            transferManager.flush();
        } catch (Exception e) {
            System.out.println(e.getMessage());
            closeConnection();
        }
    }
}
