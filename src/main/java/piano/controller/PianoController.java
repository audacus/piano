package piano.controller;

import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;

import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.beans.binding.Bindings;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.concurrent.Worker;
import javafx.concurrent.Worker.State;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.util.Pair;
import javafx.util.converter.NumberStringConverter;
import piano.data.midi.PianoNote;
import piano.data.midi.OctaveNote;

public class PianoController extends ViewController {

    public static final String PIANO_HTML = "/html/piano.html";
    public static final String PIANO_CSS = "/css/stylesheet.css";
    public static final int MIDI_NOTES = 128;
    public static final int KEYBOARD_SIZE = 18;

    public static final int DEFAULT_OCTAVE = 4;
    public static final int PIANO_MIDI_OCTAVE_OFFSET = 1;
    public static final int OCTAVE_SIZE = 12;
    public static final int OCTAVE_MIN = 0;
    public static final int OCTAVE_MAX = 8;

    private WebEngine webEngine;
    private PianoNote[] midiNotes;
    private SimpleIntegerProperty currentOctave;
    private PianoNote[] currentKeyboardNotes;
    private List<Pair<KeyCode, String>> keyboardButtons;

    @FXML
    private WebView webView;
    @FXML
    private Button buttonOctaveDecrease;
    @FXML
    private Button buttonOctaveIncrease;
    @FXML
    private Button buttonVelocityDecrease;
    @FXML
    private Button buttonVelocityIncrease;
    @FXML
    private Label valueOctave;
    @FXML
    private Label valueVelocity;

    // listeners
    private ChangeListener<Worker.State> webEngineStateChangeListener = new ChangeListener<>(){
        @Override
        public void changed(ObservableValue<? extends State> observable, State oldValue, State newValue) {
            if (newValue == Worker.State.SUCCEEDED) {
                // new page has loaded -> set default octave
                currentOctave.set(DEFAULT_OCTAVE);
            }
        }
    };

    private InvalidationListener currentOctaveChangeListener = new InvalidationListener() {
        @Override
        public void invalidated(Observable observable) {
            buttonOctaveIncrease.setDisable(currentOctave.get() == OCTAVE_MAX);
            buttonOctaveDecrease.setDisable(currentOctave.get() == OCTAVE_MIN);

            updateKeyboard();
        }
    };

    private EventHandler<KeyEvent> keyPressedHandler = new EventHandler<>() {
        @Override
        public void handle(KeyEvent event) {
            
            System.out.println("key: code: " + event.getCode());
        }
    };

    // button actions
    public void onOctaveDecrease() {
        var octaveOld = this.currentOctave.get();
        if (octaveOld > OCTAVE_MIN) this.currentOctave.set(octaveOld - 1);
    }

    public void onOctaveIncrease() {
        var octaveOld = this.currentOctave.get();
        if (octaveOld < OCTAVE_MAX) this.currentOctave.set(octaveOld + 1);
    }

    public void onVelocityDecrease() {

    }

    public void onVelocityIncrease() {

    }

    private void updateKeyboard() {
        this.currentKeyboardNotes = this.getKeyboardNotes(this.currentOctave.get());

        var document = this.webEngine.getDocument();
        var keyboard = document.getElementById("keyboard");
        // remove all piano keys
        keyboard.setTextContent("");

        List<String> cssClasses;
        for (var i = 0; i < this.currentKeyboardNotes.length; i++) {
            var note = this.currentKeyboardNotes[i];

            var isSharp = false;
            var noteName = "";

            // create piano key
            var keyElement = document.createElement("li");
            cssClasses = new ArrayList<>();
            cssClasses.add("key");

            if (note == null) {
                cssClasses.add("disabled");
                // possible to get octave note since i = 0 is always the note C (start of octave)
                var octaveNote = OctaveNote.getOctaveNoteByIndex(i % 12);
                noteName = octaveNote.toString();
                // check if note would be sharp
                isSharp = octaveNote.isSharpNote();
            } else {
                noteName = note.getNoteName();
                isSharp = note.isSharpNote();
            }

            if (isSharp) {
                cssClasses.add("sharp");
            } else {
                // add note name
                var noteElement = document.createElement("div");
                noteElement.setAttribute("class", "note");
                noteElement.setTextContent(noteName);
                keyElement.appendChild(noteElement);
            }

            // add keyboard button
            var keyboardButtonElement = document.createElement("div");
            keyboardButtonElement.setAttribute("class", "button");
            keyboardButtonElement.setTextContent(this.keyboardButtons.get(i).getValue());
            keyElement.appendChild(keyboardButtonElement);
            
            keyElement.setAttribute("class", String.join(" ", cssClasses));
            keyboard.appendChild(keyElement);
        }
    }


