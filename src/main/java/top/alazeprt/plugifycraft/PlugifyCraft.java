package top.alazeprt.plugifycraft;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import top.alazeprt.pclib.repository.HangarRepository;
import top.alazeprt.pclib.repository.SpigotMCRepository;
import top.alazeprt.plugifycraft.util.CacheManager;
import top.alazeprt.plugifycraft.util.PluginPaneManager;
import top.alazeprt.plugifycraft.util.SettingsManager;
import top.alazeprt.plugifycraft.util.StarManager;

import java.io.IOException;

public class PlugifyCraft extends Application {

    public static final SpigotMCRepository spigotRepo = new SpigotMCRepository();

    public static final HangarRepository hangarRepo = new HangarRepository();

    final Logger logger = LoggerFactory.getLogger(PlugifyCraft.class);

    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(PlugifyCraft.class.getResource("PlugifyCraft.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 1040, 585);
        stage.setTitle("PlugifyCraft");
        stage.getIcons().add(new Image(PlugifyCraft.class.getResourceAsStream("icon.png")));
        stage.setScene(scene);
        stage.show();
        stage.setOnCloseRequest(event -> {
            try {
                logger.info("Closing application and saving data...");
                StarManager.save();
                CacheManager.save();
                SettingsManager.save();
                PluginPaneManager.requestThreads.forEach(Thread::interrupt);
                System.exit(0);
            } catch (IOException e) {
                logger.error("Failed to save data when exiting", e);
            }
        });
        stage.setResizable(false);
    }

    public static void main(String[] args) {
        launch();
    }
}