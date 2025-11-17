package com.cubiclauncher.launcher.ui.views;

import com.cubiclauncher.launcher.util.loadStyles;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

public class SettingsView {

    public static VBox create() {
        VBox settingsBox = new VBox(10);
        settingsBox.setAlignment(Pos.CENTER);
        settingsBox.setPadding(new Insets(20));

        Label settingsTitle = new Label("Ajustes");
        settingsTitle.getStyleClass().add("welcome-title");

        TabPane tabPane = new TabPane();
        tabPane.getStyleClass().add("settings-tab-pane"); // Mantener la clase de estilo
        VBox.setVgrow(tabPane, Priority.ALWAYS);
        tabPane.setMaxWidth(Double.MAX_VALUE);
        tabPane.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);

        // Pestaña General
        Tab launcherTab = new Tab("Launcher");
        VBox launcherTabContent = new VBox(15);
        launcherTabContent.setAlignment(Pos.CENTER_LEFT);
        launcherTabContent.setPadding(new Insets(15));

        CheckBox autoUpdateCheckbox = new CheckBox("Habilitar actualizaciones automáticas");
        CheckBox errorConsoleCheckbox = new CheckBox("Habilitar consola de errores");
        CheckBox closeLauncheropenCheckox = new CheckBox("Cerrar launcher al abrir el juego");

        HBox languageBox = new HBox(10);
        languageBox.setAlignment(Pos.CENTER_LEFT);
        Label languageLabel = new Label("Idioma:");
        ComboBox<String> languageComboBox = new ComboBox<>();
        languageComboBox.getItems().addAll("Español", "English");
        languageComboBox.setValue("Español"); // Valor por defecto
        languageBox.getChildren().addAll(languageLabel, languageComboBox);

        // --- Sección de Información ---
        Separator separator = new Separator();
        separator.setPadding(new Insets(10, 0, 5, 0));

        Label versionLabel = new Label("Versión: 1.0.0-SNAPSHOT");
        Label devLabel = new Label("Desarrollado por: CubicLauncher Team");

        HBox sourceCodeBox = new HBox(5);
        sourceCodeBox.setAlignment(Pos.CENTER_LEFT);
        Label sourceCodeLabel = new Label("Código Fuente:");
        Hyperlink sourceCodeLink = new Hyperlink("github.com/CubicLauncher/CubicLauncher");
        sourceCodeBox.getChildren().addAll(sourceCodeLabel, sourceCodeLink);

        launcherTabContent.getChildren().addAll(
                languageBox,
                autoUpdateCheckbox,
                errorConsoleCheckbox,
                closeLauncheropenCheckox,
                separator,
                versionLabel,
                devLabel,
                sourceCodeBox
        );
        launcherTab.setContent(launcherTabContent);

        Tab minecraftTab = new Tab("Minecraft");
        VBox minecraftTabContent = new VBox(15);
        minecraftTabContent.setPadding(new Insets(15));
        minecraftTabContent.setAlignment(Pos.CENTER_LEFT);

        // --- Sección de Visibilidad de Versiones ---
        Label versionVisibilityLabel = new Label("Visibilidad de Versiones");
        versionVisibilityLabel.getStyleClass().add("settings-subtitle"); // Estilo para subtítulos
        VBox versionVisibilityBox = new VBox(5);
        versionVisibilityBox.setPadding(new Insets(5, 0, 0, 10)); // Indentación para las opciones
        CheckBox showAlphasCheckbox = new CheckBox("Mostrar versiones Alpha (muy inestables)");
        CheckBox showBetasCheckbox = new CheckBox("Mostrar versiones betas");
        versionVisibilityBox.getChildren().addAll(showAlphasCheckbox, showBetasCheckbox);

        // --- Sección de Integraciones ---
        Label integrationsLabel = new Label("Integraciones");
        integrationsLabel.getStyleClass().add("settings-subtitle");
        VBox integrationsBox = new VBox(5);
        integrationsBox.setPadding(new Insets(5, 0, 0, 10));
        CheckBox discordPresenceCheckbox = new CheckBox("Habilitar integración con Discord (Rich Presence)");
        integrationsBox.getChildren().add(discordPresenceCheckbox);

        // --- Sección de Ajustes Avanzados ---
        Label advancedLabel = new Label("Avanzado");
        advancedLabel.getStyleClass().add("settings-subtitle");
        VBox advancedBox = new VBox(5);
        advancedBox.setPadding(new Insets(5, 0, 0, 10));
        Label launchParamsLabel = new Label("Argumentos de JVM:");
        launchParamsLabel.getStyleClass().add("jvm-args-label");
        TextField launchParamsField = new TextField();
        launchParamsField.setPromptText("-Xmx2G -XX:+UnlockExperimentalVMOptions ...");
        advancedBox.getChildren().addAll(launchParamsLabel, launchParamsField);

        minecraftTabContent.getChildren().addAll(
                versionVisibilityLabel, versionVisibilityBox,
                new Separator(),
                integrationsLabel, integrationsBox,
                new Separator(),
                advancedLabel, advancedBox
        );
        minecraftTab.setContent(minecraftTabContent);


        tabPane.getTabs().addAll(launcherTab, minecraftTab);

        settingsBox.getChildren().addAll(settingsTitle, tabPane);

        loadStyles.load(settingsBox, "/com.cubiclauncher.launcher/styles/ui.settings.css");
        
        return settingsBox;
    }
}