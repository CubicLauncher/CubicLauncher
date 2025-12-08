package com.cubiclauncher.launcher.ui.components;

import com.cubiclauncher.launcher.core.SettingsManager;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
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

    private final ImageView userAvatar;
    private final Circle fallbackAvatar;
    private final Label userName;
    private final HBox editContainer;
    private final TextField userNameField;

    private boolean isEditing = false;

    private BottomBar() {
        super(20);
        setPadding(new Insets(12, 30, 12, 30));
        setAlignment(Pos.CENTER_LEFT);
        getStyleClass().add("bottom-bar");

        // --- User profile ---
        HBox userProfile = new HBox(10);
        userProfile.setAlignment(Pos.CENTER_LEFT);

        // Avatar
        fallbackAvatar = new Circle(14, Color.web("#4a6bff"));
        userAvatar = new ImageView();
        userAvatar.setFitWidth(28);
        userAvatar.setFitHeight(28);
        Circle clip = new Circle(14, 14, 14);
        userAvatar.setClip(clip);

        StackPane avatarContainer = new StackPane(fallbackAvatar, userAvatar);
        avatarContainer.setPrefSize(28, 28);

        updateAvatar(sm.getUsername());

        userName = new Label(sm.getUsername());
        userName.getStyleClass().add("user-profile");

        // --- Editing components ---
        userNameField = new TextField();
        userNameField.getStyleClass().add("user-profile-field");
        Label cancelButton = new Label("x");
        cancelButton.getStyleClass().add("cancel-edit-button");
        editContainer = new HBox(5, userNameField, cancelButton);
        editContainer.setAlignment(Pos.CENTER_LEFT);
        editContainer.setVisible(false);

        // --- Container to swap between Label and TextField ---
        StackPane userNameContainer = new StackPane(userName, editContainer);
        userNameContainer.setAlignment(Pos.CENTER_LEFT);

        userProfile.getChildren().addAll(avatarContainer, userNameContainer);

        // --- Event Handlers ---
        userName.setOnMouseClicked(event -> {
            if (isEditing) return;
            isEditing = true;
            userNameField.setText(userName.getText());
            editContainer.setVisible(true);
            userName.setVisible(false);
            userNameField.requestFocus();
        });

        cancelButton.setOnMouseClicked(event -> cancelEdit());

        userNameField.focusedProperty().addListener((obs, oldVal, newVal) -> {
            if (!newVal && isEditing) {
                // Lost focus, treat as cancel
                cancelEdit();
            }
        });

        userNameField.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ENTER) {
                updateUsername();
            } else if (event.getCode() == KeyCode.ESCAPE) {
                cancelEdit();
            }
        });

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

    private void updateUsername() {
        if (!isEditing) return;
        String newUsername = userNameField.getText();
        if (newUsername != null && !newUsername.trim().isEmpty()) {
            sm.setUsername(newUsername);
            userName.setText(newUsername);
            updateAvatar(newUsername);
        }
        editContainer.setVisible(false);
        userName.setVisible(true);
        isEditing = false;
    }

    private void cancelEdit() {
        if (!isEditing) return;
        editContainer.setVisible(false);
        userName.setVisible(true);
        isEditing = false;
    }

    private void updateAvatar(String username) {
        fallbackAvatar.setVisible(true);
        userAvatar.setVisible(false);

        String avatarUrl = "https://minotar.net/avatar/" + username + "/28";
        Image avatarImage = new Image(avatarUrl, true); // background loading

        avatarImage.progressProperty().addListener((obs, oldProgress, newProgress) -> {
            if (newProgress.doubleValue() == 1.0) {
                userAvatar.setImage(avatarImage);
                fallbackAvatar.setVisible(false);
                userAvatar.setVisible(true);
            }
        });

        avatarImage.errorProperty().addListener((obs, wasError, isError) -> {
            if (isError) {
                System.err.println("Failed to load avatar for: " + username);
                // Don't print stack trace for common errors like 404
                if (avatarImage.getException() != null && !(avatarImage.getException() instanceof java.io.FileNotFoundException)) {
                    avatarImage.getException().printStackTrace();
                }
                fallbackAvatar.setVisible(true);
                userAvatar.setVisible(false);
            }
        });
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
