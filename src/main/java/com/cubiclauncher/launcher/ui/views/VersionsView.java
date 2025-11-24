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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see https://www.gnu.org/licenses/.
 */
package com.cubiclauncher.launcher.ui.views;

import com.cubiclauncher.launcher.core.LauncherWrapper;
import com.cubiclauncher.launcher.core.InstanceManager;
import com.cubiclauncher.launcher.core.TaskManager;
import com.cubiclauncher.launcher.ui.components.VersionCell;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

public class VersionsView {
    private static final LauncherWrapper launcher = LauncherWrapper.getInstance();
    private static final InstanceManager instanceManager = InstanceManager.getInstance();

    public static VBox create() {
        VBox box = new VBox(15);
        box.setAlignment(Pos.CENTER);
        box.setPadding(new Insets(10));

        Label title = new Label("Versiones Disponibles");
        title.getStyleClass().add("welcome-title");

        ListView<String> versionsList = new ListView<>();
        versionsList.setItems(FXCollections.observableArrayList(launcher.getAvailableVersions()));
        versionsList.setCellFactory(lv -> new VersionCell());
        VBox.setVgrow(versionsList, Priority.ALWAYS);

        // Campo para el nombre de la instancia
        Label nameLabel = new Label("Nombre de la instancia:");
        TextField nameField = new TextField();
        nameField.setPromptText("Ingresa un nombre único para la instancia");

        // Botón para crear instancia
        Button createInstanceButton = new Button("Crear Instancia");
        createInstanceButton.setDisable(true);

        // Etiqueta de estado
        Label statusLabel = new Label("");
        statusLabel.setVisible(false);

        // Habilitar el botón solo cuando haya una versión seleccionada y el nombre no esté vacío
        versionsList.getSelectionModel().selectedItemProperty().addListener((obs, old, selected) -> updateCreateButton(createInstanceButton, selected, nameField.getText()));

        nameField.textProperty().addListener((obs, old, newValue) -> updateCreateButton(createInstanceButton, versionsList.getSelectionModel().getSelectedItem(), newValue));

        createInstanceButton.setOnAction(e -> {
            String version = versionsList.getSelectionModel().getSelectedItem();
            String instanceName = nameField.getText().trim();
            createInstance(version, instanceName, statusLabel);
        });

        Button refreshButton = getButton(statusLabel, versionsList);

        box.getChildren().addAll(title, versionsList, nameLabel, nameField, createInstanceButton, statusLabel, refreshButton);
        return box;
    }

    private static Button getButton(Label statusLabel, ListView<String> versionsList) {
        Button refreshButton = new Button("Actualizar Lista");
        refreshButton.setOnAction(e -> {
            refreshButton.setDisable(true);
            refreshButton.setText("Cargando...");
            statusLabel.setText("Cargando versiones disponibles...");
            statusLabel.setStyle("-fx-text-fill: #2196F3;");
            statusLabel.setVisible(true);

            TaskManager.getInstance().runAsync(launcher::getAvailableVersions)
                    .thenAccept(result -> Platform.runLater(() -> {
                        versionsList.setItems(FXCollections.observableArrayList(result));
                        statusLabel.setVisible(false);
                        refreshButton.setDisable(false);
                        refreshButton.setText("Actualizar Lista");
                    }))
                    .exceptionally(error -> {
                        Platform.runLater(() -> {
                            showStatus(statusLabel, "Error al cargar versiones: " + error.getMessage(), false);
                            refreshButton.setDisable(false);
                            refreshButton.setText("Actualizar Lista");
                        });
                        return null;
                    });
        });
        return refreshButton;
    }

    private static void updateCreateButton(Button button, String selectedVersion, String instanceName) {
        boolean disabled = selectedVersion == null || instanceName.trim().isEmpty();
        button.setDisable(disabled);
    }

    private static void createInstance(String version, String instanceName, Label statusLabel) {
        if (instanceName == null || instanceName.trim().isEmpty()) {
            showStatus(statusLabel, "Error: El nombre de la instancia no puede estar vacío", false);
            return;
        }

        if (instanceManager.instanceExists(instanceName)) {
            showStatus(statusLabel, "Error: Ya existe una instancia con el nombre '" + instanceName + "'", false);
            return;
        }

        TaskManager.getInstance().runAsync(
                () -> instanceManager.createInstance(instanceName, version),
                () -> showStatus(statusLabel, "Instancia creada exitosamente: " + instanceName, true),
                () -> showStatus(statusLabel, "Error creando instancia", false)
        );
    }

    private static void showStatus(Label statusLabel, String message, boolean isSuccess) {
        statusLabel.setText(message);
        statusLabel.setStyle(isSuccess ?
                "-fx-text-fill: #4CAF50;" :
                "-fx-text-fill: #F44336;");
        statusLabel.setVisible(true);

        // En VersionsView, método showStatus
        if (isSuccess) {
            TaskManager.getInstance().runAsync(
                    () -> {
                        try {
                            Thread.sleep(3000);
                        } catch (InterruptedException ignored) {}
                    },
                    () -> statusLabel.setVisible(false),
                    e -> {} // No hacer nada si falla
            );
        }
    }
}