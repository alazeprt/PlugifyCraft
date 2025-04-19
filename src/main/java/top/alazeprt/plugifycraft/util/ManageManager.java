package top.alazeprt.plugifycraft.util;

import com.jfoenix.controls.JFXButton;
import javafx.scene.control.Label;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.GridPane;
import javafx.scene.text.Font;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;

import static top.alazeprt.plugifycraft.util.CacheManager.currentDownloadInfo;
import static top.alazeprt.plugifycraft.util.CacheManager.downloadThread;

public class ManageManager {
    public GridPane downloadsPane;

    final Logger logger = LoggerFactory.getLogger(ManageManager.class);

    public ManageManager(GridPane downloadsPane) {
        this.downloadsPane = downloadsPane;
    }

    public void reload() {
        logger.debug("Reloading downloads pane...");
        downloadsPane.getChildren().clear();
        int newSize = CacheManager.completedQueue.size() + CacheManager.downloadQueue.size() +
                (currentDownloadInfo == null ? 0 : 1);
        if (newSize > downloadsPane.getRowCount()) {
            for (int i = 1; i <= newSize - downloadsPane.getRowCount(); i++) addLine();
        }
        downloadsPane.getChildren().clear();
        for (DownloadInfo downloadInfo : CacheManager.completedQueue) {
            downloadsPane.add(getDownloadTaskPane(downloadInfo), 0, downloadsPane.getChildren().size());
        }
        for (DownloadInfo downloadInfo : CacheManager.downloadQueue) {
            downloadsPane.add(getDownloadTaskPane(downloadInfo), 0, downloadsPane.getChildren().size());
        }
        if (currentDownloadInfo != null) {
            downloadsPane.add(getDownloadTaskPane(currentDownloadInfo), 0, downloadsPane.getChildren().size());
        }
    }

    public AnchorPane getDownloadTaskPane(DownloadInfo downloadInfo) {
        AnchorPane anchorPane = new AnchorPane();
        anchorPane.setStyle("-fx-background-color: #a5d8ff; -fx-background-radius: 20px;");
        Label name = new Label(downloadInfo.plugin().name);
        name.setMaxWidth(700);
        name.setLayoutX(14);
        name.setLayoutY(14);
        name.setFont(Font.font("System Bold", 16));
        Label info = new Label("(适用于: " + downloadInfo.platform() + ") 版本: " + downloadInfo.version());
        info.setLayoutX(14);
        info.setLayoutY(54);
        Label status = new Label(CacheManager.getStatus(downloadInfo));
        status.setLayoutX(809);
        status.setLayoutY(33);
        status.setFont(Font.font("System Regular", 14));
        JFXButton cancelBtn = new JFXButton("取消");
        cancelBtn.setLayoutX(730);
        cancelBtn.setLayoutY(54);
        cancelBtn.setPrefWidth(50);
        cancelBtn.setPrefHeight(20);
        cancelBtn.setStyle("-fx-border-color: #ff6b6b; -fx-border-radius: 10px;");
        cancelBtn.setOnAction(e -> {
            if (Objects.equals(currentDownloadInfo, downloadInfo)) {
                CacheManager.currentDownloadInfo = null;
                downloadThread.interrupt();
            } else {
                CacheManager.downloadQueue.remove(downloadInfo);
            }
            reload();
        });
        anchorPane.getChildren().addAll(name, info, status);
        if (!status.getText().equals("已完成")) anchorPane.getChildren().add(cancelBtn);
        return anchorPane;
    }

    public void addLine() {
        downloadsPane.setPrefHeight(downloadsPane.getPrefHeight() + 145);
        downloadsPane.addRow(downloadsPane.getRowCount() - 1);
    }
}
