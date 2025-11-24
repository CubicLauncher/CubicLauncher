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

package com.cubiclauncher.launcher.ui.controllers;

import com.cubiclauncher.launcher.core.LauncherWrapper;
import com.cubiclauncher.launcher.core.instances.InstanceManager;
import com.cubiclauncher.launcher.core.TaskManager;
import javafx.application.Platform;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.function.Consumer;

/**
 * Controlador para manejar la lógica de versiones e instancias
 */
public class VersionsController {
    private static final Logger log = LoggerFactory.getLogger(VersionsController.class);
    private final LauncherWrapper launcher;
    private final InstanceManager instanceManager;
    private final TaskManager taskManager;

    public VersionsController() {
        this.launcher = new LauncherWrapper();
        this.instanceManager = LauncherWrapper.instanceManager;
        this.taskManager = TaskManager.getInstance();
    }

    /**
     * Obtiene las versiones disponibles de forma asíncrona
     */
    public void loadAvailableVersions(Consumer<List<String>> onSuccess, Consumer<Exception> onError) {
        taskManager.runAsync(
                () -> launcher.getAvailableVersions(),
                onSuccess,
                onError
        );
    }

    /**
     * Obtiene las versiones instaladas
     */
    public List<String> getInstalledVersions() {
        return launcher.getInstalledVersions();
    }

    /**
     * Crea una nueva instancia
     */
    public void createInstance(String instanceName, String version,
                               Runnable onSuccess, Consumer<Exception> onError) {
        // Validaciones
        if (instanceName == null || instanceName.trim().isEmpty()) {
            Platform.runLater(() -> onError.accept(
                    new IllegalArgumentException("El nombre de la instancia no puede estar vacío")
            ));
            return;
        }

        if (instanceManager.instanceExists(instanceName)) {
            Platform.runLater(() -> onError.accept(
                    new IllegalArgumentException("Ya existe una instancia con el nombre: " + instanceName)
            ));
            return;
        }

        // Crear instancia de forma asíncrona
        taskManager.runAsync(
                () -> {
                    instanceManager.createInstance(instanceName, version);
                    log.info("Instancia '{}' creada con versión {}", instanceName, version);
                },
                onSuccess,
                onError
        );
    }

    /**
     * Descarga una versión de Minecraft
     */
    public void downloadVersion(String versionId) {
        log.info("Iniciando descarga de versión: {}", versionId);
        launcher.downloadMinecraftVersion(versionId);
    }

    /**
     * Verifica si una versión está instalada
     */
    public boolean isVersionInstalled(String versionId) {
        return launcher.getInstalledVersions().contains(versionId);
    }

    /**
     * Obtiene todas las instancias
     */
    public List<InstanceManager.Instance> getAllInstances() {
        return instanceManager.getAllInstances();
    }

    /**
     * Obtiene instancias ordenadas por última vez jugadas
     */
    public List<InstanceManager.Instance> getRecentInstances() {
        return instanceManager.getInstancesByLastPlayed();
    }
}