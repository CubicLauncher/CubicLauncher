/*
 * Copyright (C) 2025 Santiagolxx, Notstaff and CubicLauncher contributors
 * AGPL-3.0 — see https://www.gnu.org/licenses/
 */
package com.cubiclauncher.launcher.ui.components.sidebar;

import com.cubiclauncher.launcher.core.InstanceManager.Instance;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

/**
 * Celda de instancia — visual 1:1 con el original.
 */
public class InstanceCell extends ListCell<Instance> {

    @Override
    protected void updateItem(Instance item, boolean empty) {
        super.updateItem(item, empty);

        if (empty || item == null) {
            setText(null);
            setGraphic(null);
            return;
        }

        HBox container = new HBox(2);
        container.setAlignment(Pos.CENTER_LEFT);
        container.setPadding(new Insets(0));
        container.getStyleClass().add("instance-cell");
        container.setPrefHeight(30);

        Label icon = new Label("⛏");
        icon.getStyleClass().add("instance-icon");
        icon.setStyle("-fx-font-size: 14px;");

        VBox info = new VBox(2);
        info.setAlignment(Pos.CENTER_LEFT);

        Label nameLabel = new Label(item.getName());
        nameLabel.getStyleClass().add("instance-name");
        nameLabel.setStyle("-fx-font-size: 12px;");

        info.getChildren().add(nameLabel);
        container.getChildren().addAll(icon, info);

        setGraphic(container);
        setText(null);
    }
}