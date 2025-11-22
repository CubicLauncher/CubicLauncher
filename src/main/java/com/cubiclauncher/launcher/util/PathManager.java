/*
 *
 *  * Copyright (C) 2025 Santiagolxx, Notstaff and CubicLauncher contributors
 *  *
 *  * This program is free software: you can redistribute it and/or modify
 *  * it under the terms of the GNU Affero General Public License as published by
 *  * the Free Software Foundation, either version 3 of the License, or
 *  * (at your option) any later version.
 *  *
 *  * This program is distributed in the hope that it will be useful,
 *  * but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  * GNU Affero General Public License for more details.
 *  *
 *  * You should have received a copy of the GNU Affero General Public License
 *  * along with this program.  If not, see https://www.gnu.org/licenses/.
 *
 *
 */

package com.cubiclauncher.launcher.util;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;

public class PathManager {
    private static final String APP_NAME = "CubicLauncher";
    private static PathManager instance;

    private final Path settingsPath;
    private final Path instancePath;
    private final Path gamePath;

    private PathManager() {
        this.settingsPath = getSettingsDirectory();
        this.gamePath = getGameDir();
        this.instancePath = getInstanceDir();
    }

    public static PathManager getInstance() {
        if (instance == null) {
            getInstanceDir();
            getGameDir();
            getSettingsDirectory();
            instance = new PathManager();
        }
        return instance;
    }

    // ==================== GETTERS ====================

    public Path getSettingsPath() {
        return settingsPath;
    }

    public Path getInstancePath() {
        return instancePath;
    }

    public Path getGamePath() {
        return gamePath;
    }

    // ==================== MÉTODOS PRIVADOS ====================

    private static Path getSettingsDirectory() {
        String os = System.getProperty("os.name").toLowerCase();
        String configPath;

        if (os.contains("win")) {
            configPath = System.getenv("APPDATA") + File.separator + APP_NAME;
        } else if (os.contains("mac")) {
            configPath = System.getProperty("user.home") + "/Library/Application Support/" + APP_NAME;
        } else {
            configPath = System.getProperty("user.home") + "/.cubic";
        }

        File dir = new File(configPath);
        if (!dir.exists()) {
            dir.mkdirs();
        }

        return Paths.get(configPath);
    }

    private static Path getGameDir() {
        String os = System.getProperty("os.name").toLowerCase();
        String configPath;

        if (os.contains("win")) {
            configPath = System.getenv("APPDATA") + File.separator + APP_NAME;
        } else if (os.contains("mac")) {
            configPath = System.getProperty("user.home") + "/Library/Application Support/" + APP_NAME ;
        } else {
            configPath = System.getProperty("user.home") + "/.cubic";
        }

        File dir = new File(configPath);
        if (!dir.exists()) {
            dir.mkdirs();
        }

        return Paths.get(configPath);
    }

    private static Path getInstanceDir() {
        String os = System.getProperty("os.name").toLowerCase();
        String configPath;

        if (os.contains("win")) {
            configPath = System.getenv("APPDATA") + File.separator + APP_NAME + File.separator + "instances";
        } else if (os.contains("mac")) {
            configPath = System.getProperty("user.home") + "/Library/Application Support/" + APP_NAME + "/instances";
        } else {
            configPath = System.getProperty("user.home") + "/.cubic" + "/instances";
        }

        File dir = new File(configPath);
        if (!dir.exists()) {
            dir.mkdirs();
        }

        return Paths.get(configPath);
    }
}