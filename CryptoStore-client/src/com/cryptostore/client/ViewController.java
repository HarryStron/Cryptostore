package com.cryptostore.client;

import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.input.DragEvent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.MouseButton;
import javafx.scene.input.TransferMode;
import javafx.scene.text.Text;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

public class ViewController {
    private static final String HOST = "localhost";
    private static final int PORT = 5555;
    private static final int NUM_OF_SYSTEM_FILES = 2;

    private ClientManager clientManager;
    private String username;
    private String encryptionPassword;
    public static Stage stage;

    /** login screen **/
    public TextField usernameField;
    public PasswordField userPassField;
    public PasswordField encryptionPassField;
    public TextArea alertField;

    /** main screen **/
    public Button backBtn;
    public Button openBtn;
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
        updateSpaceUsed();
    }

    /** HANDLERS **/
    /** login screen **/
    public void passEnterHandler() throws IOException {
        username = usernameField.getText();
        String userPass = userPassField.getText();
        encryptionPassword = encryptionPassField.getText();

        if (!username.equals("") && !userPass.equals("") && !encryptionPassword.equals("")) {
            clientManager = new ClientManager(username, userPass, HOST, PORT);
            stage.setOnCloseRequest(event -> {
                clientManager.closeConnection();
                System.exit(0);
            });


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
        } else {
            alertField.setText("Make sure all fields are complete and try again.");
        }
    }

    /** main screen **/
    public void handleBackButtonClick() throws IOException {
        File parent;
        if (listView.getItems().size()<=0) {
            parent = new File(username);
        } else {
            parent = ((File) listView.getItems().get(0)).getParentFile();
        }

        if (parent==null || parent.getName().equals(username)) {
            listView.getItems().removeAll(listView.getItems());
            listView.getItems().addAll(getAllChildren(new File(username)));
        } else {
            File gParent = parent.getParentFile();
            listView.getItems().removeAll(listView.getItems());
            listView.getItems().addAll(getAllChildren(gParent));
        }
    }

    public void handleAddButtonClick() throws IOException {
        FileChooser fileChooser = new FileChooser();
        File selectedFile = fileChooser.showOpenDialog(stage);

        if (selectedFile != null) {
            copyToUserDirAndUpload(selectedFile);
            updateList();
        }
        updateSpaceUsed();
    }

    public  void handleOpenButtonClick() {
        File file = ((File) listView.getSelectionModel().getSelectedItem());
        if (file!=null) {
            try {
                Desktop.getDesktop().open(file);
            } catch (IOException e) {
                //TODO notify
            }
        }
    }
    public void handleDeleteButtonClick() {
        File file = ((File) listView.getSelectionModel().getSelectedItem());

        if (file!=null) {
            clientManager.delete(encryptionPassword, file.getPath());
            updateList();
        }
        updateSpaceUsed();
    }

    public void onListDragOver(final DragEvent e) {
        final Dragboard db = e.getDragboard();

        if (db.hasFiles()) {
            e.acceptTransferModes(TransferMode.COPY);
        } else {
            e.consume();
        }
    }

    public void onListDragDropped(final DragEvent e) {
        final Dragboard db = e.getDragboard();
        boolean success = false;

        if (db.hasFiles()) {
            success = true;
            // Only get the first file from the list
            final File file = db.getFiles().get(0);
            Platform.runLater(() -> {
                try {
                    copyToUserDirAndUpload(file);
                    listView.getItems().removeAll(listView.getItems());
                    listView.getItems().addAll(getAllChildren(new File(username)));
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
            });
        }
        e.setDropCompleted(success);
        e.consume();
        updateSpaceUsed();
    }

    /** HELPER METHODS **/

    public void updateList() {
        File parent = new File(username);
        if (parent.listFiles().length>NUM_OF_SYSTEM_FILES) {
            parent = ((File) listView.getItems().get(0)).getParentFile();
        }

        listView.getItems().removeAll(listView.getItems());
        listView.getItems().addAll(getAllChildren(parent));
    }

    private void setListAndHandler() {
        listView.getItems().addAll(getAllChildren(new File(username)));

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

        if (f.listFiles()==null) {
            f = new File(username);
        }

        for (File file : f.listFiles()) { //never going to be null as enc file and sync file will always be there
            if (!file.getPath().equals(FilenameManager.MAP_PATH.substring(2)) && !file.getPath().equals(SyncManager.SYNC_PATH.substring(2))) {
                elements.add(file);
            }
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

    private void copyToUserDirAndUpload(File file) throws IOException {
        String destinationPath;

        if (listView.getItems().size()<=NUM_OF_SYSTEM_FILES) {
            destinationPath = username;
        } else {
            File parent = ((File) listView.getItems().get(0)).getParentFile();
            destinationPath = parent.getPath();
        }

        clientManager.copyLocallyAndUpload(encryptionPassword, file, destinationPath);
    }

    private void updateSpaceUsed() {
        spaceUsedField.setText(String.format("%.2f", ((float) calculateFileSize()/1024/1024)) + "MB");
    }
}

