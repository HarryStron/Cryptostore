package com.cryptostore.client;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.input.MouseButton;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

public class ViewController {
    private ClientManager clientManager;
    public static Stage stage;

    public TextField usernameField;
    public PasswordField passwordField;

    public Button backBtn;
    public Button addBtn;
    public Button deleteBtn;
    public ListView listView;

    /** SETUP **/

    public void init() {
        setListAndHandler();
    }


    /** HANDLERS **/
    /** login screen **/
    public void passEnterHandler() throws IOException {
        System.out.println(usernameField.getText());
        System.out.println(passwordField.getText());

        if (passwordField.getText().equals("123")) {
//            passwordField.getScene().getWindow().hide();
            FXMLLoader loader = new FXMLLoader(getClass().getResource("mainWindow.fxml"));

            loader.setController(this);
            Parent root = loader.load();
            stage.setScene(new Scene(root));
            stage.show();

            init();
        }
//        clientManager = new ClientManager(usernameField.getText(), passwordField.getText(), "localhost", 5555);
    }

    /** main screen **/
    public void handleBackbuttonClick() throws IOException {
        File parent = ((File) listView.getItems().get(0)).getParentFile();

        if (parent==null || parent.getName().equals("demoDir")) {
            parent = new File("demoDir");
            listView.getItems().removeAll(listView.getItems());
            listView.getItems().addAll(parent);
        } else {
            File gParent = parent.getParentFile();
            listView.getItems().removeAll(listView.getItems());
            listView.getItems().addAll(getAllChildren(gParent));
        }
    }

    public void handleAddButtonClick() {
        System.out.println("add");

        FileChooser fileChooser = new FileChooser();
        File selectedFile = fileChooser.showOpenDialog(stage);

        if (selectedFile != null) {
            listView.getItems().addAll(selectedFile);
        }
    }


    /** HELPER METHODS **/

    private void setListAndHandler() {
        listView.getItems().addAll(new File("demoDir"));

        listView.setOnMouseClicked(event -> {
            if(event.getButton().equals(MouseButton.PRIMARY)){
                if(event.getClickCount() == 2) {
                    File file = new File(listView.getSelectionModel().getSelectedItem().toString());

                    if (file.isDirectory()){
                        listView.getItems().removeAll(listView.getItems());
                        listView.getItems().addAll(getAllChildren(file));
                    }
                }
            }
        });
    }

    private ArrayList getAllChildren(File f) {
        ArrayList elements = new ArrayList();

        for (File file : f.listFiles()) {
            elements.add(file);
        }

        return elements;
    }
}
