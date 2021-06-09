package piano.controller;

import java.net.URL;
import java.util.ResourceBundle;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.beans.property.SimpleListProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.Slider;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.layout.FlowPane;
import javafx.util.Callback;
import piano.data.PianoNote;
import piano.model.Note;
import piano.model.Tone;

public class MainController extends ViewController {

    private SimpleListProperty<Tone> tones;
    private SimpleObjectProperty<Tone> tone;
    private SimpleObjectProperty<Tone> toneSnapshot;

    @FXML
    private ListView<Tone> listViewTones;
    @FXML
    private FlowPane flowPaneTone;
    @FXML
    private Button buttonCancel;
    @FXML
    private Button buttonDelete;
    @FXML
    private Button buttonNew;
    @FXML
    private Button buttonSave;
    @FXML
    private ComboBox<Note> comboBoxNote;
    @FXML
    private Label valueFrequency;
    @FXML
    private Label valueObject;
    @FXML
    private Slider sliderVelocity;

    // property listeners
    private ChangeListener<Tone> selectedToneChangeListener = new ChangeListener<>() {
        @Override
        public void changed(ObservableValue<? extends Tone> observable, Tone oldValue, Tone newValue) {
            System.out.println("selected tone changed");
            // check if has unsaved changes
            if (
                tone.get() != null &&
                !tone.get().equals(toneSnapshot.get())
            ) {
                // ask if the current changes should be
                var confirmation = new Alert(AlertType.CONFIRMATION);
                var buttonTypeSave = new ButtonType("save");
                var buttonTypeDiscard = new ButtonType("discard changes");
                confirmation.setTitle("warning");
                confirmation.setHeaderText("unsaved changes");
                confirmation.setContentText("do you want to save your current changes?");
                confirmation.getButtonTypes().setAll(buttonTypeSave, buttonTypeDiscard);

                var result = confirmation.showAndWait();
                if (result.get() == buttonTypeSave) {
                    onSave();
                }
                else if (result.get() == buttonTypeDiscard) {
                    onCancel();
                }
            }
            if (newValue != null) {
                setCurrentTone(newValue);
            }
        }
    };

    private ListChangeListener<Tone> tonesChangeListener = new ListChangeListener<>(){
        @Override
        public void onChanged(Change<? extends Tone> c) {
            System.out.println("tones changed");
            buttonDelete.setDisable(tone.get() == null || !tones.contains(tone.get()));
        }
    };

    private ChangeListener<Tone> toneChangeListener = new ChangeListener<>() {
        @Override
        public void changed(ObservableValue<? extends Tone> observable, Tone oldValue, Tone newValue) {
            System.out.println("tone changed");
            flowPaneTone.setDisable(newValue == null);
            buttonDelete.setDisable(newValue == null);
            buttonSave.setDisable(newValue == null || newValue.getNote() == null);

            toneSnapshotChangeListener.changed(toneSnapshot, null, toneSnapshot.get());

            if (newValue != null) {
                valueObject.setText(newValue.toString());
            } else {
                valueObject.setText("null");
            }
        }
    };

    private ChangeListener<Note> noteChangeListener = new ChangeListener<>() {
        @Override
        public void changed(ObservableValue<? extends Note> observable, Note oldValue, Note newValue) {
            System.out.println("note changed");
            if (newValue == null) {
                valueFrequency.setText("-");
            } else {
                valueFrequency.setText(Double.toString(newValue.getFrequency()));
            }
            // if note was changed -> tone was changed
            toneChangeListener.changed(tone, null, tone.get());
        }
    };

    private InvalidationListener velocityChangeListener = new InvalidationListener(){
        @Override
        public void invalidated(Observable observable) {
            System.out.println("velocity changed");
            // if velocity was changed -> tone was changed
            toneChangeListener.changed(tone, null, tone.get());
        }
    };

    private ChangeListener<Tone> toneSnapshotChangeListener = new ChangeListener<>() {
        @Override
        public void changed(ObservableValue<? extends Tone> observable, Tone oldValue, Tone newValue) {
            System.out.println("tone snapshot changed");
            buttonDelete.setDisable(tone.get() == null || !tones.contains(tone.get()));
            buttonCancel.setDisable(
                newValue == null ||
                tone.get() == null ||
                (newValue != null && tone.get() != null && newValue.equals(tone.get()))
            );
        }
    };