    private PianoNote[] getKeyboardNotes(int octave) {
        var keyboardNotes = new PianoNote[KEYBOARD_SIZE];
        var offset = (PIANO_MIDI_OCTAVE_OFFSET + octave) * OCTAVE_SIZE;

        var midiNoteIndex = 0;
        for (var i = 0; i < keyboardNotes.length; i++) {
            midiNoteIndex = i + offset;
            keyboardNotes[i] = this.midiNotes.length > midiNoteIndex ? this.midiNotes[midiNoteIndex] : null;
        }

        return keyboardNotes;
    }

    /**
     * Map given instrument notes to a MIDI note array with 128 entries.
     * The given notes will be mapped to the corresponding MIDI note index in the array.
     * If there is no note given for the index there will be a null entry for that MIDI note.
     * 
     * @param instrumentNotes Notes to be mapped to the MIDI note array
     * @return Given instruments notes mapped to an MIDI note array.
     */
    private PianoNote[] getMappedNotes(PianoNote[] instrumentNotes) {
        var midiNotes = new PianoNote[MIDI_NOTES];
        var instrumentNoteOffset = 0;
        int instrumentNoteIndex;
        for (var i = 0; i < midiNotes.length; i++) {
            instrumentNoteIndex = i - instrumentNoteOffset;
            // check if index is inside instrument notes
            if (instrumentNotes.length > instrumentNoteIndex) {
                var instrumentNote = instrumentNotes[instrumentNoteIndex];
                // check if the instrument note at index has the same index as the "virtual piano"
                if (instrumentNote.getMidiNoteNumber() == i) {
                    midiNotes[i] = instrumentNote;
                    continue;
                }
            }
            instrumentNoteOffset = i + 1;
            midiNotes[i] = null;
        }
        
        return midiNotes;
    }

    // TODO: use Map instead of list
    // TODO: create model for key -> assigned keycode, assigned, note, isPressed, ...
    private HashMap<KeyCode, Pair<Integer, String>> getKeyboardButtons() {
        return new LinkedList<Pair<KeyCode, String>>() {{
            this.add(new Pair<KeyCode, String>(KeyCode.A,           "A"));
            this.add(new Pair<KeyCode, String>(KeyCode.W,           "W"));
            this.add(new Pair<KeyCode, String>(KeyCode.S,           "S"));
            this.add(new Pair<KeyCode, String>(KeyCode.E,           "E"));
            this.add(new Pair<KeyCode, String>(KeyCode.D,           "D"));
            this.add(new Pair<KeyCode, String>(KeyCode.F,           "F"));
            this.add(new Pair<KeyCode, String>(KeyCode.T,           "T"));
            this.add(new Pair<KeyCode, String>(KeyCode.G,           "G"));
            this.add(new Pair<KeyCode, String>(KeyCode.Y,           "Z"));
            this.add(new Pair<KeyCode, String>(KeyCode.H,           "H"));
            this.add(new Pair<KeyCode, String>(KeyCode.U,           "U"));
            this.add(new Pair<KeyCode, String>(KeyCode.J,           "J"));
            this.add(new Pair<KeyCode, String>(KeyCode.K,           "K"));
            this.add(new Pair<KeyCode, String>(KeyCode.O,           "O"));
            this.add(new Pair<KeyCode, String>(KeyCode.L,           "L"));
            this.add(new Pair<KeyCode, String>(KeyCode.P,           "P"));
            this.add(new Pair<KeyCode, String>(KeyCode.SEMICOLON,   "Ö"));
            this.add(new Pair<KeyCode, String>(KeyCode.QUOTE,       "Ä"));
        }};
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // setup keyboard buttons
        this.keyboardButtons = this.getKeyboardButtons();

        // key listner
        this.webView.setOnKeyPressed(this.keyPressedHandler);

        // setup web engine
        this.webEngine = this.webView.getEngine();
        this.webEngine.getLoadWorker().stateProperty().addListener(this.webEngineStateChangeListener);

        // load piano html
        this.webEngine.load(this.getClass().getResource(PIANO_HTML).toString());
        // load piano css
        this.webEngine.setUserStyleSheetLocation(this.getClass().getResource(PIANO_CSS).toString());

        // get available instrument notes
        this.midiNotes = this.getMappedNotes(PianoNote.values());

        // setup current octave
        this.currentOctave = new SimpleIntegerProperty();
        this.currentOctave.addListener(this.currentOctaveChangeListener);

        // bindings
        Bindings.bindBidirectional(this.valueOctave.textProperty(), this.currentOctave, new NumberStringConverter());
    }

    @Override
    public String getViewPath() {
        return "/fxml/PianoView.fxml";
    }

}
