/*
 *
 *  * Copyright (C) 2025 Santiagolxx, Notstaff and CubicLauncher contributors
 *  *
 *  * This program is free software: you can redistribute it and/or modify
 *  * it under the terms of the GNU Affero General Public License as published by
 *  * the Free Software Foundation, either version 3 of the License, or
 *  * (at your option) any later version.
 *  *
 *  * This program is distributed in the hope that it will be useful,
 *  * but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  * GNU Affero General Public License for more details.
 *  *
 *  * You should have received a copy of the GNU Affero General Public License
 *  * along with this program.  If not, see https://www.gnu.org/licenses/.
 *
 *
 */
package com.cubiclauncher.launcher.ui.views;

import com.cubiclauncher.launcher.LauncherWrapper;
import com.cubiclauncher.launcher.ui.components.VersionCell;
import com.cubiclauncher.launcher.util.StylesLoader;
import com.cubiclauncher.launcher.util.TaskManager;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Pos;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.layout.VBox;

public class VersionsView {
    private static LauncherWrapper launcher = new LauncherWrapper();
    private static Button downloadButton;

    public static VBox create() {
        VBox instancesBox = new VBox(10);
        instancesBox.setAlignment(Pos.CENTER);

        Label title = new Label("Versiones");
        title.getStyleClass().add("welcome-title");

        Label subtitle = new Label("Aquí se mostrará la lista de Versiones que puedes descargar del juego.");
        subtitle.getStyleClass().add("welcome-subtitle");

        ListView<String> versionsList = new ListView<>();
        ObservableList<String> versions = FXCollections.observableArrayList(
                launcher.getAvailableVersions()
        );
        versionsList.setItems(versions);

        versionsList.setCellFactory(listView -> new VersionCell());

        downloadButton = getDownloadButton(versionsList, launcher);

        // Inicialmente, desactivar el botón si no hay selección
        downloadButton.setDisable(true);

        // Listener para la selección de versión
        versionsList.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null && !newVal.isEmpty()) {
                boolean isInstalled = launcher.getInstalledVersions().contains(newVal);
                downloadButton.setDisable(isInstalled);
            } else {
                downloadButton.setDisable(true);
            }
        });

        instancesBox.getChildren().addAll(title, subtitle, versionsList, downloadButton);
        return instancesBox;
    }

    private static Button getDownloadButton(ListView<String> versionsList, LauncherWrapper launcher) {
        Button button = new Button("Descargar Versión");
        button.setOnAction(event -> {
            String selectedVersion = versionsList.getSelectionModel().getSelectedItem();
            if (selectedVersion != null && !selectedVersion.isEmpty()) {
                button.setDisable(true);
                TaskManager.getInstance().runAsync(
                        () -> launcher.downloadMinecraftVersion(selectedVersion),
                        () -> {
                            // Callback en el hilo de UI
                            versionsList.refresh();
                            button.setDisable(false);
                        },
                        error -> {
                            button.setDisable(false);
                            Alert alert = new Alert(Alert.AlertType.ERROR);
                            alert.setTitle("Error en descarga");
                            alert.setContentText("No se pudo descargar la versión: " + error.getMessage());
                            alert.showAndWait();
                        }
                );
            } else {
                System.out.println("Por favor, selecciona una versión para descargar.");
            }
        });
        return button;
    }

    // Método para actualizar el estado del botón cuando cambien las versiones instaladas
    public static void refreshVersionsList() {
        // Este método puede ser llamado desde otros lugares cuando se instale/desinstale una versión
        if (downloadButton != null) {
            // La lógica de actualización se maneja en el listener de selección
        }
    }
}