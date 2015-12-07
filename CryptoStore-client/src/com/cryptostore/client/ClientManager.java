package com.cryptostore.client;

import org.apache.commons.io.IOUtils;

import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.nio.ByteBuffer;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;

public class ClientManager {
    private SSLSocket clientSocket;
    private TransferManager transferManager;
    private FilenameManager filenameManager;
    private SyncManager syncManager;
    private String host;
    private int hostPort;
    private String username;
    private String userPassword;
    private boolean isAUTHed;

    public ClientManager(String username, String password, String host, int hostPort) {
        setCertificates();
        this.username = username;
        this.userPassword = password;
        this.host = host;
        this.hostPort = hostPort;
        isAUTHed = false;

        filenameManager = new FilenameManager(username);
        syncManager = new SyncManager(username);
    }

    private void setCertificates() {
        System.out.println("\nSetting up certificates. . .");

//        File file = new File("mySrvKeystore");
        File file = new File("/Volumes/SECUREV/Projects/cryptostore/CryptoStore-client/mySrvKeystore");//TODO revert to relative path
        Path path = Paths.get(file.toURI());
        System.setProperty("javax.net.ssl.trustStore", path.toString());
    }

    private void reportStatus(SSLSocket socket) {
        SSLSession session = socket.getSession();
        System.out.println("\nSession details: ");
        System.out.println("Peer Host: " + session.getPeerHost());
        System.out.println("Peer Port: " + session.getPeerPort());
        System.out.println("Cipher Suite: " + session.getCipherSuite());
        System.out.println("Protocol: " + session.getProtocol());
    }

    public void connect(String password) {
        System.out.println("\nConnecting with server. . .");

        try {
            clientSocket = establishConnection(host, hostPort, 5600); //TODO change the local port to whatever is going to be my in/out port
            reportStatus(clientSocket);

            transferManager = new TransferManager(clientSocket);

            authenticate();
            System.out.println("\nConnected!");

            getEncryptionMapping(password);

            syncWithServer(password);

        } catch (Exception e) {
            handleError(Error.CANNOT_CONNECT, e);
        }
    }

    public boolean sendHeartBeat() {
        try {
            transferManager.writeControl(Command.HEARTBEAT);

            return (getCommand() == Command.HEARTBEAT.getCode());
        } catch (Exception e) {
            return false;
        }
    }

    private SSLSocket establishConnection(String host, int hostPort, int localPort) throws IOException {
        SSLSocketFactory sslsocketfactory = (SSLSocketFactory) SSLSocketFactory.getDefault();

        String localAddress = getLocalIP(); //TODO this should be the NATs to access the client remotely. Not the lan IP
        SSLSocket socket = (SSLSocket) sslsocketfactory.createSocket(host, hostPort, InetAddress.getByName(localAddress), localPort);
        socket.setSoLinger(true, 0);

        socket.startHandshake();

        return socket;
    }

    private void authenticate() {
        System.out.println("\nAuthenticating. . .");
        try {
            if (getCommand() == Command.AUTH.getCode()) {
                transferManager.writeFileSize(username.length());

                okOrException();
                transferManager.writeFileName(username);

                okOrException();
                transferManager.writeFileSize(userPassword.length());

                okOrException();
                transferManager.writeFileName(userPassword);

                okOrException();
                isAUTHed = true;
            } else {
                throw new IOException(Error.UNKNOWN_COMMAND.getDescription());
            }
        } catch (Exception e) {
            handleError(Error.CANNOT_AUTH, e);
        }
    }

    private String getLocalIP() {
        return "127.0.0.1"; //TODO change that with a method that returns the NAT IP address
    }

    public void closeConnection() {
        try {
            isAUTHed = false;
            transferManager.writeControl(Command.CLOSE);
            transferManager.closeStreams();
            clientSocket.close();

            System.out.println("\nThe connection was shut down!");
        } catch (Exception e) {
            handleError(Error.SERER_DISCONNECTED, e);
        }
    }

    private void getEncryptionMapping(String password) {
        System.out.println("\nUpdating filename encryption-mapping. . .");

        try {
            filenameManager.createMapIfNotExists();
        } catch (Exception e) {
            Error.CANNOT_SAVE_FILE.print();
        }

        try {
            download(password, filenameManager.MAP_PATH);
        } catch (Exception e) {
            upload(password, filenameManager.MAP_PATH); //TODO if downloading fails not because the file does not exist this will delete all the existing mappings
            handleError(Error.CANNOT_RECEIVE_FILE, e);
        }
    }

