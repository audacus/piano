package piano.data.sound;

public enum PianoNote {
    
    A0(21,"A0"),
    AS0(22,"A#0/Bb0"),
    B0(23,"B0"),
    C1(24,"C1"),
    CS1(25,"C#1/Db1"),
    D1(26,"D1"),
    DS1(27,"D#1/Eb1"),
    E1(28,"E1"),
    F1(29,"F1"),
    FS1(30,"F#1/Gb1"),
    G1(31,"G1"),
    GS1(32,"G#1/Ab1"),
    A1(33,"A1"),
    AS1(34,"A#1/Bb1"),
    B1(35,"B1"),
    C2(36,"C2"),
    CS2(37,"C#2/Db2"),
    D2(38,"D2"),
    DS2(39,"D#2/Eb2"),
    E2(40,"E2"),
    F2(41,"F2"),
    FS2(42,"F#2/Gb2"),
    G2(43,"G2"),
    GS2(44,"G#2/Ab2"),
    A2(45,"A2"),
    AS2(46,"A#2/Bb2"),
    B2(47,"B2"),
    C3(48,"C3"),
    CS3(49,"C#3/Db3"),
    D3(50,"D3"),
    DS3(51,"D#3/Eb3"),
    E3(52,"E3"),
    F3(53,"F3"),
    FS3(54,"F#3/Gb3"),
    G3(55,"G3"),
    GS3(56,"G#3/Ab3"),
    A3(57,"A3"),
    AS3(58,"A#3/Bb3"),
    B3(59,"B3"),
    C4(60,"C4"),
    CS4(61,"C#4/Db4"),
    D4(62,"D4"),
    DS4(63,"D#4/Eb4"),
    E4(64,"E4"),
    F4(65,"F4"),
    FS4(66,"F#4/Gb4"),
    G4(67,"G4"),
    GS4(68,"G#4/Ab4"),
    A4(69,"A4"),
    AS4(70,"A#4/Bb4"),
    B4(71,"B4"),
    C5(72,"C5"),
    CS5(73,"C#5/Db5"),
    D5(74,"D5"),
    DS5(75,"D#5/Eb5"),
    E5(76,"E5"),
    F5(77,"F5"),
    FS5(78,"F#5/Gb5"),
    G5(79,"G5"),
    GS5(80,"G#5/Ab5"),
    A5(81,"A5"),
    AS5(82,"A#5/Bb5"),
    B5(83,"B5"),
    C6(84,"C6"),
    CS6(85,"C#6/Db6"),
    D6(86,"D6"),
    DS6(87,"D#6/Eb6"),
    E6(88,"E6"),
    F6(89,"F6"),
    FS6(90,"F#6/Gb6"),
    G6(91,"G6"),
    GS6(92,"G#6/Ab6"),
    A6(93,"A6"),
    AS6(94,"A#6/Bb6"),
    B6(95,"B6"),
    C7(96,"C7"),
    CS7(97,"C#7/Db7"),
    D7(98,"D7"),
    DS7(99,"D#7/Eb7"),
    E7(100,"E7"),
    F7(101,"F7"),
    FS7(102,"F#7/Gb7"),
    G7(103,"G7"),
    GS7(104,"G#7/Ab7"),
    A7(105,"A7"),
    AS7(106,"A#7/Bb7"),
    B7(107,"B7"),
    C8(108,"C8")
    // non standard piano notes
    // CS8(109,"C#8/Db8"),
    // D8(110,"D8"),
    // DS8(111,"D#8/Eb8"),
    // E8(112,"E8"),
    // F8(113,"F8"),
    // FS8(114,"F#8/Gb8"),
    // G8(115,"G8"),
    // GS8(116,"G#8/Ab8"),
    // A8(117,"A8"),
    // AS8(118,"A#8/Bb8"),
    // B8(119,"B8"),
    // C9(120,"C9"),
    // CS9(121,"C#9/Db9"),
    // D9(122,"D9"),
    // DS9(123,"D#9/Eb9"),
    // E9(124,"E9"),
    // F9(125,"F9"),
    // FS9(126,"F#9/Gb9"),
    // G9(127,"G9")
    ;
    
    private final int midiNoteNumber;
    private final String noteName;

    private OctaveNote octaveNote;

    private PianoNote(int midiNoteNumber, String noteName) {
        this.midiNoteNumber = midiNoteNumber;
        this.noteName = noteName;
    }

    public int getMidiNoteNumber() {
        return this.midiNoteNumber;
    }

    public String getNoteName() {
        return this.noteName;
    }

    public boolean isSharpNote() {
        return this.getOctaveNote() != null ? this.getOctaveNote().isSharpNote() : false;
    }

    public double getFrequency() {
        return 440.0 * Math.pow(2.0, (this.getMidiNoteNumber() - 69) / 12.0);
    }

    public int getOctave() {
        return Math.floorDiv(this.getMidiNoteNumber() - 12, 12);
    }

    public int getOctaveNoteIndex() {
        return this.getMidiNoteNumber() % 12;
    }

    public OctaveNote getOctaveNote() {
        if (this.octaveNote == null) {
            this.octaveNote = OctaveNote.getOctaveNoteByIndex(this.getOctaveNoteIndex());
        }
        return this.octaveNote;
    }
}