    // button actions
    /**
     * load selected tone from tones list.
     */
    public void onLoad() {
        // setup loaded tone
        this.setCurrentTone(this.listViewTones.getSelectionModel().getSelectedItem());
    }

    /**
     * create new tone.
     */
    public void onNew() {
        // setup empty tone
        this.setCurrentTone(new Tone());
    }

    /**
     * delete currently selected tone.
     */
    public void onDelete() {
        // remove tone from list
        this.tones.remove(this.tone.get());
        // set current tone to null
        this.setCurrentTone(this.listViewTones.getSelectionModel().getSelectedItem());
    }

    /**
     * discard recent changes (since last save / load).
     */
    public void onCancel() {
        // load last saved state
        this.loadToneSnapshot();
    }

    /**
     * save current values. adds a new tone if not already exists.
     */
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
        // tones
        this.tones = new SimpleListProperty<>(FXCollections.observableArrayList());
        this.tones.addListener(this.tonesChangeListener);
        // tones list
        this.listViewTones.setCellFactory(new Tone().getCellFactory());
        this.listViewTones.itemsProperty().bindBidirectional(this.tones);
        this.listViewTones.getSelectionModel().selectedItemProperty().addListener(this.selectedToneChangeListener);

        // combo box
        // https://docs.oracle.com/javase/8/javafx/api/javafx/scene/control/ComboBox.html
        Callback<ListView<Note>, ListCell<Note>> cellFactory = new Note().getCellFactory();
        this.comboBoxNote.setItems(this.getNotes());
        this.comboBoxNote.setButtonCell(cellFactory.call(null));
        this.comboBoxNote.setCellFactory(cellFactory);

        // tone
        this.tone = new SimpleObjectProperty<>();
        this.tone.addListener(this.toneChangeListener);

        // tone snapshot
        this.toneSnapshot = new SimpleObjectProperty<>();
        this.toneSnapshot.addListener(this.toneSnapshotChangeListener);

        // trigger listner
        this.selectedToneChangeListener.changed(this.listViewTones.getSelectionModel().selectedItemProperty(), null, this.listViewTones.getSelectionModel().getSelectedItem());
        this.toneChangeListener.changed(this.tone, null, this.tone.get());
        this.toneSnapshotChangeListener.changed(this.toneSnapshot, null, this.toneSnapshot.get());
    }

    /**
     * get observable list of notes from the values of the PianoNote enum.
     *
     * @return observable list of piano notes
     */
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

    /**
     * set up the new currently displayed tone
     * @param tone
     */
    private void setCurrentTone(Tone tone) {
        // clear old bindings (old tone)
        this.unbind();
        // get selected item
        this.tone.set(tone);

        if (tone != null) {
            // add field change listeners to new tone
            this.tone.get().noteProperty().addListener(this.noteChangeListener);
            this.tone.get().velocityProperty().addListener(this.velocityChangeListener);
            // trigger change listener
            this.noteChangeListener.changed(this.tone.get().noteProperty(), null, this.tone.get().getNote());
            this.velocityChangeListener.invalidated(this.tone.get().velocityProperty());
        }

        // create snapshot of new tone
        this.createToneSnapshot();
        // create new bindings (new tone)
        this.bind();
    }

    /**
     * bind bidirectional property values
     */
    private void bind() {
        if (this.tone.get() != null) {
            // note binding
            this.comboBoxNote.valueProperty().bindBidirectional(this.tone.get().noteProperty());
            // velocity binding
            this.sliderVelocity.valueProperty().bindBidirectional(this.tone.get().velocityProperty());
        }
    }

    /**
     * unbind bidirectional property values
     */
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
        if (this.tone.get() != null && this.toneSnapshot.get() != null) {
            this.tone.get().setNote(this.toneSnapshot.get().getNote());
            this.tone.get().setVelocity(this.toneSnapshot.get().getVelocity());
        } else {
            this.tone.set(null);
        }
        this.toneSnapshotChangeListener.changed(this.toneSnapshot, null, this.toneSnapshot.get());
    }
}