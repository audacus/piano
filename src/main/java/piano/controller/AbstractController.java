package piano.controller;

import java.net.URL;

import javafx.fxml.Initializable;

public abstract class AbstractController implements Initializable {

    public abstract String getViewPath();

    public URL getView() {
        return AbstractController.class.getResource(this.getViewPath());
    }

}
