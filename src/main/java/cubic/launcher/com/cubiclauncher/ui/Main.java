package cubic.launcher.com.cubiclauncher.ui;

import cubic.launcher.com.cubiclauncher.ui.components.BottomBar;
import cubic.launcher.com.cubiclauncher.ui.components.Sidebar;
import cubic.launcher.com.cubiclauncher.ui.components.TitleBar;
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

import java.net.URL;

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

        // Cargar el archivo CSS externo
        try {
            URL cssUrl = getClass().getResource("/cubic/launcher/com/cubiclauncher/styles/ui.main.css");
            if (cssUrl != null) {
                scene.getStylesheets().add(cssUrl.toExternalForm());
            } else {
                System.err.println("No se pudo encontrar el archivo CSS en la ruta especificada.");
            }
        } catch (Exception e) {
            System.err.println("Error al cargar el archivo CSS: " + e.getMessage());
        }

        primaryStage.setScene(scene);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}