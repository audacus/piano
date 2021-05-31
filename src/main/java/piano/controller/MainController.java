package piano.controller;

import java.net.URL;
import java.util.ResourceBundle;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javafx.beans.property.SimpleListProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.Slider;
import javafx.scene.layout.FlowPane;
import javafx.util.Callback;
import piano.data.PianoNote;
import piano.model.Note;
import piano.model.Tone;

public class MainController extends AbstractController {

    private SimpleListProperty<Tone> tones;
    private SimpleObjectProperty<Tone> tone;
    private SimpleObjectProperty<Tone> toneSnapshot;

    @FXML
    private ListView<Tone> listViewTones;
    @FXML
    private FlowPane flowPaneTone;
    @FXML
    private ComboBox<Note> comboBoxNote;
    @FXML
    private Label valueFrequency;
    @FXML
    private Slider sliderVelocity;
    @FXML
    private Button buttonCancel;

    // property listeners
    private ChangeListener<Tone> toneChangeListener = new ChangeListener<Tone>() {
        @Override
        public void changed(ObservableValue<? extends Tone> observable, Tone oldValue, Tone newValue) {
            flowPaneTone.setDisable(newValue == null);
        }
    };

    private ChangeListener<Tone> toneSnapshotChangeListener = new ChangeListener<Tone>() {
        @Override
        public void changed(ObservableValue<? extends Tone> observable, Tone oldValue, Tone newValue) {
            buttonCancel.setDisable(newValue == null);
        }
    };

    private ChangeListener<Note> noteChangeListener = new ChangeListener<Note>() {
        @Override
        public void changed(ObservableValue<? extends Note> observable, Note oldValue, Note newValue) {
            if (newValue == null) {
                valueFrequency.setText("-");
            } else {
                valueFrequency.setText(Double.toString(newValue.getFrequency()));
            }
        }
    };

    // button actions
    public void onNew() {
        // unbind old bindings
        this.unbind();

        // new empty tone
        this.tone.set(null);
        this.tone.set(new Tone());

        // create empty snapshot
        this.createToneSnapshot();

        // note listener
        this.tone.get().noteProperty().addListener(noteChangeListener);
        // create new bindings
        this.bind();
    }

    public void onCancel() {
        // load old snapshot
        this.loadToneSnapshot();
        // create new bindings
        this.bind();
    }

    public void onSave() {
        // save current state
        this.createToneSnapshot();

        // check if tone is already in list
        var index = this.tones.indexOf(this.tone.get());
        if (index == -1) {
            // add if not existing
            this.tones.add(this.tone.get());
        } else {
            // update if existing
            this.tones.set(index, this.tone.get());
        }
    }

    @Override
    public String getViewPath() {
        return "/fxml/MainView.fxml";
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // tones list
        this.tones = new SimpleListProperty<>(FXCollections.observableArrayList());
        this.listViewTones.setCellFactory(new Tone().getCellFactory());
        this.listViewTones.itemsProperty().bindBidirectional(this.tones);

        // tone
        this.tone = new SimpleObjectProperty<>(new Tone());
        this.tone.addListener(this.toneChangeListener);
        // trigger listener
        this.toneChangeListener.changed(this.tone, null, null);

        // snapshot
        this.toneSnapshot = new SimpleObjectProperty<>();
        this.createToneSnapshot();
        this.toneSnapshot.addListener(this.toneSnapshotChangeListener);
        // trigger listener
        this.toneSnapshotChangeListener.changed(this.toneSnapshot, null, null);

        // combo box
        // https://docs.oracle.com/javase/8/javafx/api/javafx/scene/control/ComboBox.html
        Callback<ListView<Note>, ListCell<Note>> cellFactory = new Note().getCellFactory();
        this.comboBoxNote.setItems(this.getNotes());
        this.comboBoxNote.setButtonCell(cellFactory.call(null));
        this.comboBoxNote.setCellFactory(cellFactory);
    }

    private ObservableList<Note> getNotes() {
        // create list of notes from piano notes
        return FXCollections.observableArrayList(
            Stream.of(PianoNote.values()).map(pianoNote -> {
                var note = new Note();
                note.setFrequency(pianoNote.getFrequency());
                note.setName(pianoNote.getName());
                return note;
            }).collect(Collectors.toList()));
    }

    private void bind() {
        if (this.tone.get() != null) {
            // note binding
            this.comboBoxNote.valueProperty().bindBidirectional(this.tone.get().noteProperty());
            // velocity binding
            this.sliderVelocity.valueProperty().bindBidirectional(this.tone.get().velocityProperty());
        }
    }

    private void unbind() {
        if (this.tone.get() != null) {
            // note binding
            this.comboBoxNote.valueProperty().unbindBidirectional(this.tone.get().noteProperty());
            // velocity binding
            this.sliderVelocity.valueProperty().unbindBidirectional(this.tone.get().velocityProperty());
        }
    }

    /**
     * save copy of current tone to snapshot.
     */
    private void createToneSnapshot() {
        if (this.tone.get() != null) {
            this.toneSnapshot.set(this.tone.get().getCopy());
        } else {
            this.toneSnapshot.set(null);
        }
    }

    /**
     * load values from snapshot to current tone.
     */
    private void loadToneSnapshot() {
        if (this.toneSnapshot.get() != null) {
            this.tone.get().setNote(this.toneSnapshot.get().getNote());
            this.tone.get().setVelocity(this.toneSnapshot.get().getVelocity());
        } else {
            this.tone.get().setNote(null);
            this.tone.get().setVelocity(Tone.DEFAULT_VELOCITY);
        }
    }
}
