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
                throw new IOException();
            }
        } catch (IOException e) {
            Error.CANNOT_AUTH.print();
        }
    }

    private void okOrException() throws IOException {
        if (singleByteIn() == Command.OK.getCode()) {
            return;
        } else {
            transferManager.writeControl(Command.ERROR);
            throw new IOException("Communication with server failed");
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

                    okOrException();
                    transferManager.writeFileSize(filename.length()); //TODO what if a file is too big to be represented with an int?

                    okOrException();
                    transferManager.writeFileName(filename);

                    okOrException();
                    byte[] buffer = Files.readAllBytes(path);

                    transferManager.writeFileSize(buffer.length);

                    okOrException();
                    if (buffer.length != 0) {
                        transferManager.write(new FileData(buffer));

                        okOrException();
                        System.out.println("File \'" + filename + "\' sent successfully!");

                    } else {
                        System.out.println("File \'" + filename + "\' sent successfully!");
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

                okOrException();
                transferManager.writeFileSize(filename.length());


                okOrException();
                transferManager.writeFileName(filename);

//                int response = singleByteIn();
//                if (response == Command.READY.getCode()) {
                okOrException();
                transferManager.writeControl(Command.OK);

                FileOutputStream newFile = new FileOutputStream(new File(filename));
                int sizeOfFile = singleByteIn(); //TODO change to long
                transferManager.writeControl(Command.OK);

                if (sizeOfFile > 0) {
                    byte[] buffer = transferManager.read(sizeOfFile).getData(1);

                    newFile.write(buffer, 0, buffer.length);

                    transferManager.writeControl(Command.OK);
                }

                System.out.println(filename + " received!");
//                } else if (response == Command.ERROR.getCode()) {
//                    Error.FILE_NOT_FOUND.print();
//                } else {
//                    Error.FILE_NOT_RETRIEVED.print();
//                }
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

