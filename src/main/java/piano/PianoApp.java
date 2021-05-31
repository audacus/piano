package piano;

import java.io.IOException;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import piano.controller.MainController;

public class PianoApp extends Application {

    public static final String APP_NAME = "piano";

    private Stage primaryStage;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        this.primaryStage = primaryStage;
        this.primaryStage.setTitle(APP_NAME);
        this.showMainView();
    }

    public void showMainView() {
        try {
            var mainController = new MainController();
            var loader = new FXMLLoader();

            // load main view
            loader.setLocation(mainController.getView());

            // Show the scene containing the root layout.
            var scene = new Scene(loader.load());
            primaryStage.setScene(scene);
            primaryStage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}