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

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;

public class PathManager {
    private static final String APP_NAME = "CubicLauncher";

    private final Path settingsPath;
    private final Path instancePath;
    private final Path gamePath;

    private PathManager() {
        this.settingsPath = ensure(settingsDirectory());
        this.gamePath = ensure(gameDirectory());
        this.instancePath = ensure(instanceDirectory());
    }

    public static PathManager getInstance() {
        return Holder.INSTANCE;
    }

    private static Path settingsDirectory() {
        String os = System.getProperty("os.name").toLowerCase();
        if (os.contains("win")) {
            return Paths.get(System.getenv("APPDATA"), APP_NAME);
        } else if (os.contains("mac")) {
            return Paths.get(System.getProperty("user.home"),
                    "Library", "Application Support", APP_NAME);
        } else {
            return Paths.get(System.getProperty("user.home"), ".cubic");
        }
    }

    // ==================== GETTERS ====================

    private static Path gameDirectory() {
        return settingsDirectory(); // mismo directorio base
    }

    private static Path instanceDirectory() {
        String os = System.getProperty("os.name").toLowerCase();
        if (os.contains("win")) {
            return Paths.get(System.getenv("APPDATA"), APP_NAME, "instances");
        } else if (os.contains("mac")) {
            return Paths.get(System.getProperty("user.home"),
                    "Library", "Application Support", APP_NAME, "instances");
        } else {
            return Paths.get(System.getProperty("user.home"), ".cubic", "instances");
        }
    }

    private static Path ensure(Path dir) {
        File f = dir.toFile();
        if (!f.exists() && !f.mkdirs()) {
            throw new IllegalStateException("No se pudo crear directorio: " + dir);
        }
        return dir;
    }

    // ==================== MÉTODOS SEGURIDAD ====================

    public Path getSettingsPath() {
        return settingsPath;
    }

    public Path getInstancePath() {
        return instancePath;
    }

    public Path getGamePath() {
        return gamePath;
    }

    private static class Holder {
        static final PathManager INSTANCE = new PathManager();
    }
}
