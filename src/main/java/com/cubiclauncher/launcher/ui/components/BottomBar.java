/*
 * Copyright (C) 2025 Santiagolxx, Notstaff and CubicLauncher contributors
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package com.cubiclauncher.launcher.ui.components;

import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import com.cubiclauncher.launcher.launcherWrapper;

import java.io.IOException;
import java.util.List;

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

        launcherWrapper launcher = new launcherWrapper();
        List<String> installedVersions = launcher.getInstalledVersions();

        // Selector de versión moderno
        ComboBox<String> versionSelector = new ComboBox<>();
        if (installedVersions.isEmpty()) {
            versionSelector.setPromptText("No hay versiones instaladas");
        } else {
            versionSelector.setItems(FXCollections.observableArrayList(installedVersions));
            versionSelector.getSelectionModel().selectFirst();
        }
        versionSelector.setPrefWidth(220);
        versionSelector.getStyleClass().add("combo-box");
        versionSelector.setButtonCell(new ListCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item);
                }
            }
        });

        // Botón principal de Jugar moderno
        Button mainPlayButton = new Button("JUGAR");
        mainPlayButton.getStyleClass().add("play-button");
        mainPlayButton.setOnAction(event -> {
            String selectedVersion = versionSelector.getValue();
            if (selectedVersion != null && !selectedVersion.isEmpty()) {
                new Thread(() -> {
                    try {
                        launcher.startVersion(selectedVersion);
                    } catch (IOException | InterruptedException e) {
                        e.printStackTrace();
                    }
                }).start();
            }
        });
        getChildren().addAll(userProfile, bottomSpacer, versionSelector, mainPlayButton);
    }
}