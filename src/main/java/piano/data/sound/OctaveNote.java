package piano.data.sound;

import java.util.List;

public enum OctaveNote {

    C(0),
    CS(1, true),
    D(2),
    DS(3, true),
    E(4),
    F(5),
    FS(6, true),
    G(7),
    GS(8, true),
    A(9),
    AS(10, true),
    B(11);

    private final int noteIndex;
    private final boolean isSharpNote;

    private OctaveNote(int noteIndex) {
        this(noteIndex, false);
    }

    private OctaveNote(int noteIndex, boolean isSharpNote) {
        this.noteIndex = noteIndex;
        this.isSharpNote = isSharpNote;
    }

    public int getNoteIndex() {
        return this.noteIndex;
    }

    public boolean isSharpNote() {
        return this.isSharpNote;
    }

    public static OctaveNote getOctaveNoteByIndex(int noteIndex) {
        return List.of(OctaveNote.values())
            .stream()
            .filter(octaveNote -> octaveNote.getNoteIndex() == noteIndex)
            .findFirst()
            .orElse(null);
    }

}