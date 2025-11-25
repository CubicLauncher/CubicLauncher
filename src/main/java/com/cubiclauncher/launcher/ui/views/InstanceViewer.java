package com.cubiclauncher.launcher.ui.views;

import com.cubiclauncher.launcher.core.InstanceManager;
import com.cubiclauncher.launcher.ui.controllers.LauncherController;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

public class InstanceViewer extends BorderPane {
    private static InstanceViewer instance;
    private InstanceManager.Instance currentInstance;
    private final ObjectProperty<InstanceManager.Instance> currentInstanceProperty = new SimpleObjectProperty<>();
    private Label instanceName;
    private Label instanceVersion;
    private Button playButton;
    private TabPane contentTabs;

    // Labels de stats
    private Label lastPlayedLabel;
    private Label playTimeLabel;
    private Label achievementsLabel;

    // Constructor privado para Singleton
    private InstanceViewer() {
        super();
        getStyleClass().add("instance-viewer");
        initializeHeader();
        initializeContent();
        showEmptyState();
    }

    // Obtener instancia única
    public static InstanceViewer getInstance() {
        if (instance == null) {
            instance = new InstanceViewer();
        }
        return instance;
    }

    // Resetear singleton
    public static void resetInstance() {
        instance = null;
    }

    private void initializeHeader() {
        VBox header = new VBox(15);
        header.getStyleClass().add("instance-header");
        header.setPadding(new Insets(30, 40, 20, 40));

        HBox banner = new HBox(20);
        banner.setAlignment(Pos.CENTER_LEFT);

        // Imagen placeholder
        StackPane imageContainer = new StackPane();
        imageContainer.getStyleClass().add("instance-image-container");
        imageContainer.setMinSize(460, 215);
        imageContainer.setMaxSize(460, 215);

        Label imagePlaceholder = new Label("IMAGEN DE LA INSTANCIA");
        imagePlaceholder.getStyleClass().add("instance-image-placeholder");
        imageContainer.getChildren().add(imagePlaceholder);

        // Información de la instancia
        VBox info = new VBox(8);
        instanceName = new Label();
        instanceName.getStyleClass().add("instance-title");

        instanceVersion = new Label();
        instanceVersion.getStyleClass().add("instance-subtitle");

        // Stats
        HBox stats = new HBox(30);
        stats.setAlignment(Pos.CENTER_LEFT);

        lastPlayedLabel = new Label("Nunca");
        VBox lastPlayed = createStatBox("ÚLTIMA VEZ", lastPlayedLabel);

        playTimeLabel = new Label("0 h");
        VBox playTime = createStatBox("TIEMPO TOTAL", playTimeLabel);

        achievementsLabel = new Label("0/0");
        VBox achievements = createStatBox("LOGROS", achievementsLabel);

        stats.getChildren().addAll(lastPlayed, playTime, achievements);

        // Barra de acciones
        HBox actionBar = new HBox(15);
        actionBar.setAlignment(Pos.CENTER_LEFT);

        playButton = new Button("JUGAR");
        playButton.getStyleClass().add("play-button-large");
        playButton.setOnAction(e -> LauncherController.launchInstance(getInstance().currentInstance.getName()));

        Button optionsButton = new Button("Gestionar");
        optionsButton.getStyleClass().add("options-button");

        actionBar.getChildren().addAll(playButton, optionsButton);

        info.getChildren().addAll(instanceName, instanceVersion, stats, actionBar);
        banner.getChildren().addAll(imageContainer, info);
        header.getChildren().add(banner);
        setTop(header);
    }

    // Crea un statBox a partir de un Label existente
    private VBox createStatBox(String title, Label valueLabel) {
        VBox box = new VBox(2);
        box.setAlignment(Pos.CENTER_LEFT);

        Label titleLabel = new Label(title);
        titleLabel.getStyleClass().add("stat-title");

        valueLabel.getStyleClass().add("stat-value");

        box.getChildren().addAll(titleLabel, valueLabel);
        return box;
    }

