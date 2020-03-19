package questdesigner;

import javafx.beans.property.SimpleListProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.TreeItem;

import javax.xml.bind.annotation.XmlElement;
import java.io.Serializable;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class TextItem implements Serializable {
    private SimpleStringProperty response = new SimpleStringProperty(this, "response") {
        @Override
        protected void invalidated() {
            super.invalidated();
            final TreeItem<TextItem> treeItem = TextItem.this.treeItem.get();
            if (treeItem != null) {
                treeItem.valueProperty().set(TextItem.this);
            }
        }
    };
    private SimpleStringProperty text = new SimpleStringProperty(this, "text") {
        @Override
        protected void invalidated() {
            super.invalidated();
            final TreeItem<TextItem> treeItem = TextItem.this.treeItem.get();
            if (treeItem != null) {
                treeItem.valueProperty().set(TextItem.this);
            }
        }
    };
    private SimpleListProperty<TextItem> subItems = new SimpleListProperty<>(this, "subItems", FXCollections.observableArrayList());

    //@Exclude
    public transient WeakReference<TreeItem<TextItem>> treeItem = new WeakReference<>(null);

    public TextItem(String text) {
        this.text.set(text);
    }

    public TextItem() {
    }

    @XmlElement(name = "textItem", type = TextItem.class)
    public ObservableList<TextItem> getSubItems() {
        return subItems.get();
    }

    public SimpleListProperty<TextItem> subItemsProperty() {
        return subItems;
    }

    public void setSubItems(ObservableList<TextItem> subItems) {
        this.subItems.set(subItems);
    }

    public String getResponse() {
        return response.get();
    }

    public SimpleStringProperty responseProperty() {
        return response;
    }

    public void setResponse(String response) {
        this.response.set(response);
    }

    public String getText() {
        return text.get();
    }

    public SimpleStringProperty textProperty() {
        return text;
    }

    public void setText(String text) {
        this.text.set(text);
    }

    @Override
    public String toString() {
        return getResponse() != null ? getResponse() + " -> " + getText() : String.valueOf(getText());
    }

    private Object writeReplace() {
        return new SerializedTextItem(getResponse(), subItemsProperty(), getText());
    }

    private static final class SerializedTextItem implements Serializable {
        private final String response;
        private final List<TextItem> subItems;
        private final String text;

        public SerializedTextItem(String response, List<TextItem> subItems, String text) {
            this.response = response;
            this.subItems = new ArrayList<>(subItems);
            this.text = text;
        }

        private Object readResolve() {
            final TextItem textItem = new TextItem(text);
            textItem.setResponse(response);
            textItem.setSubItems(FXCollections.observableArrayList(subItems));
            return textItem;
        }
    }
}
