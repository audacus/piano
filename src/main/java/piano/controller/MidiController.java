package piano.controller;

import javax.sound.midi.Instrument;
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
public class MidiController {

    public static final int PERCUSSION_CHANNEL = 9;
    public static final int DEFAULT_VELOCITY = 96;
    public static final int MIN_VELOCITY = 0;
    public static final int MAX_VELOCITY = 127;

    private static Synthesizer synthesizer;
    private static MidiChannel[] channels;
    private static Instrument[] instruments;
    private static boolean[] channelsInUse;

    public static Synthesizer getSynthesizer() throws MidiUnavailableException {
        if (synthesizer == null) synthesizer = MidiSystem.getSynthesizer();
        if (!synthesizer.isOpen()) synthesizer.open();

        return synthesizer;
    }

    public static Instrument[] getInstruments() throws MidiUnavailableException {
        if (instruments == null) instruments = getSynthesizer().getAvailableInstruments();
        return instruments;
    }
    
    public static MidiChannel[] getChannels() throws MidiUnavailableException {
        if (channels == null) channels = getSynthesizer().getChannels();
        return channels;
    }

    public static MidiChannel getChannel(int channelIndex) throws MidiUnavailableException {
        return getChannels()[channelIndex];
    }

    public static int getFreeChannelIndex() throws MidiUnavailableException {
        if (channelsInUse == null) channelsInUse = new boolean[getChannels().length];
        var freeChannelIndex = -1;
        for (var i = 0; i < channelsInUse.length; i++) {
            // skip percussion channel
            if (i == PERCUSSION_CHANNEL) continue;
            if (!channelsInUse[i]) {
                freeChannelIndex = i;
                channelsInUse[i] = true;
                break;
            }
        }
        return freeChannelIndex;
    }

    public static MidiChannel getPercussionChannel() throws MidiUnavailableException {
        return getChannels()[PERCUSSION_CHANNEL];
    }

    public static void freeChannel(int channelIndex) {
        channelsInUse[channelIndex] = false;
    }
}
