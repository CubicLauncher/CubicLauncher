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

import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;

/**
 * A reusable draggable header component for modal dialogs.
 */
public class ModalHeader extends HBox {
    private double xOffset = 0;
    private double yOffset = 0;

    public ModalHeader(String title, Dialog<?> dialog) {
        getStyleClass().add("editor-header");
        setAlignment(Pos.CENTER_LEFT);

        Label titleLabel = new Label(title);
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button closeButton = new Button("✕");
        closeButton.getStyleClass().add("editor-close-button");
        closeButton.setOnAction(e -> {
            dialog.setResult(null);
            dialog.close();
        });

        getChildren().addAll(titleLabel, spacer, closeButton);

        // Dragging logic
        setOnMousePressed(event -> {
            xOffset = event.getSceneX();
            yOffset = event.getSceneY();
        });

        setOnMouseDragged(event -> {
            dialog.setX(event.getScreenX() - xOffset);
            dialog.setY(event.getScreenY() - yOffset);
        });
    }
}
