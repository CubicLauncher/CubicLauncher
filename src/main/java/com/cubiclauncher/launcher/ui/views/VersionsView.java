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

import com.cubiclauncher.launcher.LauncherWrapper;
import com.cubiclauncher.launcher.LauncherWrapper.DownloadCallback;
import com.cubiclauncher.launcher.core.TaskManager;
import com.cubiclauncher.launcher.core.events.EventBus;
import com.cubiclauncher.launcher.core.events.EventType;
import com.cubiclauncher.launcher.ui.components.VersionCell;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.ProgressBar;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

public class VersionsView {
    private static final LauncherWrapper launcher = new LauncherWrapper();
    private static ProgressBar progressBar;
    private static Label statusLabel;
    private static Button downloadButton;
    private static final EventBus eventBus = EventBus.get();

    public static VBox create() {
        VBox box = new VBox(15);
        box.setAlignment(Pos.CENTER);
        box.setPadding(new Insets(10));

        Label title = new Label("Versiones");
        title.getStyleClass().add("welcome-title");

        ListView<String> versionsList = new ListView<>();
        versionsList.setItems(FXCollections.observableArrayList(launcher.getAvailableVersions()));
        versionsList.setCellFactory(lv -> new VersionCell());
        VBox.setVgrow(versionsList, Priority.ALWAYS);

        // Progreso
        progressBar = new ProgressBar(0);
        progressBar.setMaxWidth(Double.MAX_VALUE);
        progressBar.setVisible(false);

        statusLabel = new Label("");
        statusLabel.setVisible(false);

        // Botón
        downloadButton = new Button("Descargar Versión");
        downloadButton.setDisable(true);
        downloadButton.setOnAction(e -> startDownload(versionsList));

        versionsList.getSelectionModel().selectedItemProperty().addListener((obs, old, selected) -> {
            if (selected != null) {
                boolean installed = launcher.getInstalledVersions().contains(selected);
                downloadButton.setDisable(installed);
                downloadButton.setText(installed ? "Ya instalada" : "Descargar Versión");
            }
        });

        box.getChildren().addAll(title, versionsList, progressBar, statusLabel, downloadButton);
        return box;
    }

    private static void startDownload(ListView<String> versionsList) {
        String version = versionsList.getSelectionModel().getSelectedItem();
        if (version == null) return;

        // UI
        progressBar.setProgress(0);
        progressBar.setVisible(true);
        statusLabel.setVisible(true);
        downloadButton.setDisable(true);
        eventBus.subscribe(EventType.DOWNLOAD_STARTED, (eventData -> {
            downloadButton.setText("Downloading " + eventData.getString("version"));
            downloadButton.setDisable(true);
        }));
        eventBus.subscribe(EventType.DOWNLOAD_PROGRESS, (eventData -> progressBar.setProgress(
                calcProgress(eventData.getInt("type"),
                        eventData.getInt("current"),
                        eventData.get("total")))));
        eventBus.subscribe(EventType.DOWNLOAD_COMPLETED, (eventData -> {
            hideProgressAfter();
            statusLabel.setVisible(false);
            downloadButton.setDisable(false);
        }));

        TaskManager.getInstance().runAsync(() -> launcher.downloadMinecraftVersion(version));
    }

    // Calcula progreso global ponderado
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

    private static void hideProgressAfter() {
        new Thread(() -> {
            try {
                Thread.sleep((long) 2000);
            } catch (InterruptedException ignored) {
            }
            Platform.runLater(() -> {
                progressBar.setVisible(false);
                statusLabel.setVisible(false);
            });
        }).start();
    }
}