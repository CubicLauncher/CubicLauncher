/*
 * Copyright (C) 2025 Santiagolxx, Notstaff and CubicLauncher contributors
 * AGPL-3.0 — see https://www.gnu.org/licenses/
 */
package com.cubiclauncher.launcher.ui.components.sidebar;

import com.cubiclauncher.launcher.core.LanguageManager;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.layout.VBox;

/**
 * Sección inferior del sidebar con los botones de acción.
 */
public class SidebarFooter extends VBox {

    private Runnable onVersionsAction;
    private Runnable onSettingsAction;

    public SidebarFooter() {
        super(8);
        setPadding(new Insets(10, 0, 0, 0));

        LanguageManager lm = LanguageManager.getInstance();

        Button versionsButton = createActionButton(lm.get("sidebar.versions"));
        Button settingsButton = createActionButton(lm.get("sidebar.settings"));

        versionsButton.setOnAction(e -> { if (onVersionsAction != null) onVersionsAction.run(); });
        settingsButton.setOnAction(e -> { if (onSettingsAction != null) onSettingsAction.run(); });

        getChildren().addAll(versionsButton, settingsButton);
    }

    private Button createActionButton(String text) {
        Button button = new Button(text);
        button.getStyleClass().add("sidebar-action-button");
        button.setMaxWidth(Double.MAX_VALUE);
        button.setAlignment(Pos.CENTER_LEFT);
        return button;
    }

    public void setOnVersionsAction(Runnable action) { this.onVersionsAction = action; }
    public void setOnSettingsAction(Runnable action) { this.onSettingsAction = action; }
}