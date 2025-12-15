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

package com.cubiclauncher.launcher.ui.views;

import com.cubiclauncher.launcher.ui.controllers.SettingsController;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.util.List;

public class SettingsView {
    private static SettingsController controller;
    private static TextField javaPathField;
    private static boolean isScrollingProgrammatically = false;

    public static VBox create(Stage stage) {
        controller = new SettingsController();
        if (stage != null) {
            controller.setStage(stage);
        }

        VBox settingsLayout = new VBox(20);
        settingsLayout.setAlignment(Pos.TOP_CENTER);
        settingsLayout.setPadding(new Insets(20));

        Label settingsTitle = new Label("Ajustes");
        settingsTitle.getStyleClass().add("welcome-title");

        VBox launcherSection = createSection("Launcher", createLauncherPane());
        VBox minecraftSection = createSection("Minecraft", createMinecraftPane());
        VBox javaSection = createSection("Java", createJavaPane());

        VBox contentVBox = new VBox(30);
        contentVBox.setPadding(new Insets(5));
        contentVBox.getStyleClass().add("settings-content");
        contentVBox.getChildren().addAll(launcherSection, minecraftSection, javaSection);

        ScrollPane scrollPane = new ScrollPane(contentVBox);
        scrollPane.setFitToWidth(true);
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scrollPane.getStyleClass().add("settings-scroll-pane");

        VBox navigationMenu = createNavigationMenu(scrollPane, contentVBox, launcherSection, minecraftSection, javaSection);

        scrollPane.vvalueProperty().addListener((obs, oldVal, newVal) -> {
            if (!isScrollingProgrammatically) {
                updateNavigationSelection(scrollPane, contentVBox, navigationMenu, List.of(launcherSection, minecraftSection, javaSection));
            }
        });

        HBox mainLayout = new HBox(20, navigationMenu, scrollPane);
        HBox.setHgrow(scrollPane, Priority.ALWAYS);
        VBox.setVgrow(mainLayout, Priority.ALWAYS);

        settingsLayout.getChildren().addAll(settingsTitle, mainLayout);
        return settingsLayout;
    }

    private static VBox createNavigationMenu(ScrollPane scrollPane, VBox content, Node... sections) {
        VBox navigation = new VBox(10);
        navigation.setPadding(new Insets(10));
        navigation.getStyleClass().add("settings-navigation");
        navigation.setPrefWidth(150);

        Button launcherButton = createNavButton("Launcher");
        Button minecraftButton = createNavButton("Minecraft");
        Button javaButton = createNavButton("Java");

        launcherButton.setOnAction(e -> scrollTo(scrollPane, content, sections[0], navigation, launcherButton));
        minecraftButton.setOnAction(e -> scrollTo(scrollPane, content, sections[1], navigation, minecraftButton));
        javaButton.setOnAction(e -> scrollTo(scrollPane, content, sections[2], navigation, javaButton));

        navigation.getChildren().addAll(launcherButton, minecraftButton, javaButton);
        Platform.runLater(() -> launcherButton.getStyleClass().add("selected"));
        return navigation;
    }

    private static Button createNavButton(String text) {
        Button button = new Button(text);
        button.setMaxWidth(Double.MAX_VALUE);
        button.getStyleClass().add("nav-button");
        return button;
    }

    private static void scrollTo(ScrollPane scrollPane, VBox content, Node section, VBox navigation, Button button) {
        isScrollingProgrammatically = true;
        updateSelectedButton(navigation, button);

        double contentHeight = content.getBoundsInLocal().getHeight();
        double nodeY = section.getBoundsInParent().getMinY();
        double vValue = nodeY / (contentHeight - scrollPane.getViewportBounds().getHeight());

        Timeline timeline = new Timeline();
        KeyValue kv = new KeyValue(scrollPane.vvalueProperty(), vValue);
        KeyFrame kf = new KeyFrame(Duration.millis(300), kv);
        timeline.getKeyFrames().add(kf);
        timeline.setOnFinished(event -> isScrollingProgrammatically = false);
        timeline.play();
    }

    private static void updateNavigationSelection(ScrollPane scrollPane, VBox content, VBox navigation, List<Node> sections) {
        double viewportHeight = scrollPane.getViewportBounds().getHeight();
        double scrollY = (content.getBoundsInLocal().getHeight() - viewportHeight) * scrollPane.getVvalue();
        Node closestSection = getNode(sections, scrollY, viewportHeight);

        if (closestSection != null) {
            int selectedIndex = sections.indexOf(closestSection);
            if (selectedIndex != -1) {
                updateSelectedButton(navigation, (Button) navigation.getChildren().get(selectedIndex));
            }
        }
    }

