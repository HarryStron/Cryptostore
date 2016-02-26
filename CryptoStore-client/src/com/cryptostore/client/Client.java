package com.cryptostore.client;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class Client extends Application {
    public static void main(String args[]) throws IOException {
        ClientManager clientManager = new ClientManager("TestiBesti","P4$$w0rd","localhost",5550,"password");
        clientManager.connect("P4$$w0rd");
//        clientManager.registerNewUser("TestiBesti","P4$$w0rd","password",true);
        clientManager.closeConnection();
//        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("login.fxml"));
        Parent root = loader.load();

        primaryStage.setTitle("CryptoStore");
        primaryStage.setScene(new Scene(root));
        primaryStage.show();

        ViewController.primaryStage = primaryStage;
    }
}