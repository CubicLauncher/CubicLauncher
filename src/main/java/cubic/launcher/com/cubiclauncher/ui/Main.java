package cubic.launcher.com.cubiclauncher.ui;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import java.net.URL;
import javafx.stage.Stage;

public class Main extends Application {

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("CubicLauncher");

        BorderPane root = new BorderPane();
        root.getStyleClass().add("root");

        // --- Barra lateral moderna ---
        VBox leftSidebar = new VBox(25);
        leftSidebar.setPadding(new Insets(30, 20, 30, 20));
        leftSidebar.getStyleClass().add("sidebar");
        leftSidebar.setPrefWidth(240);
        leftSidebar.setMinWidth(240);

        // Logo y título
        HBox logoSection = new HBox(12);
        logoSection.setAlignment(Pos.CENTER_LEFT);

        Circle logoCircle = new Circle(20, Color.web("#4a6bff"));
        Label title = new Label("CubicLauncher");
        title.getStyleClass().add("title");
        logoSection.getChildren().addAll(logoCircle, title);

        // Navegación moderna
        VBox navButtons = new VBox(8);

        Button btnPlay = createNavButton("🎮 Jugar", true);
        Button btnInstallations = createNavButton("📦 Instalaciones", false);
        Button btnSkins = createNavButton("👤 Skins", false);
        Button btnSettings = createNavButton("⚙️ Ajustes", false);

        navButtons.getChildren().addAll(btnPlay, btnInstallations, btnSkins, btnSettings);

        // Sección de estadísticas
        VBox quickStats = new VBox(15);
        quickStats.setPadding(new Insets(20, 0, 0, 0));

        Label statsTitle = new Label("ESTADO");
        statsTitle.getStyleClass().add("stat-title");

        VBox statsItems = new VBox(8);
        addStatItem(statsItems, "Servidores", "Disponibles");
        addStatItem(statsItems, "Versiones", "12 instaladas");
        addStatItem(statsItems, "Última sesión", "Hace 2h");

        quickStats.getChildren().addAll(statsTitle, statsItems);

        leftSidebar.getChildren().addAll(logoSection, navButtons, new Region(), quickStats);
        VBox.setVgrow(navButtons, Priority.ALWAYS);
        root.setLeft(leftSidebar);

        // --- Contenido Principal Moderno ---
        StackPane centerContent = new StackPane();
        centerContent.setPadding(new Insets(30));

        // Tarjeta de noticias principal
        VBox newsCard = new VBox(20);
        newsCard.getStyleClass().add("news-card");
        newsCard.setMaxWidth(600);
        newsCard.setAlignment(Pos.TOP_CENTER);

        Label newsTitle = new Label("ÚLTIMAS NOTICIAS");
        newsTitle.getStyleClass().add("news-title");

        // Imagen de fondo o placeholder
        try {
            Image bgImage = new Image(getClass().getResourceAsStream("/assets/background.png"));
            ImageView bgImageView = new ImageView(bgImage);
            bgImageView.setFitHeight(250);
            bgImageView.setPreserveRatio(true);
            bgImageView.setStyle("-fx-background-radius: 15;");

            VBox imageContent = new VBox(bgImageView);
            imageContent.setAlignment(Pos.CENTER);
            imageContent.setStyle("-fx-background-radius: 15;");

            newsCard.getChildren().addAll(newsTitle, imageContent);
        } catch (Exception e) {
            VBox placeholder = new VBox(15);
            placeholder.setAlignment(Pos.CENTER);
            placeholder.setPadding(new Insets(40));

            Label placeholderTitle = new Label("Minecraft 1.21 ya disponible");
            placeholderTitle.getStyleClass().add("placeholder-title");

            Label placeholderDesc = new Label("Explora las nuevas características y aventuras que te esperan en la última actualización");
            placeholderDesc.getStyleClass().add("placeholder-desc");
            placeholderDesc.setWrapText(true);

            Button learnMore = new Button("Descubrir más");
            learnMore.getStyleClass().add("learn-more-button");

            placeholder.getChildren().addAll(placeholderTitle, placeholderDesc, learnMore);
            newsCard.getChildren().addAll(newsTitle, placeholder);
        }

        centerContent.getChildren().add(newsCard);
        root.setCenter(centerContent);

        // --- Barra Inferior Moderna ---
        HBox bottomBar = new HBox(20);
        bottomBar.setPadding(new Insets(20, 30, 20, 30));
        bottomBar.setAlignment(Pos.CENTER_LEFT);
        bottomBar.getStyleClass().add("bottom-bar");

        // Perfil de usuario con avatar
        HBox userProfile = new HBox(12);
        userProfile.setAlignment(Pos.CENTER_LEFT);

        Circle userAvatar = new Circle(18, Color.web("#4a6bff"));
        Label userName = new Label("Steve");
        userName.getStyleClass().add("user-profile");

        userProfile.getChildren().addAll(userAvatar, userName);

        // Espaciador
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        // Selector de versión moderno
        ComboBox<String> versionSelector = new ComboBox<>();
        versionSelector.getItems().addAll("1.20.4 - Forge", "1.20.4 - Vanilla", "1.19.2 - OptiFine");
        versionSelector.getSelectionModel().selectFirst();
        versionSelector.setPrefWidth(220);
        versionSelector.getStyleClass().add("combo-box");

        // Botón principal de Jugar moderno
        Button mainPlayButton = new Button("▶ JUGAR");
        mainPlayButton.getStyleClass().add("play-button");

        bottomBar.getChildren().addAll(userProfile, spacer, versionSelector, mainPlayButton);
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

        // Hacer la ventana transparente para efectos de desenfoque
        primaryStage.initStyle(javafx.stage.StageStyle.TRANSPARENT);

        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private Button createNavButton(String text, boolean active) {
        Button button = new Button(text);
        button.getStyleClass().add("nav-button");
        if (active) {
            button.getStyleClass().add("active");
        }
        button.setMaxWidth(Double.MAX_VALUE);
        button.setAlignment(Pos.CENTER_LEFT);
        return button;
    }

    private void addStatItem(VBox container, String title, String value) {
        HBox statItem = new HBox();
        statItem.setAlignment(Pos.CENTER_LEFT);
        statItem.setSpacing(10);

        Label statTitle = new Label(title);
        statTitle.getStyleClass().add("stat-item-title");

        Label statValue = new Label(value);
        statValue.getStyleClass().add("stat-item-value");

        HBox.setHgrow(statValue, Priority.ALWAYS);
        statValue.setAlignment(Pos.CENTER_RIGHT);

        statItem.getChildren().addAll(statTitle, statValue);
        container.getChildren().add(statItem);
    }

    public static void main(String[] args) {
        launch(args);
    }
}