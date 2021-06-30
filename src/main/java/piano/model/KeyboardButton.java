package piano.model;

import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.scene.input.KeyCode;

public class KeyboardButton extends AbstractModel {

    protected SimpleObjectProperty<KeyCode> keyCode;
    protected SimpleStringProperty keyName;
    protected SimpleBooleanProperty isPressed;

    public KeyboardButton() {
        this.init();
    }

    public KeyboardButton(KeyCode keyCode, String keyName, boolean isPressed) {
        this.init();
        this.setKeyCode(keyCode);
        this.setKeyName(keyName);
        this.setIsPressed(isPressed);
    }

    protected void init() {
        this.keyCode = new SimpleObjectProperty<>();
        this.keyName = new SimpleStringProperty();
        this.isPressed = new SimpleBooleanProperty();
    }

    public KeyCode getKeyCode() {
        return this.keyCode.get();
    }

    public void setKeyCode(KeyCode keyCode) {
        this.keyCode.set(keyCode);
    }

    public String getKeyName() {
        return this.keyName.get();
    }

    public void setKeyName(String keyName) {
        this.keyName.set(keyName);
    }

    public boolean isPressed() {
        return this.isPressed.get();
    }

    public void setIsPressed(boolean isPressed) {
        this.isPressed.set(isPressed);
    }


    @Override
    public String getDisplayText() {
        return this.getKeyName();
    }

    @Override
    public KeyboardButton getCopy() {
        var key = new KeyboardButton();
        key.setKeyCode(this.getKeyCode());
        key.setKeyName(this.getKeyName());
        key.setIsPressed(this.isPressed());
        return key;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof KeyboardButton) {
            var compare = (KeyboardButton) obj;
            return
                this.getKeyCode() == compare.getKeyCode() &&
                this.getKeyName() == compare.getKeyName() &&
                this.isPressed() == compare.isPressed();
        }
        return false;
    }
    
}
