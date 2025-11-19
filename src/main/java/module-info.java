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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

module cubic.launcher.com.cubiclauncher {
    requires javafx.controls;
    requires javafx.fxml;
    requires org.kordamp.bootstrapfx.core;
    requires com.google.gson;
    requires java.desktop;
    requires CLaunch;
    requires java.net.http;

    opens com.cubiclauncher.launcher.ui to javafx.graphics, javafx.fxml;
    opens com.cubiclauncher.launcher.ui.components to javafx.fxml, javafx.graphics;
    opens com.cubiclauncher.launcher.util to com.google.gson;

    exports com.cubiclauncher.launcher;
    exports com.cubiclauncher.launcher.ui;
    exports com.cubiclauncher.launcher.ui.views;
    exports com.cubiclauncher.launcher.ui.components;
    exports com.cubiclauncher.launcher.ui.controllers;
}