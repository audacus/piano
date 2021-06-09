package piano.controller;

import java.net.URL;

import javafx.fxml.Initializable;

public abstract class ViewController implements Initializable {

    public abstract String getViewPath();

    public URL getView() {
        return ViewController.class.getResource(this.getViewPath());
    }

}
