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

import com.cubiclauncher.launcher.core.InstanceManager;
import com.cubiclauncher.launcher.core.TaskManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Controlador principal para la gestión de instancias
 */
public class LauncherController {
    private static final Logger log = LoggerFactory.getLogger(LauncherController.class);
    private static final InstanceManager instanceManager = InstanceManager.getInstance();
    private static final TaskManager taskManager = TaskManager.getInstance();

    /**
     * Configura el manejo del botón JUGAR
     */
    public static void launchInstance(String instance) {
        taskManager.runAsync(() -> {
            try {
                instanceManager.startInstance(instance);
            } catch (Exception e) {
                log.error("Error al lanzar la instancia: {}", instance);
            }
        });
    }
    public static void createInstance(String name, String version) {
        instanceManager.createInstance(name, version);
    }
}