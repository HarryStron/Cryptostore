import control.ClientController;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
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

        primaryStage.show();
    }
}