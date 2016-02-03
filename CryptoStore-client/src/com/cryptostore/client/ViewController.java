package com.cryptostore.client;

import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.input.DragEvent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.MouseButton;
import javafx.scene.input.TransferMode;
import javafx.scene.text.Text;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;

public class ViewController {
    private static final String HOST = "localhost";
    private static final int PORT = 5555;

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
        encryptionPassword = encryptionPassField.getText();
        clientManager = new ClientManager(username, userPassField.getText(), HOST, PORT);
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
    }

    /** main screen **/
    public void handleBackbuttonClick() throws IOException {
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
    }

    public void handleDeleteButtonClick() {
        File file = ((File) listView.getSelectionModel().getSelectedItem());
        File dir = file.getParentFile();

        if (file!=null) {
            clientManager.deleteFile(encryptionPassword, file.getPath());
            deleteDirIfEmpty(dir);
            updateList();
        }
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
            Platform.runLater(new Runnable() {
                @Override
                public void run() {
                    try {
                        copyToUserDirAndUpload(file);
                        listView.getItems().removeAll(listView.getItems());
                        listView.getItems().addAll(getAllChildren(new File(username)));
                    } catch (IOException e1) {
                        e1.printStackTrace();
                    }
                }
            });
        }
        e.setDropCompleted(success);
        e.consume();
    }

    /** HELPER METHODS **/

    public void updateList() {
        File parent = ((File) listView.getItems().get(0)).getParentFile();
        if (parent==null) {
            parent = new File(username);
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

    private void copyToUserDirAndUpload(File file) throws IOException {
        String destinationPath;
        File parent = ((File) listView.getItems().get(0)).getParentFile();

        if (parent==null) {
            destinationPath = username;
        } else {
            destinationPath = parent.getPath();
        }

        try {
            if (file.isDirectory()){
                destinationPath += "/" + file.getName();

                FileUtils.copyDirectory(file, new File(destinationPath));

                ArrayList<Path> newFiles = new ArrayList<Path>();
                clientManager.getAllFilesInDir(Paths.get(destinationPath), newFiles);

                for (Path p : newFiles) {
                    clientManager.uploadFileAndMap(encryptionPassword, p.toString());
                }
            } else {
                FileUtils.copyFileToDirectory(file, new File(destinationPath));
                destinationPath += "/" + file.getName();

                clientManager.uploadFileAndMap(encryptionPassword, destinationPath);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void deleteDirIfEmpty(File parentDir) {
        if (parentDir.isDirectory() && parentDir.list().length == 0 && parentDir.getName()!=username) {
            File gParent = parentDir.getParentFile();
            parentDir.delete();
            deleteDirIfEmpty(gParent);
        }
    }
}

