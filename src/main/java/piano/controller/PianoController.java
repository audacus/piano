package piano.controller;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.ResourceBundle;

import javax.sound.midi.Instrument;
import javax.sound.midi.MidiChannel;
import javax.sound.midi.MidiUnavailableException;

import org.w3c.dom.Element;

import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.beans.binding.Bindings;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.concurrent.Worker;
import javafx.concurrent.Worker.State;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import javafx.util.Callback;
import javafx.util.StringConverter;
import javafx.util.converter.NumberStringConverter;
import piano.data.sound.OctaveNote;
import piano.data.sound.PianoNote;
import piano.model.KeyboardButton;
import piano.model.PianoButton;

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
    private MidiChannel midiChannel;
    private PianoNote[] midiNotes;
    private PianoNote[] currentKeyboardNotes;
    private HashMap<KeyCode, PianoButton> pianoButtons;
    private HashMap<KeyCode, KeyboardButton> actionButtons;

    private SimpleBooleanProperty sustain;
    private SimpleIntegerProperty midiChannelIndex;
    private SimpleIntegerProperty currentOctave;
    private SimpleIntegerProperty velocity;

    @FXML
    private WebView webView;
    @FXML
    private Button buttonOpenNew;
    @FXML
    private Button buttonOctaveDecrease;
    @FXML
    private Button buttonOctaveIncrease;
    @FXML
    private Button buttonVelocityDecrease;
    @FXML
    private Button buttonVelocityIncrease;
    @FXML
    private ComboBox<Instrument> comboBoxProgram;
    @FXML
    private Label valueSustain;
    @FXML
    private Label valueMidiChannelIndex;
    @FXML
    private Label valueOctave;
    @FXML
    private Label valueVelocity;

    // converters
    private StringConverter<Boolean> booleanStringConverterOnOff = new StringConverter<>() {
        @Override
        public String toString(Boolean value) {
            return value ? "on" : "off";
        }

        @Override
        public Boolean fromString(String string) {
            if (string.toLowerCase().equals("on")) {
                return true;
            } else if (string.toLowerCase().equals("off")) {
                return false;
            }
            return null;
        }
    };

    // listeners
    private EventHandler<WindowEvent> windowCloseHandler = new EventHandler<>() {
        @Override
        public void handle(WindowEvent event) {
            MidiController.freeChannel(midiChannelIndex.get());
        }
    };

    private InvalidationListener midiChannelChangeListener = new InvalidationListener() {
        @Override
        public void invalidated(Observable observable) {
            try {
                buttonOpenNew.setDisable(!(midiChannelIndex.get() < MidiController.getChannels().length - 1));
            } catch (MidiUnavailableException e) {
                e.printStackTrace();
            }
        }
    };

    private ChangeListener<Instrument> programChangeListener = new ChangeListener<>() {
        @Override
        public void changed(ObservableValue<? extends Instrument> observable, Instrument oldValue, Instrument newValue) {
            midiChannel.programChange(newValue.getPatch().getProgram());
        }
    };

    private ChangeListener<Worker.State> webEngineStateChangeListener = new ChangeListener<>(){
        @Override
        public void changed(ObservableValue<? extends State> observable, State oldValue, State newValue) {
            if (newValue == Worker.State.SUCCEEDED) {
                // new page has loaded -> set default octave
                currentOctave.set(DEFAULT_OCTAVE);
            }
        }
    };

    private ChangeListener<Boolean> sustainChangeListener = new ChangeListener<>(){
        @Override
        public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
            if (newValue) {
                sustainOn();
            } else {
                sustainOff();
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

    private InvalidationListener velocityChangeListener = new InvalidationListener() {
        @Override
        public void invalidated(Observable observable) {
            buttonVelocityDecrease.setDisable(velocity.get() == MidiController.MIN_VELOCITY);
            buttonVelocityIncrease.setDisable(velocity.get() == MidiController.MAX_VELOCITY);
        }
    };

    private EventHandler<KeyEvent> keyPressedHandler = new EventHandler<>() {
        @Override
        public void handle(KeyEvent event) {
            // check if keyboard button is pressed
            var pianoButton = pianoButtons.get(event.getCode());
            if (pianoButton != null && !pianoButton.isPressed()) {
                // press keyboard button if not already pressing
                pressPianoButton(pianoButton);
            }

            // check if action button is pressed
            var actionButton = actionButtons.get(event.getCode());
            if (actionButton != null) {
                pressActionButton(actionButton);
            }
        }
    };

    private EventHandler<KeyEvent> keyReleasedHandler = new EventHandler<>() {
        @Override
        public void handle(KeyEvent event) {
            // check if keyboard button is pressed
            var pianoButton = pianoButtons.get(event.getCode());
            if (pianoButton != null) {
                releasePianoButton(pianoButton);
            }

            // check if action button is pressed
            var actionButton = actionButtons.get(event.getCode());
            if (actionButton != null) {
                releaseActionButton(actionButton);
            }
        }
    };

    // button actions
    public void onOpenNew() {
        try {
            // load view
            var loader = new FXMLLoader();
            loader.setLocation(this.getView());

            // show the scene containing the root layout
            var scene = new Scene(loader.load());
            var stage = new Stage();
            stage.setScene(scene);
            stage.show();

            // set on window close action
            stage.setOnCloseRequest(((ViewController) loader.getController()).getWindowCloseHandler());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void onOctaveDecrease() {
        var octaveOld = this.currentOctave.get();
        if (octaveOld > OCTAVE_MIN) this.currentOctave.set(octaveOld - 1);
    }

    public void onOctaveIncrease() {
        var octaveOld = this.currentOctave.get();
        if (octaveOld < OCTAVE_MAX) this.currentOctave.set(octaveOld + 1);
    }

    public void onVelocityDecrease() {
        this.velocity.set(this.velocity.get() - 1);
    }

    public void onVelocityIncrease() {
        this.velocity.set(this.velocity.get() + 1);
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
            var pianoButtonElement = document.createElement("div");
            pianoButtonElement.setAttribute("class", "button");
            var pianoButton = this.getPianoButtonByNoteIndex(i);
            if (pianoButton != null) pianoButtonElement.setTextContent(pianoButton.getKeyName());
            keyElement.appendChild(pianoButtonElement);
            
            keyElement.setAttribute("class", String.join(" ", cssClasses));
            keyboard.appendChild(keyElement);
        }
    }

    // other methods
    private void sustainOn() {
        this.midiChannel.controlChange(MidiController.CONTROLLER_SUSTAIN, MidiController.SUSTAIN_ON);
    }

    private void sustainOff() {
        this.midiChannel.controlChange(MidiController.CONTROLLER_SUSTAIN, MidiController.SUSTAIN_OFF);
    }
    
    private void pressPianoButton(PianoButton pianoButton) {
        pianoButton.setIsPressed(true);
        var noteIndex = pianoButton.getNoteIndex();
        var pianoNote = currentKeyboardNotes[noteIndex];

        // check if there is a note for this instrument
        if (pianoNote == null) return;

        // play note
        this.midiChannel.noteOn(pianoNote.getMidiNoteNumber(), velocity.get());

        // get key element at index position
        var keyElement = this.getKeyElementByIndex(noteIndex);
        var cssClasses = keyElement.getAttribute("class");

        // check if has pressed class -> add if missing
        if (cssClasses.indexOf("pressed") == -1) cssClasses += " pressed";
        keyElement.setAttribute("class", cssClasses);
    }

    private void releasePianoButton(PianoButton pianoButton) {
        pianoButton.setIsPressed(false);
        var noteIndex = pianoButton.getNoteIndex();
        var pianoNote = currentKeyboardNotes[noteIndex];

        // check if there is a note for this instrument
        if (pianoNote == null) return;

        // silence note
        this.midiChannel.noteOff(pianoNote.getMidiNoteNumber());

        // get key element at index position
        var keyElement = this.getKeyElementByIndex(noteIndex);
        var cssClasses = keyElement.getAttribute("class");


        // check if has pressed class -> remove if present
        if (cssClasses.indexOf("pressed") != -1) cssClasses = cssClasses.replaceAll("pressed", "").trim();
        keyElement.setAttribute("class", cssClasses);
    }

    private void pressActionButton(KeyboardButton keyboardButton) {
        switch (keyboardButton.getKeyCode()) {
            case C:
                this.onVelocityDecrease();
                break;

            case V:
                this.onVelocityIncrease();
                break;

            case Z:
                this.onOctaveDecrease();
                break;

            case X:
                this.onOctaveIncrease();
                break;

            case SHIFT:
                this.sustain.set(true);
                break;

            default:
                // do nothing
                break;

        }
    }

    private void releaseActionButton(KeyboardButton keyboardButton) {
        switch (keyboardButton.getKeyCode()) {
            case SHIFT:
                this.sustain.set(false);
                break;

            default:
                // do nothing
                break;

        }
    }

    private Element getKeyElementByIndex(int index) {
        var document = this.webEngine.getDocument();
        // nth element == nth note
        return (Element) document.getElementById("keyboard").getChildNodes().item(index);
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

    private HashMap<KeyCode, KeyboardButton> getActionButtons() {
        return new HashMap<KeyCode, KeyboardButton>() {{
            // octave
            this.put(KeyCode.Z, new KeyboardButton(KeyCode.Z, "Y", false));
            this.put(KeyCode.X, new KeyboardButton(KeyCode.X, "X", false));
            // velocity
            this.put(KeyCode.C, new KeyboardButton(KeyCode.C, "C", false));
            this.put(KeyCode.V, new KeyboardButton(KeyCode.V, "V", false));
            // sustain
            this.put(KeyCode.SHIFT, new KeyboardButton(KeyCode.SHIFT, "SHIFT", false));
        }};
    }

    private HashMap<KeyCode, PianoButton> getPianoButtons() {
        return new HashMap<KeyCode, PianoButton>() {{
            this.put(KeyCode.A,         new PianoButton(KeyCode.A,          "A", 0, false));
            this.put(KeyCode.W,         new PianoButton(KeyCode.W,          "W", 1, false));
            this.put(KeyCode.S,         new PianoButton(KeyCode.S,          "S", 2, false));
            this.put(KeyCode.E,         new PianoButton(KeyCode.E,          "E", 3, false));
            this.put(KeyCode.D,         new PianoButton(KeyCode.D,          "D", 4, false));
            this.put(KeyCode.F,         new PianoButton(KeyCode.F,          "F", 5, false));
            this.put(KeyCode.T,         new PianoButton(KeyCode.T,          "T", 6, false));
            this.put(KeyCode.G,         new PianoButton(KeyCode.G,          "G", 7, false));
            this.put(KeyCode.Y,         new PianoButton(KeyCode.Y,          "Z", 8, false));
            this.put(KeyCode.H,         new PianoButton(KeyCode.H,          "H", 9, false));
            this.put(KeyCode.U,         new PianoButton(KeyCode.U,          "U", 10, false));
            this.put(KeyCode.J,         new PianoButton(KeyCode.J,          "J", 11, false));
            this.put(KeyCode.K,         new PianoButton(KeyCode.K,          "K", 12, false));
            this.put(KeyCode.O,         new PianoButton(KeyCode.O,          "O", 13, false));
            this.put(KeyCode.L,         new PianoButton(KeyCode.L,          "L", 14, false));
            this.put(KeyCode.P,         new PianoButton(KeyCode.P,          "P", 15, false));
            this.put(KeyCode.SEMICOLON, new PianoButton(KeyCode.SEMICOLON,  "Ö", 16, false));
            this.put(KeyCode.QUOTE,     new PianoButton(KeyCode.QUOTE,      "Ä", 17, false));
        }};
    }

    private PianoButton getPianoButtonByNoteIndex(int noteIndex) {
        return this.pianoButtons.values()
            .stream()
            .filter(k -> k.getNoteIndex() == noteIndex)
            .findFirst()
            .orElse(null);
    }

    private void setupPrograms() throws MidiUnavailableException {
        var instruments = MidiController.getInstruments();
        var cellFactory = new Callback<ListView<Instrument>, ListCell<Instrument>>() {

            @Override
            public ListCell<Instrument> call(ListView<Instrument> param) {
                return new ListCell<Instrument>() {

                    @Override
                    protected void updateItem(Instrument item, boolean empty) {
                        super.updateItem(item, empty);
                        if (empty || item == null) {
                            this.setText(null);
                        } else {
                            this.setText(item.getName());
                        }
                    }
                };
            }
        };

        this.comboBoxProgram.setItems(FXCollections.observableArrayList(instruments));
        this.comboBoxProgram.setButtonCell(cellFactory.call(null));
        this.comboBoxProgram.setCellFactory(cellFactory);
        this.comboBoxProgram.getSelectionModel().selectedItemProperty().addListener(this.programChangeListener);
        this.comboBoxProgram.getSelectionModel().select(instruments[0]);
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // setup midi
        try {
            this.midiChannelIndex = new SimpleIntegerProperty();
            this.midiChannelIndex.addListener(this.midiChannelChangeListener);
            this.midiChannelIndex.set(MidiController.getFreeChannelIndex());

            this.midiChannel = MidiController.getChannel(this.midiChannelIndex.get());

            this.setupPrograms();
        } catch (MidiUnavailableException e) {
            e.printStackTrace();
        }

        // setup keyboard buttons
        this.pianoButtons = this.getPianoButtons();
        // setup action buttons
        this.actionButtons = this.getActionButtons();

        // key listner
        this.webView.getParent().setOnKeyPressed(this.keyPressedHandler);
        this.webView.getParent().setOnKeyReleased(this.keyReleasedHandler);

        // setup web engine
        this.webEngine = this.webView.getEngine();
        this.webEngine.getLoadWorker().stateProperty().addListener(this.webEngineStateChangeListener);

        // load piano html
        this.webEngine.load(this.getClass().getResource(PIANO_HTML).toString());
        // load piano css
        this.webEngine.setUserStyleSheetLocation(this.getClass().getResource(PIANO_CSS).toString());

        // get available instrument notes
        this.midiNotes = this.getMappedNotes(PianoNote.values());

        // setup sustain
        this.sustain = new SimpleBooleanProperty();
        this.sustain.addListener(this.sustainChangeListener);

        // setup current octave
        this.currentOctave = new SimpleIntegerProperty();
        this.currentOctave.addListener(this.currentOctaveChangeListener);

        // setup velocity
        this.velocity = new SimpleIntegerProperty();
        this.velocity.addListener(this.velocityChangeListener);
        this.velocity.set(MidiController.DEFAULT_VELOCITY);

        // bindings
        Bindings.bindBidirectional(this.valueSustain.textProperty(), this.sustain, this.booleanStringConverterOnOff);
        Bindings.bindBidirectional(this.valueMidiChannelIndex.textProperty(), this.midiChannelIndex, new NumberStringConverter());
        Bindings.bindBidirectional(this.valueOctave.textProperty(), this.currentOctave, new NumberStringConverter());
        Bindings.bindBidirectional(this.valueVelocity.textProperty(), this.velocity, new NumberStringConverter());
    }

    @Override
    public String getViewPath() {
        return "/fxml/PianoView.fxml";
    }

    @Override
    public EventHandler<WindowEvent> getWindowCloseHandler() {
        return this.windowCloseHandler;
    }
}
