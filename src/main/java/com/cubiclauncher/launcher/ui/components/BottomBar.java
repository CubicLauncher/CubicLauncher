package com.cubiclauncher.launcher.ui.components;

import com.cubiclauncher.launcher.core.SettingsManager;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;

public class BottomBar extends HBox {
    private static final SettingsManager sm = SettingsManager.getInstance();
    private static BottomBar instance;

    private final ProgressBar progressBar;
    private final Label progressLabel;
    private final Label progressText;
    private final Label statusLabel;

    private BottomBar() {
        super(20);
        setPadding(new Insets(12, 30, 12, 30));
        setAlignment(Pos.CENTER_LEFT);
        getStyleClass().add("bottom-bar");

        // --- User profile ---
        HBox userProfile = new HBox(10);
        userProfile.setAlignment(Pos.CENTER_LEFT);
        Circle userAvatar = new Circle(14, Color.web("#4a6bff"));
        Label userName = new Label(sm.getUsername());
        userName.getStyleClass().add("user-profile");
        userProfile.getChildren().addAll(userAvatar, userName);

        // --- Spacers ---
        Region leftSpacer = new Region();
        HBox.setHgrow(leftSpacer, Priority.ALWAYS);
        Region rightSpacer = new Region();
        HBox.setHgrow(rightSpacer, Priority.ALWAYS);

        // --- Progress center ---
        VBox progressCenter = new VBox(5);
        progressCenter.setAlignment(Pos.CENTER);
        HBox.setHgrow(progressCenter, Priority.ALWAYS);

        progressText = new Label("");
        progressText.getStyleClass().add("progress-text");
        progressText.setVisible(false);

        HBox progressContainer = new HBox(10);
        progressContainer.setAlignment(Pos.CENTER);

        progressBar = new ProgressBar(0);
        progressBar.setVisible(false);
        progressBar.setPrefWidth(300);
        progressBar.setPrefHeight(16);
        progressBar.getStyleClass().add("cdark-progress-bar");

        progressLabel = new Label("");
        progressLabel.setVisible(false);
        progressLabel.getStyleClass().add("progress-percent");

        progressContainer.getChildren().addAll(progressBar, progressLabel);
        progressCenter.getChildren().addAll(progressText, progressContainer);

        statusLabel = new Label("Listo");
        statusLabel.getStyleClass().add("status-label");

        getChildren().addAll(userProfile, leftSpacer, progressCenter, rightSpacer, statusLabel);
    }

    public static BottomBar getInstance() {
        if (instance == null) {
            instance = new BottomBar();
        }
        return instance;
    }

    // Getters para que UIBridge pueda actualizar la UI
    public ProgressBar getProgressBar() { return progressBar; }
    public Label getProgressLabel() { return progressLabel; }
    public Label getProgressText() { return progressText; }
    public Label getStatusLabel() { return statusLabel; }
}