    private void initializeContent() {
        contentTabs = new TabPane();
        contentTabs.getStyleClass().add("instance-tabs");

        Tab detailsTab = new Tab("DETALLES");
        detailsTab.setClosable(false);
        detailsTab.setContent(createDetailsContent());

        Tab settingsTab = new Tab("CONFIGURACIÓN");
        settingsTab.setClosable(false);
        settingsTab.setContent(createSettingsContent());

        contentTabs.getTabs().addAll(detailsTab, settingsTab);
        setCenter(contentTabs);
    }

    private VBox createDetailsContent() {
        VBox content = new VBox(15);
        content.setPadding(new Insets(20));

        Label description = new Label("Información detallada de la instancia");
        description.getStyleClass().add("content-description");

        VBox versionInfo = new VBox(10);
        versionInfo.getStyleClass().add("info-box");

        Label versionTitle = new Label("Versión de Minecraft");
        versionTitle.getStyleClass().add("info-title");

        Label versionValue = new Label();
        versionValue.getStyleClass().add("info-value");
        versionValue.textProperty().bind(
                javafx.beans.binding.Bindings.when(
                        javafx.beans.binding.Bindings.isNotNull(currentInstanceProperty)
                ).then(
                        javafx.beans.binding.Bindings.selectString(currentInstanceProperty, "version")
                ).otherwise("No seleccionada")
        );

        Label pathTitle = new Label("Ubicación");
        pathTitle.getStyleClass().add("info-title");

        Label pathValue = new Label();
        pathValue.getStyleClass().add("info-value");
        pathValue.textProperty().bind(
                javafx.beans.binding.Bindings.when(
                        javafx.beans.binding.Bindings.isNotNull(currentInstanceProperty)
                ).then(
                        javafx.beans.binding.Bindings.createStringBinding(
                                () -> "./instances/" + (currentInstance != null ? currentInstance.getName() : ""),
                                currentInstanceProperty
                        )
                ).otherwise("No seleccionada")
        );

        versionInfo.getChildren().addAll(versionTitle, versionValue, pathTitle, pathValue);
        content.getChildren().addAll(description, versionInfo);

        return content;
    }

    private VBox createSettingsContent() {
        VBox content = new VBox(15);
        content.setPadding(new Insets(20));

        Label description = new Label("Configuración específica de esta instancia");
        description.getStyleClass().add("content-description");

        VBox settingsContainer = new VBox(10);

        CheckBox autoUpdate = new CheckBox("Actualizar automáticamente");
        CheckBox enableSnapshots = new CheckBox("Permitir snapshots");
        CheckBox keepLauncherOpen = new CheckBox("Mantener launcher abierto");

        settingsContainer.getChildren().addAll(autoUpdate, enableSnapshots, keepLauncherOpen);
        content.getChildren().addAll(description, settingsContainer);

        return content;
    }

    private void showEmptyState() {
        instanceName.setText("Selecciona una instancia");
        instanceVersion.setText("Elige una instancia para ver los detalles");
        playButton.setDisable(true);
        contentTabs.setDisable(true);

        lastPlayedLabel.setText("Nunca");
        playTimeLabel.setText("0 h");
        achievementsLabel.setText("0/0");
    }

    public void showInstance(InstanceManager.Instance instance) {
        this.currentInstance = instance;

        if (instance != null) {
            instanceName.setText(instance.getName());
            instanceVersion.setText(cleanVersion(instance.getVersion()));
            playButton.setDisable(false);
            contentTabs.setDisable(false);

            // Actualiza los stats dinámicamente
            lastPlayedLabel.setText(instance.getLastPlayedFormatted());
            playTimeLabel.setText("not implemented");
            achievementsLabel.setText("not implemented");
        } else {
            showEmptyState();
        }
    }

    private String cleanVersion(String version) {
        if (version == null) return "Minecraft";

        String cleaned = version
                .replace("quilt", "")
                .replace("fabric", "")
                .replace("forge", "")
                .replace("--", "-")
                .replace("-loader", "")
                .trim();

        if (cleaned.startsWith("-")) cleaned = cleaned.substring(1);
        if (cleaned.endsWith("-")) cleaned = cleaned.substring(0, cleaned.length() - 1);

        return cleaned.isEmpty() ? "Minecraft" : "Minecraft " + cleaned;
    }
}
