package piano;

import javax.sound.midi.MidiUnavailableException;

import piano.sound.Midi;

public class Piano {

	public static void main(String[] args) {
		try {
			Midi.getChannels();
		} catch (MidiUnavailableException e) {
			e.printStackTrace();
		}

		PianoApp.main(args);
	}
}