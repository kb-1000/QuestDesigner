package questdesigner;

import javafx.beans.binding.Bindings;
import javafx.beans.binding.ObjectBinding;
import javafx.beans.binding.StringBinding;
import javafx.beans.binding.When;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ObjectPropertyBase;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TreeCell;
import javafx.scene.control.TreeItem;

import java.awt.event.ActionEvent;
import java.io.IOException;

public class CustomTreeView extends javafx.scene.control.TreeView<TextItem> {
    public void setRootValue(TextItem root) {
        setRoot(QuestDesignerController.createTreeItem(root));
    }

    public TextItem getRootValue() {
        return getRoot().getValue();
    }

    {
        setCellFactory(list -> new TreeCell<TextItem>() {
            {
                treeItemProperty().addListener(new ChangeListener<TreeItem<TextItem>>() {
                    @Override
                    public void changed(ObservableValue<? extends TreeItem<TextItem>> observable, TreeItem<TextItem> oldValue, TreeItem<TextItem> newValue) {
                        if (newValue != null) {
                            final ObjectBinding<Object> value = Bindings.select(Bindings.when(treeItemProperty().isNotNull()).<Object>then(Utils.cast(treeItemProperty())).otherwise(Utils.SelectFixer.INSTANCE), "value");
                            final ObjectBinding<String> text = Bindings.select(Bindings.when(value.isNotNull()).then(value).otherwise(Utils.SelectFixer.INSTANCE), "text");
                            textProperty().bind(text);
                            treeItemProperty().removeListener(this);
                        }
                    }
                });
                final MenuItem newAnswerMenuItem = new MenuItem("New Answer");
                newAnswerMenuItem.setOnAction(event -> getTreeItem().getValue().getSubItems().add(new TextItem("New Answer")));
                setContextMenu(new ContextMenu(newAnswerMenuItem));
            }
        });
        setEditable(true);
    }
}
