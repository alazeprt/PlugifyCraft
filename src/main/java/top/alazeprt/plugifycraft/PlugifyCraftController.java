package top.alazeprt.plugifycraft;

import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXCheckBox;
import javafx.application.Platform;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.GridPane;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.web.WebView;
import javafx.stage.DirectoryChooser;
import top.alazeprt.pclib.util.Plugin;
import top.alazeprt.plugifycraft.util.PluginPaneManager;
import top.alazeprt.plugifycraft.util.StarManager;

import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.util.*;

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
    public WebView pluginDesc;
    public Label pluginDownloads;
    public Label pluginUpdate;
    public Label pluginRelease;
    public Label pluginCategory;
    public ChoiceBox<String> starsChoice;
    public TextField downloadPath;
    public JFXButton starButton;

    // stars
    public TextField starSearchField;
    public ChoiceBox<String> starFolders;
    public GridPane starPane;
    
    public PluginPaneManager pluginPaneManager;
    public StarManager starManager;

    public void initialize() throws IOException, ParseException {
        label = new Label();
        pluginPaneManager = new PluginPaneManager(pluginViewPane, pluginIcon, pluginTitle, pluginAuthor, pluginDesc, pluginDownloads, pluginUpdate, pluginRelease, pluginCategory, starsChoice, downloadPath, starButton, label);
        starManager = new StarManager(starPane, label, starFolders, pluginPaneManager);
        List<String> starFolders = new ArrayList<>();
        starFolders.add("默认收藏夹");
        starsChoice.getItems().addAll(starFolders);
        this.starFolders.getItems().addAll(starFolders);
        starsChoice.setValue("默认收藏夹");
        this.starFolders.setValue("默认收藏夹");
        StarManager.load();
        pluginPaneManager.mainPaneManager.add(exploreMain, manageMain, starMain, settingsMain, pluginViewPane);
        label = new Label("从 SpigotMC 获取数据中...");
        label.setFont(Font.font("System", 12));
        label.setTextFill(Color.WHITE);
        label.setLayoutX(870);
        label.setLayoutY(22);
        topPane.getChildren().add(label);
        new Thread(() -> {
            List<Plugin> list;
            try {
                int page = Math.abs(new Random().nextInt()%200)+1;
                list = spigotRepo.fastGetPlugins(15, page);
                List<Plugin> finalList = list;
                Platform.runLater(() -> {
                    explorePane.getChildren().remove(label);
                    explorePane.add(pluginPaneManager.getPluginPane(finalList.get(0)), 0, 0);
                    explorePane.add(pluginPaneManager.getPluginPane(finalList.get(1)), 1, 0);
                    explorePane.add(pluginPaneManager.getPluginPane(finalList.get(2)), 2, 0);
                    explorePane.add(pluginPaneManager.getPluginPane(finalList.get(3)), 0, 1);
                    explorePane.add(pluginPaneManager.getPluginPane(finalList.get(4)), 1, 1);
                    explorePane.add(pluginPaneManager.getPluginPane(finalList.get(5)), 2, 1);
                    explorePane.add(pluginPaneManager.getPluginPane(finalList.get(6)), 0, 2);
                    explorePane.add(pluginPaneManager.getPluginPane(finalList.get(7)), 1, 2);
                    explorePane.add(pluginPaneManager.getPluginPane(finalList.get(8)), 2, 2);
                    explorePane.add(pluginPaneManager.getPluginPane(finalList.get(9)), 0, 3);
                    explorePane.add(pluginPaneManager.getPluginPane(finalList.get(10)), 1, 3);
                    explorePane.add(pluginPaneManager.getPluginPane(finalList.get(11)), 2, 3);
                    addLine(explorePane);
                    explorePane.add(pluginPaneManager.getPluginPane(finalList.get(12)), 0, 4);
                    explorePane.add(pluginPaneManager.getPluginPane(finalList.get(13)), 1, 4);
                    explorePane.add(pluginPaneManager.getPluginPane(finalList.get(14)), 2, 4);
                    label.setVisible(false);
                });
            } catch (IOException e) {
                e.printStackTrace();
                Platform.runLater(() -> label.setText("获取数据失败, 请检查网络连接"));
            }
        }).start();
        searchSpigotMC.setSelected(true);
    }



    public void addLine(GridPane pane) {
        pane.setPrefHeight(pane.getPrefHeight() + 20 + 415/4.0);
        pane.addRow(pane.getRowCount());
    }

    public void onSearch() {
        pluginPaneManager.search(searchSpigotMC.isSelected(), searchHangar.isSelected(), searchField.getText(), explorePane);
    }

    public void onExplorePane() {
        pluginPaneManager.nowViewingPlugin = null;
        pluginPaneManager.requestThreads.forEach(Thread::interrupt);
        pluginPaneManager.mainPaneManager.handleMainPane(exploreMain);
    }

    public void onManagePane() {
        pluginPaneManager.nowViewingPlugin = null;
        pluginPaneManager.requestThreads.forEach(Thread::interrupt);
        pluginPaneManager.mainPaneManager.handleMainPane(manageMain);
    }

    public void onStarPane() {
        pluginPaneManager.nowViewingPlugin = null;
        pluginPaneManager.requestThreads.forEach(Thread::interrupt);
        pluginPaneManager.mainPaneManager.handleMainPane(starMain);
        starPane.getChildren().clear();
        starManager.addAllDataToPane();
    }

    public void onSettingsPane() {
        pluginPaneManager.nowViewingPlugin = null;
        pluginPaneManager.requestThreads.forEach(Thread::interrupt);
        pluginPaneManager.mainPaneManager.handleMainPane(settingsMain);
    }

    public void onPluginDownload() {}

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
}