package questdesigner;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;

public class QuestDesigner extends Application {
    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(@NotNull Stage primaryStage) throws IOException {
        Scene scene = FXMLLoader.load(QuestDesigner.class.getResource("QuestDesigner.fxml"));
        primaryStage.setScene(scene);
        primaryStage.setTitle("QuestDesigner");
        primaryStage.show();
    }
}
