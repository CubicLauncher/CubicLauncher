/*
 * Copyright (C) 2025 Santiagolxx, Notstaff and CubicLauncher contributors
 * AGPL-3.0 License
 */
package com.cubiclauncher.launcher.util;

import javafx.application.Platform;
import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URI;
import java.nio.file.*;
import java.util.concurrent.*;
import java.util.function.Consumer;

/**
 * Gestor centralizado de descargas con soporte para múltiples descargas simultáneas.
 */
public class DownloadManager {
    private static DownloadManager instance;
    private final ExecutorService executor;
    private final ObservableList<DownloadTask> activeTasks = FXCollections.observableArrayList();
    private final IntegerProperty activeDownloads = new SimpleIntegerProperty(0);

    // Progreso global (para barra de progreso general)
    private final DoubleProperty globalProgress = new SimpleDoubleProperty(0);
    private final StringProperty globalStatus = new SimpleStringProperty("Sin descargas activas");

    private DownloadManager() {
        this.executor = Executors.newFixedThreadPool(4, r -> {
            Thread t = new Thread(r, "DownloadManager-Worker");
            t.setDaemon(true);
            return t;
        });
    }

    public static synchronized DownloadManager getInstance() {
        if (instance == null) instance = new DownloadManager();
        return instance;
    }

    /**
     * Inicia una descarga y retorna el task para monitorear el progreso.
     */
    public DownloadTask download(String url, Path destination) {
        return download(url, destination, null, null);
    }

    public DownloadTask download(String url, Path destination,
                                 Runnable onSuccess, Consumer<Exception> onError) {
        DownloadTask task = new DownloadTask(url, destination, onSuccess, onError);

        Platform.runLater(() -> {
            activeTasks.add(task);
            activeDownloads.set(activeTasks.size());
            updateGlobalProgress();
        });

        executor.submit(task);
        return task;
    }

    /**
     * Descarga múltiples archivos y reporta progreso global.
     */
    public CompletableFuture<Void> downloadAll(java.util.List<DownloadRequest> requests,
                                               Consumer<DownloadProgress> progressCallback) {
        DownloadProgress globalProgress = new DownloadProgress();
        globalProgress.setStatus("Preparando descargas...");

        return CompletableFuture.runAsync(() -> {
            int total = requests.size();
            int[] completed = {0};

            for (DownloadRequest req : requests) {
                globalProgress.setFileName(req.fileName());
                globalProgress.setStatus("Descargando " + (completed[0] + 1) + "/" + total);

                try {
                    downloadSync(req.url(), req.destination(), progress -> {
                        double fileProgress = progress.getProgress();
                        double overall = (completed[0] + fileProgress) / total;
                        globalProgress.update(
                                (long)(overall * 100), 100
                        );
                        Platform.runLater(() -> progressCallback.accept(globalProgress));
                    });
                    completed[0]++;
                } catch (Exception e) {
                    globalProgress.fail(e.getMessage());
                    throw new CompletionException(e);
                }
            }
            globalProgress.complete();
            Platform.runLater(() -> progressCallback.accept(globalProgress));
        }, executor);
    }

    /**
     * Descarga síncrona con callback de progreso.
     */
    public void downloadSync(String url, Path destination,
                             Consumer<DownloadProgress> progressCallback) throws IOException {
        DownloadProgress progress = new DownloadProgress();
        progress.setFileName(destination.getFileName().toString());
        progress.setStatus("Conectando...");

        HttpURLConnection conn = (HttpURLConnection) URI.create(url).toURL().openConnection();
        conn.setRequestProperty("User-Agent", "CubicLauncher/1.0");
        conn.setConnectTimeout(10000);
        conn.setReadTimeout(30000);

        try {
            int responseCode = conn.getResponseCode();
            if (responseCode != 200) {
                throw new IOException("HTTP " + responseCode);
            }

            long totalBytes = conn.getContentLengthLong();
            progress.setStatus("Descargando...");

            Files.createDirectories(destination.getParent());

            try (InputStream in = new BufferedInputStream(conn.getInputStream());
                 OutputStream out = new BufferedOutputStream(Files.newOutputStream(destination))) {

                byte[] buffer = new byte[8192];
                long downloaded = 0;
                int bytesRead;

                while ((bytesRead = in.read(buffer)) != -1) {
                    out.write(buffer, 0, bytesRead);
                    downloaded += bytesRead;

                    progress.update(downloaded, totalBytes);
                    if (progressCallback != null) {
                        final DownloadProgress p = progress;
                        Platform.runLater(() -> progressCallback.accept(p));
                    }
                }
            }
            progress.complete();
        } finally {
            conn.disconnect();
        }
    }

    private void updateGlobalProgress() {
        if (activeTasks.isEmpty()) {
            globalProgress.set(0);
            globalStatus.set("Sin descargas activas");
            return;
        }

        double total = activeTasks.stream()
                .mapToDouble(t -> t.getProgress().getProgress())
                .average()
                .orElse(0);

        globalProgress.set(total);
        globalStatus.set(activeTasks.size() + " descarga(s) activa(s)");
    }

    void taskCompleted(DownloadTask task) {
        Platform.runLater(() -> {
            activeTasks.remove(task);
            activeDownloads.set(activeTasks.size());
            updateGlobalProgress();
        });
    }

    // Properties para binding
    public ObservableList<DownloadTask> getActiveTasks() { return activeTasks; }
    public IntegerProperty activeDownloadsProperty() { return activeDownloads; }
    public DoubleProperty globalProgressProperty() { return globalProgress; }
    public StringProperty globalStatusProperty() { return globalStatus; }

    public void shutdown() {
        executor.shutdown();
        try {
            executor.awaitTermination(5, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            executor.shutdownNow();
        }
    }

    // Record para requests de descarga múltiple
    public record DownloadRequest(String url, Path destination, String fileName) {}

    /**
     * Task individual de descarga
     */
    public class DownloadTask implements Runnable {
        private final String url;
        private final Path destination;
        private final DownloadProgress progress = new DownloadProgress();
        private final Runnable onSuccess;
        private final Consumer<Exception> onError;
        private volatile boolean cancelled = false;

        DownloadTask(String url, Path destination, Runnable onSuccess, Consumer<Exception> onError) {
            this.url = url;
            this.destination = destination;
            this.onSuccess = onSuccess;
            this.onError = onError;
            progress.setFileName(destination.getFileName().toString());
        }

        @Override
        public void run() {
            try {
                downloadSync(url, destination, p -> {
                    progress.update(p.bytesDownloadedProperty().get(), p.totalBytesProperty().get());
                    Platform.runLater(DownloadManager.this::updateGlobalProgress);
                });

                progress.complete();
                if (onSuccess != null) Platform.runLater(onSuccess);
            } catch (Exception e) {
                progress.fail(e.getMessage());
                if (onError != null) Platform.runLater(() -> onError.accept(e));
            } finally {
                taskCompleted(this);
            }
        }

        public void cancel() {
            cancelled = true;
            progress.setStatus("Cancelado");
        }

        public DownloadProgress getProgress() { return progress; }
        public String getUrl() { return url; }
        public Path getDestination() { return destination; }
    }
}