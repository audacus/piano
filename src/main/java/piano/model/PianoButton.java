package piano.model;

import javafx.beans.property.SimpleIntegerProperty;
import javafx.scene.input.KeyCode;

public class PianoButton extends KeyboardButton {

    private SimpleIntegerProperty noteIndex;

    public PianoButton(KeyCode keyCode, String keyName, int noteIndex, boolean isPressed) {
        super(keyCode, keyName, isPressed);
        this.setNoteIndex(noteIndex);
    }

    @Override
    protected void init() {
        super.init();
        this.noteIndex = new SimpleIntegerProperty();
    }

    public int getNoteIndex() {
        return this.noteIndex.get();
    }

    public void setNoteIndex(int noteIndex) {
        this.noteIndex.set(noteIndex);
    }

    @Override
    public PianoButton getCopy() {
        var key = (PianoButton) super.getCopy();
        key.setNoteIndex(this.getNoteIndex());
        return key;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof PianoButton) {
            var compare = (PianoButton) obj;
            return
                this.getKeyCode() == compare.getKeyCode() &&
                this.getKeyName() == compare.getKeyName() &&
                this.isPressed() == compare.isPressed() &&
                this.getNoteIndex() == compare.getNoteIndex();
        }
        return false;
    }
}
