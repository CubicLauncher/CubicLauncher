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

package com.cubiclauncher.launcher.ui;

import com.cubiclauncher.launcher.core.SettingsManager;
import com.cubiclauncher.launcher.core.TaskManager;
import com.cubiclauncher.launcher.core.events.EventBus;
import com.cubiclauncher.launcher.ui.components.BottomBar;
import com.cubiclauncher.launcher.ui.components.Sidebar;
import com.cubiclauncher.launcher.ui.views.InstanceViewer;
import com.cubiclauncher.launcher.ui.views.SettingsView;
import com.cubiclauncher.launcher.ui.views.VersionsView;
import com.cubiclauncher.launcher.util.StylesLoader;
import javafx.animation.FadeTransition;
import javafx.application.Application;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.layout.BorderPane;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Duration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;

public class Main extends Application {
    private static final Logger log = LoggerFactory.getLogger(Main.class);
    private static final UIBridge uiBridge = UIBridge.getInstance();
    private static final EventBus eventBus = EventBus.get();
    final SettingsManager settings = SettingsManager.getInstance();
    private InstanceViewer instanceViewer;
    private BorderPane root;

    @Override
    public void stop() {
        log.info("Apagando threads");
        TaskManager.getInstance().shutdown();
        log.info("Eliminando listeners de eventos");
        eventBus.clearAll();
        log.info("Closing CubicLauncher. Goodbye :)");
    }

    @Override
    public void start(Stage primaryStage) {
        log.info("Starting CubicLauncher");
        primaryStage.setTitle("CubicLauncher");
        primaryStage.initStyle(StageStyle.DECORATED);
        primaryStage.setMinWidth(1000);
        primaryStage.setMinHeight(700);
        root = new BorderPane();
        root.getStyleClass().add("root");

        // --- Componentes principales estilo Steam ---
        Sidebar sidebar = new Sidebar();
        BottomBar bottomBar = BottomBar.getInstance();
        instanceViewer = InstanceViewer.getInstance();

        // --- Configurar la navegación ---
        sidebar.setOnInstanceSelected(instance -> {
            showViewWithAnimation(instanceViewer);
            instanceViewer.showInstance(instance);
        });

        sidebar.setOnSettingsAction(() -> showViewWithAnimation(SettingsView.create(primaryStage)));
        sidebar.setOnVersionsAction(() -> showViewWithAnimation(VersionsView.create()));

        // --- Vista inicial ---
        root.setLeft(sidebar);
        root.setCenter(instanceViewer);
        root.setBottom(bottomBar);

        // --- Escena ---
        Scene scene = new Scene(root, 1400, 900);
        scene.setFill(Color.web("a1a1a1"));

        // Cargar estilos CSS unificados
        if (!settings.isNative_styles()) {
            StylesLoader.load(scene, "/com.cubiclauncher.launcher/styles/ui.main.css");
        }

        InputStream iconStream = com.cubiclauncher.launcher.Launcher.class.getResourceAsStream("/com.cubiclauncher.launcher/assets/logos/cdark.png");
        if (iconStream != null) {
            primaryStage.getIcons().add(new Image(iconStream));
        } else {
            System.err.println("No se encontró el ícono en /assets/logos/cdark.png");
        }
        primaryStage.setScene(scene);
        primaryStage.show();
        log.info("Hi again :)");
    }

    private void showViewWithAnimation(Node newView) {
        Node currentView = root.getCenter();
        if (currentView == newView) {
            return;
        }

        if (currentView == null) {
            root.setCenter(newView);
            return;
        }

        // Fade out
        FadeTransition fadeOut = new FadeTransition(Duration.millis(150), currentView);
        fadeOut.setFromValue(1.0);
        fadeOut.setToValue(0.0);

        fadeOut.setOnFinished(e -> {
            root.setCenter(newView);

            // Fade in
            newView.setOpacity(0.0);
            FadeTransition fadeIn = new FadeTransition(Duration.millis(200), newView);
            fadeIn.setFromValue(0.0);
            fadeIn.setToValue(1.0);
            fadeIn.play();
        });

        fadeOut.play();
    }
}