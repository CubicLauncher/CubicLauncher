package com.cubiclauncher.launcher.ui.components;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;

public class BottomBar extends HBox {

    public BottomBar() {
        super(20);
        setPadding(new Insets(20, 30, 20, 30));
        setAlignment(Pos.CENTER_LEFT);
        getStyleClass().add("bottom-bar");

        // Perfil de usuario con avatar
        HBox userProfile = new HBox(12);
        userProfile.setAlignment(Pos.CENTER_LEFT);

        Circle userAvatar = new Circle(18, Color.web("#4a6bff"));
        Label userName = new Label("Steve");
        userName.getStyleClass().add("user-profile");

        userProfile.getChildren().addAll(userAvatar, userName);

        // Espaciador
        Region bottomSpacer = new Region();
        HBox.setHgrow(bottomSpacer, Priority.ALWAYS);

        // Selector de versión moderno
        ComboBox<String> versionSelector = new ComboBox<>();
        versionSelector.getItems().addAll("1.20.4 - Forge", "1.20.4 - Vanilla", "1.19.2 - OptiFine");
        versionSelector.getSelectionModel().selectFirst();
        versionSelector.setPrefWidth(220);
        versionSelector.getStyleClass().add("combo-box");

        // Botón principal de Jugar moderno
        Button mainPlayButton = new Button("▶ JUGAR");
        mainPlayButton.getStyleClass().add("play-button");

        getChildren().addAll(userProfile, bottomSpacer, versionSelector, mainPlayButton);
    }
}