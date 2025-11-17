package com.cubiclauncher.launcher.ui.components;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Priority;

public class Sidebar extends VBox {

    private final Button btnPlay;
    private final Button btnSettings;
    private final VBox navButtonsContainer;

    public Sidebar() {
        super(25);
        setPadding(new Insets(30, 20, 30, 20));
        getStyleClass().add("sidebar");
        setPrefWidth(240);
        setMinWidth(240);

        // Logo y título
        HBox logoSection = new HBox(12);
        logoSection.setAlignment(Pos.CENTER_LEFT);

        //Image logoImage = new Image(getClass().getResourceAsStream("/com.cubiclauncher.launcher/assets/logos/Cubic Dark.png"));
       // ImageView logoView = new ImageView(logoImage);
        //logoView.setFitWidth(40);
        //logoView.setFitHeight(40);
        Label title = new Label("Cubic");
        title.getStyleClass().add("title");
        logoSection.getChildren().addAll(title);

        // Navegación moderna
        navButtonsContainer = new VBox(8);

        btnPlay = createNavButton("Jugar");
        Button btnInstallations = createNavButton("Instalaciones");
        btnSettings = createNavButton("Ajustes");

        navButtonsContainer.getChildren().addAll(btnPlay, btnInstallations, btnSettings);

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

        getChildren().addAll(logoSection, navButtonsContainer, new Region(), quickStats);
        VBox.setVgrow(navButtonsContainer, Priority.ALWAYS);

        setActive(btnPlay);
    }

    private Button createNavButton(String text) {
        Button button = new Button(text);
        button.getStyleClass().add("nav-button");
        button.setMaxWidth(Double.MAX_VALUE);
        button.setAlignment(Pos.CENTER_LEFT);
        return button;
    }

    private void setNavigationAction(Button button, Runnable viewAction) {
        button.setOnAction(event -> {
            setActive(button);
            viewAction.run();
        });
    }

    private void setActive(Button activeButton) {
        navButtonsContainer.getChildren().forEach(node -> node.getStyleClass().remove("active"));
        activeButton.getStyleClass().add("active");
    }

    public void setPlayAction(Runnable action) { setNavigationAction(btnPlay, action); }
    public void setSettingsAction(Runnable action) { setNavigationAction(btnSettings, action); }

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