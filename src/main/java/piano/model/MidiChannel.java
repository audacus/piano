package piano.model;

import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;

public class MidiChannel extends AbstractModel {

    private SimpleIntegerProperty channelNumber;
    private SimpleObjectProperty<MidiInstrument> instrument;

    public MidiChannel() {
        this.channelNumber = new SimpleIntegerProperty();
        this.instrument = new SimpleObjectProperty<>();
    }

    public int getChannelNumber() {
        return this.channelNumber.get();
    }

    public void setChannelNumber(int channelNumber) {
        this.channelNumber.set(channelNumber);
    }

    public MidiInstrument getInstrument() {
        return this.instrument.get();
    }

    public void setInstrument(MidiInstrument instrument) {
        this.instrument.set(instrument);
    }

    public SimpleIntegerProperty channelNumberProperty() {
        return this.channelNumber;
    }

    public SimpleObjectProperty<MidiInstrument> instrumentProperty() {
        return this.instrument;
    }

    @Override
    public String getDisplayText() {
        return this.getChannelNumber() + " - " + this.getInstrument().getDisplayText();
    }

    @Override
    public AbstractModel getCopy() {
        var midiChannel = new MidiChannel();
        midiChannel.setInstrument(this.getInstrument());
        return midiChannel;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof MidiChannel) {
            var compare = (MidiChannel) obj;
            return this.getInstrument() == compare.getInstrument();
        }
        return false;
    }
}
