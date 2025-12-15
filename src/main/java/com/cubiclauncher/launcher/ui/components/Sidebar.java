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

package com.cubiclauncher.launcher.ui.components;

import com.cubiclauncher.launcher.core.InstanceManager;
import com.cubiclauncher.launcher.core.InstanceManager.Instance;
import com.cubiclauncher.launcher.core.TaskManager;
import com.cubiclauncher.launcher.core.events.EventBus;
import com.cubiclauncher.launcher.core.events.EventType;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

import java.util.Optional;
import java.util.function.Consumer;

public class Sidebar extends VBox {
    private final ListView<Instance> instancesList;
    private final MenuButton instanceActionsButton;
    private Consumer<Instance> onInstanceSelected;
    private Runnable onSettingsAction;
    private Runnable onVersionsAction;
    private static final EventBus eventBus = EventBus.get();
    private static final TaskManager taskManager = TaskManager.getInstance();

    public Sidebar() {
        super(10);
        this.instancesList = new ListView<>(); // Initialize instancesList here

        setPadding(new Insets(15));
        getStyleClass().add("sidebar");
        setPrefWidth(280);
        setMinWidth(280);
        // Header con logo y perfil
        HBox header = new HBox(12);
        header.setAlignment(Pos.CENTER_LEFT);
        header.setPadding(new Insets(0, 0, 20, 0));

        Label title = new Label("CUBICLAUNCHER");
        title.getStyleClass().add("sidebar-title");

        HBox.setHgrow(header, Priority.ALWAYS);
        header.getChildren().add(title);

        // Encabezado de la lista de instancias
        HBox instancesHeader = new HBox();
        instancesHeader.setAlignment(Pos.CENTER_LEFT);
        Label instancesLabel = new Label("TUS INSTANCIAS");
        instancesLabel.getStyleClass().add("instances-label");
        HBox spacer = new HBox();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        // Botón de menú de acciones de instancia
        instanceActionsButton = new MenuButton("Acciones");
        instanceActionsButton.getStyleClass().add("menu-button-sidebar");
        instanceActionsButton.setVisible(false); // Oculto por defecto

        MenuItem renameItem = new MenuItem("Renombrar");
        renameItem.setOnAction(e -> {
            Instance selected = instancesList.getSelectionModel().getSelectedItem();
            if (selected != null) {
                showRenameDialog(selected);
            }
        });

        MenuItem deleteItem = new MenuItem("Borrar");
        deleteItem.setOnAction(e -> {
            Instance selected = instancesList.getSelectionModel().getSelectedItem();
            if (selected != null) {
                InstanceManager.getInstance().deleteInstance(selected.getName());
            }
        });
        instanceActionsButton.getItems().addAll(renameItem, deleteItem);

        instancesHeader.getChildren().addAll(instancesLabel, spacer, instanceActionsButton);

        // Lista de instancias
        instancesList.getStyleClass().add("instance-list");
        VBox.setVgrow(instancesList, Priority.ALWAYS);
        instancesList.setCellFactory(lv -> new InstanceCell());

        // Cargar instancias
        refreshInstances();

        // Botones de acción en la parte inferior
        VBox actionButtons = new VBox(8);
        actionButtons.setPadding(new Insets(10, 0, 0, 0));

        Button versionsButton = createActionButton("Descargar Versiones");
        Button settingsButton = createActionButton("Ajustes");

        versionsButton.setOnAction(e -> {
            if (onVersionsAction != null) onVersionsAction.run();
        });

        settingsButton.setOnAction(e -> {
            if (onSettingsAction != null) onSettingsAction.run();
        });

        actionButtons.getChildren().addAll(versionsButton, settingsButton);

        getChildren().addAll(header, instancesHeader, instancesList, actionButtons);

        // Configurar selección
        instancesList.getSelectionModel().selectedItemProperty().addListener((obs, old, selected) -> {
            instanceActionsButton.setVisible(selected != null);
            if (selected != null && onInstanceSelected != null) {
                onInstanceSelected.accept(selected);
            }
        });
        eventBus.subscribe(EventType.INSTANCE_CREATED, eventData -> taskManager.runAsyncAtJFXThread(this::refreshInstances));
        eventBus.subscribe(EventType.INSTANCE_DELETED, eventData -> taskManager.runAsyncAtJFXThread(this::refreshInstances));
        eventBus.subscribe(EventType.INSTANCE_RENAME, eventData -> taskManager.runAsyncAtJFXThread(this::refreshInstances));
    }

    private void showRenameDialog(Instance instance) {
        TextInputDialog dialog = new TextInputDialog(instance.getName());
        dialog.setTitle("Renombrar Instancia");
        dialog.setHeaderText("Renombrar la instancia '" + instance.getName() + "'");
        dialog.setContentText("Nuevo nombre:");

        Optional<String> result = dialog.showAndWait();
        result.ifPresent(newName -> {
            if (!newName.isEmpty() && !newName.equals(instance.getName())) {
                InstanceManager.getInstance().renameInstance(instance.getName(), newName);
            }
        });
    }

    private Button createActionButton(String text) {
        Button button = new Button(text);
        button.getStyleClass().add("sidebar-action-button");
        button.setMaxWidth(Double.MAX_VALUE);
        button.setAlignment(Pos.CENTER_LEFT);
        return button;
    }

    public void refreshInstances() {
        instancesList.getItems().setAll(InstanceManager.getInstance().getAllInstances());
    }

    public void clearSelection() {
        instancesList.getSelectionModel().clearSelection();
    }

    public void setOnInstanceSelected(Consumer<Instance> handler) {
        this.onInstanceSelected = handler;
    }

    public void setOnSettingsAction(Runnable action) {
        this.onSettingsAction = action;
    }

    public void setOnVersionsAction(Runnable action) {
        this.onVersionsAction = action;
    }

    private static class InstanceCell extends ListCell<Instance> {
        @Override
        protected void updateItem(Instance item, boolean empty) {
            super.updateItem(item, empty);
            if (empty || item == null) {
                setText(null);
                setGraphic(null);
            } else {
                HBox container = new HBox(2);
                container.setAlignment(Pos.CENTER_LEFT);
                container.setPadding(new Insets(0));  // Cambiado de 1 a 0
                container.getStyleClass().add("instance-cell");
                container.setPrefHeight(30);  // Establecemos altura preferida

                // Icono de Minecraft
                Label icon = new Label("⛏");
                icon.getStyleClass().add("instance-icon");
                // Ajustar el tamaño del icono si es necesario
                icon.setStyle("-fx-font-size: 14px;");  // Tamaño de icono

                VBox info = new VBox(2);
                info.setAlignment(Pos.CENTER_LEFT);
                Label nameLabel = new Label(item.getName());
                nameLabel.getStyleClass().add("instance-name");
                nameLabel.setStyle("-fx-font-size: 12px;");  // Tamaño de fuente más pequeño

                info.getChildren().addAll(nameLabel);
                container.getChildren().addAll(icon, info);

                setGraphic(container);
            }
        }
    }
    }