    private void syncWithServer(String password) {
        System.out.println("\nSynchronising with server. . .");
        try {
            transferManager.writeControl(Command.SYNC);

            SyncFile syncFile = syncManager.getSyncFile();

            ArrayList<String> serverFileList = new ArrayList<>();

            int numberOfFiles = getSize();
            transferManager.writeControl(Command.OK);

            ArrayList<String> tempList = new ArrayList<>();
            for (int i = 0; i < numberOfFiles; i++) {
                String filename = listenForString();
                String originalPath = filenameManager.getOriginalPath(filename);

                serverFileList.add(originalPath);

                if ((new File(originalPath)).exists()) { //If the file is new there is no need to ask for the hash
                    transferManager.writeControl(Command.OK);

                    String fileHash = listenForString();
                    transferManager.writeControl(Command.OK);

                    if (!fileHash.equals(syncFile.getHashOfFile(filename))) {
                        tempList.add(originalPath);
                    }
                } else {
                    transferManager.writeControl(Command.SKIP);
                    tempList.add(originalPath);
                }
            }

            for (String s : tempList) {
                download(password, s);
            }

            ArrayList<Path> localFiles = new ArrayList<>();
            getAllUserFiles((new File("./"+username+"/")).toPath(), localFiles);

            for (Path p : localFiles) {
                if (serverFileList.contains(p.toString())) {
                    serverFileList.remove(p);
                }
            }
            System.out.println("Synchronisation Completed!");

        } catch (Exception e) {
            handleError(Error.CANNOT_SYNC, e);
        }
    }

    public ArrayList<Path> getAllUserFiles(Path path, ArrayList<Path> pathsInDir) throws IOException {
        DirectoryStream<Path> newDirectoryStream = Files.newDirectoryStream(path);

        for(Path filePath : newDirectoryStream) {
            if(Files.isDirectory(filePath)) {
                getAllUserFiles(filePath, pathsInDir);
            } else {
                pathsInDir.add(filePath);
            }
        }

        return pathsInDir;
    }

    public void uploadFileAndMap(String password, String filename) {
        if (upload(password, filename)) {
            upload(password, filenameManager.MAP_PATH);
        }
    }

    private boolean upload(String password, String filename) {
        System.out.println("\nSending \'" + filename + "\' to server . . .");
        try {
            Path path = Paths.get(filename);
            if (path.toFile().exists()) {
                //SEND FILE
                byte[] encryptedFileBytes = EncryptionManager.encryptFile(password.toCharArray(), path);

                String encryptedFilename = filenameManager.randomisePath(filename);
                sendFile(encryptedFilename, encryptedFileBytes);

                //UPDATE MAP
                // if mapping already exists don't create another entry
                if (filenameManager.getOriginalPath(encryptedFilename) == null) {
                    if (!filenameManager.addToMap(filename, encryptedFilename)) {
                        System.out.println("Storing the mapping of the file failed!");
                        throw new Exception(Error.CANNOT_SAVE_FILE.getDescription()+" : MAP FILE");
                    }
                }

                //UPDATE SYNC FILE
                if(!syncManager.updateEntry(encryptedFilename, encryptedFileBytes, true)) {
                    System.out.println("Updating the sync file failed!");
                    throw new Exception(Error.CANNOT_SAVE_FILE.getDescription()+" : SYNC FILE");
                }

            } else {
                throw new Exception(Error.FILE_NOT_FOUND.getDescription());
            }
        } catch (Exception e) {
            handleError(Error.FILE_NOT_SENT, e);
            return false;
        }
        return true;
    }

    private void sendFile(String filename, byte[] buffer) throws Exception {
        if (isAUTHed) {
            try {
                transferManager.writeControl(Command.FILE_FROM_CLIENT);

                okOrException();
                transferManager.writeFileSize(filename.length());

                okOrException();
                transferManager.writeFileName(filename);

                okOrException();
                transferManager.writeFileSize(buffer.length);

                okOrException();
                if (buffer.length != 0) {
                    transferManager.writeFile(new FileData(buffer));

                    okOrException();
                    System.out.println("\nFile \'" + filename + "\' sent successfully!");

                } else {
                    System.out.println("\nFile \'" + filename + "\' sent successfully!");
                }
            } catch (Exception e) {
                throw new Exception(e.getMessage());
            }
        } else {
            throw new Exception(Error.CANNOT_AUTH.getDescription());
        }
    }

    public void download(String password, String filename) {
        try {
            System.out.println("\nDownloading " + filename + " from server. . .");

            String encryptedFilename = filenameManager.getEncryptedPath(filename);
            if (encryptedFilename == null) {
                throw new Exception(Error.FILE_NOT_FOUND.getDescription());
            } else {
                getFile(filename, encryptedFilename);

                //UPDATE SYNC FILE
                if(!syncManager.updateEntry(encryptedFilename, Files.readAllBytes(Paths.get(filename)), true)) {
                    System.out.println("Updating the sync file failed!");
                }

                //decrypt after update SYNC file. File needs to be same as in server in order to produce same hash
                byte[] decryptedFile = EncryptionManager.decryptFile(password.toCharArray(), Paths.get(filename)); //TODO create method. It is used more than once!
                FileOutputStream fos = new FileOutputStream(filename);
                fos.write(decryptedFile); /** WARNING: will overwrite existing file with same name **/
                fos.close();
            }
        } catch (Exception e) {
            handleError(Error.CANNOT_RECEIVE_FILE, e);
        }
    }

