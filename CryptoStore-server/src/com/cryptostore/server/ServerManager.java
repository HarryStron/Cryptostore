package com.cryptostore.server;

import javax.net.ssl.SSLServerSocket;
import javax.net.ssl.SSLServerSocketFactory;
import javax.net.ssl.SSLSocket;
import java.io.File;
import java.io.IOException;
import java.lang.*;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;

public class ServerManager {
    public static String KEYSTORE_PATH = "mySrvKeystore"; //public for testing purposes
    private String KEYSTORE_PASS = "123456";
    public static ArrayList<String> onlineUsers;
    private boolean serverIsOn = true;

    public ServerManager(int listeningPort) {
        onlineUsers = new ArrayList<>();
        setCertificates();
        connect(listeningPort);
    }

    private void setCertificates() {
        System.out.println("Setting up certificates.\n");

        File file = new File(KEYSTORE_PATH);
        Path path = Paths.get(file.toURI());
        System.setProperty("javax.net.ssl.keyStore", path.toString());
        System.setProperty("javax.net.ssl.keyStorePassword", KEYSTORE_PASS);
    }

    private void connect(int listeningPort) {
        try {
            SSLServerSocketFactory sslserversocketfactory = (SSLServerSocketFactory) SSLServerSocketFactory.getDefault();
            SSLServerSocket sslserversocket = (SSLServerSocket) sslserversocketfactory.createServerSocket(listeningPort);

            waitForConnection(sslserversocket);

        } catch (IOException e) {
            Error.CANNOT_CONNECT.print();
        }
    }

    private void waitForConnection(SSLServerSocket sslserversocket) {
        System.out.println("Server is running...");
        while (serverIsOn) {
            try {
                SSLSocket clientSocket = (SSLSocket) sslserversocket.accept();
                clientSocket.setSoLinger(true, 0);

                ClientThread clientThread = new ClientThread(clientSocket);
                clientThread.run();

            } catch (IOException e) {
                Error.SOCKET_CLOSED.print();
            }
        }
    }
}
