package com.cryptostore.client;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;

import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import java.io.*;
import java.nio.ByteBuffer;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Base64;

public class ClientManager {
    public static String KEYSTORE_PATH = "mySrvKeystore"; //public for testing purposes
    public static String IMAGE_PATH = "res"+File.separator+"kite.png"; //use a default
    public static String HOST = "localhost"; //public for testing suite
    private SSLSocket clientSocket;
    private TransferManager transferManager;
    private FilenameManager filenameManager;
    private SyncManager syncManager;
    private int hostPort;
    private String username;
    private String userPassword;
    private String encPassword;
    private boolean isAUTHed;
    private boolean stegoEnabled = false;

    public ClientManager(String username, String password, int hostPort, String encPassword) {
        setCertificates();
        this.username = username;
        this.userPassword = password;
        this.hostPort = hostPort;
        this.encPassword = encPassword;
        isAUTHed = false;

        filenameManager = new FilenameManager(username);
        syncManager = new SyncManager(username);
    }

    private void setCertificates() {
        System.out.println("\nSetting up certificates. . .");

        File file = new File(KEYSTORE_PATH);
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

    public boolean connect(String password) {
        System.out.println("\nConnecting with server. . .");

        try { //TODO add timeout if not responsive server
            clientSocket = establishConnection(HOST, hostPort);
            reportStatus(clientSocket);

            transferManager = new TransferManager(clientSocket);

            authenticate();

            if (isAUTHed) {
                System.out.println("\nConnected!");

                getEncryptionMapping(password);

                syncWithServer(password);
            } else {
                throw new Exception(Error.CANNOT_AUTH.getDescription());
            }
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public void registerNewUser(String username, String pass, String encPass, boolean isAdmin) throws Exception {
        System.out.println("\nRegistering a new user. . .");
        if (isAUTHed && sendHeartBeat()) {
            transferManager.writeControl(Command.NEW_USER);
            int encSaltSize = getSize();
            transferManager.writeControl(Command.OK);

            String encSalt = listenForString(encSaltSize);
            transferManager.writeControl(Command.OK);

            transferManager.writeFileSize(username.length());
            okOrException();

            transferManager.writeFileName(username);
            int response = getCommand();
            if (response == Command.ERROR.getCode()) {
                Error.USER_EXISTS.print();
                throw new Exception(Error.USER_EXISTS.getDescription());
            } else if (response != Command.OK.getCode()) {
                throw new Exception(Error.UNKNOWN_COMMAND.getDescription());
            }

            transferManager.writeFileSize(pass.length());
            okOrException();

            transferManager.writeFileName(pass);
            okOrException();

            encPass = HashGenerator.getPBKDF2(encPass, Base64.getDecoder().decode(encSalt));
            transferManager.writeFileSize(encPass.length());
            okOrException();

            transferManager.writeFileName(encPass);
            okOrException();

            if (isAdmin) {
                transferManager.writeFileSize(1);
            } else {
                transferManager.writeFileSize(0);
            }
            okOrException();

            System.out.println("User '" + username + "' has been registered!");
        } else {
            throw new Exception(Error.CANNOT_AUTH.getDescription());
        }
    }

    public boolean sendHeartBeat() { //public so it can be used by test suite
        try {
            transferManager.writeControl(Command.HEARTBEAT);

            return (getCommand() == Command.HEARTBEAT.getCode());
        } catch (Exception e) {
            return false;
        }
    }

    private SSLSocket establishConnection(String host, int hostPort) throws IOException {
        SSLSocketFactory sslsocketfactory = (SSLSocketFactory) SSLSocketFactory.getDefault();

        SSLSocket socket = (SSLSocket) sslsocketfactory.createSocket(host, hostPort);
        socket.setSoLinger(true, 0);

        socket.startHandshake();

        return socket;
    }

    private void authenticate() throws Exception {
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
                String salt = listenForString();
                transferManager.writeControl(Command.OK);

                String hashedEncPass = HashGenerator.getPBKDF2(encPassword, Base64.getDecoder().decode(salt));
                transferManager.writeFileSize(hashedEncPass.length());

                okOrException();
                transferManager.writeFileName(hashedEncPass);

                okOrException();
                isAUTHed = true;
            } else {
                throw new IOException(Error.UNKNOWN_COMMAND.getDescription());
            }
        } catch (Exception e) {
            throw new Exception(Error.CANNOT_AUTH.getDescription());
        }
    }

    public boolean closeConnection() {
        try {
            isAUTHed = false;
            transferManager.writeControl(Command.CLOSE);
            transferManager.closeStreams();
            clientSocket.close();

            System.out.println("\nThe connection was shut down!");

            return true;
        } catch (Exception e) {
            return false;
        }
    }

    private void getEncryptionMapping(String password) throws Exception {
        System.out.println("\nUpdating filename encryption-mapping. . .");
        filenameManager.createMapIfNotExists();

        if(!download(password, filenameManager.MAP_PATH)) {
            upload(password, filenameManager.MAP_PATH); //TODO if downloading fails not because the file does not exist this will delete all the existing mappings
        }
    }

    private void syncWithServer(String password) throws Exception {
        System.out.println("\nSynchronising with server. . .");
        try {
            transferManager.writeControl(Command.SYNC);

            SyncFile syncFile = syncManager.getSyncFile();

            if (getSize()!=syncManager.getVersion()) {
                transferManager.writeControl(Command.OK);

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
                getAllFilesInDir((new File(username)).toPath(), localFiles);

                for (Path p : localFiles) {
                    if (!arrayContains(p.toString(), serverFileList)) {
                        deleteLocalFile(p.toString());
                    }
                }
            } else {
                transferManager.writeControl(Command.SKIP);
            }

            System.out.println("Updating version. . .");
            transferManager.writeControl(Command.VERSION);
            syncManager.setVersion(getSize());
            transferManager.writeControl(Command.OK);

            System.out.println("Synchronisation Completed!");

        } catch (Exception e) {
            throw new Exception(Error.CANNOT_SYNC.getDescription());
        }
    }

    private boolean arrayContains (String element, ArrayList<String> list) {
        for (String s : list) {
            if (Paths.get(s).getFileName().equals(Paths.get(element).getFileName())) {
                return true;
            }
        }
        return false;
    }

    public ArrayList<Path> getAllFilesInDir(Path path, ArrayList<Path> pathsInDir) throws IOException {
        DirectoryStream<Path> newDirectoryStream = Files.newDirectoryStream(path);

        for(Path filePath : newDirectoryStream) {
            if(Files.isDirectory(filePath)) {
                getAllFilesInDir(filePath, pathsInDir);
            } else if (!filePath.toString().equals(filenameManager.MAP_PATH) && !filePath.toString().equals(syncManager.SYNC_PATH)){
                pathsInDir.add(filePath);
            }
        }

        return pathsInDir;
    }

    public void setStegoEnabled(boolean stegoEnabled) {
        this.stegoEnabled = stegoEnabled;
    }

    public boolean isStegoEnabled(String filename) {
        return filenameManager.isStegOn(filename);
    }

    public boolean copyLocallyAndUpload(String encryptionPassword, File file, String destinationPath) {
        if (isAUTHed && sendHeartBeat()) {
            try {
                if (file.isDirectory()) {
                    destinationPath += File.separator + file.getName();

                    FileUtils.copyDirectory(file, new File(destinationPath));

                    ArrayList<Path> newFiles = new ArrayList<Path>();
                    getAllFilesInDir(Paths.get(destinationPath), newFiles);

                    for (Path p : newFiles) {
                        uploadFileAndMap(encryptionPassword, p.toString());
                        syncManager.setVersion(syncManager.getVersion() + 1);
                    }
                } else {
                    FileUtils.copyFileToDirectory(file, new File(destinationPath));
                    destinationPath += File.separator + file.getName();

                    uploadFileAndMap(encryptionPassword, destinationPath);
                }

            } catch (Exception e) {
                return false;
            }
            return true;
        } else {
            return false;
        }
    }

    public void uploadFileAndMap(String password, String filename) throws Exception { //public so it's usable by test suite
        upload(password, filename);
        upload(password, filenameManager.MAP_PATH);
    }

    private void upload(String password, String filename) throws Exception {
        System.out.println("\nSending \'" + filename + "\' to server . . .");
        Path path = Paths.get(filename);
        if (path.toFile().exists()) {
            //SEND FILE
            String encryptedFilename = filenameManager.randomisePath(filename);

            byte[] encryptedFileBytes = EncryptionManager.encryptFile(password.toCharArray(), path);
            if (stegoEnabled && !encryptedFilename.equals(filenameManager.HEX_MAP_PATH)) {
                if (SteganographyManager.fitsInImage(encryptedFileBytes, IMAGE_PATH)) {
                    encryptedFileBytes = SteganographyManager.hide(IMAGE_PATH, encryptedFileBytes);
                    if (!encryptedFilename.endsWith(".png")) { //if filename does not already have the right format
                        encryptedFilename += ".png";
                    }
                } else {
                    throw new Exception(Error.CANNOT_SAVE_FILE.getDescription()+" : PNG FILE");
                }
            }
            sendFile(encryptedFilename, encryptedFileBytes);

            //UPDATE MAP
            // if mapping already exists don't create another entry
            if (filenameManager.getOriginalPath(encryptedFilename) == null) {
                if (!filenameManager.addToMap(filename, encryptedFilename, stegoEnabled)) {
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
    }

    private void sendFile(String filename, byte[] buffer) throws Exception {
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
    }

    public boolean download(String password, String filename) { //public so it can be used by the test suite
        System.out.println("\nDownloading " + filename + " from server. . .");

        try {
            String encryptedFilename = filenameManager.getEncryptedPath(filename);
            if (encryptedFilename == null) {
                throw new Exception(Error.FILE_NOT_FOUND.getDescription());
            } else {
                getFile(filename, encryptedFilename);

                //UPDATE SYNC FILE
                if (!syncManager.updateEntry(encryptedFilename, Files.readAllBytes(Paths.get(filename)), true)) {
                    System.out.println("Updating the sync file failed!");
                }

                //decrypt after update SYNC file. File needs to be same as in server in order to produce same hash
                byte[] decryptedFile;
                if (!filename.equals(filenameManager.MAP_PATH) && filenameManager.isStegOn(filename)) {
                    decryptedFile = SteganographyManager.retrieve(filename);
                    decryptedFile = EncryptionManager.decryptFile(password.toCharArray(), decryptedFile); //TODO create method. It is used more than once!
                } else {
                    decryptedFile = EncryptionManager.decryptFile(password.toCharArray(), Files.readAllBytes(Paths.get(filename)));
                }
                StorageManager.store(filename, decryptedFile);
            }
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    private void getFile(String filename, String encryptedFilename) throws Exception {
        if (isAUTHed) {
            transferManager.writeControl(Command.FILE_FROM_SERVER);

            okOrException();
            transferManager.writeFileSize(encryptedFilename.length());

            okOrException();
            transferManager.writeFileName(encryptedFilename);

            okOrException();
            transferManager.writeControl(Command.OK);

            int sizeOfFile = getSize();

            if (sizeOfFile < 0) {
                throw new IOException(Error.NEGATIVE_SIZE.getDescription());
            }

            transferManager.writeControl(Command.OK);

            if (sizeOfFile > 0) {
                byte[] buffer = transferManager.read(sizeOfFile).getData(1);
                StorageManager.createDirAndStore(filename, buffer);

                transferManager.writeControl(Command.OK);
            }

            System.out.println(filename + " received!");
        } else {
            throw new Exception(Error.CANNOT_AUTH.getDescription());
        }
    }

    public boolean delete(String password, String filename) {
        if (isAUTHed && sendHeartBeat()) {
            if (new File(filename).isDirectory()) {
                try {
                    Files.walk(Paths.get(filename)).filter(Files::isRegularFile).forEach((path) -> {
                        deleteFile(password, path.toString());
                        syncManager.setVersion(syncManager.getVersion() + 1);
                    });
                } catch (IOException e) {
                    return false;
                }
            } else {
                deleteFile(password, filename);
            }
            return true;
        } else {
            return false;
        }
    }

    private void deleteFile(String password, String filename) {
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
            Error.DELETE_FAIL.print();
        }
        recursivelyDeleteDirIfEmpty(new File(filename).getParentFile());
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

        if (StorageManager.delete(filename)) {
            System.out.println("\nRemoving file from filename encryption map. . .");

            if (syncManager.updateEntry(filenameManager.getEncryptedPath(filename), null, false)) {
                System.out.println("\nSync file updated!");

                if (filenameManager.removeFromMap(filename)) {
                    System.out.println("\nMapping removed!");
                } else {//TODO should find a way to abort instead if ignoring the failure
                    System.out.println("\nFailed to remove mapping!");
                }
            } else {//TODO should find a way to abort instead if ignoring the failure
                System.out.println("\nFailed to remove entry fro sync file!");
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

    private String listenForString(int size) throws Exception {
        String filename = null;

        try {
            filename = IOUtils.toString(transferManager.read(size).getData(1), "UTF-8");
        } catch (Exception e) {
            throw new Exception(Error.COMMUNICATION_FAILED.getDescription());
        }

        return filename;
    }

    private void recursivelyDeleteDirIfEmpty(File parentDir) {
        if (parentDir.isDirectory() && parentDir.list().length == 0 && parentDir.getName()!=username) {
            File gParent = parentDir.getParentFile();
            StorageManager.delete(parentDir.getPath());
            recursivelyDeleteDirIfEmpty(gParent);
        }
    }

    public String getMAP_PATH() {
        return filenameManager.MAP_PATH;
    }

    public String getSYNC_PATH() {
        return syncManager.SYNC_PATH;
    }

    private void greaterThanZero(int num) throws Exception {
        if (num > 0) {
            return;
        } else {
            throw new Exception(Error.ZERO_SIZE.getDescription());
        }
    }
}