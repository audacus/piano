package piano;

import java.io.IOException;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import piano.controller.PianoController;
import piano.controller.ViewController;

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
            var pianoController = new PianoController();

            // load view
            var loader = new FXMLLoader();
            loader.setLocation(pianoController.getView());

            // show the scene containing the root layout
            var scene = new Scene(loader.load());
            this.primaryStage.setScene(scene);
            this.primaryStage.show();

            // set on window close action
            this.primaryStage.setOnCloseRequest(((ViewController) loader.getController()).getWindowCloseHandler());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}