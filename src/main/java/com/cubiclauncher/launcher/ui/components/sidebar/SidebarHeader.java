/*
 * Copyright (C) 2025 Santiagolxx, Notstaff and CubicLauncher contributors
 * AGPL-3.0 — see https://www.gnu.org/licenses/
 */
package com.cubiclauncher.launcher.ui.components.sidebar;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;

/**
 * Header del sidebar: muestra "CUBICLAUNCHER" igual que antes.
 */
public class SidebarHeader extends HBox {

    public SidebarHeader() {
        super(12);
        setAlignment(Pos.CENTER_LEFT);
        setPadding(new Insets(0, 0, 20, 0));

        Label title = new Label("CUBICLAUNCHER");
        title.getStyleClass().add("sidebar-title");

        HBox.setHgrow(this, Priority.ALWAYS);
        getChildren().add(title);
    }
}