package com.cubiclauncher.launcher.ui;

import com.cubiclauncher.launcher.ui.components.BottomBar;
import com.cubiclauncher.launcher.ui.components.Sidebar;
import com.cubiclauncher.launcher.util.loadStyles;
import com.cubiclauncher.launcher.ui.components.TitleBar;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

public class Main extends Application {

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

        // --- Contenido Principal ---
        StackPane centerContent = new StackPane();
        centerContent.getStyleClass().add("main-content");
        centerContent.setPadding(new Insets(30));

        VBox welcomeBox = new VBox(10);
        welcomeBox.setAlignment(Pos.CENTER);
        Label welcomeTitle = new Label("Bienvenido a CubicLauncher");
        welcomeTitle.getStyleClass().add("welcome-title");
        Label welcomeSubtitle = new Label("Selecciona una versión y haz clic en 'Jugar'");
        welcomeSubtitle.getStyleClass().add("welcome-subtitle");
        welcomeBox.getChildren().addAll(welcomeTitle, welcomeSubtitle);

        centerContent.getChildren().add(welcomeBox);

        // --- Organizar Layout ---
        root.setTop(titleBar);
        root.setLeft(sidebar);
        root.setCenter(centerContent);
        root.setBottom(bottomBar);

        // --- Escena ---
        Scene scene = new Scene(root, 1200, 720);
        scene.setFill(Color.TRANSPARENT);

        // Cargar estilos CSS
        loadStyles.load(scene, "/com.cubiclauncher.launcher/styles/ui.main.css");

        primaryStage.setScene(scene);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}