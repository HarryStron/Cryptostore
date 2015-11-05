package control;

import javafx.fxml.Initializable;
import model.ClientManager;
import java.net.URL;
import java.util.ResourceBundle;

public class ClientController implements Initializable {
    private ClientManager clientManager;

    @Override
    public void initialize(URL location, ResourceBundle resources) {

    }

    public ClientController() {}

    public void register() {
        clientManager = new ClientManager("Admin1", "P4$$w0rd", "localhost", 5556);
    }

    public void send() {
        // Gets file from view
        clientManager.sendFile("P4$$w0rd", "./testDir/test.txt");
    }

    public void get() {
        clientManager.getFile("P4$$w0rd", "./testDir/test.txt");
    }
}