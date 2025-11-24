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

import com.cubiclauncher.launcher.core.SettingsManager;
import com.cubiclauncher.launcher.util.StylesLoader;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.TextField;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;

/**
 * Controlador para manejar todas las acciones de la vista de configuración
 */
public class SettingsController {
    private static final Logger log = LoggerFactory.getLogger(SettingsController.class);
    private final SettingsManager settings;
    private Stage stage;
    private String selectedJavaVersion = "8"; // Por defecto Java 8

    public SettingsController() {
        this.settings = SettingsManager.getInstance();
    }

    public void setStage(Stage stage) {
        this.stage = stage;
    }

    // ==================== LAUNCHER SETTINGS ====================

    public void onLanguageChanged(String language) {
        settings.setLanguage(language);
        log.info("Idioma cambiado a: {}", language);
        // TODO: Implementar cambio de idioma en la UI
    }

    public void onAutoUpdateChanged(boolean enabled) {
        settings.setAutoUpdate(enabled);
        log.info("Auto-actualización: {}", enabled ? "activada" : "desactivada");
    }

    public void onErrorConsoleChanged(boolean enabled) {
        settings.setErrorConsole(enabled);
        log.info("Consola de errores: {}", enabled ? "activada" : "desactivada");
    }

    public void onCloseLauncherChanged(boolean enabled) {
        settings.setCloseLauncherOnGameStart(enabled);
        log.info("Cerrar launcher: {}", enabled ? "activado" : "desactivado");
    }

    public void onNativeStylesChanged(boolean enabled) {
        settings.setNativeStyles(enabled);
        log.info("Estilos nativos: {}", enabled ? "activado" : "desactivado");

        // Buscar cualquier ventana abierta
        javafx.stage.Window window = javafx.stage.Window.getWindows().stream()
                .filter(javafx.stage.Window::isShowing)
                .findFirst()
                .orElse(null);

        if (window == null) {
            log.debug("No hay ventana disponible");
            return;
        }

        Scene scene = window.getScene();
        if (scene == null) {
            return;
        }

        scene.getStylesheets().clear();

        if (!enabled) {
            StylesLoader.load(scene, "/com.cubiclauncher.launcher/styles/ui.main.css");
        }
    }

    public void onSourceCodeLinkClicked() {
        openUrl();
    }

    // ==================== MINECRAFT SETTINGS ====================

    public void onShowAlphasChanged(boolean enabled) {
        settings.setShowAlphaVersions(enabled);
        log.info("Mostrar alphas: {}", enabled ? "activado" : "desactivado");
    }

    public void onShowBetasChanged(boolean enabled) {
        settings.setShowBetaVersions(enabled);
        log.info("Mostrar betas: {}", enabled ? "activado" : "desactivado");
    }

    public void onUseDiscreteGpuChanged(boolean enabled) {
        settings.setForceDiscreteGpu(enabled);
        log.info("GPU dedicada: {}", enabled ? "forzada" : "automática");
    }

    // ==================== JAVA SETTINGS ====================

    public void onJava8Selected() {
        selectedJavaVersion = "8";
        log.info("Seleccionada versión Java 8 para editar");
    }

    public void onJava17Selected() {
        selectedJavaVersion = "17";
        log.info("Seleccionada versión Java 17 para editar");
    }

    public void onJava21Selected() {
        selectedJavaVersion = "21";
        log.info("Seleccionada versión Java 21 para editar");
    }

    public void onBrowseJavaPath(TextField javaPathField) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Seleccionar ejecutable de Java " + selectedJavaVersion);

        // Configurar filtros para archivos ejecutables de Java
        FileChooser.ExtensionFilter exeFilter = new FileChooser.ExtensionFilter(
                "Ejecutables Java", getJavaExecutablePatterns());
        FileChooser.ExtensionFilter allFilter = new FileChooser.ExtensionFilter(
                "Todos los archivos", "*.*");

        fileChooser.getExtensionFilters().addAll(exeFilter, allFilter);
        fileChooser.setSelectedExtensionFilter(exeFilter);

        File selectedFile = fileChooser.showOpenDialog(stage);

        if (selectedFile != null) {
            String path = selectedFile.getAbsolutePath();
            javaPathField.setText(path);
            setJavaPathForSelectedVersion(path);
        }
    }

    private String[] getJavaExecutablePatterns() {
        String os = System.getProperty("os.name").toLowerCase();
        if (os.contains("win")) {
            return new String[]{"*.exe", "*.bat"};
        } else if (os.contains("mac")) {
            return new String[]{"*"};
        } else {
            // Linux/Unix
            return new String[]{"*"};
        }
    }

    public void onJavaPathChanged(String path) {
        if (path == null || path.trim().isEmpty()) {
            setJavaPathForSelectedVersion(""); // Usar automático
            log.info("Usando Java {} automático del sistema", selectedJavaVersion);
        } else {
            setJavaPathForSelectedVersion(path);
            log.info("Ruta de Java {}: {}", selectedJavaVersion, path);
        }
    }

    public void onMinMemoryChanged(int memoryInMB) {
        if (memoryInMB > 0) {
            // Determinar la mejor unidad
            String unit = memoryInMB >= 1024 ? "GB" : "MB";
            int value = memoryInMB >= 1024 ? memoryInMB / 1024 : memoryInMB;

            settings.setMinMemory(value);
            settings.setMinMemoryUnit(unit);
            log.info("RAM mínima: {} {}", value, unit);
        } else {
            log.warn("Intento de establecer RAM mínima inválida: {}", memoryInMB);
            showError("La RAM mínima debe ser mayor a 0");
        }
    }

    public void onMaxMemoryChanged(int memoryInMB) {
        if (memoryInMB > 0) {
            String unit = memoryInMB >= 1024 ? "GB" : "MB";
            int value = memoryInMB >= 1024 ? memoryInMB / 1024 : memoryInMB;

            settings.setMaxMemory(value);
            settings.setMaxMemoryUnit(unit);
            log.info("RAM máxima: {} {}", value, unit);
        } else {
            log.warn("Intento de establecer RAM máxima inválida: {}", memoryInMB);
            showError("La RAM máxima debe ser mayor a 0");
        }
    }

    public void onJvmArgsChanged(String args) {
        settings.setJvmArguments(args);
        log.info("Argumentos JVM actualizados: {}", args);
    }

    // ==================== UTILITY METHODS ====================

    private void setJavaPathForSelectedVersion(String path) {
        switch (selectedJavaVersion) {
            case "8" -> settings.setJre8_path(path);
            case "17" -> settings.setJre17_path(path);
            case "21" -> settings.setJre21_path(path);
        }
    }

    /**
     * Obtener el SettingsManager
     */
    public SettingsManager getSettings() {
        return settings;
    }

    public String getSelectedJavaVersion() {
        return selectedJavaVersion;
    }

    private void openUrl() {
        String url = "https://github.com/CubicLauncher/CubicLauncher";
        try {
            java.awt.Desktop.getDesktop().browse(new java.net.URI(url));
            log.debug("Abriendo URL: {}", url);
        } catch (Exception e) {
            log.error("No se pudo abrir el enlace: {}", url, e);
            showError("No se pudo abrir el enlace: " + url);
        }
    }

    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}