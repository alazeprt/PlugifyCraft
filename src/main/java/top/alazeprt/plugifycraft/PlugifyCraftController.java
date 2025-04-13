package top.alazeprt.plugifycraft;

import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXCheckBox;
import javafx.application.Platform;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.GridPane;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.web.WebView;
import javafx.stage.DirectoryChooser;
import one.jpro.platform.mdfx.MarkdownView;
import top.alazeprt.pclib.util.Plugin;
import top.alazeprt.pclib.util.SpigotPlugin;
import top.alazeprt.plugifycraft.util.*;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.text.ParseException;
import java.time.Duration;
import java.util.*;
import java.util.List;

import static top.alazeprt.plugifycraft.PlugifyCraft.hangarRepo;
import static top.alazeprt.plugifycraft.PlugifyCraft.spigotRepo;

public class PlugifyCraftController {

    public AnchorPane topPane;
    public AnchorPane exploreMain;
    public AnchorPane manageMain;
    public AnchorPane starMain;
    public AnchorPane settingsMain;
    public AnchorPane pluginViewPane;

    // explore
    public GridPane explorePane;

    public JFXCheckBox searchSpigotMC;
    public JFXCheckBox searchHangar;

    public Label label;
    public TextField searchField;

    // plugin view
    public ImageView pluginIcon;
    public Label pluginTitle;
    public Label pluginAuthor;
    public MarkdownView pluginDesc;
    public Label pluginDownloads;
    public Label pluginUpdate;
    public Label pluginRelease;
    public Label pluginCategory;
    public ChoiceBox<String> starsChoice;
    public TextField downloadPath;
    public JFXButton starButton;
    public ChoiceBox<String> versionChoice;

    // stars
    public TextField starSearchField;
    public ChoiceBox<String> starFolders;
    public GridPane starPane;

    // settings
    public Slider threadCount;
    public Label threadCountLabel;
    public Slider pluginCount;
    public Label pluginCountLabel;
    public TextField createStarField;
    public ChoiceBox<String> deleteStarChoiceBox;

    // load data
    public AnchorPane loadDataPane;
    public Label loadDataLabel;

    // manage
    public AnchorPane managePane;
    public GridPane downloadsPane;
    
    public PluginPaneManager pluginPaneManager;
    public StarManager starManager;
    public ManageManager manageManager;

    public void initialize() throws IOException, ParseException {
        label = new Label("");
        label.setFont(Font.font("System", 12));
        label.setTextFill(Color.WHITE);
        label.setLayoutX(870);
        label.setLayoutY(22);
        loadDataLabel.setText("从 SpigotMC 获取数据中...");
        loadDataPane.setVisible(true);
        pluginPaneManager = new PluginPaneManager(pluginViewPane, pluginIcon, pluginTitle, pluginAuthor, pluginDesc, pluginDownloads, pluginUpdate, pluginRelease, pluginCategory, starsChoice, downloadPath, starButton, label, versionChoice, loadDataLabel, loadDataPane, threadCount, pluginCount);
        starManager = new StarManager(starPane, label, starFolders, pluginPaneManager);
        manageManager = new ManageManager(downloadsPane);
        StarManager.load();
        CacheManager.load();
        manageManager.reload();
        SettingsManager.load();
        starsChoice.getItems().addAll(StarManager.starMap.keySet());
        this.starFolders.getItems().addAll(StarManager.starMap.keySet());
        deleteStarChoiceBox.getItems().addAll(StarManager.starMap.keySet());
        starsChoice.setValue("默认收藏夹");
        this.starFolders.setValue("默认收藏夹");
        deleteStarChoiceBox.setValue("默认收藏夹");
        pluginPaneManager.mainPaneManager.add(exploreMain, manageMain, starMain, settingsMain, pluginViewPane);
        topPane.getChildren().add(label);
        if (SettingsManager.get("threadCount") != null) {
            threadCount.setValue(SettingsManager.get("threadCount").getAsInt());
            threadCountLabel.setText(SettingsManager.get("threadCount").getAsString());
        }
        if (SettingsManager.get("pluginCount") != null) {
            pluginCount.setValue(SettingsManager.get("pluginCount").getAsInt());
            pluginCountLabel.setText(SettingsManager.get("pluginCount").getAsString());
        }
        new Thread(() -> {
            List<Plugin> list;
            try {
                int page = Math.abs(new Random().nextInt()%200)+1;
                list = spigotRepo.fastGetPlugins((int) pluginCount.getValue(), page);
                List<Plugin> finalList = list;
                Platform.runLater(() -> {
                    pluginPaneManager.handlePaneList(explorePane, finalList);
                    loadDataPane.setVisible(false);
                });
            } catch (IOException e) {
                e.printStackTrace();
                Platform.runLater(() -> loadDataLabel.setText("获取数据失败, 请检查网络连接"));
            }
        }).start();
        searchSpigotMC.setSelected(true);
        starFolders.setOnAction(event -> {
            starPane.getChildren().clear();
            starManager.addAllDataToPane();
        });
        threadCount.valueProperty().addListener(((observable, oldValue, newValue) -> {
            threadCountLabel.setText(String.valueOf((int) threadCount.getValue()));
            SettingsManager.setInt("threadCount", (int) threadCount.getValue());
        }));
        pluginCount.valueProperty().addListener(((observable, oldValue, newValue) -> {
            pluginCountLabel.setText(String.valueOf((int) pluginCount.getValue()));
            SettingsManager.setInt("pluginCount", (int) pluginCount.getValue());
        }));
    }

