import control.ClientController;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.SplitPane;
import javafx.scene.control.TextArea;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Border;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;

public class Main extends Application {

    public static void main(String[] args) {
        Main.launch(args);

        new ClientController();
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        FXMLLoader fxmlLoader = new FXMLLoader();
        Parent root = fxmlLoader.load(getClass().getResource("view/main.fxml"));
        ClientController clientController = fxmlLoader.getController();

        primaryStage.setTitle("Cryptostore");
        primaryStage.setScene(new Scene(root));

        primaryStage.setMinWidth(500);
        primaryStage.setMinHeight(300);

        TextArea userStatus = (TextArea) root.lookup("#userStatus");
        TextArea lastSync = (TextArea) root.lookup("#lastSync");

        SplitPane splitPane = (SplitPane) root.lookup("#splitPane");
        GridPane gridPane = new GridPane();
        splitPane.getChildrenUnmodifiable().add(gridPane);

        AnchorPane settingsPane = (AnchorPane) root.lookup("#settingsPane");

        primaryStage.show();
    }
}