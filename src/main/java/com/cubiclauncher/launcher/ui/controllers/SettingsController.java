package com.cubiclauncher.launcher.ui.controllers;

import com.cubiclauncher.launcher.util.SettingsManager;
import javafx.scene.control.*;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;
import java.io.File;

/**
 * Controlador para manejar todas las acciones de la vista de configuración
 */
public class SettingsController {
    private final SettingsManager settings;
    private Stage stage;

    public SettingsController() {
        this.settings = SettingsManager.getInstance();
    }

    public void setStage(Stage stage) {
        this.stage = stage;
    }

    // ==================== LAUNCHER SETTINGS ====================

    public void onLanguageChanged(String language) {
        settings.setLanguage(language);
        System.out.println("Idioma cambiado a: " + language);
        // TODO: Implementar cambio de idioma en la UI
    }

    public void onAutoUpdateChanged(boolean enabled) {
        settings.setAutoUpdate(enabled);
        System.out.println("Auto-actualización: " + (enabled ? "activada" : "desactivada"));
    }

    public void onErrorConsoleChanged(boolean enabled) {
        settings.setErrorConsole(enabled);
        System.out.println("Consola de errores: " + (enabled ? "activada" : "desactivada"));
    }

    public void onCloseLauncherChanged(boolean enabled) {
        settings.setCloseLauncherOnGameStart(enabled);
        System.out.println("Cerrar launcher: " + (enabled ? "activado" : "desactivado"));
    }
    public void onNativeStylesChanged(boolean enabled) {
        settings.setNativeStyles(enabled);
        System.out.println("Estilos nativos: " + (enabled ? "activado" : "desactivado"));
    }
    public void onSourceCodeLinkClicked() {
        openUrl("https://github.com/CubicLauncher/CubicLauncher");
    }

    // ==================== MINECRAFT SETTINGS ====================

    public void onShowAlphasChanged(boolean enabled) {
        settings.setShowAlphaVersions(enabled);
        System.out.println("Mostrar alphas: " + (enabled ? "activado" : "desactivado"));
    }

    public void onShowBetasChanged(boolean enabled) {
        settings.setShowBetaVersions(enabled);
        System.out.println("Mostrar betas: " + (enabled ? "activado" : "desactivado"));
    }

    public void onDiscordPresenceChanged(boolean enabled) {
        settings.setDiscordRichPresence(enabled);
        System.out.println("Discord Rich Presence: " + (enabled ? "activado" : "desactivado"));

        if (enabled) {
            // TODO: Inicializar Discord Rich Presence
        } else {
            // TODO: Desconectar Discord Rich Presence
        }
    }

    public void onUseDiscreteGpuChanged(boolean enabled) {
        settings.setForceDiscreteGpu(enabled);
        System.out.println("GPU dedicada: " + (enabled ? "forzada" : "automática"));
    }

    // ==================== JAVA SETTINGS ====================

    public void onBrowseJavaPath(TextField javaPathField) {
        DirectoryChooser directoryChooser = new DirectoryChooser();
        directoryChooser.setTitle("Seleccionar ejecutable de Java");

        File selectedDirectory = directoryChooser.showDialog(stage);

        if (selectedDirectory != null) {
            String path = selectedDirectory.getAbsolutePath();
            javaPathField.setText(path);
            settings.setJavaPath(path);
            System.out.println("Ruta de Java establecida: " + path);
        }
    }

    public void onJavaPathChanged(String path) {
        if (path == null || path.trim().isEmpty()) {
            settings.setJavaPath(null); // Usar automático
            System.out.println("Usando Java automático del sistema");
        } else {
            settings.setJavaPath(path);
            System.out.println("Ruta de Java: " + path);
        }
    }

    public void onMinMemoryChanged(int memoryInMB) {
        if (memoryInMB > 0) {
            // Determinar la mejor unidad
            String unit = memoryInMB >= 1024 ? "GB" : "MB";
            int value = memoryInMB >= 1024 ? memoryInMB / 1024 : memoryInMB;

            settings.setMinMemory(value);
            settings.setMinMemoryUnit(unit);
            System.out.println("RAM mínima: " + value + " " + unit);
        } else {
            showError("La RAM mínima debe ser mayor a 0");
        }
    }

    public void onMaxMemoryChanged(int memoryInMB) {
        if (memoryInMB > 0) {
            String unit = memoryInMB >= 1024 ? "GB" : "MB";
            int value = memoryInMB >= 1024 ? memoryInMB / 1024 : memoryInMB;

            settings.setMaxMemory(value);
            settings.setMaxMemoryUnit(unit);
            System.out.println("RAM máxima: " + value + " " + unit);
        } else {
            showError("La RAM máxima debe ser mayor a 0");
        }
    }

    public void onJvmArgsChanged(String args) {
        settings.setJvmArguments(args);
        System.out.println("Argumentos JVM actualizados: " + args);
    }

    // ==================== UTILITY METHODS ====================

    /**
     * Carga los valores guardados en los controles de la UI
     */
    public void loadSettings(
            ComboBox<String> languageCombo,
            CheckBox autoUpdate,
            CheckBox errorConsole,
            CheckBox closeLauncher,
            CheckBox showAlphas,
            CheckBox showBetas,
            CheckBox discordPresence,
            CheckBox useDiscreteGpu,
            TextField javaPath,
            TextField minMemory,
            TextField maxMemory,
            TextField jvmArgs
    ) {
        // Cargar valores del SettingsManager
        languageCombo.setValue(settings.getLanguage());
        autoUpdate.setSelected(settings.isAutoUpdate());
        errorConsole.setSelected(settings.isErrorConsole());
        closeLauncher.setSelected(settings.isCloseLauncherOnGameStart());

        showAlphas.setSelected(settings.isShowAlphaVersions());
        showBetas.setSelected(settings.isShowBetaVersions());
        discordPresence.setSelected(settings.isDiscordRichPresence());
        useDiscreteGpu.setSelected(settings.isForceDiscreteGpu());

        if (settings.getJavaPath() != null) {
            javaPath.setText(settings.getJavaPath());
        }
        minMemory.setText(String.valueOf(settings.getMinMemory()));
        maxMemory.setText(String.valueOf(settings.getMaxMemory()));

        if (settings.getJvmArguments() != null) {
            jvmArgs.setText(settings.getJvmArguments());
        }
    }

    private void openUrl(String url) {
        try {
            java.awt.Desktop.getDesktop().browse(new java.net.URI(url));
        } catch (Exception e) {
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

    /**
     * Obtener el SettingsManager
     */
    public SettingsManager getSettings() {
        return settings;
    }
}