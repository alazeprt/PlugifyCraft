package top.alazeprt.plugifycraft.util;

import javafx.animation.FadeTransition;
import javafx.scene.layout.AnchorPane;
import javafx.util.Duration;

import java.util.ArrayList;
import java.util.List;

public class PaneManager {

    private List<AnchorPane> panes = new ArrayList<>();

    public PaneManager(AnchorPane... panes) {
        this.panes.addAll(List.of(panes));
    }

    public void handleMainPane(AnchorPane pane) {
        for (AnchorPane p : panes) {
            if (p.isVisible() && p != pane) {
                fadeOutAnchorPane(p, Duration.millis(200));
            }
        }
        fadeInAnchorPane(pane, Duration.millis(200));
    }

    public void fadeOutAnchorPane(AnchorPane pane, Duration duration) {
        FadeTransition fadeTransition = new FadeTransition(duration, pane);
        fadeTransition.setFromValue(1.0);
        fadeTransition.setToValue(0.0);
        fadeTransition.play();
        fadeTransition.setOnFinished(event -> pane.setVisible(false));
    }

    public void fadeInAnchorPane(AnchorPane pane, Duration duration) {
        pane.setVisible(true);
        FadeTransition fadeTransition = new FadeTransition(duration, pane);
        fadeTransition.setFromValue(0.0);
        fadeTransition.setToValue(1.0);
        fadeTransition.play();
    }

    public void add(AnchorPane... pane) {
        panes.addAll(List.of(pane));
    }

    public void remove(AnchorPane... pane) {
        for (AnchorPane p : pane) {
            panes.remove(p);
        }
    }

    public boolean contains(AnchorPane pane) {
        return panes.contains(pane);
    }

    public void clear() {
        panes.clear();
    }
}
