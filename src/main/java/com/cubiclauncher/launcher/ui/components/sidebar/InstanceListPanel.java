/*
 * Copyright (C) 2025 Santiagolxx, Notstaff and CubicLauncher contributors
 * AGPL-3.0 — see https://www.gnu.org/licenses/
 */
package com.cubiclauncher.launcher.ui.components.sidebar;

import com.cubiclauncher.launcher.core.InstanceManager;
import com.cubiclauncher.launcher.core.InstanceManager.Instance;
import com.cubiclauncher.launcher.core.LanguageManager;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

import java.util.function.Consumer;

/**
 * Panel con el header "Instancias / botón Editar" y la lista de instancias.
 */
public class InstanceListPanel extends VBox {

    private final ListView<Instance> listView = new ListView<>();
    private Consumer<Instance> onInstanceSelected;

    public InstanceListPanel() {
        super(0); // mismo spacing que el original (los hijos del VBox padre tenían gap=10)
        VBox.setVgrow(this, Priority.ALWAYS);

        LanguageManager lm = LanguageManager.getInstance();

        // ── Header ──────────────────────────────────────────────────────────
        HBox header = new HBox();
        header.setAlignment(Pos.CENTER_LEFT);

        Label instancesLabel = new Label(lm.get("sidebar.instances_title"));
        instancesLabel.getStyleClass().add("instances-label");

        HBox spacer = new HBox();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        header.getChildren().addAll(instancesLabel, spacer);

        // ── Lista ────────────────────────────────────────────────────────────
        listView.getStyleClass().add("instance-list");
        VBox.setVgrow(listView, Priority.ALWAYS);
        listView.setCellFactory(lv -> new InstanceCell());

        getChildren().addAll(header, listView);

        // ── Selección ─────────────────────────────────────────────────────────
        listView.getSelectionModel().selectedItemProperty().addListener((obs, old, selected) -> {
            if (selected != null && onInstanceSelected != null) {
                onInstanceSelected.accept(selected);
            }
        });
    }

    public void refresh() {
        listView.getItems().setAll(InstanceManager.getInstance().getAllInstances());
    }

    public void clearSelection() {
        listView.getSelectionModel().clearSelection();
    }

    public void setOnInstanceSelected(Consumer<Instance> handler) {
        this.onInstanceSelected = handler;
    }
}