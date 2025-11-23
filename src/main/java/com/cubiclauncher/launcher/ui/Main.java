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
import com.cubiclauncher.launcher.ui.views.SettingsView;
import com.cubiclauncher.launcher.ui.views.VersionsView;
import com.cubiclauncher.launcher.util.StylesLoader;
import javafx.animation.FadeTransition;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Duration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;

public class Main extends Application {
    private static final Logger log = LoggerFactory.getLogger(Main.class);
    final SettingsManager settings = SettingsManager.getInstance();
    private BottomBar bottomBar;
    private static final EventBus eventBus = EventBus.get();
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
        primaryStage.setMinWidth(800);
        primaryStage.setMinHeight(600);
        BorderPane root = new BorderPane();
        root.getStyleClass().add("root");

        // --- Componentes de la UI ---
        Sidebar sidebar = new Sidebar();
        bottomBar = new BottomBar();

        // --- Contenedor Principal con animación ---
        StackPane centerContent = new StackPane();
        centerContent.getStyleClass().add("main-content");
        centerContent.setPadding(new Insets(40, 40, 30, 40));

        // --- Vistas ---
        VBox welcomeBox = createWelcomeView();
        VBox instancesBox = VersionsView.create();
        VBox settingsBox = SettingsView.create();

        // --- Lógica de Navegación con animaciones ---
        sidebar.setPlayAction(() -> showViewWithAnimation(centerContent, welcomeBox));
        sidebar.setInstancesAction(() -> {
            showViewWithAnimation(centerContent, instancesBox);
            bottomBar.updateInstalledVersions();
        });
        sidebar.setSettingsAction(() -> showViewWithAnimation(centerContent, settingsBox));

        showView(centerContent, welcomeBox);

        // --- Organizar Layout ---
        root.setLeft(sidebar);
        root.setCenter(centerContent);
        root.setBottom(bottomBar);

        // --- Escena ---
        Scene scene = new Scene(root, 1280, 760);
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

    private VBox createWelcomeView() {
        VBox welcomeBox = new VBox(20);
        welcomeBox.setAlignment(Pos.CENTER);

        Label welcomeTitle = new Label("Bienvenido a CubicLauncher");
        welcomeTitle.getStyleClass().add("welcome-title");

        Label welcomeSubtitle = new Label("Gestiona tus versiones de Minecraft con estilo");
        welcomeSubtitle.getStyleClass().add("welcome-subtitle");

        VBox stats = createStatsBox();

        welcomeBox.getChildren().addAll(welcomeTitle, welcomeSubtitle, stats);
        return welcomeBox;
    }

    private VBox createStatsBox() {
        VBox statsBox = new VBox(15);
        statsBox.setAlignment(Pos.CENTER);
        statsBox.setMaxWidth(600);
        statsBox.setStyle(
                "-fx-background-color: rgba(42, 42, 42, 0.6);" +
                        "-fx-background-radius: 16;" +
                        "-fx-padding: 30;" +
                        "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.3), 12, 0, 0, 4);"
        );

        Label statsTitle = new Label("Panel de Control");
        statsTitle.setStyle(
                "-fx-font-size: 20px;" +
                        "-fx-font-weight: 700;" +
                        "-fx-text-fill: #ffffff;"
        );

        Label instructionLabel = new Label(
                """
                        • Descarga versiones desde la pestaña 'Versiones'
                        • Configura Java y memoria desde 'Ajustes'
                        • Selecciona una versión y haz clic en 'JUGAR'"""
        );
        instructionLabel.setStyle(
                "-fx-font-size: 14px;" +
                        "-fx-text-fill: #c0c0c0;" +
                        "-fx-line-spacing: 6px;"
        );
        instructionLabel.setWrapText(true);

        statsBox.getChildren().addAll(statsTitle, instructionLabel);
        return statsBox;
    }

    private void showView(StackPane container, Node view) {
        container.getChildren().clear();
        container.getChildren().add(view);
    }

    private void showViewWithAnimation(StackPane container, Node newView) {
        if (container.getChildren().isEmpty()) {
            container.getChildren().add(newView);
            return;
        }

        Node currentView = container.getChildren().getFirst();

        // Fade out
        FadeTransition fadeOut = new FadeTransition(Duration.millis(150), currentView);
        fadeOut.setFromValue(1.0);
        fadeOut.setToValue(0.0);

        fadeOut.setOnFinished(e -> {
            container.getChildren().clear();
            container.getChildren().add(newView);

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