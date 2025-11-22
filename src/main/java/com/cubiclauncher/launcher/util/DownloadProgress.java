/*
 * Copyright (C) 2025 Santiagolxx, Notstaff and CubicLauncher contributors
 * AGPL-3.0 License
 */
package com.cubiclauncher.launcher.util;

import javafx.beans.property.*;

/**
 * Modelo observable para el progreso de descargas.
 * Permite binding directo con componentes de JavaFX.
 */
public class DownloadProgress {
    private final StringProperty fileName = new SimpleStringProperty("");
    private final LongProperty bytesDownloaded = new SimpleLongProperty(0);
    private final LongProperty totalBytes = new SimpleLongProperty(0);
    private final DoubleProperty progress = new SimpleDoubleProperty(0);
    private final StringProperty status = new SimpleStringProperty("Esperando...");
    private final BooleanProperty completed = new SimpleBooleanProperty(false);
    private final BooleanProperty failed = new SimpleBooleanProperty(false);
    private final StringProperty errorMessage = new SimpleStringProperty("");

    // Velocidad de descarga
    private final StringProperty speed = new SimpleStringProperty("0 KB/s");
    private final StringProperty eta = new SimpleStringProperty("--:--");

    private long startTime;
    private long lastUpdateTime;
    private long lastBytesDownloaded;

    public DownloadProgress() {
        this.startTime = System.currentTimeMillis();
        this.lastUpdateTime = startTime;
    }

    public void update(long downloaded, long total) {
        this.bytesDownloaded.set(downloaded);
        this.totalBytes.set(total);

        if (total > 0) {
            this.progress.set((double) downloaded / total);
        }

        updateSpeed(downloaded);
    }

    private void updateSpeed(long downloaded) {
        long now = System.currentTimeMillis();
        long elapsed = now - lastUpdateTime;

        if (elapsed >= 500) { // Actualizar cada 500ms
            long bytesInInterval = downloaded - lastBytesDownloaded;
            double bytesPerSecond = (bytesInInterval * 1000.0) / elapsed;

            speed.set(formatSpeed(bytesPerSecond));

            // Calcular ETA
            if (bytesPerSecond > 0 && totalBytes.get() > 0) {
                long remaining = totalBytes.get() - downloaded;
                long seconds = (long) (remaining / bytesPerSecond);
                eta.set(formatTime(seconds));
            }

            lastUpdateTime = now;
            lastBytesDownloaded = downloaded;
        }
    }

    private String formatSpeed(double bytesPerSecond) {
        if (bytesPerSecond >= 1024 * 1024) {
            return String.format("%.1f MB/s", bytesPerSecond / (1024 * 1024));
        } else if (bytesPerSecond >= 1024) {
            return String.format("%.1f KB/s", bytesPerSecond / 1024);
        }
        return String.format("%.0f B/s", bytesPerSecond);
    }

    private String formatTime(long seconds) {
        if (seconds < 60) return seconds + "s";
        if (seconds < 3600) return (seconds / 60) + "m " + (seconds % 60) + "s";
        return (seconds / 3600) + "h " + ((seconds % 3600) / 60) + "m";
    }

    public static String formatBytes(long bytes) {
        if (bytes >= 1024 * 1024 * 1024) {
            return String.format("%.2f GB", bytes / (1024.0 * 1024 * 1024));
        } else if (bytes >= 1024 * 1024) {
            return String.format("%.2f MB", bytes / (1024.0 * 1024));
        } else if (bytes >= 1024) {
            return String.format("%.2f KB", bytes / 1024.0);
        }
        return bytes + " B";
    }

    public void complete() {
        this.completed.set(true);
        this.progress.set(1.0);
        this.status.set("Completado");
    }

    public void fail(String error) {
        this.failed.set(true);
        this.errorMessage.set(error);
        this.status.set("Error: " + error);
    }

    // Property getters para binding
    public StringProperty fileNameProperty() { return fileName; }
    public LongProperty bytesDownloadedProperty() { return bytesDownloaded; }
    public LongProperty totalBytesProperty() { return totalBytes; }
    public DoubleProperty progressProperty() { return progress; }
    public StringProperty statusProperty() { return status; }
    public BooleanProperty completedProperty() { return completed; }
    public BooleanProperty failedProperty() { return failed; }
    public StringProperty speedProperty() { return speed; }
    public StringProperty etaProperty() { return eta; }

    // Setters
    public void setFileName(String name) { fileName.set(name); }
    public void setStatus(String s) { status.set(s); }

    // Getters de valor
    public double getProgress() { return progress.get(); }
    public boolean isCompleted() { return completed.get(); }
    public boolean isFailed() { return failed.get(); }
    public String getFileName() { return fileName.get(); }
    public long getBytesDownloaded() { return bytesDownloaded.get(); }
    public long getTotalBytes() { return totalBytes.get(); }
    public String getStatus() { return status.get(); }
    public String getSpeed() { return speed.get(); }
    public String getEta() { return eta.get(); }
}