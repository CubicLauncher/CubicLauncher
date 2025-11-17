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

        // --- Sección de Rendimiento ---
        Label performanceLabel = new Label("Rendimiento");
        performanceLabel.getStyleClass().add("settings-subtitle");
        VBox performanceBox = new VBox(5);
        performanceBox.setPadding(new Insets(5, 0, 0, 10));
        CheckBox useDiscreteGpuCheckbox = new CheckBox("Forzar el uso de la tarjeta gráfica dedicada");
        performanceBox.getChildren().add(useDiscreteGpuCheckbox);

        minecraftTabContent.getChildren().addAll(
                versionVisibilityLabel, versionVisibilityBox,
                new Separator(),
                integrationsLabel, integrationsBox,
                new Separator(),
                performanceLabel, performanceBox
        );
        minecraftTab.setContent(minecraftTabContent);

        // Pestaña de Java
        Tab javaTab = new Tab("Java");
        VBox javaTabContent = new VBox(15);
        javaTabContent.setPadding(new Insets(15));
        javaTabContent.setAlignment(Pos.CENTER_LEFT);

        // --- Sección de Ejecutable de Java ---
        Label javaExecutableLabel = new Label("Ejecutable de Java");
        javaExecutableLabel.getStyleClass().add("settings-subtitle");
        VBox javaExecutableBox = new VBox(5);
        javaExecutableBox.setPadding(new Insets(5, 0, 0, 10));
        HBox javaPathBox = new HBox(5);
        javaPathBox.setAlignment(Pos.CENTER_LEFT);
        TextField javaPathField = new TextField();
        javaPathField.setPromptText("Automático");
        HBox.setHgrow(javaPathField, Priority.ALWAYS);
        Button browseButton = new Button("Examinar...");
        javaPathBox.getChildren().addAll(javaPathField, browseButton);
        javaExecutableBox.getChildren().add(javaPathBox);

        // --- Sección de Asignación de Memoria ---
        Label memoryAllocationLabel = new Label("Asignación de Memoria");
        memoryAllocationLabel.getStyleClass().add("settings-subtitle");
        VBox memoryAllocationBox = new VBox(5);
        memoryAllocationBox.setPadding(new Insets(5, 0, 0, 10));

        // RAM Mínima
        HBox minMemoryBox = new HBox(10);
        minMemoryBox.setAlignment(Pos.CENTER_LEFT);
        Label minRamLabel = new Label("RAM Mínima:");
        TextField minRamField = new TextField("1"); // Valor por defecto
        minRamField.setPrefWidth(80);
        ComboBox<String> minMemoryUnitComboBox = new ComboBox<>();
        minMemoryUnitComboBox.getItems().addAll("GB");
        minMemoryUnitComboBox.setValue("GB");
        minMemoryBox.getChildren().addAll(minRamLabel, minRamField, minMemoryUnitComboBox);

        // RAM Máxima
        HBox maxMemoryBox = new HBox(10);
        maxMemoryBox.setAlignment(Pos.CENTER_LEFT);
        Label maxRamLabel = new Label("RAM Máxima:");
        TextField maxRamField = new TextField("4"); // Valor por defecto
        maxRamField.setPrefWidth(80);
        ComboBox<String> maxMemoryUnitComboBox = new ComboBox<>();
        maxMemoryUnitComboBox.getItems().addAll("GB");
        maxMemoryUnitComboBox.setValue("GB");
        maxMemoryBox.getChildren().addAll(maxRamLabel, maxRamField, maxMemoryUnitComboBox);

        memoryAllocationBox.getChildren().addAll(minMemoryBox, maxMemoryBox);

        // --- Sección de Ajustes Avanzados (movida desde Minecraft) ---
        Label advancedLabel = new Label("Avanzado");
        advancedLabel.getStyleClass().add("settings-subtitle");
        VBox advancedBox = new VBox(5);
        advancedBox.setPadding(new Insets(5, 0, 0, 10));
        Label launchParamsLabel = new Label("Argumentos de JVM:");
        launchParamsLabel.getStyleClass().add("jvm-args-label");
        TextField launchParamsField = new TextField();
        launchParamsField.setPromptText("-XX:+UnlockExperimentalVMOptions ...");
        advancedBox.getChildren().addAll(launchParamsLabel, launchParamsField);

        javaTabContent.getChildren().addAll(
                javaExecutableLabel, javaExecutableBox,
                new Separator(),
                memoryAllocationLabel, memoryAllocationBox,
                new Separator(),
                advancedLabel, advancedBox
        );
        javaTab.setContent(javaTabContent);

        tabPane.getTabs().addAll(launcherTab, minecraftTab, javaTab);

        settingsBox.getChildren().addAll(settingsTitle, tabPane);

        loadStyles.load(settingsBox, "/com.cubiclauncher.launcher/styles/ui.settings.css");
        
        return settingsBox;
    }
}