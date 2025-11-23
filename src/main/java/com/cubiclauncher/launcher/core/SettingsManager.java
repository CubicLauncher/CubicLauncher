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

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.io.*;

public class SettingsManager {
    private static final String SETTINGS_FILE = "settings.json";
    private static final PathManager pathManager = PathManager.getInstance();
    private static SettingsManager instance;
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
    public Integer minMemory = 512;
    public Integer maxMemory = 2;
    public String jre8_path = "";
    public String jre17_path = "";
    public String jre21_path = "";
    public String jvmArguments = "";
    public String minMemoryUnit = "MB"; // o "MB"
    public String maxMemoryUnit = "GB";

    // Usuario
    public String username = "steve";
    public boolean native_styles = true;

    private SettingsManager() {
    }

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

    public static String getConfigPath() {
        return getSettingsFile().getAbsolutePath();
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

    // ==================== GETTERS ====================

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
        save();
    }

    public boolean isAutoUpdate() {
        return autoUpdate;
    }

    public void setAutoUpdate(boolean autoUpdate) {
        this.autoUpdate = autoUpdate;
        save();
    }

    public boolean isErrorConsole() {
        return errorConsole;
    }

    public void setErrorConsole(boolean errorConsole) {
        this.errorConsole = errorConsole;
        save();
    }

    public boolean isCloseLauncherOnGameStart() {
        return closeLauncherOnGameStart;
    }

    public void setCloseLauncherOnGameStart(boolean closeLauncherOnGameStart) {
        this.closeLauncherOnGameStart = closeLauncherOnGameStart;
        save();
    }

    public boolean isNative_styles() {
        return native_styles;
    }

    public boolean isShowAlphaVersions() {
        return showAlphaVersions;
    }

    public void setShowAlphaVersions(boolean showAlphaVersions) {
        this.showAlphaVersions = showAlphaVersions;
        save();
    }

    public boolean isShowBetaVersions() {
        return showBetaVersions;
    }

    public void setShowBetaVersions(boolean showBetaVersions) {
        this.showBetaVersions = showBetaVersions;
        save();
    }

    public boolean isDiscordRichPresence() {
        return discordRichPresence;
    }

    public void setDiscordRichPresence(boolean discordRichPresence) {
        this.discordRichPresence = discordRichPresence;
        save();
    }

    public boolean isForceDiscreteGpu() {
        return forceDiscreteGpu;
    }

    public void setForceDiscreteGpu(boolean forceDiscreteGpu) {
        this.forceDiscreteGpu = forceDiscreteGpu;
        save();
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
        save();
    }

    public String getJava8Path() {
        return jre8_path;
    }
    public String getJava17Path() {
        return jre17_path;
    }
    public String getJava21path() {
        return jre21_path;
    }

    public Integer getMinMemory() {
        return minMemory;
    }

    public void setMinMemory(Integer minMemory) {
        this.minMemory = minMemory;
        save();
    }

    public Integer getMaxMemory() {
        return maxMemory;
    }

    public void setMaxMemory(Integer maxMemory) {
        this.maxMemory = maxMemory;
        save();
    }

    public String getJvmArguments() {
        return jvmArguments;
    }

    public void setJvmArguments(String jvmArguments) {
        this.jvmArguments = jvmArguments;
        save();
    }

    // ==================== SETTERS CON AUTO-SAVE ====================
    public void setMinMemoryUnit(String unit) {
        this.minMemoryUnit = unit;
        save();
    }

    public void setMaxMemoryUnit(String unit) {
        this.maxMemoryUnit = unit;
        save();
    }

    public int getMinMemoryInMB() {
        return "GB".equals(minMemoryUnit) ? minMemory * 1024 : minMemory;
    }

    // ==================== MÉTODOS LEGACY ====================

    public int getMaxMemoryInMB() {
        return "GB".equals(maxMemoryUnit) ? maxMemory * 1024 : maxMemory;
    }

    public void setJre8_path(String path) {
        this.jre8_path = path;
        save();
    }

    public void setJre17_path(String path) {
        this.jre17_path = path;
        save();
    }

    public void setJre21_path(String path) {
        this.jre21_path = path;
        save();
    }
    public void setNativeStyles(boolean enabled) {
        this.native_styles = enabled;
        save();
    }
}