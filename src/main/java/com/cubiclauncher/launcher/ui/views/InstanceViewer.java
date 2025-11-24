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

import com.cubiclauncher.launcher.core.InstanceManager;
import com.cubiclauncher.launcher.ui.controllers.LauncherController;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

import java.util.function.Consumer;

public class InstanceViewer extends BorderPane {
    private static InstanceViewer instance;
    private InstanceManager.Instance currentInstance;
    private Consumer<InstanceManager.Instance> onPlayAction;
    private Label instanceName;
    private Label instanceVersion;
    private Button playButton;
    private TabPane contentTabs;

    // Constructor privado para Singleton
    private InstanceViewer() {
        super();
        getStyleClass().add("instance-viewer");
        initializeHeader();
        initializeContent();
        showEmptyState();
    }

    // Método estático para obtener la instancia única
    public static InstanceViewer getInstance() {
        if (instance == null) {
            instance = new InstanceViewer();
        }
        return instance;
    }

    // Método para resetear el Singleton (útil para testing)
    public static void resetInstance() {
        instance = null;
    }

    private void initializeHeader() {
        VBox header = new VBox(15);
        header.getStyleClass().add("instance-header");
        header.setPadding(new Insets(30, 40, 20, 40));

        HBox banner = new HBox(20);
        banner.setAlignment(Pos.CENTER_LEFT);

        // Imagen placeholder de la instancia
        StackPane imageContainer = new StackPane();
        imageContainer.getStyleClass().add("instance-image-container");
        imageContainer.setMinSize(460, 215);
        imageContainer.setMaxSize(460, 215);

        Label imagePlaceholder = new Label("IMAGEN DE LA INSTANCIA");
        imagePlaceholder.getStyleClass().add("instance-image-placeholder");
        imageContainer.getChildren().add(imagePlaceholder);

        // Información de la instancia
        VBox info = new VBox(8);
        instanceName = new Label();
        instanceName.getStyleClass().add("instance-title");

        instanceVersion = new Label();
        instanceVersion.getStyleClass().add("instance-subtitle");

        HBox stats = new HBox(30);
        stats.setAlignment(Pos.CENTER_LEFT);

        VBox lastPlayed = createStatBox("ÚLTIMA VEZ", "Hace 2 días");
        VBox playTime = createStatBox("TIEMPO TOTAL", "23.9 h");
        VBox achievements = createStatBox("LOGROS", "0/0");

        stats.getChildren().addAll(lastPlayed, playTime, achievements);

        HBox actionBar = new HBox(15);
        actionBar.setAlignment(Pos.CENTER_LEFT);

        playButton = new Button("JUGAR");
        playButton.getStyleClass().add("play-button-large");
        playButton.setOnAction(e -> {
            LauncherController.launchInstance(getInstance().currentInstance.getName());
        });

        Button optionsButton = new Button("Gestionar");
        optionsButton.getStyleClass().add("options-button");

        actionBar.getChildren().addAll(playButton, optionsButton);
        info.getChildren().addAll(instanceName, instanceVersion, stats, actionBar);
        banner.getChildren().addAll(imageContainer, info);

        header.getChildren().add(banner);
        setTop(header);
    }

    private VBox createStatBox(String title, String value) {
        VBox box = new VBox(2);
        box.setAlignment(Pos.CENTER_LEFT);

        Label titleLabel = new Label(title);
        titleLabel.getStyleClass().add("stat-title");

        Label valueLabel = new Label(value);
        valueLabel.getStyleClass().add("stat-value");

        box.getChildren().addAll(titleLabel, valueLabel);
        return box;
    }

    private void initializeContent() {
        contentTabs = new TabPane();
        contentTabs.getStyleClass().add("instance-tabs");

        // Pestaña de Detalles
        Tab detailsTab = new Tab("DETALLES");
        detailsTab.setClosable(false);
        detailsTab.setContent(createDetailsContent());

        // Pestaña de Configuración
        Tab settingsTab = new Tab("CONFIGURACIÓN");
        settingsTab.setClosable(false);
        settingsTab.setContent(createSettingsContent());

        contentTabs.getTabs().addAll(detailsTab, settingsTab);
        setCenter(contentTabs);
    }

