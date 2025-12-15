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

package com.cubiclauncher.launcher.ui;

import com.cubiclauncher.launcher.core.TaskManager;
import com.cubiclauncher.launcher.core.events.EventBus;
import com.cubiclauncher.launcher.core.events.EventData;
import com.cubiclauncher.launcher.core.events.EventType;
import com.cubiclauncher.launcher.ui.components.BottomBar;
import com.cubiclauncher.launcher.ui.views.VersionsView;
import javafx.application.Platform;
import java.util.function.Consumer;

public class UIBridge {
    private static final EventBus eventBus = EventBus.get();
    private static UIBridge instance;
    private static final TaskManager taskManager = TaskManager.getInstance();
    private final BottomBar bottomBar;
    private final VersionsView versionsView;

    private UIBridge() {
        bottomBar = BottomBar.getInstance();
        versionsView = VersionsView.getInstance();
        registerListeners();
    }

    public static UIBridge getInstance() {
        if (instance == null) {
            instance = new UIBridge();
        }
        return instance;
    }

    private void registerListeners() {
        subscribe(EventType.DOWNLOAD_PROGRESS, data -> Platform.runLater(() -> taskManager.runAsyncAtJFXThread(() -> {
            bottomBar.getProgressBar().setVisible(true);
            bottomBar.getProgressLabel().setVisible(true);
            bottomBar.getProgressText().setVisible(true);

            int current = data.getInt("current");
            int total = data.getInt("total");
            int type = data.getInt("type");

            double progress = calcProgress(type, current, total);
            int percent = (int)(progress*100);

            bottomBar.getProgressLabel().setText(percent + "%");
            bottomBar.getProgressBar().setProgress(progress);
            bottomBar.getProgressText().setText(getDownloadTypeText(type) + " (" + current + "/" + total + ")");
            bottomBar.getStatusLabel().setText(String.format("Descargando version %s %d%%",data.getString("version"), percent));
        })));

        subscribe(EventType.DOWNLOAD_COMPLETED, data -> Platform.runLater(() -> {
            bottomBar.getProgressBar().setVisible(false);
            bottomBar.getProgressLabel().setVisible(false);
            bottomBar.getProgressText().setText("Descarga completada: " + data.getString("version"));

            taskManager.runAsyncAtJFXThread(() -> {
                bottomBar.getProgressText().setVisible(false);
                bottomBar.getStatusLabel().setText("listo");
            });
        }));
        subscribe(EventType.GAME_STARTED, data -> Platform.runLater(() ->
                bottomBar.getStatusLabel().setText("La versión " + data.getString("version") + " ha sido iniciada.")
        ));
    }

    private double calcProgress(int type, int current, int total) {
        if (total == 0) return 0;
        double p = (double) current / total;
        return switch (type) {
            case 0 -> p*0.05;      // TYPE_CLIENT
            case 1 -> 0.05 + p*0.15; // TYPE_LIBRARY
            case 2 -> 0.20 + p*0.75; // TYPE_ASSET
            case 3 -> 0.95 + p*0.05; // TYPE_NATIVE
            default -> p;
        };
    }

    private String getDownloadTypeText(int type) {
        return switch (type) {
            case 0 -> "Descargando cliente...";
            case 1 -> "Descargando librerías...";
            case 2 -> "Descargando recursos...";
            case 3 -> "Descargando nativos...";
            default -> "Descargando...";
        };
    }

    private void subscribe(EventType type, Consumer<EventData> handler) {
        eventBus.subscribe(type, handler);
    }

}