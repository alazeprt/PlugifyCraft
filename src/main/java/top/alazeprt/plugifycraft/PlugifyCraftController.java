package top.alazeprt.plugifycraft;

import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXCheckBox;
import javafx.application.Platform;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.GridPane;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.web.WebView;
import javafx.stage.DirectoryChooser;
import top.alazeprt.pclib.util.Plugin;
import top.alazeprt.pclib.util.SpigotPlugin;
import top.alazeprt.plugifycraft.util.PaneManager;
import top.alazeprt.plugifycraft.util.StarManager;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.CancellationException;

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


    public Map<AnchorPane, Plugin> pluginPanes = new HashMap<>();
    public List<Thread> requestThreads = new ArrayList<>();
    public PaneManager mainPaneManager = new PaneManager();
    public Plugin nowViewingPlugin;

    public void initialize() throws IOException {
        List<String> starFolders = new ArrayList<>();
        starFolders.add("默认收藏夹");
        starsChoice.getItems().addAll(starFolders);
        this.starFolders.getItems().addAll(starFolders);
        starsChoice.setValue("默认收藏夹");
        this.starFolders.setValue("默认收藏夹");
        StarManager.load();
        mainPaneManager.add(exploreMain, manageMain, starMain, settingsMain, pluginViewPane);
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
                    explorePane.add(getPluginPane(finalList.get(0)), 0, 0);
                    explorePane.add(getPluginPane(finalList.get(1)), 1, 0);
                    explorePane.add(getPluginPane(finalList.get(2)), 2, 0);
                    explorePane.add(getPluginPane(finalList.get(3)), 0, 1);
                    explorePane.add(getPluginPane(finalList.get(4)), 1, 1);
                    explorePane.add(getPluginPane(finalList.get(5)), 2, 1);
                    explorePane.add(getPluginPane(finalList.get(6)), 0, 2);
                    explorePane.add(getPluginPane(finalList.get(7)), 1, 2);
                    explorePane.add(getPluginPane(finalList.get(8)), 2, 2);
                    explorePane.add(getPluginPane(finalList.get(9)), 0, 3);
                    explorePane.add(getPluginPane(finalList.get(10)), 1, 3);
                    explorePane.add(getPluginPane(finalList.get(11)), 2, 3);
                    addLine(explorePane);
                    explorePane.add(getPluginPane(finalList.get(12)), 0, 4);
                    explorePane.add(getPluginPane(finalList.get(13)), 1, 4);
                    explorePane.add(getPluginPane(finalList.get(14)), 2, 4);
                    label.setVisible(false);
                });
            } catch (IOException e) {
                e.printStackTrace();
                Platform.runLater(() -> label.setText("获取数据失败, 请检查网络连接"));
            }
        }).start();
        searchSpigotMC.setSelected(true);
    }

    public AnchorPane getPluginPane(Plugin plugin) {
        AnchorPane anchorPane = new AnchorPane();
        pluginPanes.put(anchorPane, plugin);
        anchorPane.setPrefSize(840/3.0, 415/4.0); // 280 * 103.75
        anchorPane.setMaxSize(840/3.0, 415/4.0);
        anchorPane.setStyle("-fx-background-color: #a5d8ff; -fx-border-radius: 10px; -fx-background-radius: 10px;");
        ImageView icon = new ImageView();
        icon.setLayoutX(205);
        icon.setLayoutY(10);
        icon.setFitHeight(65);
        icon.setFitWidth(65);
        icon.setImage(new Image(new ByteArrayInputStream(Base64.getDecoder().decode(plugin.image.getBytes(StandardCharsets.UTF_8)))));
        Label title = new Label(plugin.name);
        title.setTextFill(Color.WHITE);
        title.setFont(Font.font("System Bold", 20));
        title.setLayoutX(5);
        title.setLayoutY(5);
        title.setMaxWidth(200);
        Label description = new Label(plugin.description);
        description.setLayoutX(5);
        description.setLayoutY(35);
        description.setWrapText(true);
        description.setMaxHeight(50);
        description.setMaxWidth(200);
        Label downloads = new Label("下载量: " +
                (plugin.downloads > 1000000 ?
                        (new BigDecimal((plugin.downloads/1000000.0)).setScale(2, RoundingMode.HALF_UP) + "m")
                        : (plugin.downloads > 10000 ?
                        new BigDecimal((plugin.downloads/10000.0)).setScale(2, RoundingMode.HALF_UP) + "w" :
                        plugin.downloads))
        );
        downloads.setMaxWidth(85);
        downloads.setLayoutX(205);
        downloads.setLayoutY(90);
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Label update = new Label("最新更新于: " + format.format(plugin.updateDate));
        update.setLayoutX(5);
        update.setLayoutY(90);
        anchorPane.getChildren().add(title);
        anchorPane.getChildren().add(description);
        anchorPane.getChildren().add(downloads);
        anchorPane.getChildren().add(icon);
        anchorPane.getChildren().add(update);
        anchorPane.setOnMouseClicked((event) -> {
            nowViewingPlugin = plugin;
            requestThreads.forEach(Thread::interrupt);
            mainPaneManager.handleMainPane(pluginViewPane);
            label.setText("正在拉取插件详细信息...");
            label.setFont(Font.font("System", 12));
            label.setLayoutX(870);
            label.setLayoutY(22);
            label.setVisible(true);
            pluginTitle.setText(plugin.name);
            pluginAuthor.setText("作者: " + plugin.author.name);
            pluginCategory.setText("类别: " + plugin.category);
            if (plugin instanceof SpigotPlugin) {
                pluginDesc.getEngine().loadContent("<html><body>" + plugin.description + "</body></html>");
            } else {
                pluginDesc.getEngine().loadContent(plugin.description);
            }
            pluginDownloads.setText("下载量: " + plugin.downloads);
            pluginUpdate.setText("更新于: " + format.format(plugin.updateDate));
            pluginRelease.setText("发布于: " + format.format(plugin.releaseDate));
            if (!StarManager.isStarred(starsChoice.getValue(), nowViewingPlugin)) {
                starButton.setText("收藏");
                starButton.setStyle("-fx-border-color: #339af0; -fx-border-radius: 10px;");
            } else {
                starButton.setText("取消收藏");
                starButton.setStyle("-fx-border-color: #ff6b6b; -fx-border-radius: 10px;");
            }
            pluginIcon.setImage(new Image(new ByteArrayInputStream(Base64.getDecoder().decode(plugin.image.getBytes(StandardCharsets.UTF_8)))));
            Thread thread = new Thread(() -> {
                Plugin plugin1 = pluginPanes.get(anchorPane);
                try {
                    if (plugin1 instanceof SpigotPlugin) {
                        Plugin detail = spigotRepo.getPlugin(plugin1.id);
                        Platform.runLater(() -> {
                            if (detail != null) {
                                pluginDesc.getEngine().loadContent("<html><body>" + detail.description + "</body></html>");
                                pluginCategory.setText("类别: " + detail.category);
                                pluginAuthor.setText("作者: " + detail.author.name);
                            }
                        });
                    }
                    Platform.runLater(() -> label.setText("拉取完成!"));
                    Thread.sleep(2000);
                    Platform.runLater(() -> label.setVisible(false));
                } catch (IOException e) {
                    e.printStackTrace();
                    Platform.runLater(() -> label.setText("获取数据失败, 请检查网络连接"));
                } catch (InterruptedException | CancellationException ignored) {
                    Platform.runLater(() -> label.setVisible(false));
                }
            });
            thread.start();
            requestThreads.add(thread);
        });
        return anchorPane;
    }

    public void addLine(GridPane pane) {
        pane.setPrefHeight(pane.getPrefHeight() + 20 + 415/4.0);
        pane.addRow(pane.getRowCount());
    }

    public void onSearch() {
        if (!searchSpigotMC.isSelected() && !searchHangar.isSelected()) {
            label.setText("你未选择任何数据来源!");
            label.setFont(Font.font("System", 12));
            label.setLayoutX(870);
            label.setLayoutY(22);
            label.setVisible(true);
            new Thread(() -> {
                try {
                    Thread.sleep(2000);
                } catch (InterruptedException ignored) {}
                Platform.runLater(() -> label.setVisible(false));
            }).start();
        } else if (searchSpigotMC.isSelected() && !searchHangar.isSelected()) {
            label.setText("搜索中...");
            label.setFont(Font.font("System", 12));
            label.setLayoutX(870);
            label.setLayoutY(22);
            label.setVisible(true);
            Thread thread = new Thread(() -> {
                List<Plugin> list;
                try {
                    list = spigotRepo.fastSearch(searchField.getText(), 15);
                    List<Plugin> finalList = list;
                    Platform.runLater(() -> {
                        explorePane.getChildren().clear();
                        pluginPanes.clear();
                        int maxRow = 4;
                        int maxCol = 3;
                        int row = 0;
                        int col = 0;
                        for (Plugin plugin : finalList) {
                            explorePane.add(getPluginPane(plugin), col, row);
                            col++;
                            if (col == maxCol) {
                                col = 0;
                                row++;
                            }
                            if (row == maxRow) {
                                break;
                            }
                        }
                        label.setVisible(false);
                    });
                } catch (IOException e) {
                    e.printStackTrace();
                    Platform.runLater(() -> label.setText("获取数据失败, 请检查网络连接"));
                }
            });
            thread.start();
            requestThreads.add(thread);
        } else if (!searchSpigotMC.isSelected() && searchHangar.isSelected()) {
            label.setText("搜索中...");
            label.setFont(Font.font("System", 12));
            label.setLayoutX(870);
            label.setLayoutY(22);
            label.setVisible(true);
            Thread thread = new Thread(() -> {
                List<Plugin> list;
                try {
                    list = hangarRepo.search(searchField.getText(), 15);
                    List<Plugin> finalList = list;
                    Platform.runLater(() -> {
                        explorePane.getChildren().clear();
                        pluginPanes.clear();
                        int maxRow = 4;
                        int maxCol = 3;
                        int row = 0;
                        int col = 0;
                        for (Plugin plugin : finalList) {
                            explorePane.add(getPluginPane(plugin), col, row);
                            col++;
                            if (col == maxCol) {
                                col = 0;
                                row++;
                            }
                            if (row == maxRow) {
                                break;
                            }
                        }
                        label.setVisible(false);
                    });
                } catch (IOException e) {
                    e.printStackTrace();
                    Platform.runLater(() -> label.setText("获取数据失败, 请检查网络连接"));
                }
            });
            thread.start();
            requestThreads.add(thread);
        } else {
            label.setText("选择多个数据源可能会出现重复的插件!");
            label.setFont(Font.font("System", 12));
            label.setLayoutX(820);
            label.setLayoutY(22);
            label.setVisible(true);
            Thread thread = new Thread(() -> {
                try {
                    Thread.sleep(2000);
                } catch (InterruptedException ignored) {}
                Platform.runLater(() -> {
                    label.setText("搜索中...");
                });
                List<Plugin> list;
                try {
                    List<Plugin> spigotList = spigotRepo.fastSearch(searchField.getText(), 9);
                    list = hangarRepo.search(searchField.getText(), 6);
                    list.addAll(spigotList);
                    List<Plugin> finalList = list;
                    Platform.runLater(() -> {
                        explorePane.getChildren().clear();
                        pluginPanes.clear();
                        int maxRow = 4;
                        int maxCol = 3;
                        int row = 0;
                        int col = 0;
                        for (Plugin plugin : finalList) {
                            explorePane.add(getPluginPane(plugin), col, row);
                            col++;
                            if (col == maxCol) {
                                col = 0;
                                row++;
                            }
                            if (row == maxRow) {
                                break;
                            }
                        }
                        label.setVisible(false);
                    });
                } catch (IOException e) {
                    e.printStackTrace();
                    Platform.runLater(() -> label.setText("获取数据失败, 请检查网络连接"));
                }
            });
            thread.start();
            requestThreads.add(thread);
        }
    }

    public void onExplorePane() {
        nowViewingPlugin = null;
        requestThreads.forEach(Thread::interrupt);
        mainPaneManager.handleMainPane(exploreMain);
    }

    public void onManagePane() {
        nowViewingPlugin = null;
        requestThreads.forEach(Thread::interrupt);
        mainPaneManager.handleMainPane(manageMain);
    }

    public void onStarPane() {
        nowViewingPlugin = null;
        requestThreads.forEach(Thread::interrupt);
        mainPaneManager.handleMainPane(starMain);
        if (StarManager.hasFinishedInit) {
            starPane.getChildren().clear();
            List<Plugin> stars = StarManager.getFolder("默认收藏夹");
            int maxCol = 3;
            int maxRow = stars.size() % maxCol == 0 ? stars.size() / maxCol : stars.size() / maxCol + 1;
            if (maxRow > starPane.getRowCount()) {
                for (int i = 1; i <= maxRow - starPane.getRowCount(); i++) {
                    addLine(starPane);
                }
            }
            int col = 0, row = 0;
            for (Plugin plugin : stars) {
                starPane.add(getPluginPane(plugin), col, row);
                col++;
                if (col == maxCol) {
                    col = 0;
                    row++;
                }
            }
        }
    }

    public void onSettingsPane() {
        nowViewingPlugin = null;
        requestThreads.forEach(Thread::interrupt);
        mainPaneManager.handleMainPane(settingsMain);
    }

    public void onPluginDownload() {}
    public void onPluginStar() {
        if (nowViewingPlugin == null) return;
        StarManager.handle(starsChoice.getValue(), nowViewingPlugin);
        if (!StarManager.isStarred(starsChoice.getValue(), nowViewingPlugin)) {
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
    }
}