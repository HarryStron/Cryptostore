import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.nio.file.Files;
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

        } catch (IOException e) {
            Error.CANNOT_CONNECT.print();
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
            if (singleByteIn() == Command.AUTH.getCode())
                transferManager.writeFileSize(username.length());

            if (singleByteIn() == Command.OK.getCode())
                transferManager.writeFileName(username);

            if (singleByteIn() == Command.OK.getCode())
                transferManager.writeFileSize(password.length());

            if (singleByteIn() == Command.OK.getCode())
                transferManager.writeFileName(password);

            if (singleByteIn() == Command.READY.getCode())
                isAUTHed = true;

        } catch (IOException e) {
            Error.CANNOT_AUTH.print();
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
        } catch (IOException e) {
            Error.SERVER_DISCONECTED.print();
        }
    }

    public void sendFile(String filename) {
        connect();
        if (isAUTHed) {

            System.out.println("\nSending \'" + filename + "\' to server . . .");

            Path path = Paths.get(filename);

            if (path.toFile().exists()) {
                try {
                    transferManager.writeControl(Command.file_from_client);

                    if (singleByteIn() == Command.OK.getCode()) {
                        transferManager.writeFileSize(filename.length()); //TODO what if a file is too big to be represented with an int?
                    }

                    if (singleByteIn() == Command.OK.getCode()) {
                        transferManager.writeFileName(filename);
                    }

                    int response = singleByteIn();
                    if (response == Command.READY.getCode()) {
                        byte[] buffer = Files.readAllBytes(path);

                        transferManager.writeFileSize(buffer.length);

                        if (singleByteIn() == Command.OK.getCode()) {
                            if (buffer.length != 0) {
                                transferManager.write(new FileData(buffer));

                                if (singleByteIn() == Command.DONE.getCode()) {
                                    System.out.println("File \'" + filename + "\' sent successfully!");
                                }
                            } else {
                                System.out.println("File \'" + filename + "\' sent successfully!");
                            }
                        }
                    } else if (response == Command.ERROR.getCode()) {
                        Error.EMPTY_FILENAME.print();
                    } else {
                        Error.FILE_NOT_SENT.print();
                    }
                } catch (IOException e) {
                    Error.FILE_NOT_SENT.print();
                }
            } else {
                Error.FILE_NOT_FOUND.print();
            }

            closeConnection();
        }
    }

    public void retrieveFile(String filename) {
        connect();
        if (isAUTHed) {

            System.out.println("\nDownloading " + filename + " from server. . .");

            try {
                transferManager.writeControl(Command.file_from_server);

                if (singleByteIn() == Command.OK.getCode()) {
                    transferManager.writeFileSize(filename.length());
                }

                if (singleByteIn() == Command.OK.getCode()) {
                    transferManager.writeFileName(filename);
                }

                int response = singleByteIn();
                if (response == Command.READY.getCode()) {
                    transferManager.writeControl(Command.READY);

                    FileOutputStream newFile = new FileOutputStream(new File(filename));
                    int sizeOfFile = singleByteIn(); //TODO change to long
                    transferManager.writeControl(Command.OK);

                    if (sizeOfFile > 0) {
                        byte[] buffer = transferManager.read(sizeOfFile).getData(1);

                        newFile.write(buffer, 0, buffer.length);

                        transferManager.writeControl(Command.DONE);
                    }

                    System.out.println(filename + " received!");
                } else if (response == Command.ERROR.getCode()) {
                    Error.FILE_NOT_FOUND.print();
                } else {
                    Error.FILE_NOT_RETRIEVED.print();
                }
            } catch (IOException e) {
                Error.FILE_NOT_RETRIEVED.print();
            }

            closeConnection();
        }
    }

    private int singleByteIn() throws IOException {
        return transferManager.read(0).getData(1)[0];
    }
}

