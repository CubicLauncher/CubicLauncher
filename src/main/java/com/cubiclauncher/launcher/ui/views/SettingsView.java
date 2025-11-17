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
                separator,
                versionLabel,
                devLabel,
                sourceCodeBox
        );
        launcherTab.setContent(launcherTabContent);

        Tab minecraftTab = new Tab("Minecraft");
        VBox minecraftTabContent = new VBox(10);
        minecraftTabContent.setPadding(new Insets(10));
        minecraftTabContent.getChildren().add(new Label("Aquí van los ajustes de apariencia."));
        minecraftTab.setContent(minecraftTabContent);


        tabPane.getTabs().addAll(launcherTab, minecraftTab);

        settingsBox.getChildren().addAll(settingsTitle, tabPane);

        loadStyles.load(settingsBox, "/com.cubiclauncher.launcher/styles/ui.settings.css");
        
        return settingsBox;
    }
}