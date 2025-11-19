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
        versionSelector.getItems().addAll("1.20.4", "1.16.5", "1.12.2", "1.21.10", "1.20.1");
        versionSelector.getSelectionModel().selectFirst();
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
        Button downloadButton = new Button("DESCARGAR");
        downloadButton.getStyleClass().add("play-button");
        downloadButton.setOnAction(event -> {
            new Thread(() -> {
               launcherWrapper launcherWrapper = new launcherWrapper();
               launcherWrapper.downloadMinecraftVersion(versionSelector.getValue());
            }).start();
        });
        mainPlayButton.setOnAction(event -> {
            new Thread(() -> {
                launcherWrapper launcherWrapper = new launcherWrapper();
                try {
                    launcherWrapper.startVersion(versionSelector.getValue());
                } catch (IOException e) {
                    throw new RuntimeException(e);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }).start();
        });
        getChildren().addAll(userProfile, bottomSpacer, versionSelector, mainPlayButton, downloadButton);
    }
}