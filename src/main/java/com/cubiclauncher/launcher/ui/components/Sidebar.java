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

import com.cubiclauncher.launcher.core.InstanceManager.Instance;
import com.cubiclauncher.launcher.core.TaskManager;
import com.cubiclauncher.launcher.core.events.EventBus;
import com.cubiclauncher.launcher.core.events.EventType;
import com.cubiclauncher.launcher.ui.components.sidebar.InstanceListPanel;
import com.cubiclauncher.launcher.ui.components.sidebar.SidebarFooter;
import com.cubiclauncher.launcher.ui.components.sidebar.SidebarHeader;
import javafx.geometry.Insets;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

import java.util.function.Consumer;

/**
 * Sidebar principal. Visual idéntica al original, lógica separada en:
 * - SidebarHeader → título "CUBICLAUNCHER"
 * - InstanceListPanel → header de instancias + botón editar + ListView
 * - SidebarFooter → botones Versions y Settings
 */
public class Sidebar extends VBox {

    private static final EventBus eventBus = EventBus.get();
    private static final TaskManager taskManager = TaskManager.getInstance();

    private final InstanceListPanel instanceListPanel;
    private final SidebarFooter footer;

    public Sidebar() {
        super(10);
        setPadding(new Insets(15));
        getStyleClass().add("sidebar");
        setPrefWidth(280);
        setMinWidth(280);

        SidebarHeader header = new SidebarHeader();
        instanceListPanel = new InstanceListPanel();
        footer = new SidebarFooter();

        VBox.setVgrow(instanceListPanel, Priority.ALWAYS);
        getChildren().addAll(header, instanceListPanel, footer);

        eventBus.subscribe(EventType.INSTANCE_CREATED,
                data -> taskManager.runAsyncAtJFXThread(instanceListPanel::refresh));
        eventBus.subscribe(EventType.INSTANCE_DELETED,
                data -> taskManager.runAsyncAtJFXThread(instanceListPanel::refresh));
        eventBus.subscribe(EventType.INSTANCE_RENAME,
                data -> taskManager.runAsyncAtJFXThread(instanceListPanel::refresh));

        instanceListPanel.refresh();
    }

    // ── API público idéntico al original ─────────────────────────────────────

    public void refreshInstances() {
        instanceListPanel.refresh();
    }

    public void clearSelection() {
        instanceListPanel.clearSelection();
    }

    public void setOnInstanceSelected(Consumer<Instance> handler) {
        instanceListPanel.setOnInstanceSelected(handler);
    }

    public void setOnSettingsAction(Runnable action) {
        footer.setOnSettingsAction(action);
    }

    public void setOnVersionsAction(Runnable action) {
        footer.setOnVersionsAction(action);
    }
}