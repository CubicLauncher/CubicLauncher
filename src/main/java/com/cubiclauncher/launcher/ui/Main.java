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
package com.cubiclauncher.launcher.ui;

import com.cubiclauncher.launcher.ui.components.BottomBar;
import com.cubiclauncher.launcher.ui.components.Sidebar;
import com.cubiclauncher.launcher.ui.views.VersionsView;
import com.cubiclauncher.launcher.util.SettingsManager;
import com.cubiclauncher.launcher.util.StylesLoader;
import com.cubiclauncher.launcher.ui.views.SettingsView;
import com.cubiclauncher.launcher.ui.components.TitleBar;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

public class Main extends Application {
    SettingsManager settings = SettingsManager.getInstance();
    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("CubicLauncher");
        primaryStage.initStyle(javafx.stage.StageStyle.TRANSPARENT);

        BorderPane root = new BorderPane();
        root.getStyleClass().add("root");

        // --- Componentes de la UI ---
        TitleBar titleBar = new TitleBar(primaryStage);
        Sidebar sidebar = new Sidebar();
        BottomBar bottomBar = new BottomBar();

        // --- Contenedor Principal ---
        StackPane centerContent = new StackPane();
        centerContent.getStyleClass().add("main-content");
        centerContent.setPadding(new Insets(30));

        // --- Vista de Bienvenida ---
        VBox welcomeBox = createWelcomeView();

        // --- Vista de Instancias ---
        VBox instancesBox = VersionsView.create();

        // --- Vista de Ajustes (Placeholder) ---
        VBox settingsBox = SettingsView.create();

        // --- Lógica de Navegación ---
        sidebar.setInstancesAction(() -> showView(centerContent, instancesBox));
        // Pasamos la lógica de cambio de vista a la Sidebar.
        sidebar.setPlayAction(() -> showView(centerContent, welcomeBox));
        sidebar.setSettingsAction(() -> showView(centerContent, settingsBox));

        showView(centerContent, welcomeBox);

        // --- Organizar Layout ---
        root.setTop(titleBar);
        root.setLeft(sidebar);
        root.setCenter(centerContent);
        root.setBottom(bottomBar);

        // --- Escena ---
        Scene scene = new Scene(root, 1200, 720);
        scene.setFill(Color.TRANSPARENT);

        // Cargar estilos CSS
        if (!settings.isNative_styles()) {
            System.out.println("Cargando estilos");
            StylesLoader.load(scene, "/com.cubiclauncher.launcher/styles/ui.main.css");
        }

        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private VBox createWelcomeView() {
        VBox welcomeBox = new VBox(10);
        welcomeBox.setAlignment(Pos.CENTER);
        Label welcomeTitle = new Label("Bienvenido a CubicLauncher");
        welcomeTitle.getStyleClass().add("welcome-title");
        Label welcomeSubtitle = new Label("Selecciona una versión y haz clic en 'Jugar'");
        welcomeSubtitle.getStyleClass().add("welcome-subtitle");
        welcomeBox.getChildren().addAll(welcomeTitle, welcomeSubtitle);
        return welcomeBox;
    }

    private void showView(StackPane container, Node view) {
        container.getChildren().clear();
        container.getChildren().add(view);
    }
}
