package com.cryptostore.client;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.input.MouseButton;
import javafx.scene.text.Text;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

public class ViewController {
    private static final String HOST = "localhost";
    private static final int PORT = 5555;

    private ClientManager clientManager;
    private String username;
    public static Stage stage;

    public TextField usernameField;
    public PasswordField userPassField;
    public PasswordField encryptionPassField;
    public TextArea alertField;

    public Button backBtn;
    public Button addBtn;
    public Button deleteBtn;
    public ListView listView;
    public Text userField;
    public Text statusField;
    public Text spaceUsedField;


    /** SETUP **/

    public void init() {
        setListAndHandler();

        userField.setText(username);
        statusField.setText("Connected");
        spaceUsedField.setText(String.format("%.2f", ((float) calculateFileSize()/1024)) + "MB");
    }


    /** HANDLERS **/
    /** login screen **/
    public void passEnterHandler() throws IOException {
        username = usernameField.getText();
        clientManager = new ClientManager(username, userPassField.getText(), HOST, PORT);

        if (clientManager.connect(encryptionPassField.getText())) {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("mainWindow.fxml"));

            loader.setController(this);
            Parent root = loader.load();
            stage.setScene(new Scene(root));
            stage.show();

            init();
        } else {
            clientManager.closeConnection();
            alertField.setText("Wrong username or user password! Please try again.");
        }
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

    private int calculateFileSize() {
        ArrayList files = getAllChildren(new File(username));
        int size = 0;

        for (Object f : files) {
            size += ((File) f).length();
        }

        return size;
    }
}
