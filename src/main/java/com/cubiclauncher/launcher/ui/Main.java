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

import com.cubiclauncher.launcher.core.DownloadManager;
import com.cubiclauncher.launcher.core.SettingsManager;
import com.cubiclauncher.launcher.core.TaskManager;
import com.cubiclauncher.launcher.core.events.EventBus;
import com.cubiclauncher.launcher.ui.components.BottomBar;
import com.cubiclauncher.launcher.ui.components.Sidebar;
import com.cubiclauncher.launcher.ui.views.instanceViewer.InstanceViewer;
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
import com.cubiclauncher.launcher.ui.views.ErrorConsoleView;
import com.cubiclauncher.launcher.util.LogAppender;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.encoder.PatternLayoutEncoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.io.InputStream;
import javafx.stage.Screen;
import javafx.geometry.Rectangle2D;

public class Main extends Application {
    private static final Logger log = LoggerFactory.getLogger(Main.class);
    private static final EventBus eventBus = EventBus.get();
    final SettingsManager settings = SettingsManager.getInstance();
    private InstanceViewer instanceViewer;
    private BorderPane root;
    private Scene scene;

    @Override
    public void stop() {
        log.info("Apagando threads");
        TaskManager.getInstance().shutdown();
        DownloadManager.getInstance().shutdown();
        log.info("Eliminando listeners de eventos");
        eventBus.clearAll();
        log.info("Cerrando CubicLauncher. Adios :)");
    }

    @Override
    public void start(Stage primaryStage) {
        initLogging();
        log.info("Starting CubicLauncher");
        primaryStage.setTitle("CubicLauncher ALPHA");
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

        sidebar.setOnSettingsAction(() -> {
            showViewWithAnimation(SettingsView.create(primaryStage));
            sidebar.clearSelection();
        });
        sidebar.setOnVersionsAction(() -> {
            showViewWithAnimation(VersionsView.getInstance().create());
            sidebar.clearSelection();
        });

        // --- Vista inicial ---
        root.setLeft(sidebar);
        root.setCenter(instanceViewer);
        root.setBottom(bottomBar);

        // --- Escena ---
        scene = new Scene(root, 1400, 900);
        scene.setFill(Color.web("a1a1a1"));

        // Cargar estilos CSS unificados
        StylesLoader.load(scene, "/com.cubiclauncher.launcher/styles/ui.main.css");

        InputStream iconStream = com.cubiclauncher.launcher.Launcher.class
                .getResourceAsStream("/com.cubiclauncher.launcher/assets/logos/cdark.png");
        if (iconStream != null) {
            primaryStage.getIcons().add(new Image(iconStream));
        } else {
            System.err.println("No se encontró el ícono en /assets/logos/cdark.png");
        }
        primaryStage.setScene(scene);

        // Fix compatibility: Force windowed mode on Windows and Mac and adjust to
        // resolution
        String os = System.getProperty("os.name").toLowerCase();
        if (os.contains("win") || os.contains("mac")) {
            Rectangle2D bounds = Screen.getPrimary().getVisualBounds();
            primaryStage.setX(bounds.getMinX());
            primaryStage.setY(bounds.getMinY());
            primaryStage.setWidth(bounds.getWidth());
            primaryStage.setHeight(bounds.getHeight());
            primaryStage.setMaximized(true);
            primaryStage.setFullScreen(false);
            primaryStage.setResizable(true);
        }
        if (settings.isFirstLaunch()) {
            SetupWizardDialog wizard = new SetupWizardDialog();
            wizard.showAndWait();
            refreshUI();
        }
        primaryStage.show();
        log.info("Hi again :)");

        if (settings.isErrorConsole()) {
            ErrorConsoleView.getInstance().show();
        }
    }

    private void initLogging() {
        LoggerContext lc = (LoggerContext) LoggerFactory.getILoggerFactory();
        PatternLayoutEncoder ple = new PatternLayoutEncoder();

        ple.setPattern("%d{HH:mm:ss} [%thread] %-5level %logger{36} - %msg%n");
        ple.setContext(lc);
        ple.start();

        LogAppender logAppender = new LogAppender();
        logAppender.setContext(lc);
        logAppender.setEncoder(ple);
        logAppender.start();

        ch.qos.logback.classic.Logger root = (ch.qos.logback.classic.Logger) LoggerFactory
                .getLogger(org.slf4j.Logger.ROOT_LOGGER_NAME);
        root.addAppender(logAppender);
    }

    /**
     * Refreshes the entire UI to apply language and style changes immediately.
     */
    public void refreshUI() {
        log.info("Refreshing Main UI components...");
        // Re-create components (they will pick up new language/settings automatically)
        Sidebar sidebar = new Sidebar();

        root.setLeft(sidebar);
        // Refresh style
        StylesLoader.load(scene, "/com.cubiclauncher.launcher/styles/ui.main.css");

        // Setup navigation again for the new sidebar
        sidebar.setOnInstanceSelected(instance -> {
            showViewWithAnimation(instanceViewer);
            instanceViewer.showInstance(instance);
        });

        sidebar.setOnSettingsAction(() -> {
            showViewWithAnimation(SettingsView.create((Stage) scene.getWindow()));
            sidebar.clearSelection();
        });
        sidebar.setOnVersionsAction(() -> {
            showViewWithAnimation(VersionsView.getInstance().create());
            sidebar.clearSelection();
        });
    }

    private void showViewWithAnimation(Node newView) {
        Node currentView = root.getCenter();
        final String currentViewName = (currentView != null) ? currentView.getClass().getSimpleName() : "ninguna";
        final String newViewName = (newView != null) ? newView.getClass().getSimpleName() : "ninguna";
        log.info("Cambiando la vista central de '{}' a '{}'", currentViewName, newViewName);

        if (currentView == newView) {
            return;
        }

        if (currentView == null) {
            root.setCenter(newView);
            return;
        }

        // Fade out
        FadeTransition fadeOut = getFadeOut(newView, currentView);

        fadeOut.play();
    }

    private FadeTransition getFadeOut(Node newView, Node currentView) {
        FadeTransition fadeOut = new FadeTransition(Duration.millis(150), currentView);
        fadeOut.setFromValue(1.0);
        fadeOut.setToValue(0.0);

        fadeOut.setOnFinished(e -> {
            root.setCenter(newView);

            if (newView != null) {
                // Fade in
                newView.setOpacity(0.0);
                FadeTransition fadeIn = new FadeTransition(Duration.millis(200), newView);
                fadeIn.setFromValue(0.0);
                fadeIn.setToValue(1.0);
                fadeIn.play();
            }
        });
        return fadeOut;
    }
}