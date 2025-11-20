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
package com.cubiclauncher.launcher.ui.views;

import com.cubiclauncher.launcher.LaucherWrapper;
import com.cubiclauncher.launcher.ui.components.VersionCell;
import com.cubiclauncher.launcher.util.StylesLoader;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.layout.VBox;

public class VersionsView {

    public static VBox create() {
        VBox instancesBox = new VBox(10);
        instancesBox.setAlignment(Pos.CENTER);

        Label title = new Label("Versiones");
        title.getStyleClass().add("welcome-title");

        Label subtitle = new Label("Aquí se mostrará la lista de Versiones que puedes descargar del juego.");
        subtitle.getStyleClass().add("welcome-subtitle");

        LaucherWrapper launcher = new LaucherWrapper();

        ListView<String> versionsList = new ListView<>();
        ObservableList<String> versions = FXCollections.observableArrayList(
                launcher.getAvailableVersions()
        );
        versionsList.setItems(versions);

        versionsList.setCellFactory(listView -> new VersionCell());

        Button downloadButton = getDownloadButton(versionsList, launcher);

        StylesLoader.load(instancesBox, "/com.cubiclauncher.launcher/styles/ui.main.css");

        instancesBox.getChildren().addAll(title, subtitle, versionsList, downloadButton);
        return instancesBox;
    }

    private static Button getDownloadButton(ListView<String> versionsList, LaucherWrapper launcher) {
        Button downloadButton = new Button("Descargar Versión");
        downloadButton.setOnAction(event -> {
            String selectedVersion = versionsList.getSelectionModel().getSelectedItem();
            if (selectedVersion != null && !selectedVersion.isEmpty()) {
                new Thread(() -> launcher.downloadMinecraftVersion(selectedVersion)).start();
            } else {
                System.out.println("Por favor, selecciona una versión para descargar.");
            }
        });
        return downloadButton;
    }
}