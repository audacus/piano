package piano.model;

import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleStringProperty;

public class Note extends AbstractModel {

    private SimpleDoubleProperty frequency;
    private SimpleStringProperty name;

    public Note() {
        this.frequency = new SimpleDoubleProperty();
        this.name = new SimpleStringProperty();
    }

    public double getFrequency() {
        return this.frequency.get();
    }

    public void setFrequency(double frequency) {
        this.frequency.set(frequency);
    }

    public SimpleDoubleProperty frequencyProperty() {
        return this.frequency;
    }

    public String getName() {
        return this.name.get();
    }

    public void setName(String name) {
        this.name.set(name);
    }

    public SimpleStringProperty nameProperty() {
        return this.name;
    }

    @Override
    public String getDisplayText() {
        return this.getName();
    }

    @Override
    public Note getCopy() {
        var note = new Note();
        note.setFrequency(this.getFrequency());
        note.setName(this.getName());
        return note;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Note) {
            var compare = (Note) obj;
            return
                this.getFrequency() == compare.getFrequency() &&
                this.getName() == compare.getName();
        }
        return false;
    }
}
