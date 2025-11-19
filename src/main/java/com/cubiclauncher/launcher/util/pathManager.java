package com.cubiclauncher.launcher.util;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;

public class pathManager {
    private static final String APP_NAME = "CubicLauncher";
    private static pathManager instance;

    private final Path settingsPath;
    private final Path instancePath;
    private final Path gamePath;

    private pathManager() {
        this.settingsPath = getSettingsDirectory();
        this.gamePath = getGameDir();
        this.instancePath = getInstanceDir();
    }

    public static pathManager getInstance() {
        if (instance == null) {
            instance = new pathManager();
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
            configPath = System.getenv("APPDATA") + File.separator + APP_NAME + File.separator + "config";
        } else if (os.contains("mac")) {
            configPath = System.getProperty("user.home") + "/Library/Application Support/" + APP_NAME + "/config";
        } else {
            configPath = System.getProperty("user.home") + "/.config/" + APP_NAME + "/config";
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
            configPath = System.getenv("APPDATA") + File.separator + APP_NAME + File.separator + "shared";
        } else if (os.contains("mac")) {
            configPath = System.getProperty("user.home") + "/Library/Application Support/" + APP_NAME + "/shared";
        } else {
            configPath = System.getProperty("user.home") + "/.config/" + APP_NAME + "/shared";
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
            configPath = System.getProperty("user.home") + "/.config/" + APP_NAME + "/instances";
        }

        File dir = new File(configPath);
        if (!dir.exists()) {
            dir.mkdirs();
        }

        return Paths.get(configPath);
    }
}