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
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

import java.util.function.Consumer;

public class Sidebar extends VBox {
    private final ListView<Instance> instancesList;
    private Consumer<Instance> onInstanceSelected;
    private Runnable onSettingsAction;
    private Runnable onVersionsAction;
    private static final EventBus eventBus = EventBus.get();
    private static final TaskManager taskManager = TaskManager.getInstance();

    public Sidebar() {
        super(10);
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

        // Lista de instancias
        Label instancesLabel = new Label("TUS INSTANCIAS");
        instancesLabel.getStyleClass().add("instances-label");

        instancesList = new ListView<>();
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

        getChildren().addAll(header, instancesLabel, instancesList, actionButtons);

        // Configurar selección
        instancesList.getSelectionModel().selectedItemProperty().addListener((obs, old, selected) -> {
            if (selected != null && onInstanceSelected != null) {
                onInstanceSelected.accept(selected);
            }
        });
        eventBus.subscribe(EventType.INSTANCE_CREATED, eventData -> taskManager.runAsyncAtJFXThread(this::refreshInstances));
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
                HBox container = new HBox(8);
                container.setAlignment(Pos.CENTER_LEFT);
                container.setPadding(new Insets(2));
                container.getStyleClass().add("instance-cell");

                // Icono de Minecraft
                Label icon = new Label("⛏");
                icon.getStyleClass().add("instance-icon");

                VBox info = new VBox(2);
                Label nameLabel = new Label(item.getVersion());
                nameLabel.getStyleClass().add("instance-name");

                info.getChildren().addAll(nameLabel);
                container.getChildren().addAll(icon, info);

                setGraphic(container);
            }
        }

    }
}