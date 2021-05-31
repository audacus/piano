package piano.model;

import javafx.beans.property.Property;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;

public class Tone extends AbstractModel {

    public static final int DEFAULT_VELOCITY = 50;

    // https://en.wikipedia.org/wiki/Piano_key_frequencies
    private SimpleObjectProperty<Note> note;
    private SimpleIntegerProperty velocity;

    public Tone() {
        this.note = new SimpleObjectProperty<>();
        this.velocity = new SimpleIntegerProperty(DEFAULT_VELOCITY);
    }

    public Note getNote() {
        return this.note.get();
    }

    public void setNote(Note note) {
        this.note.set(note);
    }

    public SimpleObjectProperty<Note> noteProperty() {
        return this.note;
    }

    public int getVelocity() {
        return this.velocity.get();
    }

    public void setVelocity(int velocity) {
        this.velocity.set(velocity);
    }

    public SimpleIntegerProperty velocityProperty() {
        return this.velocity;
    }

    @Override
    public Property<String> getDisplayProperty() {
        return this.getNote().getDisplayProperty();
    }

    @Override
    public Tone getCopy() {
        var tone = new Tone();
        tone.setNote(this.getNote());
        tone.setVelocity(this.getVelocity());
        return tone;
    }
}
