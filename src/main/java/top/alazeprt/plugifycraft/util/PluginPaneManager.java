package top.alazeprt.plugifycraft.util;

import com.jfoenix.controls.JFXButton;
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
import top.alazeprt.pclib.util.Plugin;
import top.alazeprt.pclib.util.SpigotPlugin;

import java.beans.Introspector;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.CancellationException;

import static top.alazeprt.plugifycraft.PlugifyCraft.hangarRepo;
import static top.alazeprt.plugifycraft.PlugifyCraft.spigotRepo;

public class PluginPaneManager {
    public AnchorPane pluginViewPane;

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
    public ChoiceBox<String> versionChoice;

    public Label label;

    public Map<AnchorPane, Plugin> pluginPanes = new HashMap<>();
    public List<Thread> requestThreads = new ArrayList<>();
    public PaneManager mainPaneManager = new PaneManager();
    public Plugin nowViewingPlugin;
    public Map<String, Integer> versionMap = new HashMap<>();
    
    public PluginPaneManager(AnchorPane pluginViewPane, ImageView pluginIcon, Label pluginTitle, Label pluginAuthor, WebView pluginDesc, Label pluginDownloads, Label pluginUpdate, Label pluginRelease, Label pluginCategory, ChoiceBox<String> starsChoice, TextField downloadPath, JFXButton starButton, Label label, ChoiceBox<String> versionChoice) {
        this.pluginViewPane = pluginViewPane;
        this.pluginIcon = pluginIcon;
        this.pluginTitle = pluginTitle;
        this.pluginAuthor = pluginAuthor;
        this.pluginDesc = pluginDesc;
        this.pluginDownloads = pluginDownloads;
        this.pluginUpdate = pluginUpdate;
        this.pluginRelease = pluginRelease;
        this.pluginCategory = pluginCategory;
        this.starsChoice = starsChoice;
        this.downloadPath = downloadPath;
        this.starButton = starButton;
        this.label = label;
        this.versionChoice = versionChoice;
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
            versionMap.clear();
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
                        Map<String, Integer> versions = spigotRepo.getVersions(plugin1.id);
                        versionMap.putAll(versions);
                    } else {
                        Map<String, Integer> versions = hangarRepo.getVersions(plugin1.id);
                        versionMap.putAll(versions);
                    }
                    Platform.runLater(() -> {
                        versionChoice.getItems().clear();
                        versionMap.keySet().forEach(version -> {
                            versionChoice.getItems().add(version);
                        });
                        label.setText("拉取完成!");
                    });
                    delayedLabel(label, Duration.ofSeconds(2));
                } catch (IOException e) {
                    e.printStackTrace();
                    Platform.runLater(() -> label.setText("获取数据失败, 请检查网络连接"));
                } catch (CancellationException ignored) {
                    Platform.runLater(() -> label.setVisible(false));
                }
            });
            thread.start();
            requestThreads.add(thread);
        });
        return anchorPane;
    }

    public void download() {
        if (downloadPath.getText().isEmpty()) {
            label.setText("下载路径不能为空!");
            delayedLabel(label, Duration.ofSeconds(2));
            return;
        }
        if (versionChoice.getItems().isEmpty()) {
            label.setText("请选择版本!");
            delayedLabel(label, Duration.ofSeconds(2));
            return;
        }
        label.setText("下载中...");
        label.setFont(Font.font("System", 12));
        label.setLayoutX(870);
        label.setLayoutY(22);
        label.setVisible(true);
        Thread thread = new Thread(() -> {
            // TODO: cache
            try {
                CacheManager.download(nowViewingPlugin, versionMap.get(versionChoice.getValue()), new File(downloadPath.getText()));
                Platform.runLater(() -> label.setText("下载完成!"));
            } catch (IOException e) {
                e.printStackTrace();
                Platform.runLater(() -> label.setText("下载失败, 请检查网络连接!"));
                delayedLabel(label, Duration.ofSeconds(2));
            }
        });
        thread.start();
    }
    
    public void search(boolean searchSpigotMC, boolean searchHangar, String content, GridPane explorePane) {
        if (!searchSpigotMC && !searchHangar) {
            label.setText("你未选择任何数据来源!");
            label.setFont(Font.font("System", 12));
            label.setLayoutX(870);
            label.setLayoutY(22);
            delayedLabel(label, Duration.ofSeconds(2));
        } else if (searchSpigotMC && !searchHangar) {
            label.setText("搜索中...");
            label.setFont(Font.font("System", 12));
            label.setLayoutX(870);
            label.setLayoutY(22);
            label.setVisible(true);
            Thread thread = new Thread(() -> {
                List<Plugin> list;
                try {
                    list = spigotRepo.fastSearch(content, 15);
                    handlePaneList(explorePane, list);
                } catch (IOException e) {
                    e.printStackTrace();
                    Platform.runLater(() -> label.setText("获取数据失败, 请检查网络连接"));
                }
            });
            thread.start();
            requestThreads.add(thread);
        } else if (!searchSpigotMC && searchHangar) {
            label.setText("搜索中...");
            label.setFont(Font.font("System", 12));
            label.setLayoutX(870);
            label.setLayoutY(22);
            label.setVisible(true);
            Thread thread = new Thread(() -> {
                List<Plugin> list;
                try {
                    list = hangarRepo.search(content, 15);
                    handlePaneList(explorePane, list);
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
            delayedLabel(label, Duration.ofSeconds(2));
            Thread thread = new Thread(() -> {
                Platform.runLater(() -> {
                    label.setText("搜索中...");
                });
                List<Plugin> list;
                try {
                    List<Plugin> spigotList = spigotRepo.fastSearch(content, 9);
                    list = hangarRepo.search(content, 6);
                    list.addAll(spigotList);
                    handlePaneList(explorePane, list);
                } catch (IOException e) {
                    e.printStackTrace();
                    Platform.runLater(() -> label.setText("获取数据失败, 请检查网络连接"));
                }
            });
            thread.start();
            requestThreads.add(thread);
        }
    }

    private void handlePaneList(GridPane explorePane, List<Plugin> list) {
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
    }

    public void delayedLabel(Label label, Duration delay) {
        label.setVisible(true);
        new Thread(() -> {
            this.label = label;
            try {
                Thread.sleep(delay.toMillis());
            } catch (InterruptedException ignored) {}
            Platform.runLater(() -> label.setVisible(false));
        }).start();
    }
}
