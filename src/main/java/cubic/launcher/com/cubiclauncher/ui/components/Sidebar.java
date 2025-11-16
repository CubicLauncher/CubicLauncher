package cubic.launcher.com.cubiclauncher.ui.components;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;

public class Sidebar extends VBox {

    public Sidebar() {
        super(25);
        setPadding(new Insets(30, 20, 30, 20));
        getStyleClass().add("sidebar");
        setPrefWidth(240);
        setMinWidth(240);

        // Logo y título
        HBox logoSection = new HBox(12);
        logoSection.setAlignment(Pos.CENTER_LEFT);

        Circle logoCircle = new Circle(20, Color.web("#4a6bff"));
        Label title = new Label("Cubic");
        title.getStyleClass().add("title");
        logoSection.getChildren().addAll(logoCircle, title);

        // Navegación moderna
        VBox navButtons = new VBox(8);

        Button btnPlay = createNavButton("🎮 Jugar", true);
        Button btnInstallations = createNavButton("📦 Instalaciones", false);
        Button btnSettings = createNavButton("⚙️ Ajustes", false);

        navButtons.getChildren().addAll(btnPlay, btnInstallations, btnSettings);

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

        getChildren().addAll(logoSection, navButtons, new Region(), quickStats);
        VBox.setVgrow(navButtons, Priority.ALWAYS);
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
}