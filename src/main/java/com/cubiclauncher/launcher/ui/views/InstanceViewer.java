package com.cubiclauncher.launcher.ui.views;

import com.cubiclauncher.launcher.core.InstanceManager;
import com.cubiclauncher.launcher.core.events.EventBus;
import com.cubiclauncher.launcher.core.events.EventData;
import com.cubiclauncher.launcher.core.events.EventType;
import com.cubiclauncher.launcher.ui.controllers.InstanceController;
import javafx.application.Platform;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;

public class InstanceViewer extends BorderPane {
    private static InstanceViewer instance;
    private InstanceManager.Instance currentInstance;
    private final ObjectProperty<InstanceManager.Instance> currentInstanceProperty = new SimpleObjectProperty<>();

    private Label instanceName;
    private Label instanceVersion;
    private Button playButton;
    private TextArea logsArea;
    private ScrollPane logsScrollPane;
    private Label versionLabel;
    private Label loaderLabel;
    private Label locationLabel;
    private Label lastPlayedLabel;

    private InstanceViewer() {
        super();
        getStyleClass().add("instance-viewer");
        initializeHeader();
        initializeContent();
        showEmptyState();
        setupEventSubscriptions();
    }

    public static InstanceViewer getInstance() {
        if (instance == null) {
            instance = new InstanceViewer();
        }
        return instance;
    }

    private void setupEventSubscriptions() {
        EventBus eventBus = EventBus.get();
        eventBus.subscribe(EventType.GAME_OUTPUT, eventData -> {
            String instance_name = eventData.getString("instance_name");
            String output = eventData.getString("line");
            if (output != null && !output.trim().isEmpty() && currentInstance != null && currentInstance.getName().equals(instance_name)) {
                Platform.runLater(() -> appendLog(output));
            }
        });
    }

    private void initializeHeader() {
        VBox header = new VBox(20);
        header.getStyleClass().add("instance-header");
        header.setPadding(new Insets(40, 40, 40, 40));

        // Main content container
        HBox mainContent = new HBox(40);
        mainContent.setAlignment(Pos.CENTER_LEFT);

        // Minimalist image container
        StackPane imageContainer = new StackPane();
        imageContainer.getStyleClass().add("instance-image");
        imageContainer.setMinSize(180, 180);
        imageContainer.setMaxSize(180, 180);

        Label imageIcon = new Label("⬢");
        imageIcon.getStyleClass().add("instance-icon");
        imageContainer.getChildren().add(imageIcon);

        // Instance information - clean and minimal
        VBox info = new VBox(16);
        info.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(info, Priority.ALWAYS);

        instanceName = new Label();
        instanceName.getStyleClass().add("instance-name");

        instanceVersion = new Label();
        instanceVersion.getStyleClass().add("instance-version");

        // Minimalist info grid
        GridPane metaInfo = new GridPane();
        metaInfo.getStyleClass().add("instance-meta");
        metaInfo.setHgap(32);
        metaInfo.setVgap(8);

        Label lastPlayedTitle = new Label("Last played");
        lastPlayedTitle.getStyleClass().add("meta-label");

        lastPlayedLabel = new Label("Never");
        lastPlayedLabel.getStyleClass().add("meta-value");

        metaInfo.add(lastPlayedTitle, 0, 0);
        metaInfo.add(lastPlayedLabel, 0, 1);

        // Primary action button - minimalist design
        playButton = new Button("Play");
        playButton.getStyleClass().add("play-button-primary");
        playButton.setOnAction(e -> {
            if (currentInstance != null) {
                InstanceController.launchInstance(currentInstance.getName());
            }
        });

        info.getChildren().addAll(instanceName, instanceVersion, metaInfo, playButton);
        mainContent.getChildren().addAll(imageContainer, info);
        header.getChildren().add(mainContent);
        setTop(header);
    }

