package com.cryptostore.client;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.io.IOException;

public class Client extends Application {
    private static final String encryptionPassword = "password";

    public static void main(String args[]) throws IOException {
        launch(args);

//        ClientManager clientManager1 = new ClientManager("Admin1", "P4$$w0rd", "localhost", 5555);
//        ClientManager clientManager2 = new ClientManager("Admin2", "P4$$w0rd", "localhost", 5555);
//        ClientManager clientManager3 = new ClientManager("Harry", "P4$$w0rd", "localhost", 5555);

//        ClientManager.getAllFilesInDir((new File("./Admin1/")).toPath()).forEach(System.out::println);
//        clientManager1.connect(encryptionPassword);
//        clientManager1.uploadFileAndMap(encryptionPassword, "./Admin1/test1.txt");
//        clientManager1.uploadFileAndMap(encryptionPassword, "./Admin1/test3.txt");
//        clientManager1.uploadFileAndMap(encryptionPassword, "./Admin1/Admin2/test2.txt");
//        clientManager1.download(encryptionPassword, "./Admin1/test1.txt");
//        clientManager1.download(encryptionPassword, "./Admin1/Admin2/test2.txt");
//        clientManager1.deleteFile(encryptionPassword, "./Admin1/test3.txt");
//        clientManager1.closeConnection();

//        clientManager2.connect(encryptionPassword);
//        clientManager2.uploadFileAndMap(encryptionPassword, "./Admin2/test2.txt");
//        clientManager2.deleteFile(encryptionPassword, "./Admin2/test2.txt");
//        clientManager2.closeConnection();

//        clientManager3.connect(encryptionPassword);
//        clientManager3.uploadFileAndMap(encryptionPassword, "./Admin1/test1.txt");
//        clientManager3.download(encryptionPassword, "./Admin1/test1.txt");
//        clientManager3.closeConnection();

    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("login.fxml"));
        Parent root = loader.load();

        primaryStage.setTitle("CryptoStore");
        primaryStage.setScene(new Scene(root));
        primaryStage.show();

        ViewController.stage = primaryStage;
    }
}