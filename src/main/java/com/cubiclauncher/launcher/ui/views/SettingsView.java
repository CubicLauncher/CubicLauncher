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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package com.cubiclauncher.launcher.ui.views;

import com.cubiclauncher.launcher.ui.controllers.SettingsController;
import com.cubiclauncher.launcher.util.StylesLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class SettingsView {
    private static SettingsController controller;

    public static VBox create() {
        return create(null);
    }

    public static VBox create(Stage stage) {
        controller = new SettingsController();
        if (stage != null) {
            controller.setStage(stage);
        }

        VBox settingsBox = new VBox(10);
        settingsBox.setAlignment(Pos.CENTER);
        settingsBox.setPadding(new Insets(20));

        Label settingsTitle = new Label("Ajustes");
        settingsTitle.getStyleClass().add("welcome-title");

        TabPane tabPane = createTabPane();

        settingsBox.getChildren().addAll(settingsTitle, tabPane);
        StylesLoader.load(settingsBox, "/com.cubiclauncher.launcher/styles/ui.settings.css");

        return settingsBox;
    }

    private static TabPane createTabPane() {
        TabPane tabPane = new TabPane();
        tabPane.getStyleClass().add("settings-tab-pane");
        VBox.setVgrow(tabPane, Priority.ALWAYS);
        tabPane.setMaxWidth(Double.MAX_VALUE);
        tabPane.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);

        tabPane.getTabs().addAll(
                createLauncherTab(),
                createMinecraftTab(),
                createJavaTab()
        );

        return tabPane;
    }

    // ==================== PESTAÑA LAUNCHER ====================
    private static Tab createLauncherTab() {
        Tab tab = new Tab("Launcher");
        VBox content = new VBox(15);
        content.setAlignment(Pos.CENTER_LEFT);
        content.setPadding(new Insets(15));

        // Idioma con evento
        HBox languageBox = createLanguageSelector();

        // Opciones con eventos conectados
        CheckBox autoUpdateCheckbox = new CheckBox("Habilitar actualizaciones automáticas");
        autoUpdateCheckbox.setSelected(controller.getSettings().isAutoUpdate());
        autoUpdateCheckbox.setOnAction(e -> controller.onAutoUpdateChanged(autoUpdateCheckbox.isSelected()));

        CheckBox errorConsoleCheckbox = new CheckBox("Habilitar consola de errores");
        errorConsoleCheckbox.setSelected(controller.getSettings().isErrorConsole());
        errorConsoleCheckbox.setOnAction(e -> controller.onErrorConsoleChanged(errorConsoleCheckbox.isSelected()));

        CheckBox closeLaunchCheckbox = new CheckBox("Cerrar launcher al abrir el juego");
        closeLaunchCheckbox.setSelected(controller.getSettings().isCloseLauncherOnGameStart());
        closeLaunchCheckbox.setOnAction(e -> controller.onCloseLauncherChanged(closeLaunchCheckbox.isSelected()));

        CheckBox nativeStyles = new CheckBox("Utilizar estilos nativos");
        nativeStyles.setSelected(controller.getSettings().isNative_styles());
        nativeStyles.setOnAction(e -> controller.onNativeStylesChanged(nativeStyles.isSelected()));

        // Información del launcher
        VBox infoSection = createLauncherInfo();

        content.getChildren().addAll(
                languageBox,
                autoUpdateCheckbox,
                errorConsoleCheckbox,
                closeLaunchCheckbox,
                nativeStyles,
                new Separator(),
                infoSection
        );

        tab.setContent(content);
        return tab;
    }

    private static HBox createLanguageSelector() {
        HBox box = new HBox(10);
        box.setAlignment(Pos.CENTER_LEFT);

        Label label = new Label("Idioma:");
        ComboBox<String> comboBox = new ComboBox<>();
        comboBox.getItems().addAll("Español", "English");
        comboBox.setValue(controller.getSettings().getLanguage());

        // Conectar evento
        comboBox.setOnAction(e -> controller.onLanguageChanged(comboBox.getValue()));

        box.getChildren().addAll(label, comboBox);
        return box;
    }

    private static VBox createLauncherInfo() {
        VBox infoBox = new VBox(5);

        Label versionLabel = new Label("Versión: 1.0.0-SNAPSHOT");
        Label devLabel = new Label("Desarrollado por: Santiagolxx, Notstaff & CubicLauncher contributors");

        HBox sourceCodeBox = new HBox(5);
        sourceCodeBox.setAlignment(Pos.CENTER_LEFT);
        Label sourceCodeLabel = new Label("Código Fuente:");
        Hyperlink sourceCodeLink = new Hyperlink("github.com/CubicLauncher/CubicLauncher");
        sourceCodeLink.setOnAction(e -> controller.onSourceCodeLinkClicked());
        sourceCodeBox.getChildren().addAll(sourceCodeLabel, sourceCodeLink);

        infoBox.getChildren().addAll(versionLabel, devLabel, sourceCodeBox);
        return infoBox;
    }

    // ==================== PESTAÑA MINECRAFT ====================
    private static Tab createMinecraftTab() {
        Tab tab = new Tab("Minecraft");
        VBox content = new VBox(15);
        content.setPadding(new Insets(15));
        content.setAlignment(Pos.CENTER_LEFT);

        content.getChildren().addAll(
                createVersionVisibilitySection(),
                new Separator(),
                createIntegrationsSection(),
                new Separator(),
                createPerformanceSection()
        );

        tab.setContent(content);
        return tab;
    }

    private static VBox createVersionVisibilitySection() {
        VBox section = new VBox(5);

        Label title = new Label("Visibilidad de Versiones");
        title.getStyleClass().add("settings-subtitle");

        VBox options = new VBox(5);
        options.setPadding(new Insets(5, 0, 0, 10));

        CheckBox showAlphas = new CheckBox("Mostrar versiones Alpha (muy inestables)");
        showAlphas.setSelected(controller.getSettings().isShowAlphaVersions());
        showAlphas.setOnAction(e -> controller.onShowAlphasChanged(showAlphas.isSelected()));

        CheckBox showBetas = new CheckBox("Mostrar versiones betas");
        showBetas.setSelected(controller.getSettings().isShowBetaVersions());
        showBetas.setOnAction(e -> controller.onShowBetasChanged(showBetas.isSelected()));

        options.getChildren().addAll(showAlphas, showBetas);

        section.getChildren().addAll(title, options);
        return section;
    }

    private static VBox createIntegrationsSection() {
        VBox section = new VBox(5);

        Label title = new Label("Integraciones");
        title.getStyleClass().add("settings-subtitle");

        VBox options = new VBox(5);
        options.setPadding(new Insets(5, 0, 0, 10));

        CheckBox discordPresence = new CheckBox("Habilitar integración con Discord (Rich Presence)");
        discordPresence.setSelected(controller.getSettings().isDiscordRichPresence());
        discordPresence.setOnAction(e -> controller.onDiscordPresenceChanged(discordPresence.isSelected()));

        options.getChildren().add(discordPresence);

        section.getChildren().addAll(title, options);
        return section;
    }

    private static VBox createPerformanceSection() {
        VBox section = new VBox(5);

        Label title = new Label("Rendimiento");
        title.getStyleClass().add("settings-subtitle");

        VBox options = new VBox(5);
        options.setPadding(new Insets(5, 0, 0, 10));

        CheckBox useDiscreteGpu = new CheckBox("Forzar el uso de la tarjeta gráfica dedicada");
        useDiscreteGpu.setSelected(controller.getSettings().isForceDiscreteGpu());
        useDiscreteGpu.setOnAction(e -> controller.onUseDiscreteGpuChanged(useDiscreteGpu.isSelected()));

        options.getChildren().add(useDiscreteGpu);

        section.getChildren().addAll(title, options);
        return section;
    }

    // ==================== PESTAÑA JAVA ====================
    private static Tab createJavaTab() {
        Tab tab = new Tab("Java");
        VBox content = new VBox(15);
        content.setPadding(new Insets(15));
        content.setAlignment(Pos.CENTER_LEFT);

        content.getChildren().addAll(
                createJavaExecutableSection(),
                new Separator(),
                createMemoryAllocationSection(),
                new Separator(),
                createAdvancedSection()
        );

        tab.setContent(content);
        return tab;
    }

    private static VBox createJavaExecutableSection() {
        VBox section = new VBox(5);

        Label title = new Label("Ejecutable de Java");
        title.getStyleClass().add("settings-subtitle");

        VBox options = new VBox(5);
        options.setPadding(new Insets(5, 0, 0, 10));

        HBox pathBox = new HBox(5);
        pathBox.setAlignment(Pos.CENTER_LEFT);

        TextField pathField = new TextField();
        pathField.setPromptText("Automático");
        if (controller.getSettings().getJavaPath() != null) {
            pathField.setText(controller.getSettings().getJavaPath());
        }
        pathField.textProperty().addListener((obs, oldVal, newVal) -> controller.onJavaPathChanged(newVal));

        HBox.setHgrow(pathField, Priority.ALWAYS);

        Button browseButton = new Button("Examinar...");
        browseButton.setOnAction(e -> controller.onBrowseJavaPath(pathField));

        pathBox.getChildren().addAll(pathField, browseButton);

        options.getChildren().add(pathBox);
        section.getChildren().addAll(title, options);
        return section;
    }

    // REEMPLAZAR createMemoryAllocationSection completamente
    private static VBox createMemoryAllocationSection() {
        VBox section = new VBox(5);

        Label title = new Label("Asignación de Memoria");
        title.getStyleClass().add("settings-subtitle");

        VBox options = new VBox(5);
        options.setPadding(new Insets(5, 0, 0, 10));

        // Convertir a MB para trabajar internamente
        int minMemMB = controller.getSettings().getMinMemoryInMB();
        int maxMemMB = controller.getSettings().getMaxMemoryInMB();

        HBox minMemoryBox = createMemoryInput("RAM Mínima:", minMemMB, controller::onMinMemoryChanged);
        HBox maxMemoryBox = createMemoryInput("RAM Máxima:", maxMemMB, controller::onMaxMemoryChanged);

        options.getChildren().addAll(minMemoryBox, maxMemoryBox);
        section.getChildren().addAll(title, options);
        return section;
    }

    // REEMPLAZAR createMemoryInput completamente
    private static HBox createMemoryInput(String labelText, int memoryInMB,
                                          java.util.function.Consumer<Integer> onChange) {
        HBox box = new HBox(10);
        box.setAlignment(Pos.CENTER_LEFT);

        Label label = new Label(labelText);
        TextField field = new TextField();
        field.setPrefWidth(80);

        ComboBox<String> unit = new ComboBox<>();
        unit.getItems().addAll("MB", "GB");
        unit.setValue(memoryInMB >= 1024 ? "GB" : "MB");

        // Mostrar valor en la unidad apropiada
        if ("GB".equals(unit.getValue())) {
            field.setText(String.valueOf(memoryInMB / 1024));
        } else {
            field.setText(String.valueOf(memoryInMB));
        }

        // Cuando cambia la unidad, convertir el valor
        unit.setOnAction(e -> {
            try {
                int currentValue = Integer.parseInt(field.getText());

                if ("GB".equals(unit.getValue())) {
                    // MB -> GB
                    field.setText(String.valueOf(currentValue / 1024));
                } else {
                    // GB -> MB
                    field.setText(String.valueOf(currentValue * 1024));
                }
            } catch (NumberFormatException ignored) {}
        });

        // Guardar cuando cambia el valor
        field.textProperty().addListener((obs, oldVal, newVal) -> {
            if (!newVal.isEmpty() && !newVal.equals(oldVal)) {
                try {
                    int value = Integer.parseInt(newVal);
                    int memoryMB = "GB".equals(unit.getValue()) ? value * 1024 : value;
                    onChange.accept(memoryMB);
                } catch (NumberFormatException ignored) {}
            }
        });

        box.getChildren().addAll(label, field, unit);
        return box;
    }

    private static VBox createAdvancedSection() {
        VBox section = new VBox(5);

        Label title = new Label("Avanzado");
        title.getStyleClass().add("settings-subtitle");

        VBox options = new VBox(5);
        options.setPadding(new Insets(5, 0, 0, 10));

        Label argsLabel = new Label("Argumentos de JVM:");
        argsLabel.getStyleClass().add("jvm-args-label");

        TextField argsField = new TextField();
        argsField.setPromptText("-XX:+UnlockExperimentalVMOptions ...");
        if (controller.getSettings().getJvmArguments() != null &&
                !controller.getSettings().getJvmArguments().isEmpty()) {
            argsField.setText(controller.getSettings().getJvmArguments());
        }
        argsField.textProperty().addListener((obs, oldVal, newVal) -> controller.onJvmArgsChanged(newVal));

        options.getChildren().addAll(argsLabel, argsField);
        section.getChildren().addAll(title, options);
        return section;
    }
}