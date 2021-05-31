package piano.model;

import javafx.beans.property.Property;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.util.Callback;

public abstract class AbstractModel {

    public abstract Property<?> getDisplayProperty();

    public abstract AbstractModel getCopy();

    public <T extends AbstractModel> Callback<ListView<T>, ListCell<T>> getCellFactory() {
        return new Callback<ListView<T>,ListCell<T>>(){

            @Override
            public ListCell<T> call(ListView<T> param) {
                return new ListCell<T>() {

                    @Override
                    protected void updateItem(T item, boolean empty) {
                        super.updateItem(item, empty);
                        if (empty || item == null) {
                            this.setText(null);
                        } else {
                            this.setText(String.valueOf(item.getDisplayProperty().getValue()));
                        }
                    }
                };
            }
        };
    }
}
