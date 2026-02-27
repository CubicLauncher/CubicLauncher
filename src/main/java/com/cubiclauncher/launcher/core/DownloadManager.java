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
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Manages a queue for download tasks to control concurrency and reduce memory
 * usage.
 */
public class DownloadManager {
    private static DownloadManager instance;
    private final Logger log = LoggerFactory.getLogger(DownloadManager.class);
    private final AtomicInteger downloadCounter = new AtomicInteger(0);
    private final TaskManager taskManager = TaskManager.getInstance();
    private DownloadManager() {}

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
        taskManager.runAsync(downloadTask);
    }
}