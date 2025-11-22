/*
 * Copyright (C) 2025 Santiagolxx, Notstaff and CubicLauncher contributors
 * AGPL-3.0 License
 */
package com.cubiclauncher.launcher.ui.components;

import com.cubiclauncher.launcher.util.DownloadProgress;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

/**
 * Componente visual para mostrar el progreso de una descarga.
 */
public class DownloadProgressBar extends VBox {
    private final ProgressBar progressBar;
    private final Label fileNameLabel;
    private final Label progressLabel;
    private final Label speedLabel;
    private final Label etaLabel;

    private DownloadProgress boundProgress;
    private boolean isBound = false;

    public DownloadProgressBar() {
        super(8);
        setPadding(new Insets(15));
        getStyleClass().add("download-progress-container");

        // Nombre del archivo
        fileNameLabel = new Label("Preparando descarga...");
        fileNameLabel.getStyleClass().add("download-filename");

        // Barra de progreso
        progressBar = new ProgressBar(0);
        progressBar.setMaxWidth(Double.MAX_VALUE);
        progressBar.setPrefHeight(20);
        progressBar.getStyleClass().add("download-progress-bar");
        HBox.setHgrow(progressBar, Priority.ALWAYS);

        // Info de progreso
        HBox infoRow = new HBox(15);
        infoRow.setAlignment(Pos.CENTER_LEFT);

        progressLabel = new Label("0%");
        progressLabel.getStyleClass().add("download-percent");

        speedLabel = new Label("-- KB/s");
        speedLabel.getStyleClass().add("download-speed");

        etaLabel = new Label("ETA: --:--");
        etaLabel.getStyleClass().add("download-eta");

        HBox spacer = new HBox();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        infoRow.getChildren().addAll(progressLabel, spacer, speedLabel, etaLabel);

        getChildren().addAll(fileNameLabel, progressBar, infoRow);
    }

    /**
     * Vincula este componente a un DownloadProgress observable.
     */
    public void bind(DownloadProgress progress) {
        // Desvincular anterior si existe
        unbind();

        this.boundProgress = progress;
        this.isBound = true;

        // Vincular propiedades
        fileNameLabel.textProperty().bind(progress.fileNameProperty());
        progressBar.progressProperty().bind(progress.progressProperty());
        speedLabel.textProperty().bind(progress.speedProperty());
        etaLabel.textProperty().bind(progress.etaProperty().map(e -> "ETA: " + e));

        // Actualizar porcentaje con listener (no bind porque es calculado)
        progress.progressProperty().addListener((obs, old, newVal) -> {
            int percent = (int) (newVal.doubleValue() * 100);
            progressLabel.setText(percent + "%");
        });

        // Cambiar estilo según estado completado
        progress.completedProperty().addListener((obs, old, completed) -> {
            if (completed) {
                progressBar.getStyleClass().add("completed");
            }
        });

        // Cambiar estilo según estado de error
        progress.failedProperty().addListener((obs, old, failed) -> {
            if (failed) {
                progressBar.getStyleClass().add("failed");
            }
        });
    }

    /**
     * Desvincula todas las propiedades.
     */
    public void unbind() {
        if (isBound) {
            fileNameLabel.textProperty().unbind();
            progressBar.progressProperty().unbind();
            speedLabel.textProperty().unbind();
            etaLabel.textProperty().unbind();

            // Limpiar estilos de estado
            progressBar.getStyleClass().removeAll("completed", "failed");

            isBound = false;
            boundProgress = null;
        }
    }

    /**
     * Actualización manual sin binding (para casos simples).
     * Solo funciona si NO está vinculado.
     */
    public void update(String fileName, double progress, String speed, String eta) {
        if (isBound) {
            throw new IllegalStateException("No se puede actualizar manualmente mientras está vinculado. Usa unbind() primero.");
        }
        fileNameLabel.setText(fileName);
        progressBar.setProgress(progress);
        progressLabel.setText((int)(progress * 100) + "%");
        speedLabel.setText(speed);
        etaLabel.setText("ETA: " + eta);
    }

    /**
     * Establece modo indeterminado.
     * Solo funciona si NO está vinculado.
     */
    public void setIndeterminate(boolean indeterminate) {
        if (isBound) {
            // Si está vinculado, desvincular primero
            unbind();
        }
        progressBar.setProgress(indeterminate ? -1 : 0);
    }

    /**
     * Marca como completado.
     * Si está vinculado, actualiza el DownloadProgress.
     * Si no, actualiza directamente los componentes.
     */
    public void complete() {
        if (isBound && boundProgress != null) {
            // El DownloadProgress manejará el estado
            boundProgress.complete();
        } else {
            progressBar.setProgress(1.0);
            progressBar.getStyleClass().add("completed");
            progressLabel.setText("100%");
            etaLabel.setText("Completado");
        }
    }

    /**
     * Marca como fallido.
     * Si está vinculado, actualiza el DownloadProgress.
     * Si no, actualiza directamente los componentes.
     */
    public void fail(String error) {
        if (isBound && boundProgress != null) {
            boundProgress.fail(error);
        } else {
            progressBar.getStyleClass().add("failed");
            etaLabel.setText("Error");
            fileNameLabel.setText(fileNameLabel.getText() + " - " + error);
        }
    }

    /**
     * Resetea el componente a su estado inicial.
     */
    public void reset() {
        unbind();
        progressBar.setProgress(0);
        progressBar.getStyleClass().removeAll("completed", "failed");
        fileNameLabel.setText("Preparando descarga...");
        progressLabel.setText("0%");
        speedLabel.setText("-- KB/s");
        etaLabel.setText("ETA: --:--");
    }

    public boolean isBound() {
        return isBound;
    }
}