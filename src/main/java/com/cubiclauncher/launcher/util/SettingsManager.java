package com.cubiclauncher.launcher.util;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.io.*;
import java.nio.file.*;
import com.cubiclauncher.launcher.util.PathManager;

public class SettingsManager {
    private static final String SETTINGS_FILE = "settings.json";
    private static SettingsManager instance;
    private static final PathManager pathManager = PathManager.getInstance();

    // Launcher settings
    public String language = "Español";
    public boolean autoUpdate = true;
    public boolean errorConsole = false;
    public boolean closeLauncherOnGameStart = false;

    // Minecraft settings
    public boolean showAlphaVersions = false;
    public boolean showBetaVersions = true;
    public boolean discordRichPresence = false;
    public boolean forceDiscreteGpu = false;

    // Java settings
    public String javaPath = null; // null = automático
    public Integer minMemory = 1;
    public Integer maxMemory = 2;
    public String jvmArguments = "";
    public String minMemoryUnit = "GB"; // o "MB"
    public String maxMemoryUnit = "GB";

    // Usuario
    public String username = "steve";
    public String[] jre_paths = new String[0];
    public boolean native_styles = true;

    private SettingsManager() {}

    public static SettingsManager getInstance() {
        if (instance == null) {
            instance = load();
        }
        return instance;
    }

    private static File getSettingsFile() {
        return new File(pathManager.getSettingsPath().toFile(), SETTINGS_FILE);
    }

    public static SettingsManager load() {
        File file = getSettingsFile();

        if (!file.exists()) {
            System.out.println("Creando nueva configuración en: " + file.getAbsolutePath());
            return new SettingsManager();
        }

        try (Reader reader = new FileReader(file)) {
            Gson gson = new Gson();
            SettingsManager settings = gson.fromJson(reader, SettingsManager.class);
            return settings != null ? settings : new SettingsManager();
        } catch (IOException e) {
            System.err.println("Error al cargar configuración: " + e.getMessage());
            return new SettingsManager();
        }
    }

    public void save() {
        File file = getSettingsFile();
        try (Writer writer = new FileWriter(file)) {
            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            gson.toJson(this, writer);
        } catch (IOException e) {
            System.err.println("Error al guardar configuración: " + e.getMessage());
        }
    }

    public static String getConfigPath() {
        return getSettingsFile().getAbsolutePath();
    }

    // ==================== GETTERS ====================

    public String getLanguage() { return language; }
    public boolean isAutoUpdate() { return autoUpdate; }
    public boolean isErrorConsole() { return errorConsole; }
    public boolean isCloseLauncherOnGameStart() { return closeLauncherOnGameStart; }
    public boolean isNative_styles() { return native_styles; }
    public boolean isShowAlphaVersions() { return showAlphaVersions; }
    public boolean isShowBetaVersions() { return showBetaVersions; }
    public boolean isDiscordRichPresence() { return discordRichPresence; }
    public boolean isForceDiscreteGpu() { return forceDiscreteGpu; }
    public String getUsername() { return username; }
    public String getJavaPath() { return javaPath; }
    public Integer getMinMemory() { return minMemory; }
    public Integer getMaxMemory() { return maxMemory; }
    public String getJvmArguments() { return jvmArguments; }
    public String getMinMemoryUnit() { return minMemoryUnit; }
    public String getMaxMemoryUnit() { return maxMemoryUnit; }
    // ==================== SETTERS CON AUTO-SAVE ====================
    public void setMinMemoryUnit(String unit) {
        this.minMemoryUnit = unit;
        save();
    }

    public void setMaxMemoryUnit(String unit) {
        this.maxMemoryUnit = unit;
        save();
    }
    public void setLanguage(String language) {
        this.language = language;
        save();
    }

    public void setAutoUpdate(boolean autoUpdate) {
        this.autoUpdate = autoUpdate;
        save();
    }

    public void setErrorConsole(boolean errorConsole) {
        this.errorConsole = errorConsole;
        save();
    }

    public void setCloseLauncherOnGameStart(boolean closeLauncherOnGameStart) {
        this.closeLauncherOnGameStart = closeLauncherOnGameStart;
        save();
    }

    public void setShowAlphaVersions(boolean showAlphaVersions) {
        this.showAlphaVersions = showAlphaVersions;
        save();
    }

    public void setShowBetaVersions(boolean showBetaVersions) {
        this.showBetaVersions = showBetaVersions;
        save();
    }

    public void setDiscordRichPresence(boolean discordRichPresence) {
        this.discordRichPresence = discordRichPresence;
        save();
    }

    public void setForceDiscreteGpu(boolean forceDiscreteGpu) {
        this.forceDiscreteGpu = forceDiscreteGpu;
        save();
    }

    public void setJavaPath(String javaPath) {
        this.javaPath = javaPath;
        save();
    }
    public int getMinMemoryInMB() {
        return "GB".equals(minMemoryUnit) ? minMemory * 1024 : minMemory;
    }

    public int getMaxMemoryInMB() {
        return "GB".equals(maxMemoryUnit) ? maxMemory * 1024 : maxMemory;
    }
    public void setMinMemory(Integer minMemory) {
        this.minMemory = minMemory;
        save();
    }

    public void setMaxMemory(Integer maxMemory) {
        this.maxMemory = maxMemory;
        save();
    }

    public void setJvmArguments(String jvmArguments) {
        this.jvmArguments = jvmArguments;
        save();
    }

    // ==================== MÉTODOS LEGACY ====================

    public void setUsername(String username) {
        this.username = username;
        save();
    }

    public void setJrePaths(String[] paths) {
        this.jre_paths = paths;
        save();
    }

    public void setNativeStyles(boolean enabled) {
        this.native_styles = enabled;
        save();
    }

    public void addJrePath(String path) {
        String[] newPaths = new String[jre_paths.length + 1];
        System.arraycopy(jre_paths, 0, newPaths, 0, jre_paths.length);
        newPaths[jre_paths.length] = path;
        setJrePaths(newPaths);
    }
}