    public void onSearch() {
        pluginPaneManager.requestThreads.forEach(Thread::interrupt);
        explorePane.getChildren().clear();
        pluginPaneManager.search(searchSpigotMC.isSelected(), searchHangar.isSelected(), searchField.getText(), explorePane);
    }

    public void onExplorePane() {
        pluginPaneManager.nowViewingPlugin = null;
        pluginPaneManager.mainPaneManager.handleMainPane(exploreMain);
        if (explorePane.getChildren().isEmpty()) {
            loadDataPane.setVisible(true);
            loadDataPane.setLayoutY(247.5);
        } else {
            loadDataPane.setVisible(false);
        }
    }

    public void onManagePane() {
        pluginPaneManager.nowViewingPlugin = null;
        pluginPaneManager.mainPaneManager.handleMainPane(manageMain);
        loadDataPane.setVisible(false);
    }

    public void onStarPane() {
        pluginPaneManager.nowViewingPlugin = null;
        pluginPaneManager.mainPaneManager.handleMainPane(starMain);
        starPane.getChildren().clear();
        starManager.addAllDataToPane();
        loadDataPane.setVisible(false);
    }

    public void onSettingsPane() {
        pluginPaneManager.nowViewingPlugin = null;
        pluginPaneManager.mainPaneManager.handleMainPane(settingsMain);
        loadDataPane.setVisible(false);
    }

    public void onPluginDownload() throws IOException {
        pluginPaneManager.download();
    }

    public void onPluginStar() {
        if (pluginPaneManager.nowViewingPlugin == null) return;
        StarManager.handle(starsChoice.getValue(), pluginPaneManager.nowViewingPlugin);
        if (!StarManager.isStarred(starsChoice.getValue(), pluginPaneManager.nowViewingPlugin)) {
            starButton.setText("收藏");
            starButton.setStyle("-fx-border-color: #339af0; -fx-border-radius: 10px;");
        } else {
            starButton.setText("取消收藏");
            starButton.setStyle("-fx-border-color: #ff6b6b; -fx-border-radius: 10px;");
        }
    }

    public void onChooseDownloadPath() {
        DirectoryChooser directoryChooser = new DirectoryChooser();
        directoryChooser.setTitle("选择下载路径");
        File file = directoryChooser.showDialog(null);
        if (file != null) {
            downloadPath.setText(file.getAbsolutePath());
        }
    }

    public void onSearchInStar() {
        label.setText("搜索中...");
        label.setFont(Font.font("System", 12));
        label.setLayoutX(870);
        label.setLayoutY(22);
        label.setVisible(true);
        starManager.addSearchDataToPane(starSearchField.getText());
        label.setVisible(false);
    }

    public void onCreateStarFolder() {
        if (createStarField.getText().isBlank()) {
            label.setText("收藏夹名不能为空!");
            label.setFont(Font.font("System", 12));
            label.setLayoutX(870);
            label.setLayoutY(22);
            pluginPaneManager.delayedLabel(label, Duration.ofSeconds(2));
        } else if (StarManager.contains(createStarField.getText())) {
            label.setText("收藏夹名已存在!");
            label.setFont(Font.font("System", 12));
            label.setLayoutX(870);
            label.setLayoutY(22);
            pluginPaneManager.delayedLabel(label, Duration.ofSeconds(2));
        } else {
            StarManager.createFolder(createStarField.getText());
            resetStarFolders();
        }
    }

    private void resetStarFolders() {
        starsChoice.getItems().clear();
        starFolders.getItems().clear();
        deleteStarChoiceBox.getItems().clear();
        starsChoice.getItems().addAll(StarManager.starMap.keySet());
        starFolders.getItems().addAll(StarManager.starMap.keySet());
        deleteStarChoiceBox.getItems().addAll(StarManager.starMap.keySet());
        starsChoice.setValue("默认收藏夹");
        starFolders.setValue("默认收藏夹");
        deleteStarChoiceBox.setValue("默认收藏夹");
    }

    public void onDeleteStarFolder() {
        if (deleteStarChoiceBox.getValue().equals("默认收藏夹")) {
            label.setText("默认收藏夹不能删除!");
            label.setFont(Font.font("System", 12));
            label.setLayoutX(870);
            label.setLayoutY(22);
            pluginPaneManager.delayedLabel(label, Duration.ofSeconds(2));
        } else {
            StarManager.starMap.remove(deleteStarChoiceBox.getValue());
            resetStarFolders();
        }
    }

    public void onDownloadsPane() {
        manageManager.reload();
    }

    public void onPluginDownloadAll() throws IOException {
        DirectoryChooser directoryChooser = new DirectoryChooser();
        directoryChooser.setTitle("选择下载路径");
        File file = directoryChooser.showDialog(null);
        if (file != null) {
            List<Plugin> plugins = starManager.getAllData();
            for (Plugin plugin : plugins) {
                int version = -1;
                if (plugin instanceof SpigotPlugin) {
                    Map<String, Integer> versions = spigotRepo.getVersions(plugin.id);
                    for (int s : versions.values()) {
                        version = s;
                        break;
                    }
                } else {
                    Map<String, Integer> versions = hangarRepo.getVersions(plugin.id);
                    for (int s : versions.values()) {
                        version = s;
                        break;
                    }
                }
                if (version == -1) continue;
                CacheManager.download(plugin, version, file, (int) threadCount.getValue());
            }
        }
    }

    public void onOpenGitHub() {
        // Open https://github.com/alazeprt/PlugifyCraft in default browser
        Desktop desktop = Desktop.getDesktop();
        try {
            desktop.browse(new URI("https://github.com/alazeprt/PlugifyCraft"));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}