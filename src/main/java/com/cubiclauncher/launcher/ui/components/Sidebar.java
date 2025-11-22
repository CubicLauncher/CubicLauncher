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

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

public class Sidebar extends VBox {

    private final Button btnPlay;
    private final Button btnVersions;
    private final Button btnSettings;
    private final VBox navButtonsContainer;

    public Sidebar() {
        super(25);
        setPadding(new Insets(30, 20, 30, 20));
        getStyleClass().add("sidebar");
        setPrefWidth(240);
        setMinWidth(240);

        // Logo y título
        HBox logoSection = new HBox(12);
        logoSection.setAlignment(Pos.CENTER_LEFT);

        Label title = new Label("Cubic");
        title.getStyleClass().add("title");
        logoSection.getChildren().addAll(title);

        // Navegación moderna
        navButtonsContainer = new VBox(8);

        btnPlay = createNavButton("Jugar");
        btnVersions = createNavButton("Versiones");
        btnSettings = createNavButton("Ajustes");

        navButtonsContainer.getChildren().addAll(btnPlay, btnVersions, btnSettings);

        getChildren().addAll(logoSection, navButtonsContainer, new Region());
        VBox.setVgrow(navButtonsContainer, Priority.ALWAYS);

        setActive(btnPlay);
    }

    private Button createNavButton(String text) {
        Button button = new Button(text);
        button.getStyleClass().add("nav-button");
        button.setMaxWidth(Double.MAX_VALUE);
        button.setAlignment(Pos.CENTER_LEFT);
        return button;
    }

    private void setNavigationAction(Button button, Runnable viewAction) {
        button.setOnAction(event -> {
            setActive(button);
            viewAction.run();
        });
    }

    private void setActive(Button activeButton) {
        navButtonsContainer.getChildren().forEach(node -> node.getStyleClass().remove("active"));
        activeButton.getStyleClass().add("active");
    }

    public void setPlayAction(Runnable action) {
        setNavigationAction(btnPlay, action);
    }

    public void setInstancesAction(Runnable action) {
        setNavigationAction(btnVersions, action);
    }

    public void setSettingsAction(Runnable action) {
        setNavigationAction(btnSettings, action);
    }

}