    private void initializeContent() {
        // Contenedor principal con las dos cards juntas
        HBox content = new HBox(32);
        content.getStyleClass().add("instance-content");
        content.setPadding(new Insets(32, 40, 40, 20));

        // Details card
        VBox detailsCard = new VBox(12);
        detailsCard.getStyleClass().add("details-card");
        detailsCard.setPrefWidth(400);

        Label detailsTitle = new Label("Details");
        detailsTitle.getStyleClass().add("section-title");

        // Grid de detalles
        VBox detailsGrid = new VBox(16);
        detailsGrid.getStyleClass().add("details-grid");

        // Version info
        VBox versionBox = createInfoBox("Version", versionLabel = new Label("-"));

        // Loader info
        VBox loaderBox = createInfoBox("Loader", loaderLabel = new Label("-"));

        // Location info
        VBox locationBox = createInfoBox("Location", locationLabel = new Label("-"));

        detailsGrid.getChildren().addAll(versionBox, loaderBox, locationBox);
        detailsCard.getChildren().addAll(detailsTitle, detailsGrid);

        // Logs card
        VBox logsCard = new VBox(0);
        logsCard.getStyleClass().add("logs-card");
        HBox.setHgrow(logsCard, Priority.ALWAYS);

        // Header de logs
        HBox logsHeader = new HBox(8);
        logsHeader.setAlignment(Pos.CENTER_LEFT);

        Label logsTitle = new Label("Console");
        logsTitle.getStyleClass().add("section-title");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button copyLogsButton = new Button("Copy");
        copyLogsButton.getStyleClass().add("btn-secondary");
        copyLogsButton.setOnAction(e -> {
            if (!logsArea.getText().isEmpty()) {
                javafx.scene.input.Clipboard clipboard = javafx.scene.input.Clipboard.getSystemClipboard();
                javafx.scene.input.ClipboardContent clipboardContent = new javafx.scene.input.ClipboardContent();
                clipboardContent.putString(logsArea.getText());
                clipboard.setContent(clipboardContent);
            }
        });

        Button clearLogsButton = new Button("Clear");
        clearLogsButton.getStyleClass().add("btn-secondary");
        clearLogsButton.setOnAction(e -> {
            logsArea.clear();
            appendLog("Console cleared");
        });

        logsHeader.getChildren().addAll(logsTitle, spacer, copyLogsButton, clearLogsButton);

        // Área de logs - estilo terminal minimalista
        logsArea = new TextArea();
        logsArea.getStyleClass().add("console-area");
        logsArea.setEditable(false);
        logsArea.setWrapText(true);
        logsArea.setStyle("-fx-background-color: transparent; -fx-control-inner-background: transparent;");

        logsScrollPane = new ScrollPane(logsArea);
        logsScrollPane.getStyleClass().add("console-scroll");
        logsScrollPane.setFitToWidth(true);
        logsScrollPane.setFitToHeight(true);
        logsScrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        logsScrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);

        // Quitar el fondo blanco del ScrollPane también
        logsScrollPane.setStyle("-fx-background: transparent; -fx-background-color: transparent;");

        VBox logsContainer = new VBox(0);
        logsContainer.getStyleClass().add("logs-container");
        logsContainer.getChildren().addAll(logsHeader, logsScrollPane);
        VBox.setVgrow(logsScrollPane, Priority.ALWAYS);

        logsCard.getChildren().add(logsContainer);

        // Añadir ambas cards al contenido principal
        content.getChildren().addAll(detailsCard, logsCard);
        setCenter(content);
    }

    private VBox createInfoBox(String label, Label valueLabel) {
        VBox box = new VBox(4);
        box.getStyleClass().add("info-box");

        Label titleLabel = new Label(label);
        titleLabel.getStyleClass().add("info-label");

        valueLabel.getStyleClass().add("info-value");

        box.getChildren().addAll(titleLabel, valueLabel);
        return box;
    }

    private void showEmptyState() {
        instanceName.setText("No instance selected");
        instanceVersion.setText("");
        playButton.setDisable(true);

        versionLabel.setText("-");
        loaderLabel.setText("-");
        locationLabel.setText("-");
        lastPlayedLabel.setText("Never");

        logsArea.clear();
        logsArea.appendText("Select an instance to view console output\n");
    }

    public void showInstance(InstanceManager.Instance instance) {
        this.currentInstance = instance;
        this.currentInstanceProperty.set(instance);

        if (instance != null) {
            instanceName.setText(instance.getName());
            instanceVersion.setText(formatVersion(instance.getVersion()));
            playButton.setDisable(false);

            versionLabel.setText(instance.getVersion());
            loaderLabel.setText(extractLoader(instance.getVersion()));
            locationLabel.setText("./instances/" + instance.getName());
            lastPlayedLabel.setText(instance.getLastPlayedFormatted());

            logsArea.clear();
            logsArea.appendText("Instance: " + instance.getName() + "\n");
            logsArea.appendText("Version: " + instance.getVersion() + "\n");
            logsArea.appendText("Ready\n");

        } else {
            showEmptyState();
        }
    }

    private String formatVersion(String version) {
        if (version == null) return "";
        return version.replace("-", " ").replace("loader", "").trim();
    }

    private String extractLoader(String version) {
        if (version == null) return "Vanilla";
        if (version.contains("fabric")) return "Fabric";
        if (version.contains("forge")) return "Forge";
        if (version.contains("quilt")) return "Quilt";
        return "Vanilla";
    }

    public void appendLog(String message) {
        if (logsArea != null) {
            String timestamp = java.time.LocalTime.now().format(java.time.format.DateTimeFormatter.ofPattern("HH:mm:ss"));
            Platform.runLater(() -> logsArea.appendText("[" + timestamp + "] " + message + "\n"));
        }
    }

    public void appendError(String error) {
        if (logsArea != null) {
            String timestamp = java.time.LocalTime.now().format(java.time.format.DateTimeFormatter.ofPattern("HH:mm:ss"));
            Platform.runLater(() -> logsArea.appendText("[" + timestamp + "] ERROR: " + error + "\n"));
        }
    }

    public void clearLogs() {
        if (logsArea != null) {
            logsArea.clear();
        }
    }
}