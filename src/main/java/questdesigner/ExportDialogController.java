package questdesigner;

import javafx.beans.property.SimpleObjectProperty;
import javafx.fxml.FXML;
import javafx.scene.control.Dialog;
import javafx.scene.control.DialogEvent;
import javafx.scene.control.TextField;
import javafx.scene.layout.Border;
import javafx.scene.layout.BorderStroke;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.util.StringConverter;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;

public class ExportDialogController {
    @FXML
    private Dialog<ExportJavaSettings> dialog;
    @FXML
    private TextField path;
    @FXML
    private TextField packageName;
    @FXML
    private TextField className;

    private final SimpleObjectProperty<Path> pathProperty = new SimpleObjectProperty<>(this, "pathProperty", Paths.get(".").toRealPath());

    public ExportDialogController() throws IOException {
    }

    @FXML
    private void setResult() {
        dialog.setResult(new ExportJavaSettings(pathProperty.get(), packageName.getText(), className.getText()));
    }

    @FXML
    private void initialize() {
        path.textProperty().bindBidirectional(pathProperty, new StringConverter<Path>() {
            @Override
            public String toString(Path object) {
                return object.toUri().toString();
            }

            @Override
            public Path fromString(String string) {
                try {
                    final Path pathObject = Paths.get(new URI(string));
                    path.setStyle("");
                    return pathObject;
                } catch (URISyntaxException | IllegalArgumentException e) {
                    path.setStyle("-fx-text-box-border: red; -fx-focus-color: red;");
                }
                return Paths.get(".");
            }
        });
    }

    @FXML
    private void browse() {
        final DirectoryChooser directoryChooser = new DirectoryChooser();
        directoryChooser.setInitialDirectory(pathProperty.get().toFile());
        final File file = directoryChooser.showDialog(this.path.getScene().getWindow());
        if (file != null) {
            pathProperty.set(file.toPath());
        }
    }
}
