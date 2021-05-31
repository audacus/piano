package piano.data;

import java.util.stream.Stream;

public enum PianoNote {

    A_0(1, "A0"),
    Bb0(2, "A#0/Bb0"),
    B0(3, "B0"),
    C1(4, "C1"),
    Db1(5, "C#1/Db1"),
    D1(6, "D1");

    private final int keyNumber;
    private final String name;

    private PianoNote(int keyNumber, String name) {
        this.keyNumber = keyNumber;
        this.name = name;
    }

    public int getKeyNumber() {
        return this.keyNumber;
    }

    public String getName() {
        return this.name;
    }

    public double getFrequency() {
        return Math.pow(2.0, (this.keyNumber - 49.0) / 12.0) * 440.0;
    }

    public PianoNote getNoteFromFrequency(double frequency) {
        return Stream.of(PianoNote.values())
            .filter(note -> note.getFrequency() == frequency)
            .findFirst()
            .orElse(null);
    }
}
