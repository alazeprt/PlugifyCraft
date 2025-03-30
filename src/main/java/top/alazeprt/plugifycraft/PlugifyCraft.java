package top.alazeprt.plugifycraft;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import top.alazeprt.pclib.repository.HangarRepository;
import top.alazeprt.pclib.repository.SpigotMCRepository;
import top.alazeprt.pclib.util.Plugin;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

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
    }

    public static void main(String[] args) {
        launch();
    }
}