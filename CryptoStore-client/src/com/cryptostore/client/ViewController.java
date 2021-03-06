package com.cryptostore.client;

import javafx.application.Platform;
import javafx.fxml.FXML;
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
import javafx.scene.layout.AnchorPane;
import javafx.scene.text.Text;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;

public class ViewController {
    private static final int PORT = 5550;
    private static final int NUM_OF_SYSTEM_FILES = 2;

    private ClientManager clientManager;
    private String username;
    private String encryptionPassword;
    private boolean firstTimeStegoClicked;
    public static Stage primaryStage;

    /** login screen **/
    @FXML
    private TextField usernameField;
    @FXML
    private PasswordField userPassField;
    @FXML
    private PasswordField encryptionPassField;

    /** register screen **/
    @FXML
    private TextField regUsernameField;
    @FXML
    private PasswordField regUserPassField;
    @FXML
    private PasswordField regEncryptionPassField;
    @FXML
    private ToggleButton regIsAdminBtn;
    @FXML
    private Button regGoBtn;

    /** main screen **/
    @FXML
    private Button backBtn;
    @FXML
    private Button openBtn;
    @FXML
    private Button selectPngBtn;
    @FXML
    private ToggleButton stegoBtn;
    @FXML
    private Button pushBtn;
    @FXML
    private Button addBtn;
    @FXML
    private Button deleteBtn;
    @FXML
    private ListView listView;
    @FXML
    private Button createUserBtn;
    @FXML
    private Text userField;
    @FXML
    private Text statusField;
    @FXML
    private Text spaceUsedField;

    /** popup screen **/
    @FXML
    private TextArea popupText;
    @FXML
    private Button popupOK;

    /** SETUP **/

    public void init() {
        setListAndHandler();

        userField.setText(username);
        statusField.setText("Connected");
        selectPngBtn.setDisable(true);
        firstTimeStegoClicked = true;
        updateSpaceUsed();
    }

