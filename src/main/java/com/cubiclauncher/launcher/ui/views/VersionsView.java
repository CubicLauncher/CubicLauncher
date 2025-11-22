/*
 * Copyright (C) 2025 Santiagolxx, Notstaff and CubicLauncher contributors
 * AGPL-3.0 License
 */
package com.cubiclauncher.launcher.ui.views;

import com.cubiclauncher.launcher.LauncherWrapper;
import com.cubiclauncher.launcher.LauncherWrapper.DownloadCallback;
import com.cubiclauncher.launcher.ui.components.VersionCell;
import com.cubiclauncher.launcher.core.TaskManager;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

public class VersionsView {
    private static final LauncherWrapper launcher = new LauncherWrapper();
    private static ProgressBar progressBar;
    private static Label statusLabel;
    private static Button downloadButton;

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
        downloadButton.setText("Descargando...");

        TaskManager.getInstance().runAsync(() -> {
            launcher.downloadMinecraftVersion(version, new DownloadCallback() {
                @Override
                public void onProgress(int type, int current, int total, String fileName) {
                    Platform.runLater(() -> {
                        double progress = calcProgress(type, current, total);
                        progressBar.setProgress(progress);
                        statusLabel.setText(String.format("%s: %d/%d",
                                DownloadCallback.getTypeName(type), current, total));
                    });
                }

                @Override
                public void onComplete() {
                    Platform.runLater(() -> {
                        progressBar.setProgress(1.0);
                        statusLabel.setText("¡Completado!");
                        versionsList.refresh();
                        downloadButton.setText("Descargar Versión");
                        downloadButton.setDisable(launcher.getInstalledVersions().contains(version));
                        hideProgressAfter(2000);
                    });
                }

                @Override
                public void onError(String error) {
                    Platform.runLater(() -> {
                        statusLabel.setText("Error: " + error);
                        downloadButton.setText("Reintentar");
                        downloadButton.setDisable(false);
                    });
                }
            });
        });
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

    private static void hideProgressAfter(long ms) {
        new Thread(() -> {
            try { Thread.sleep(ms); } catch (InterruptedException ignored) {}
            Platform.runLater(() -> {
                progressBar.setVisible(false);
                statusLabel.setVisible(false);
            });
        }).start();
    }
}