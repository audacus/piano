package piano.model;

import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;

public class MidiInstrument extends AbstractModel {

    private SimpleIntegerProperty midiProgramNumber;
    private SimpleStringProperty instrumentName;

    public MidiInstrument() {
        this.midiProgramNumber = new SimpleIntegerProperty();
        this.instrumentName = new SimpleStringProperty();
    }

    public int getMidiProgramNumber() {
        return this.midiProgramNumber.get();
    }

    public void setMidiProgramNumber(int midiProgramNumber) {
        this.midiProgramNumber.set(midiProgramNumber);
    }

    public String getInstrumentName() {
        return this.instrumentName.get();
    }

    public void setInstrumentName(String instrumentName) {
        this.instrumentName.set(instrumentName);
    }

    public SimpleIntegerProperty midiProgramNumberProperty() {
        return this.midiProgramNumber;
    }

    public SimpleStringProperty instrumentNameProperty() {
        return this.instrumentName;
    }

    @Override
    public String getDisplayText() {
        return this.getInstrumentName();
    }

    @Override
    public AbstractModel getCopy() {
        var midiInstrument = new MidiInstrument();
        midiInstrument.setMidiProgramNumber(this.getMidiProgramNumber());
        midiInstrument.setInstrumentName(this.getInstrumentName());
        return midiInstrument;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof MidiInstrument) {
            var compare = (MidiInstrument) obj;
            return
                this.getMidiProgramNumber() == compare.getMidiProgramNumber() &&
                this.getInstrumentName() == compare.getInstrumentName();
        }
        return false;
    }

}
