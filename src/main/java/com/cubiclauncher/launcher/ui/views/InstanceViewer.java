package com.cubiclauncher.launcher.ui.views;

import com.cubiclauncher.launcher.core.InstanceManager;
import com.cubiclauncher.launcher.core.PathManager;
import com.cubiclauncher.launcher.core.events.EventBus;
import com.cubiclauncher.launcher.core.events.EventType;
import com.cubiclauncher.launcher.ui.controllers.InstanceController;
import javafx.application.Platform;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.shape.Rectangle;
import javafx.stage.Modality;
import javafx.stage.StageStyle;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

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
    private ImageView imageView;
    private Label imageIcon;

    private double xOffset = 0;
    private double yOffset = 0;

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
            if (output != null && !output.trim().isEmpty() && currentInstance != null
                    && currentInstance.getName().equals(instance_name)) {
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

        imageIcon = new Label("⬢");
        imageIcon.getStyleClass().add("instance-icon");

        imageView = new ImageView();
        imageView.setFitWidth(180);
        imageView.setFitHeight(180);
        imageView.setPreserveRatio(true);

        Rectangle clip = new Rectangle(180, 180);
        clip.setArcWidth(20);
        clip.setArcHeight(20);
        imageView.setClip(clip);

        imageContainer.getChildren().addAll(imageIcon, imageView);

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

        updateCoverImage(null);

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

            // Update image
            updateCoverImage(instance);

            versionLabel.setText(instance.getVersion());
            loaderLabel.setText(extractLoader(instance.getVersion()));
            locationLabel.setText(PathManager.getInstance().getInstancePath().resolve(instance.getName()).toString());
            lastPlayedLabel.setText(instance.getLastPlayedFormatted());

            logsArea.clear();
            logsArea.appendText("Instance: " + instance.getName() + "\n");
            logsArea.appendText("Version: " + instance.getVersion() + "\n");
            logsArea.appendText("Ready\n");

        } else {
            showEmptyState();
        }
    }

    public void showEditDialog(InstanceManager.Instance instance) {
        Dialog<Void> dialog = new Dialog<>();
        dialog.initOwner(getScene().getWindow());
        dialog.initModality(Modality.WINDOW_MODAL);
        dialog.initStyle(StageStyle.TRANSPARENT);
        dialog.setTitle("Editar Instancia");

        DialogPane dialogPane = dialog.getDialogPane();
        dialogPane.getStyleClass().add("editor-dialog-pane");
        dialogPane.setHeaderText(null);
        dialogPane.setGraphic(null);
        dialogPane.getButtonTypes().add(ButtonType.CLOSE);
        Node closeBtnType = dialogPane.lookupButton(ButtonType.CLOSE);
        if (closeBtnType != null)
            closeBtnType.setVisible(false);
        dialogPane.setBackground(null);

        VBox windowRoot = new VBox();
        windowRoot.getStyleClass().add("editor-window-root");
        windowRoot.setPrefWidth(500);

        // --- Custom Header (Draggable) ---
        HBox customHeader = new HBox();
        customHeader.getStyleClass().add("editor-header");
        customHeader.setAlignment(Pos.CENTER_LEFT);

        Label titleLabel = new Label("Editor de Instancia");
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button closeButton = new Button("✕");
        closeButton.getStyleClass().add("editor-close-button");
        closeButton.setOnAction(e -> {
            dialog.setResult(null);
            dialog.close();
        });

        customHeader.getChildren().addAll(titleLabel, spacer, closeButton);

        // Dragging logic
        customHeader.setOnMousePressed(event -> {
            xOffset = event.getSceneX();
            yOffset = event.getSceneY();
        });
        customHeader.setOnMouseDragged(event -> {
            dialog.setX(event.getScreenX() - xOffset);
            dialog.setY(event.getScreenY() - yOffset);
        });

        // --- Content ---
        VBox content = new VBox(25);
        content.getStyleClass().add("editor-content");
        content.setAlignment(Pos.TOP_LEFT);

        GridPane grid = new GridPane();
        grid.setHgap(15);
        grid.setVgap(20);
        grid.setAlignment(Pos.CENTER_LEFT);

        TextField nameField = createModalField(grid, "Nombre", instance.getName(), 0);
        TextField versionField = createModalField(grid, "Versión", instance.getVersion(), 1);

        Label ramLabel = new Label("RAM (MB)");
        ramLabel.getStyleClass().add("editor-field-label");
        HBox ramBox = new HBox(10);
        TextField minMemField = new TextField(
                instance.getMinMemory() != null ? String.valueOf(instance.getMinMemory()) : "");
        TextField maxMemField = new TextField(
                instance.getMaxMemory() != null ? String.valueOf(instance.getMaxMemory()) : "");
        minMemField.getStyleClass().add("editor-text-field");
        maxMemField.getStyleClass().add("editor-text-field");
        minMemField.setPromptText("Min");
        maxMemField.setPromptText("Max");
        minMemField.setPrefWidth(110);
        maxMemField.setPrefWidth(110);
        ramBox.getChildren().addAll(minMemField, maxMemField);

        grid.add(ramLabel, 0, 2);
        grid.add(ramBox, 1, 2);

        TextField jvmArgsField = createModalField(grid, "Argumentos JVM",
                instance.getJvmArgs() != null ? instance.getJvmArgs() : "", 3);

        VBox gallerySection = new VBox(10);
        Label galleryLabel = new Label("Seleccionar Portada (Capturas)");
        galleryLabel.getStyleClass().add("editor-field-label");

        HBox galleryItems = new HBox(10);
        galleryItems.setPadding(new Insets(5));

        String[] selectedCover = { instance.getCoverImage() };

        List<File> screenshots = getScreenshots(instance);
        if (screenshots.isEmpty()) {
            Label noScreenshots = new Label("No se encontraron capturas en la carpeta de la instancia.");
            noScreenshots.setStyle("-fx-text-fill: #666; -fx-font-size: 12px;");
            galleryItems.getChildren().add(noScreenshots);
        } else {
            for (File screenshot : screenshots) {
                VBox thumbContainer = createThumbnail(screenshot, selectedCover);
                galleryItems.getChildren().add(thumbContainer);
            }
        }

        ScrollPane galleryScroll = new ScrollPane(galleryItems);
        galleryScroll.setPrefHeight(130);
        galleryScroll.getStyleClass().add("console-scroll");
        galleryScroll.setVbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        galleryScroll.setFitToHeight(true);
        galleryScroll.setStyle("-fx-background: transparent; -fx-background-color: transparent;");

        gallerySection.getChildren().addAll(galleryLabel, galleryScroll);
        content.getChildren().addAll(grid, gallerySection);

        // --- Footer ---
        HBox footer = new HBox(15);
        footer.getStyleClass().add("editor-footer");
        footer.setAlignment(Pos.CENTER_RIGHT);

        Button deleteBtn = new Button("ELIMINAR");
        deleteBtn.getStyleClass().add("btn-secondary");
        deleteBtn.setStyle("-fx-text-fill: #ff5555; -fx-font-size: 11px; -fx-padding: 8 20;");

        Button saveBtn = new Button("GUARDAR CAMBIOS");
        saveBtn.getStyleClass().add("play-button-primary");
        saveBtn.setStyle("-fx-font-size: 11px; -fx-padding: 8 24;");

        Region footerSpacer = new Region();
        HBox.setHgrow(footerSpacer, Priority.ALWAYS);

        footer.getChildren().addAll(deleteBtn, footerSpacer, saveBtn);

        windowRoot.getChildren().addAll(customHeader, content, footer);
        dialogPane.setContent(windowRoot);

        // Handle buttons manually since we use custom footer
        saveBtn.setOnAction(e -> {
            String oldName = instance.getName();
            String newName = nameField.getText();

            instance.setVersion(versionField.getText());
            try {
                instance.setMinMemory(minMemField.getText().isEmpty() ? null : Integer.parseInt(minMemField.getText()));
                instance.setMaxMemory(maxMemField.getText().isEmpty() ? null : Integer.parseInt(maxMemField.getText()));
            } catch (NumberFormatException ex) {
            }

            instance.setJvmArgs(jvmArgsField.getText());
            instance.setCoverImage(selectedCover[0]);

            if (!newName.equals(oldName)) {
                InstanceManager.getInstance().renameInstance(oldName, newName);
            } else {
                InstanceManager.getInstance().saveInstance(instance);
                showInstance(instance);
            }
            dialog.close();
        });

        deleteBtn.setOnAction(e -> {
            Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
            confirm.setTitle("Confirmar Eliminación");
            confirm.setHeaderText("Eliminar Instancia: " + instance.getName());
            confirm.setContentText(
                    "¿Estás seguro de que deseas eliminar esta instancia? Esta acción no se puede deshacer.");
            confirm.getDialogPane().getStylesheets()
                    .add(getClass().getResource("/com.cubiclauncher.launcher/styles/ui.main.css").toExternalForm());
            confirm.getDialogPane().getStyleClass().add("editor-dialog-pane");

            if (confirm.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK) {
                InstanceManager.getInstance().deleteInstance(instance.getName());
                dialog.close();
            }
        });

        // Style the Scene for transparency
        Platform.runLater(() -> {
            if (dialogPane.getScene() != null) {
                dialogPane.getScene().setFill(null);
                dialogPane.getScene().getStylesheets()
                        .add(getClass().getResource("/com.cubiclauncher.launcher/styles/ui.main.css").toExternalForm());
            }
            nameField.requestFocus();
        });

        dialog.showAndWait();
    }

    private List<File> getScreenshots(InstanceManager.Instance instance) {
        List<File> screenshots = new ArrayList<>();
        File screenshotsDir = PathManager.getInstance().getInstancePath()
                .resolve(instance.getName())
                .resolve("screenshots")
                .toFile();

        if (screenshotsDir.exists() && screenshotsDir.isDirectory()) {
            File[] files = screenshotsDir.listFiles((dir, name) -> name.toLowerCase().endsWith(".png")
                    || name.toLowerCase().endsWith(".jpg") || name.toLowerCase().endsWith(".jpeg"));
            if (files != null) {
                for (File f : files)
                    screenshots.add(f);
            }
        }
        return screenshots;
    }

    private VBox createThumbnail(File file, String[] selectedCover) {
        VBox container = new VBox();
        container.setPrefSize(100, 100);
        container.getStyleClass().add("editor-thumb-container");
        container.setAlignment(Pos.CENTER);

        ImageView thumb = new ImageView(new Image(file.toURI().toString(), 90, 90, true, true));
        thumb.setFitWidth(90);
        thumb.setFitHeight(90);
        thumb.setPreserveRatio(true);

        container.getChildren().add(thumb);

        updateThumbStyle(container, file.getAbsolutePath().equals(selectedCover[0]));

        container.setOnMouseClicked(e -> {
            selectedCover[0] = file.getAbsolutePath();
            // Update UI for all siblings
            if (container.getParent() instanceof Pane) {
                Pane parent = (Pane) container.getParent();
                parent.getChildren().forEach(node -> {
                    if (node instanceof VBox) {
                        updateThumbStyle((VBox) node, false);
                    }
                });
            }
            updateThumbStyle(container, true);
        });

        return container;
    }

    private void updateThumbStyle(VBox container, boolean selected) {
        if (selected) {
            container.setStyle("-fx-border-color: #3a86ff; -fx-border-width: 2; -fx-background-color: #1a1a1a;");
        } else {
            container.setStyle("-fx-border-color: #2a2a2a; -fx-border-width: 1; -fx-background-color: transparent;");
        }
    }

    private TextField createModalField(GridPane grid, String label, String value, int row) {
        Label fieldLabel = new Label(label);
        fieldLabel.getStyleClass().add("editor-field-label");

        TextField field = new TextField(value);
        field.getStyleClass().add("editor-text-field");
        field.setPrefWidth(230);

        grid.add(fieldLabel, 0, row);
        grid.add(field, 1, row);
        return field;
    }

    private String formatVersion(String version) {
        if (version == null)
            return "";
        return version.replace("-", " ").replace("loader", "").trim();
    }

    private String extractLoader(String version) {
        if (version == null)
            return "Vanilla";
        if (version.contains("fabric"))
            return "Fabric";
        if (version.contains("forge"))
            return "Forge";
        if (version.contains("quilt"))
            return "Quilt";
        return "Vanilla";
    }

    public void appendLog(String message) {
        if (logsArea != null) {
            String timestamp = java.time.LocalTime.now()
                    .format(java.time.format.DateTimeFormatter.ofPattern("HH:mm:ss"));
            Platform.runLater(() -> logsArea.appendText("[" + timestamp + "] " + message + "\n"));
        }
    }

    public void appendError(String error) {
        if (logsArea != null) {
            String timestamp = java.time.LocalTime.now()
                    .format(java.time.format.DateTimeFormatter.ofPattern("HH:mm:ss"));
            Platform.runLater(() -> logsArea.appendText("[" + timestamp + "] ERROR: " + error + "\n"));
        }
    }

    public void clearLogs() {
        if (logsArea != null) {
            logsArea.clear();
        }
    }

    private void updateCoverImage(InstanceManager.Instance instance) {
        if (instance != null && instance.getCoverImage() != null) {
            File imgFile = new File(instance.getCoverImage());
            if (imgFile.exists()) {
                try {
                    Image image = new Image(imgFile.toURI().toString(), 180, 180, true, true);
                    imageView.setImage(image);
                    imageIcon.setVisible(false);
                    imageView.setVisible(true);
                    return;
                } catch (Exception e) {
                    // Fallback to icon
                }
            }
        }
        imageView.setImage(null);
        imageView.setVisible(false);
        imageIcon.setVisible(true);
    }
}