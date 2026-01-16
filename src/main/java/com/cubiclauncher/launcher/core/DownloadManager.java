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

package com.cubiclauncher.launcher.core;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Manages a queue for download tasks to control concurrency and reduce memory usage.
 */
public class DownloadManager {
    private static DownloadManager instance;
    private final ExecutorService downloadExecutor;
    private final Logger log = LoggerFactory.getLogger(DownloadManager.class);
    private final AtomicInteger downloadCounter = new AtomicInteger(0);

    private DownloadManager() {
        ThreadFactory factory = r -> {
            Thread t = new Thread(r, "CubicLauncher-Download-" + downloadCounter.incrementAndGet());
            t.setDaemon(true);
            return t;
        };
        // Use a fixed thread pool to limit concurrent downloads
        this.downloadExecutor = Executors.newFixedThreadPool(3, factory);
    }

    public static synchronized DownloadManager getInstance() {
        if (instance == null) {
            instance = new DownloadManager();
        }
        return instance;
    }

    /**
     * Submits a download task to the queue.
     *
     * @param downloadTask The download task to execute.
     */
    public void submitDownload(Runnable downloadTask) {
        log.info("New download task submitted.");
        downloadExecutor.submit(() -> {
            try {
                downloadTask.run();
            } catch (Exception e) {
                log.error("Error in download task: {}", e.getMessage(), e);
            } finally {
                log.info("Download task finished.");
            }
        });
    }

    /**
     * Shuts down the download executor service.
     */
    public void shutdown() {
        log.info("Shutting down DownloadManager...");
        downloadExecutor.shutdown();
        try {
            if (!downloadExecutor.awaitTermination(5, TimeUnit.SECONDS)) {
                log.warn("Timeout waiting for download tasks to complete, forcing shutdown...");
                downloadExecutor.shutdownNow();
            }
        } catch (InterruptedException e) {
            log.error("Interrupted while shutting down DownloadManager", e);
            downloadExecutor.shutdownNow();
            Thread.currentThread().interrupt();
        }
        log.info("DownloadManager shut down.");
    }
}