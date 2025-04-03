package top.alazeprt.plugifycraft;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import top.alazeprt.pclib.repository.HangarRepository;
import top.alazeprt.pclib.repository.SpigotMCRepository;
import top.alazeprt.plugifycraft.util.StarManager;

import java.io.IOException;

public class PlugifyCraft extends Application {

    public static final SpigotMCRepository spigotRepo = new SpigotMCRepository();

    public static final HangarRepository hangarRepo = new HangarRepository();

    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(PlugifyCraft.class.getResource("PlugifyCraft.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 1040, 585);
        stage.setTitle("PlugifyCraft");
        stage.setScene(scene);
        stage.show();
        stage.setOnCloseRequest(event -> {
            try {
                StarManager.save();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
    }

    public static void main(String[] args) {
        launch();
    }
}