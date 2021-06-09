package piano.sound;

import javax.sound.midi.MidiChannel;
import javax.sound.midi.MidiSystem;
import javax.sound.midi.MidiUnavailableException;
import javax.sound.midi.Synthesizer;

/**
 * java sound: https://docs.oracle.com/javase/tutorial/sound/index.html
 * sequencers: https://docs.oracle.com/javase/tutorial/sound/MIDI-seq-intro.html
 * stackoverflow simple piano: https://codereview.stackexchange.com/questions/58439/very-basic-java-piano
 * stackoverflow violin tuner: https://codereview.stackexchange.com/questions/57502/using-sounds-in-java/57516#57516
 */
public class Midi {

    private static Synthesizer synthesizer;
    private static MidiChannel[] channels;

    public static Synthesizer getSynthesizer() throws MidiUnavailableException {
        if (synthesizer == null) synthesizer = MidiSystem.getSynthesizer();
        if (!synthesizer.isOpen()) synthesizer.open();
        return synthesizer;
    }

    public static MidiChannel[] getChannels() throws MidiUnavailableException {
        if (channels == null) channels = getSynthesizer().getChannels();
        return channels;
    }
}
