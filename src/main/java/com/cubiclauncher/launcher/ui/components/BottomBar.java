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
import com.cubiclauncher.launcher.core.SettingsManager;
import com.cubiclauncher.launcher.core.TaskManager;
import com.cubiclauncher.launcher.core.events.EventBus;
import com.cubiclauncher.launcher.core.events.EventType;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;

public class BottomBar extends HBox {
    private static final SettingsManager sm = SettingsManager.getInstance();
    private static final EventBus eventBus = EventBus.get();
    private final ProgressBar progressBar;
    private final Label progressLabel;
    private final Label progressText;
    private final Label statusLabel;
    private final LauncherWrapper launcher = LauncherWrapper.getInstance();

    public BottomBar() {
        super(20);
        setPadding(new Insets(12, 30, 12, 30));
        setAlignment(Pos.CENTER_LEFT);
        getStyleClass().add("bottom-bar");

        HBox userProfile = new HBox(10);
        userProfile.setAlignment(Pos.CENTER_LEFT);

        Circle userAvatar = new Circle(14, Color.web("#4a6bff"));
        Label userName = new Label(sm.getUsername());
        userName.getStyleClass().add("user-profile");

        userProfile.getChildren().addAll(userAvatar, userName);

        // Espaciador izquierdo
        Region leftSpacer = new Region();
        HBox.setHgrow(leftSpacer, Priority.ALWAYS);

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

        // Espaciador derecho
        Region rightSpacer = new Region();
        HBox.setHgrow(rightSpacer, Priority.ALWAYS);
        statusLabel = new Label("Listo");
        statusLabel.getStyleClass().add("status-label");

        getChildren().addAll(userProfile, leftSpacer, progressCenter, rightSpacer, statusLabel);

        setupEventListeners();
    }

    private static double calcProgress(int type, int current, int total) {
        if (total == 0) return 0;
        double p = (double) current / total;
        return switch (type) {
            case LauncherWrapper.DownloadCallback.TYPE_CLIENT -> p * 0.05;
            case LauncherWrapper.DownloadCallback.TYPE_LIBRARY -> 0.05 + p * 0.15;
            case LauncherWrapper.DownloadCallback.TYPE_ASSET -> 0.20 + p * 0.75;
            case LauncherWrapper.DownloadCallback.TYPE_NATIVE -> 0.95 + p * 0.05;
            default -> p;
        };
    }

    private void setupEventListeners() {
        eventBus.subscribe(EventType.DOWNLOAD_PROGRESS, (eventData -> Platform.runLater(() -> {
            progressBar.setVisible(true);
            progressLabel.setVisible(true);
            progressText.setVisible(true);

            int current = eventData.getInt("current");
            int total = eventData.getInt("total");
            int type = eventData.getInt("type");

            double progress = calcProgress(type, current, total);
            int percent = (int) (progress * 100);

            progressLabel.setText(percent + "%");
            progressBar.setProgress(progress);

            // Texto descriptivo según el tipo de descarga
            String typeText = getDownloadTypeText(type);
            progressText.setText(typeText + " (" + current + "/" + total + ")");
        })));

        eventBus.subscribe(EventType.DOWNLOAD_COMPLETED, (eventData -> Platform.runLater(() -> {
            progressBar.setVisible(false);
            progressLabel.setVisible(false);
            progressText.setText("Descarga completada: " + eventData.getString("version"));

            // Ocultar después de 3 segundos
            TaskManager.getInstance().runAsync(() -> {
                try {
                    Thread.sleep(3000);
                    Platform.runLater(() -> progressText.setVisible(false));
                } catch (InterruptedException ignored) {
                }
            });
        })));

        eventBus.subscribe(EventType.INSTANCE_VERSION_NOT_INSTALLED, (eventData -> Platform.runLater(() -> {
            progressBar.setVisible(true);
            progressLabel.setVisible(true);
            progressText.setVisible(true);
            progressText.setText("Descargando versión requerida...");
            progressBar.setProgress(ProgressBar.INDETERMINATE_PROGRESS);
            progressLabel.setText("");
        })));
        eventBus.subscribe(EventType.GAME_STARTED, eventData -> {
            TaskManager.getInstance().runAsyncAtJFXThread(() -> {
                statusLabel.setText(String.format("Iniciando %s", eventData.getString("version")));
            });
        });
    }

    private String getDownloadTypeText(int type) {
        return switch (type) {
            case LauncherWrapper.DownloadCallback.TYPE_CLIENT -> "Descargando cliente...";
            case LauncherWrapper.DownloadCallback.TYPE_LIBRARY -> "Descargando librerías...";
            case LauncherWrapper.DownloadCallback.TYPE_ASSET -> "Descargando recursos...";
            case LauncherWrapper.DownloadCallback.TYPE_NATIVE -> "Descargando nativos...";
            default -> "Descargando...";
        };
    }
}