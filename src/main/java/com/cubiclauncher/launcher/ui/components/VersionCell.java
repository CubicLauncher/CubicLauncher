/*
 *
 *  * Copyright (C) 2025 Santiagolxx, Notstaff and CubicLauncher contributors
 *  *
 *  * This program is free software: you can redistribute it and/or modify
 *  * it under the terms of the GNU Affero General Public License as published by
 *  * the Free Software Foundation, either version 3 of the License, or
 *  * (at your option) any later version.
 *  *
 *  * This program is distributed in the hope that it will be useful,
 *  * but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  * GNU Affero General Public License for more details.
 *  *
 *  * You should have received a copy of the GNU Affero General Public License
 *  * along with this program.  If not, see https://www.gnu.org/licenses/.
 *
 *
 */
package com.cubiclauncher.launcher.ui.components;

import com.cubiclauncher.launcher.LauncherWrapper;
import javafx.scene.control.ListCell;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.geometry.Pos;

public class VersionCell extends ListCell<String> {
    private final LauncherWrapper launcher = new LauncherWrapper();

    @Override
    protected void updateItem(String item, boolean empty) {
        super.updateItem(item, empty);
        if (empty || item == null) {
            setText(null);
            setGraphic(null);
        } else {
            // Verificar si la versión está instalada
            boolean isInstalled = launcher.getInstalledVersions().contains(item);

            HBox container = new HBox(10);
            container.setAlignment(Pos.CENTER_LEFT);

            Label versionLabel = new Label(item);

            if (isInstalled) {
                // Si está instalada, mostrar con (I)
                Label installedLabel = new Label(item + " (I)");
                installedLabel.setStyle("-fx-text-fill: #4CAF50; -fx-font-weight: bold;");
                container.getChildren().add(installedLabel);
            } else {
                // Si no está instalada, mostrar normal
                container.getChildren().add(versionLabel);
            }

            setGraphic(container);
            setText(null);
        }
    }
}