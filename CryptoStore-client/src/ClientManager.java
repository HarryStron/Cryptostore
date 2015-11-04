import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.nio.file.Path;
import java.nio.file.Paths;

public class ClientManager {
    private SSLSocket clientSocket;
    private TransferManager transferManager;
    private String host;
    private int hostPort;
    private String username;
    private String password;
    private boolean isAUTHed;

    public ClientManager(String username, String password, String host, int hostPort) {
        setCertificates();
        this.username = username;
        this.password = password;
        this.host = host;
        this.hostPort = hostPort;
        isAUTHed = false;
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
            if (singleByteIn() == Command.AUTH.getCode()) {
                transferManager.writeFileSize(username.length());

                okOrException();
                transferManager.writeFileName(username);

                okOrException();
                transferManager.writeFileSize(password.length());

                okOrException();
                transferManager.writeFileName(password);

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

    public void sendFile(String password, String filename) {
        System.out.println("\nSending \'" + filename + "\' to server . . .");

        try {

            Path path = Paths.get(filename);
            if (path.toFile().exists()) {
                byte[] buffer = EncryptionManager.encryptFile(password.toCharArray(), path);

                deliverFile(filename, buffer);
            } else {
                throw new Exception(Error.FILE_NOT_FOUND.getDescription());
            }

        } catch (Exception e) {
            handleError(Error.FILE_NOT_SENT, e);
        }
    }

    private void deliverFile(String filename, byte[] buffer) throws Exception {
        connect();

        if (isAUTHed) {
            try {
                transferManager.writeControl(Command.FILE_FROM_CLIENT);

                okOrException();
                transferManager.writeFileSize(filename.length()); //TODO what if a file is too big to be represented with an int?

                okOrException();
                transferManager.writeFileName(filename);

                okOrException();
                transferManager.writeFileSize(buffer.length);

                okOrException();
                if (buffer.length != 0) {
                    transferManager.write(new FileData(buffer));

                    okOrException();
                    System.out.println("File \'" + filename + "\' sent successfully!");

                } else {
                    System.out.println("File \'" + filename + "\' sent successfully!");
                }
            } catch (Exception e) {
                throw new Exception(e.getMessage());
            }

            closeConnection();
        } else {
            throw new Exception(Error.CANNOT_AUTH.getDescription());
        }
    }

    public void getFile(String password, String filename) {
        System.out.println("\nDownloading " + filename + " from server. . .");

        try {
            retrieveFile(filename);

            byte[] decryptedFile = EncryptionManager.decryptFile(password.toCharArray(), Paths.get(filename));
            FileOutputStream fos = new FileOutputStream(filename);
            fos.write(decryptedFile); /** WARNING: will overwrite existing file with same name **/
            fos.close();
        } catch (Exception e) {
            handleError(Error.CANNOT_SAVE_FILE, e);
        }
    }

    private void retrieveFile(String filename) throws Exception{
        connect();

        if (isAUTHed) {
            try {
                transferManager.writeControl(Command.FILE_FROM_SERVER);

                okOrException();
                transferManager.writeFileSize(filename.length());


                okOrException();
                transferManager.writeFileName(filename);

                okOrException();
                transferManager.writeControl(Command.OK);

                File file = new File(filename);
                file.getParentFile().mkdirs();
                FileOutputStream fos = new FileOutputStream(new File(filename));
                int sizeOfFile = singleByteIn(); //TODO change to long

                if (sizeOfFile < 0) {
                    throw new IOException(Error.NEGATIVE_SIZE.getDescription());
                }

                transferManager.writeControl(Command.OK);

                if (sizeOfFile > 0) {
                    byte[] buffer = transferManager.read(sizeOfFile).getData(1);
                    fos.write(buffer, 0, buffer.length);

                    transferManager.writeControl(Command.OK);
                }

                System.out.println(filename + " received!");

            } catch (IOException e) {
                throw new Exception(e.getMessage());
            }

            closeConnection();
        } else {
            throw new Exception(Error.CANNOT_AUTH.getDescription());
        }
    }

    private int singleByteIn() throws Exception {
        try {
            return transferManager.read(0).getData(1)[0];
        } catch (IOException e) {
            throw new Exception(Error.FAILED_TO_READ.getDescription());
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
            err1.print();

            if (err2.getMessage() != null)
                System.out.println(err2.getMessage()+'\n');

            closeConnection();
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }
}

