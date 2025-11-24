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
package com.cubiclauncher.launcher.ui.components;

import com.cubiclauncher.launcher.core.LauncherWrapper;
import com.cubiclauncher.launcher.core.LauncherWrapper.DownloadCallback;
import com.cubiclauncher.launcher.core.InstanceManager;
import com.cubiclauncher.launcher.core.SettingsManager;
import com.cubiclauncher.launcher.core.TaskManager;
import com.cubiclauncher.launcher.core.events.EventBus;
import com.cubiclauncher.launcher.core.events.EventType;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;

import java.util.List;
import java.util.stream.Collectors;

import static com.cubiclauncher.launcher.core.InstanceManager.getInstance;

public class BottomBar extends HBox {
    private static final SettingsManager sm = SettingsManager.getInstance();
    private final ComboBox<String> versionSelector;
    private final ProgressBar progressBar;
    private final Label progressLabel;
    private final LauncherWrapper launcher = LauncherWrapper.getInstance();
    private static final EventBus eventBus = EventBus.get();

    public BottomBar() {
        super(20);
        setPadding(new Insets(20, 30, 20, 30));
        setAlignment(Pos.CENTER_LEFT);
        getStyleClass().add("bottom-bar");

        // Perfil de usuario con avatar
        HBox userProfile = new HBox(12);
        userProfile.setAlignment(Pos.CENTER_LEFT);

        Circle userAvatar = new Circle(18, Color.web("#4a6bff"));
        Label userName = new Label(sm.getUsername());
        userName.getStyleClass().add("user-profile");

        userProfile.getChildren().addAll(userAvatar, userName);
        userProfile.setOnMouseClicked(event -> {
            TextInputDialog dialog = new TextInputDialog(sm.getUsername());
            dialog.setTitle("Cambiar nombre de usuario");
            dialog.setHeaderText(null);
            dialog.setContentText("Nuevo nombre:");

            dialog.showAndWait().ifPresent(newName -> {
                if (!newName.trim().isEmpty()) {
                    sm.setUsername(newName.trim());
                    userName.setText(newName);
                }
            });
        });

        // Espaciador izquierdo
        Region leftSpacer = new Region();
        HBox.setHgrow(leftSpacer, Priority.ALWAYS);

        // Contenedor centrado para ProgressBar
        StackPane centerContainer = new StackPane();
        centerContainer.setAlignment(Pos.CENTER);
        HBox.setHgrow(centerContainer, Priority.ALWAYS);

        // ProgressBar y label centrados
        HBox progressContainer = new HBox(10);
        progressContainer.setAlignment(Pos.CENTER);

        progressBar = new ProgressBar(0);
        progressBar.setVisible(false);
        progressBar.setPrefWidth(600);
        progressBar.setPrefHeight(16);

        progressLabel = new Label("");
        progressLabel.setVisible(false);
        progressLabel.getStyleClass().add("progress-label");

        progressContainer.getChildren().addAll(progressBar, progressLabel);
        centerContainer.getChildren().add(progressContainer);

        // Espaciador derecho
        Region rightSpacer = new Region();
        HBox.setHgrow(rightSpacer, Priority.ALWAYS);

        // Selector de versión moderno
        versionSelector = new ComboBox<>();
        updateInstalledVersions(); // Carga inicial
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
        mainPlayButton.setOnAction(_ -> {
            String selectedVersion = versionSelector.getValue();
            if (selectedVersion != null && !selectedVersion.isEmpty()) {
                TaskManager.getInstance().runAsync(
                        () -> InstanceManager.getInstance().startInstance(selectedVersion)
                );
            }
        });
        // En BottomBar.java, alrededor de la línea 120
        eventBus.subscribe(EventType.DOWNLOAD_PROGRESS, (eventData -> {
            Platform.runLater(() -> {
                progressBar.setVisible(true);

                int current = eventData.getInt("current");
                int total = eventData.getInt("total");
                int type = eventData.getInt("type");

                progressLabel.setText(current + "/" + total);
                progressBar.setProgress(calcProgress(type, current, total));
            });
        }));

        eventBus.subscribe(EventType.DOWNLOAD_COMPLETED, (eventData -> {
            Platform.runLater(() -> {
                progressBar.setVisible(false);
                progressLabel.setText("Version " + eventData.getString("version") + " ha sido descargada");
                progressBar.setProgress(0);
            });
        }));
        eventBus.subscribe(EventType.INSTANCE_VERSION_NOT_INSTALLED, (eventData -> {
            Platform.runLater(() -> {
                progressBar.setVisible(true);
                progressLabel.setVisible(true);
                progressLabel.setText("Descargando versión requerida...");
                progressBar.setProgress(ProgressBar.INDETERMINATE_PROGRESS);
            });
        }));
        eventBus.subscribe(EventType.INSTANCE_CREATED, eventData -> Platform.runLater(this::updateInstalledVersions));
        getChildren().addAll(userProfile, leftSpacer, centerContainer, rightSpacer, versionSelector, mainPlayButton);
    }

    public void updateInstalledVersions() {
        List<String> installedVersions = InstanceManager.getInstance().getAllInstances().stream()
                .map(InstanceManager.Instance::getName)
                .collect(Collectors.toList());

        String currentlySelected = versionSelector.getValue();

        if (installedVersions.isEmpty()) {
            versionSelector.setPromptText("No hay instancias disponibles");
            versionSelector.setItems(FXCollections.observableArrayList());
        } else {
            versionSelector.setItems(FXCollections.observableArrayList(installedVersions));

            if (currentlySelected != null && installedVersions.contains(currentlySelected)) {
                versionSelector.getSelectionModel().select(currentlySelected);
            } else {
                versionSelector.getSelectionModel().selectFirst();
            }
        }
    }

    private static double calcProgress(int type, int current, int total) {
        if (total == 0) return 0;
        double p = (double) current / total;
        return switch (type) {
            case DownloadCallback.TYPE_CLIENT -> p * 0.05;
            case DownloadCallback.TYPE_LIBRARY -> 0.05 + p * 0.15;
            case DownloadCallback.TYPE_ASSET -> 0.20 + p * 0.75;
            case DownloadCallback.TYPE_NATIVE -> 0.95 + p * 0.05;
            default -> p;
        };
    }
}