    private VBox createDetailsContent() {
        VBox content = new VBox(15);
        content.setPadding(new Insets(20));

        Label description = new Label("Información detallada de la instancia");
        description.getStyleClass().add("content-description");

        // Información de la versión
        VBox versionInfo = new VBox(10);
        versionInfo.getStyleClass().add("info-box");

        Label versionTitle = new Label("Versión de Minecraft");
        versionTitle.getStyleClass().add("info-title");

        Label versionValue = new Label();
        versionValue.getStyleClass().add("info-value");
        // Usamos un binding para mostrar la versión de la instancia actual
        versionValue.textProperty().bind(
                javafx.beans.binding.Bindings.when(
                        javafx.beans.binding.Bindings.isNotNull(currentInstanceProperty())
                ).then(
                        javafx.beans.binding.Bindings.selectString(currentInstanceProperty(), "version")
                ).otherwise("No seleccionada")
        );

        // Información adicional
        Label pathTitle = new Label("Ubicación");
        pathTitle.getStyleClass().add("info-title");

        Label pathValue = new Label();
        pathValue.getStyleClass().add("info-value");
        pathValue.textProperty().bind(
                javafx.beans.binding.Bindings.when(
                        javafx.beans.binding.Bindings.isNotNull(currentInstanceProperty())
                ).then(
                        javafx.beans.binding.Bindings.createStringBinding(
                                () -> "./instances/" + (currentInstance != null ? currentInstance.getName() : ""),
                                currentInstanceProperty()
                        )
                ).otherwise("No seleccionada")
        );

        versionInfo.getChildren().addAll(versionTitle, versionValue, pathTitle, pathValue);
        content.getChildren().addAll(description, versionInfo);

        return content;
    }

    private VBox createSettingsContent() {
        VBox content = new VBox(15);
        content.setPadding(new Insets(20));

        Label description = new Label("Configuración específica de esta instancia");
        description.getStyleClass().add("content-description");

        // Configuraciones específicas de la instancia
        VBox settingsContainer = new VBox(10);

        CheckBox autoUpdate = new CheckBox("Actualizar automáticamente");
        CheckBox enableSnapshots = new CheckBox("Permitir snapshots");
        CheckBox keepLauncherOpen = new CheckBox("Mantener launcher abierto");

        settingsContainer.getChildren().addAll(autoUpdate, enableSnapshots, keepLauncherOpen);
        content.getChildren().addAll(description, settingsContainer);

        return content;
    }

    private void showEmptyState() {
        instanceName.setText("Selecciona una instancia");
        instanceVersion.setText("Elige una instancia para ver los detalles");
        playButton.setDisable(true);
        contentTabs.setDisable(true);
    }

    public void showInstance(InstanceManager.Instance instance) {
        this.currentInstance = instance;

        if (instance != null) {
            instanceName.setText(instance.getName());

            // Limpiar la versión para mostrar solo Minecraft
            String version = cleanVersion(instance.getVersion());
            instanceVersion.setText(version);

            playButton.setDisable(false);
            contentTabs.setDisable(false);
        } else {
            showEmptyState();
        }
    }

    private String cleanVersion(String version) {
        if (version == null) return "Minecraft";

        // Remover quilt, fabric, forge, etc.
        String cleaned = version
                .replace("quilt", "")
                .replace("fabric", "")
                .replace("forge", "")
                .replace("--", "-")
                .replace("-loader", "")
                .trim();

        // Limpiar guiones extras
        if (cleaned.startsWith("-")) cleaned = cleaned.substring(1);
        if (cleaned.endsWith("-")) cleaned = cleaned.substring(0, cleaned.length() - 1);

        return cleaned.isEmpty() ? "Minecraft" : "Minecraft " + cleaned;
    }

    public void setOnPlayAction(Consumer<InstanceManager.Instance> handler) {
        this.onPlayAction = handler;
    }

    // Property para bindings
    private javafx.beans.property.ObjectProperty<InstanceManager.Instance> currentInstanceProperty() {
        return new javafx.beans.property.SimpleObjectProperty<>(currentInstance);
    }
}