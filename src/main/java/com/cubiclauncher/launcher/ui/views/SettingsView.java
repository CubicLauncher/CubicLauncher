package com.cubiclauncher.launcher.ui.views;

import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;

public class SettingsView {

    public static VBox create() {
        VBox settingsBox = new VBox(20);
        settingsBox.setAlignment(Pos.CENTER);
        Label settingsTitle = new Label("Ajustes");
        settingsTitle.getStyleClass().add("welcome-title");
        Label settingsSubtitle = new Label("Aquí podrás configurar el launcher.");
        settingsSubtitle.getStyleClass().add("welcome-subtitle");
        settingsBox.getChildren().addAll(settingsTitle, settingsSubtitle);
        return settingsBox;
    }
}