    private static Node getNode(List<Node> sections, double scrollY, double viewportHeight) {
        double viewportCenter = scrollY + viewportHeight / 2;

        Node closestSection = null;
        double minDistance = Double.MAX_VALUE;

        for (Node section : sections) {
            double sectionCenter = section.getBoundsInParent().getMinY() + section.getBoundsInLocal().getHeight() / 2;
            double distance = Math.abs(sectionCenter - viewportCenter);
            if (distance < minDistance) {
                minDistance = distance;
                closestSection = section;
            }
        }
        return closestSection;
    }

    private static void updateSelectedButton(VBox parent, Button selectedButton) {
        parent.getChildren().forEach(node -> node.getStyleClass().remove("selected"));
        selectedButton.getStyleClass().add("selected");
    }

    private static VBox createSection(String title, VBox pane) {
        VBox sectionWrapper = new VBox(10);
        Label sectionTitle = new Label(title);
        sectionTitle.getStyleClass().add("settings-category-title");
        sectionWrapper.getChildren().addAll(sectionTitle, pane);
        return sectionWrapper;
    }

    private static VBox createLauncherPane() {
        VBox pane = new VBox(15);
        pane.getStyleClass().add("settings-card");
        HBox languageBox = createLanguageSelector();
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
        VBox infoSection = createLauncherInfo();
        pane.getChildren().addAll(languageBox, autoUpdateCheckbox, errorConsoleCheckbox, closeLaunchCheckbox, nativeStyles, new Separator(), infoSection);
        return pane;
    }

    private static HBox createLanguageSelector() {
        HBox box = new HBox(10);
        box.setAlignment(Pos.CENTER_LEFT);
        Label label = new Label("Idioma:");
        ComboBox<String> comboBox = new ComboBox<>();
        comboBox.getItems().addAll("Español", "English");
        comboBox.setValue(controller.getSettings().getLanguage());
        comboBox.setOnAction(e -> controller.onLanguageChanged(comboBox.getValue()));
        box.getChildren().addAll(label, comboBox);
        return box;
    }