    private void getFile(String filename, String encryptedFilename) throws Exception {
        if (isAUTHed) {
            try {
                transferManager.writeControl(Command.FILE_FROM_SERVER);

                okOrException();
                transferManager.writeFileSize(encryptedFilename.length());

                okOrException();
                transferManager.writeFileName(encryptedFilename);

                okOrException();
                transferManager.writeControl(Command.OK);

                File file = new File(filename);
                file.getParentFile().mkdirs();
                FileOutputStream fos = new FileOutputStream(new File(filename));
                int sizeOfFile = getSize();

                if (sizeOfFile < 0) {
                    throw new IOException(Error.NEGATIVE_SIZE.getDescription());
                }

                transferManager.writeControl(Command.OK);

                if (sizeOfFile > 0) {
                    byte[] buffer = transferManager.read(sizeOfFile).getData(1);
                    fos.write(buffer, 0, buffer.length);

                    transferManager.writeControl(Command.OK);
                }
                fos.close();

                System.out.println(filename + " received!");

            } catch (IOException e) {
                throw new Exception(e.getMessage());
            }
        } else {
            throw new Exception(Error.CANNOT_AUTH.getDescription());
        }
    }

    public void deleteFile(String password, String filename) {
        System.out.println("\nDeleting " + filename + ". . .");
        try{
            if (!filenameManager.containsOriginal(filename)) {
                throw new Exception(Error.FILE_NOT_FOUND.getDescription());
            }

            if (deleteFromServer(filename)) {
                if (deleteLocalFile(filename)) {
                    System.out.println("\n" + filename + " has been deleted!");

                    upload(password, filenameManager.MAP_PATH);

                } else {
                    throw new Exception(Error.LOCAL_DELETE_FAIL.getDescription());
                }
            } else {
                throw new Exception(Error.SERVER_DELETE_FAIL.getDescription());
            }
        } catch (Exception e) {
            handleError(Error.DELETE_FAIL, e);
        }
    }

    private boolean deleteFromServer(String filename) throws Exception {
        System.out.println("\nDeleting " + filename + " from the server. . .");

        String encryptedFilename = filenameManager.getEncryptedPath(filename);

        if (isAUTHed) {
            try {
                transferManager.writeControl(Command.DELETE);

                okOrException();
                transferManager.writeFileSize(encryptedFilename.length());


                okOrException();
                transferManager.writeFileName(encryptedFilename);

                okOrException();

                System.out.println(filename + " deleted from server!");

                return true;
            } catch (IOException e) {
                throw new Exception(e.getMessage());
            }
        } else {
            throw new Exception(Error.CANNOT_AUTH.getDescription());
        }
    }

    private boolean deleteLocalFile (String filename) {
        System.out.println("\nDeleting " + filename + " from local machine. . .");

        if (new File(filename).delete()) {
            System.out.println("\nRemoving file from filename encryption map. . .");

            if (filenameManager.removeFromMap(filename)) {
                System.out.println("\nMapping removed!");

                if (syncManager.updateEntry(filenameManager.getEncryptedPath(filename), null, false)) {
                    System.out.println("\nSync file updated!");
                } else {//TODO should find a way to abort instead if ignoring the failure
                    System.out.println("\nFailed to remove entry fro sync file!");
                }
            } else {//TODO should find a way to abort instead if ignoring the failure
                System.out.println("\nFailed to remove mapping!");
            }
            return true;
        } else {
            return false;
        }
    }

    private int getCommand() throws Exception {
        try {
            return transferManager.read(0).getData(1)[0];
        } catch (IOException e) {
            throw new Exception(Error.FAILED_TO_READ.getDescription());
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

    private void okOrException() throws Exception {
        if (getCommand() == Command.OK.getCode()) {
            return;
        } else {
            throw new Exception(Error.COMMUNICATION_FAILED.getDescription());
        }
    }

    private String listenForString() throws Exception {
        int filenameSize = getSize();
        greaterThanZero(filenameSize);
        transferManager.writeControl(Command.OK);

        String filename = listenForString(filenameSize);
        greaterThanZero(filename.length());

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

    private void handleError(Error err1, Exception err2) {
        try {
            err1.print();

            if (err2.getMessage() != null)
                System.out.println(err2.getMessage()+'\n');

        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
        try {
            if (transferManager != null)
                transferManager.flush();
//                transferManager.writeControl(Command.ERROR);
        } catch (Exception e) {
            System.out.println(e.getMessage()+'\n');
        }
    }

    private void greaterThanZero(int num) throws Exception {
        if (num > 0) {
            return;
        } else {
            throw new Exception(Error.ZERO_SIZE.getDescription());
        }
    }
}