    /** HANDLERS **/
    /** login screen **/
    public void passEnterHandler() throws IOException {
        username = usernameField.getText();
        String userPass = userPassField.getText();
        encryptionPassword = encryptionPassField.getText();

        if (!username.equals("") && !userPass.equals("") && !encryptionPassword.equals("")) {
            clientManager = new ClientManager(username, userPass, PORT, encryptionPassword);
            primaryStage.setOnCloseRequest(event -> {
                clientManager.closeConnection();
                System.exit(0);
            });


            if (clientManager.connect(encryptionPassField.getText())) {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("mainWindow.fxml"));

                loader.setController(this);
                Parent root = loader.load();
                primaryStage.setScene(new Scene(root));
                primaryStage.show();

                init();
            } else {
                clientManager.closeConnection();
                notify("Cannot connect! Please try again.");
            }
        } else {
            notify("Make sure all fields are complete and try again.");
        }
    }

    /** main screen **/
    public void handleBackButtonClick() throws IOException {
        blockActions(true);
        File parent;
        if (listView.getItems().size()<=0) {
            parent = new File(username);
        } else {
            parent = ((File) listView.getItems().get(0)).getParentFile();
        }

        if (parent==null || parent.getName().equals(username)) {
            listView.getItems().removeAll(listView.getItems());
            listView.getItems().addAll(getAllChildren(new File(username)));
            notify("You are currently in the root \"/yourUsername\" directory");
        } else {
            File gParent = parent.getParentFile();
            listView.getItems().removeAll(listView.getItems());
            listView.getItems().addAll(getAllChildren(gParent));
        }
        blockActions(false);
    }

    public void stegoClicked() {
        if (stegoBtn.isSelected()) {
            stegoBtn.setText("Stego On");
            selectPngBtn.setDisable(false);
            clientManager.setStegoEnabled(true);
            if (firstTimeStegoClicked) {
                notify("Select a PNG to hide the file in. If not a default image is going to be used!");
                firstTimeStegoClicked = false;
            }
        } else {
            stegoBtn.setText("Stego Off");
            selectPngBtn.setDisable(true);
            clientManager.setStegoEnabled(false);
        }
    }

    public void selectStegoImg() {
        blockActions(true);
        FileChooser fileChooser = new FileChooser();
        FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter("PNG", "*.png");
        fileChooser.getExtensionFilters().add(extFilter);
        File selectedFile = fileChooser.showOpenDialog(primaryStage);

        if (selectedFile != null) {
            ClientManager.IMAGE_PATH = selectedFile.getAbsolutePath();
            selectPngBtn.setText(selectedFile.getName());
        }
        blockActions(false);
    }

    public void handlePushButtonClick() {
        blockActions(true);

        File file = ((File) listView.getSelectionModel().getSelectedItem());

        if (file!=null) {
            try {
                boolean stegoEnabledForFile = clientManager.isStegoEnabled(file.getPath());
                if (stegoEnabledForFile) {
                    stegoBtn.setText("Stego On");
                    stegoBtn.setSelected(true);
                    selectPngBtn.setDisable(false);
                    clientManager.setStegoEnabled(true);
                } else {
                    stegoBtn.setText("Stego Off");
                    stegoBtn.setSelected(false);
                    selectPngBtn.setDisable(true);
                    clientManager.setStegoEnabled(false);
                }

                clientManager.uploadFileAndMap(encryptionPassword, file.getPath());
                updateList();
                updateSpaceUsed();
            } catch (Exception e) {
                notify("Failed to push changes to server!\n" +
                        "Warning: Push changes will only work for individual files but not directories.");
            }
        } else {
            notify("No file selected!");
        }

        blockActions(false);
    }

    public void handleAddButtonClick() throws IOException {
        blockActions(true);

        FileChooser fileChooser = new FileChooser();
        File selectedFile = fileChooser.showOpenDialog(primaryStage);

        if (selectedFile != null) {
            copyToUserDirAndUpload(selectedFile);
            updateList();
        }
        updateSpaceUsed();
        blockActions(false);
    }

    public  void handleOpenButtonClick() {
        File file = ((File) listView.getSelectionModel().getSelectedItem());
        if (file!=null) {
            try {
                Desktop.getDesktop().open(file);
            } catch (IOException e) {
                notify("Cannot open file!");
            }
        } else {
            notify("No file selected!");
        }
    }
    public void handleDeleteButtonClick() {
        blockActions(true);
        File file = ((File) listView.getSelectionModel().getSelectedItem());

        if (file!=null) {
            if (clientManager.delete(encryptionPassword, file.getPath())) {
                updateList();
                updateSpaceUsed();
            } else {
                notify("Deleting the file failed!");
            }
        } else {
            notify("No file selected!");
        }
        blockActions(false);
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
        blockActions(true);
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
                    notify("Uploading has failed!");
                }
            });
        }
        e.setDropCompleted(success);
        e.consume();
        updateSpaceUsed();
        blockActions(false);
    }

    public void createUser() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("registerUser.fxml"));
            loader.setController(this);
            AnchorPane page = loader.load();
            Scene scene = new Scene(page);
            Stage stage = new Stage();
            stage.setScene(scene);
            stage.initOwner(primaryStage);
            stage.initModality(Modality.WINDOW_MODAL);
            stage.show();

            notify("This feature is only available for Admins!\n" +
                    "The registration will fail if you do not have Admin rights.");
        } catch (IOException e) {
            notify("Cannot to create user!");
        }
    }

    /** register screen **/
    public void registerUser() {
        try {
            String user = regUsernameField.getText();
            String pass = regUserPassField.getText();
            String encPass = regEncryptionPassField.getText();
            boolean isAdmin = regIsAdminBtn.isSelected();

            if (!username.equals("") && !pass.equals("") && !encPass.equals("")) {
                clientManager.registerNewUser(user, pass, encPass, isAdmin);

                notify("User '" + regUsernameField.getText() + "' has been registered!");
            } else {
                notify("Make sure all fields are complete!");
            }
        } catch (Exception e) {
            if (e.getMessage()!=null && e.getMessage().equals(Error.USER_EXISTS.getDescription())) {
                notify("Username already exists. Try something else!");
            } else if (e.getMessage()!=null && e.getMessage().equals(Error.CANNOT_AUTH.getDescription())) {
                notify("Server unresponsive!");
            } else {
                notify("Something went wrong!");
            }
        }
    }

    /** HELPER METHODS **/

    public void updateList() {
        File parent = new File(username);
        if (listView.getItems().size()>NUM_OF_SYSTEM_FILES) {
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

    private ArrayList<File> getAllChildren(File f) {
        ArrayList elements = new ArrayList();

        if (f.listFiles()==null) {
            f = new File(username);
        }

        for (File file : f.listFiles()) { //never going to be null as enc file and sync file will always be there
            if (!file.getPath().equals(clientManager.getMAP_PATH()) && !file.getPath().equals(clientManager.getSYNC_PATH())) {
                if (!file.getName().startsWith(".")) {
                    elements.add(file);
                }
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
        if (fileAlreadyExistsInDir(file)) {
            notify("The file is already in the user directory");
        } else {
            if (stegoBtn.isSelected() && !fileFitsInImg(file)) {
                notify("The file is too large to be hidden inside the image!");
            } else {
                String destinationPath;

                if (listView.getItems().size() <= NUM_OF_SYSTEM_FILES) {
                    destinationPath = username;
                } else {
                    File parent = ((File) listView.getItems().get(0)).getParentFile();
                    destinationPath = parent.getPath();
                }

                if (!clientManager.copyLocallyAndUpload(encryptionPassword, file, destinationPath)) {
                    notify("Uploading has failed!");
                }
            }
        }
    }

    private boolean fileFitsInImg(File file) throws IOException {
        boolean fits = true;

        if (file.isDirectory()) {
            for (File f : getAllChildren(file)) {
                if (!SteganographyManager.fitsInImage(Files.readAllBytes(f.toPath()), ClientManager.IMAGE_PATH)) {
                    fits = false;
                }
            }
        } else if (!SteganographyManager.fitsInImage(Files.readAllBytes(file.toPath()), ClientManager.IMAGE_PATH)) {
            fits = false;
        }

        return fits;
    }

    private boolean fileAlreadyExistsInDir(File file) {
        File parent;
        if (listView.getItems().size()<=0) {
            parent = new File(username);
        } else {
            parent = ((File) listView.getItems().get(0)).getParentFile();
        }
        ArrayList<File> files = getAllChildren(parent);

        boolean out = false;
        for (File f : files) {
            if (f.getName().equals(file.getName())) {
                out = true;
            }
        }
        return out;
    }

    private void updateSpaceUsed() {
        spaceUsedField.setText(String.format("%.2f", ((float) calculateFileSize()/1024/1024)) + "MB");
    }

    private void blockActions(boolean val) {
        stegoBtn.setDisable(val);
        backBtn.setDisable(val);
        openBtn.setDisable(val);
        addBtn.setDisable(val);
        deleteBtn.setDisable(val);
        listView.setDisable(val);
    }

    private void notify(String txt) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("popup.fxml"));
            loader.setController(this);
            AnchorPane page = loader.load();
            Scene scene = new Scene(page);
            Stage stage = new Stage();
            stage.setScene(scene);
            stage.setTitle("Error");
            stage.initOwner(primaryStage);
            stage.initModality(Modality.WINDOW_MODAL);
            stage.show();

            popupText.setText(txt);
            popupOK.setOnMouseClicked(event -> {
                stage.hide();
            });

        } catch (IOException e) {
            System.out.println(txt);
        }
    }
}