    private static VBox createLauncherInfo() {
        VBox infoBox = new VBox(5);
        Label versionLabel = new Label("Versión: 2501a");
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

    private static VBox createMinecraftPane() {
        VBox pane = new VBox(15);
        pane.getStyleClass().add("settings-card");
        pane.getChildren().addAll(createVersionVisibilitySection(), new Separator(), createIntegrationsSection(), new Separator(), createPerformanceSection());
        return pane;
    }

    private static Node createVersionVisibilitySection() {
        VBox subSection = new VBox(10);
        Label title = new Label("Visibilidad de Versiones");
        title.getStyleClass().add("settings-subtitle");
        CheckBox showAlphas = new CheckBox("Mostrar versiones Alpha (muy inestables)");
        showAlphas.setSelected(controller.getSettings().isShowAlphaVersions());
        showAlphas.setOnAction(e -> controller.onShowAlphasChanged(showAlphas.isSelected()));
        CheckBox showBetas = new CheckBox("Mostrar versiones betas");
        showBetas.setSelected(controller.getSettings().isShowBetaVersions());
        showBetas.setOnAction(e -> controller.onShowBetasChanged(showBetas.isSelected()));
        subSection.getChildren().addAll(title, showAlphas, showBetas);
        return subSection;
    }

    private static Node createIntegrationsSection() {
        VBox subSection = new VBox(10);
        Label title = new Label("Integraciones");
        title.getStyleClass().add("settings-subtitle");
        subSection.getChildren().add(title);
        return subSection;
    }

    private static Node createPerformanceSection() {
        VBox subSection = new VBox(10);
        Label title = new Label("Rendimiento");
        title.getStyleClass().add("settings-subtitle");
        CheckBox useDiscreteGpu = new CheckBox("Forzar el uso de la tarjeta gráfica dedicada");
        useDiscreteGpu.setSelected(controller.getSettings().isForceDiscreteGpu());
        useDiscreteGpu.setOnAction(e -> controller.onUseDiscreteGpuChanged(useDiscreteGpu.isSelected()));
        subSection.getChildren().addAll(title, useDiscreteGpu);
        return subSection;
    }

    private static VBox createJavaPane() {
        VBox pane = new VBox(15);
        pane.getStyleClass().add("settings-card");
        pane.getChildren().addAll(createJavaExecutableSection(), new Separator(), createMemoryAllocationSection(), new Separator(), createAdvancedSection());
        return pane;
    }

    private static Node createJavaExecutableSection() {
        VBox subSection = new VBox(10);
        Label title = new Label("Ejecutable de Java");
        title.getStyleClass().add("settings-subtitle");
        HBox radioContainer = createJavaVersionRadioButtons();
        HBox pathBox = new HBox(5);
        pathBox.setAlignment(Pos.CENTER_LEFT);
        javaPathField = new TextField();
        javaPathField.setPromptText("Ruta automática (dejar vacío)");
        String initialPath = controller.getSettings().getJava8Path();
        if (initialPath != null && !initialPath.isEmpty()) {
            javaPathField.setText(initialPath);
        }
        javaPathField.textProperty().addListener((obs, oldVal, newVal) -> controller.onJavaPathChanged(newVal));
        HBox.setHgrow(javaPathField, Priority.ALWAYS);
        Button browseButton = new Button("Examinar...");
        browseButton.setOnAction(e -> controller.onBrowseJavaPath(javaPathField));
        pathBox.getChildren().addAll(new Label("Ruta:"), javaPathField, browseButton);
        subSection.getChildren().addAll(title, radioContainer, pathBox);
        return subSection;
    }

    private static HBox createJavaVersionRadioButtons() {
        HBox radioContainer = new HBox(15);
        radioContainer.setAlignment(Pos.CENTER_LEFT);
        ToggleGroup javaVersionGroup = new ToggleGroup();
        RadioButton java8Radio = new RadioButton("Java 8");
        java8Radio.setToggleGroup(javaVersionGroup);
        java8Radio.setSelected(true);
        java8Radio.setOnAction(e -> {
            controller.onJava8Selected();
            updateJavaPathField();
        });
        RadioButton java17Radio = new RadioButton("Java 17");
        java17Radio.setToggleGroup(javaVersionGroup);
        java17Radio.setOnAction(e -> {
            controller.onJava17Selected();
            updateJavaPathField();
        });
        RadioButton java21Radio = new RadioButton("Java 21");
        java21Radio.setToggleGroup(javaVersionGroup);
        java21Radio.setOnAction(e -> {
            controller.onJava21Selected();
            updateJavaPathField();
        });
        radioContainer.getChildren().addAll(new Label("Editar ruta de:"), java8Radio, java17Radio, java21Radio);
        return radioContainer;
    }

    private static void updateJavaPathField() {
        if (javaPathField != null) {
            String currentVersion = controller.getSelectedJavaVersion();
            String path = "";
            switch (currentVersion) {
                case "8" -> path = controller.getSettings().getJava8Path();
                case "17" -> path = controller.getSettings().getJava17Path();
                case "21" -> path = controller.getSettings().getJava21path();
            }
            javaPathField.setText(path != null ? path : "");
        }
    }

    private static Node createMemoryAllocationSection() {
        VBox subSection = new VBox(10);
        Label title = new Label("Asignación de Memoria");
        title.getStyleClass().add("settings-subtitle");
        int minMemMB = controller.getSettings().getMinMemoryInMB();
        int maxMemMB = controller.getSettings().getMaxMemoryInMB();
        HBox minMemoryBox = createMemoryInput("RAM Mínima:", minMemMB, controller::onMinMemoryChanged);
        HBox maxMemoryBox = createMemoryInput("RAM Máxima:", maxMemMB, controller::onMaxMemoryChanged);
        subSection.getChildren().addAll(title, minMemoryBox, maxMemoryBox);
        return subSection;
    }

    private static HBox createMemoryInput(String labelText, int memoryInMB, java.util.function.Consumer<Integer> onChange) {
        HBox box = new HBox(10);
        box.setAlignment(Pos.CENTER_LEFT);
        Label label = new Label(labelText);
        TextField field = new TextField();
        field.setPrefWidth(80);
        ComboBox<String> unit = new ComboBox<>();
        unit.getItems().addAll("MB", "GB");
        unit.setValue(memoryInMB >= 1024 ? "GB" : "MB");
        if ("GB".equals(unit.getValue())) {
            field.setText(String.valueOf(memoryInMB / 1024));
        } else {
            field.setText(String.valueOf(memoryInMB));
        }
        unit.setOnAction(e -> {
            try {
                int currentValue = Integer.parseInt(field.getText());
                if ("GB".equals(unit.getValue())) {
                    field.setText(String.valueOf(currentValue / 1024));
                } else {
                    field.setText(String.valueOf(currentValue * 1024));
                }
            } catch (NumberFormatException ignored) {}
        });
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

    private static Node createAdvancedSection() {
        VBox subSection = new VBox(10);
        Label title = new Label("Avanzado");
        title.getStyleClass().add("settings-subtitle");
        Label argsLabel = new Label("Argumentos de JVM:");
        argsLabel.getStyleClass().add("jvm-args-label");
        TextField argsField = new TextField();
        argsField.setPromptText("-XX:+UnlockExperimentalVMOptions ...");
        if (controller.getSettings().getJvmArguments() != null && !controller.getSettings().getJvmArguments().isEmpty()) {
            argsField.setText(controller.getSettings().getJvmArguments());
        }
        argsField.textProperty().addListener((obs, oldVal, newVal) -> controller.onJvmArgsChanged(newVal));
        subSection.getChildren().addAll(title, argsLabel, argsField);
        return subSection;
    }
}
