package piano.controller;

import javafx.event.EventHandler;
import java.net.URL;

import javafx.fxml.Initializable;
import javafx.stage.WindowEvent;

public abstract class ViewController implements Initializable {

    public abstract String getViewPath();

    public abstract EventHandler<WindowEvent> getWindowCloseHandler();

    public URL getView() {
        return ViewController.class.getResource(this.getViewPath());
    }

}
