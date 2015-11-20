import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.nio.ByteBuffer;
import java.nio.file.Path;
import java.nio.file.Paths;

public class ClientManager {
    private final String MAP = "./ENCRYPTION_MAPPING";
    private SSLSocket clientSocket;
    private TransferManager transferManager;
    private FilenameManager filenameManager;
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
    }

    private void setCertificates() {
        System.out.println("\nSetting up certificates. . .");

        File file = new File("mySrvKeystore");
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

    private void connect() {
        System.out.println("\nConnecting with server. . .");

        try {
            clientSocket = establishConnection(host, hostPort, 5600); //TODO change the local port to whatever is going to be my in/out port

            reportStatus(clientSocket);

            transferManager = new TransferManager(clientSocket);

            authenticate();

        } catch (Exception e) {
            handleError(Error.CANNOT_CONNECT, e);
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

    private void closeConnection() {
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

    public void getEncryptionMapping(String password) {
        System.out.println("\nUpdating filename encryption-mapping. . .");

        getFile(password, filenameManager.MAP_PATH);

        File mapFile = new File(filenameManager.MAP_PATH); //TODO move this block to the FilenameManager! Might use again
        if (!mapFile.exists()) {
            try {
                mapFile.getParentFile().mkdirs();
                mapFile.createNewFile();
                FileOutputStream fileOutputStream = new FileOutputStream(mapFile);
                ObjectOutputStream objectOutputStream = new ObjectOutputStream(fileOutputStream);
                objectOutputStream.writeObject(new FileMap());
                objectOutputStream.close();

                System.out.println("New encryption-mapping created!");
            } catch (Exception e) {
                handleError(Error.CANNOT_SAVE_FILE, e);
            }
        }
    }

    public void sendFile(String password, String filename) {
        getEncryptionMapping(password);

        System.out.println("\nSending \'" + filename + "\' to server . . .");
        try {
            Path path = Paths.get(filename);
            if (path.toFile().exists()) {
                byte[] buffer = EncryptionManager.encryptFile(password.toCharArray(), path);

                String encryptedFilename = filenameManager.randomisePath(filename);
                connect();
                deliverFile(encryptedFilename, buffer);
                if(!filenameManager.addToMap(filename, encryptedFilename)) {
                    System.out.println("Storing the mapping of the file failed!");
                }
                byte[] mapBuffer = EncryptionManager.encryptFile(password.toCharArray(), Paths.get(filenameManager.MAP_PATH));

                deliverFile(MAP, mapBuffer);

                closeConnection();
            } else {
                throw new Exception(Error.FILE_NOT_FOUND.getDescription());
            }

        } catch (Exception e) {
            handleError(Error.FILE_NOT_SENT, e);
        }
    }

    private void deliverFile(String filename, byte[] buffer) throws Exception {
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

    public void getFile(String password, String filename) {
        connect();

        try {
            System.out.println("\nUpdating filename encryption-mapping. . .");
            retrieveFile(filenameManager.MAP_PATH, MAP);

            byte[] decryptedMap = EncryptionManager.decryptFile(password.toCharArray(), Paths.get(filenameManager.MAP_PATH));
            FileOutputStream fileOutputStream = new FileOutputStream(filenameManager.MAP_PATH);
            fileOutputStream.write(decryptedMap); /** WARNING: will overwrite existing file with same name **/
            fileOutputStream.close();

            if (!filename.equals(filenameManager.MAP_PATH)) {
                System.out.println("\nDownloading " + filename + " from server. . .");

                String encryptedFilename = filenameManager.getEncryptedPath(filename);
                if (encryptedFilename == null) {
                    throw new Exception(Error.FILE_NOT_FOUND.getDescription());
                } else {
                    retrieveFile(filename, encryptedFilename);

                    byte[] decryptedFile = EncryptionManager.decryptFile(password.toCharArray(), Paths.get(filename));
                    FileOutputStream fos = new FileOutputStream(filename);
                    fos.write(decryptedFile); /** WARNING: will overwrite existing file with same name **/
                    fos.close();
                }
            }
        } catch (Exception e) {
            handleError(Error.CANNOT_RECEIVE_FILE, e);
        }

        closeConnection();
    }

    private void retrieveFile(String filename, String encryptedFilename) throws Exception {
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
        connect();

        System.out.println("\nDeleting " + filename + ". . .");
        try{
            if (deleteFromServer(filename)) {
                if (deleteLocalFile(filename)) {
                    System.out.println("\n" + filename + " has been deleted!");

                    byte[] mapBuffer = EncryptionManager.encryptFile(password.toCharArray(), Paths.get(filenameManager.MAP_PATH));

                    deliverFile(MAP, mapBuffer);
                } else {
                    throw new Exception(Error.LOCAL_DELETE_FAIL.getDescription());
                }
            } else {
                throw new Exception(Error.SERVER_DELETE_FAIL.getDescription());
            }
        } catch (Exception e) {
            handleError(Error.DELETE_FAIL, e);
        }
        closeConnection();
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
                // even if the deletion of the entry fails the file is already deleted so no point returning false.
                // Just inform the user!
            } else {
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
                transferManager.writeControl(Command.ERROR);
        } catch (Exception e) {
            System.out.println(e.getMessage()+'\n');
        }
    }
}

