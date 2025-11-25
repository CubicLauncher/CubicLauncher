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
package com.cubiclauncher.launcher.ui.views;

import com.cubiclauncher.launcher.core.InstanceManager;
import com.cubiclauncher.launcher.core.LauncherWrapper;
import com.cubiclauncher.launcher.core.TaskManager;
import com.cubiclauncher.launcher.core.events.EventBus;
import com.cubiclauncher.launcher.core.events.EventType;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;

import java.util.List;

public class VersionsView {
    private static final LauncherWrapper launcher = LauncherWrapper.getInstance();
    private static final InstanceManager instanceManager = InstanceManager.getInstance();
    private static final TaskManager taskManager = TaskManager.getInstance();
    private static final EventBus eventBus = EventBus.get();

    // Estado del lazy loading y referencias
    private static boolean availableVersionsLoaded = false;
    private static ListView<String> currentVersionsList;
    private static ToggleButton currentInstalledBtn;
    private static ToggleButton currentAvailableBtn;

    public static BorderPane create() {
        BorderPane root = new BorderPane();
        root.getStyleClass().add("versions-view");
        root.setPadding(new Insets(20));

        VBox header = createHeader();
        root.setTop(header);

        VBox content = createContent();
        root.setCenter(content);

        // Suscribirse a eventos de descarga completada
        setupEventListeners();

        return root;
    }

    private static VBox createHeader() {
        VBox header = new VBox(15);
        header.setPadding(new Insets(0, 0, 20, 0));

        Label title = new Label("Gestor de Versiones");
        title.getStyleClass().add("versions-main-title");

        Label subtitle = new Label("Administra las versiones de Minecraft y crea nuevas instancias");
        subtitle.getStyleClass().add("versions-subtitle");

        header.getChildren().addAll(title, subtitle);
        return header;
    }

    private static VBox createContent() {
        VBox content = new VBox(20);

        HBox toggleBox = createToggleButtons();

        currentVersionsList = new ListView<>();
        currentVersionsList.getStyleClass().add("version-list");
        currentVersionsList.setPrefHeight(350);
        VBox.setVgrow(currentVersionsList, Priority.ALWAYS);

        VBox createInstanceSection = createInstanceCreationSection();

        content.getChildren().addAll(toggleBox, currentVersionsList, new Separator(), createInstanceSection);

        setupToggleButtons(toggleBox, currentVersionsList);

        return content;
    }

    private static HBox createToggleButtons() {
        HBox toggleBox = new HBox(0);
        toggleBox.setAlignment(Pos.CENTER_LEFT);
        toggleBox.getStyleClass().add("toggle-button-group");

        ToggleGroup toggleGroup = new ToggleGroup();

        currentInstalledBtn = new ToggleButton("Instaladas");
        currentInstalledBtn.setUserData("installed");
        currentInstalledBtn.getStyleClass().add("toggle-button-left");
        currentInstalledBtn.setToggleGroup(toggleGroup);
        currentInstalledBtn.setSelected(true);

        currentAvailableBtn = new ToggleButton("Disponibles");
        currentAvailableBtn.setUserData("available");
        currentAvailableBtn.getStyleClass().add("toggle-button-right");
        currentAvailableBtn.setToggleGroup(toggleGroup);

        Button refreshBtn = new Button("↻");
        refreshBtn.getStyleClass().add("refresh-button");
        refreshBtn.setTooltip(new Tooltip("Actualizar lista"));

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        toggleBox.getChildren().addAll(currentInstalledBtn, currentAvailableBtn, spacer, refreshBtn);

        return toggleBox;
    }

    private static void setupToggleButtons(HBox toggleBox, ListView<String> versionsList) {
        ToggleButton installedBtn = (ToggleButton) toggleBox.getChildren().get(0);
        ToggleButton availableBtn = (ToggleButton) toggleBox.getChildren().get(1);
        Button refreshBtn = (Button) toggleBox.getChildren().get(3);

        // Cargar instaladas por defecto
        loadInstalledVersions(versionsList);

        // LAZY LOADING: Solo cargar disponibles cuando se selecciona
        availableBtn.setOnAction(e -> {
            if (availableBtn.isSelected()) {
                if (!availableVersionsLoaded) {
                    showLoadingPlaceholder(versionsList);
                    loadAvailableVersionsLazy(versionsList);
                } else {
                    loadAvailableVersions(versionsList);
                }
            }
        });

        installedBtn.setOnAction(e -> {
            if (installedBtn.isSelected()) {
                loadInstalledVersions(versionsList);
                versionsList.setCellFactory(lv -> new InstalledVersionCell(versionsList));
            }
        });

        // Botón refresh
        refreshBtn.setOnAction(e -> {
            if (installedBtn.isSelected()) {
                loadInstalledVersions(versionsList);
            } else {
                refreshBtn.setDisable(true);
                refreshBtn.setText("⟳");
                availableVersionsLoaded = false;
                showLoadingPlaceholder(versionsList);

                taskManager.runAsync(launcher::getAvailableVersions)
                        .thenAccept(versions -> Platform.runLater(() -> {
                            versionsList.setItems(FXCollections.observableArrayList(versions));
                            versionsList.setCellFactory(lv -> new AvailableVersionCell());
                            availableVersionsLoaded = true;
                            refreshBtn.setDisable(false);
                            refreshBtn.setText("↻");
                        }))
                        .exceptionally(error -> {
                            Platform.runLater(() -> {
                                showErrorPlaceholder(versionsList, error.getMessage());
                                refreshBtn.setDisable(false);
                                refreshBtn.setText("↻");
                            });
                            return null;
                        });
            }
        });
    }

    // ==================== EVENT BUS INTEGRATION ====================

    private static void setupEventListeners() {
        // Actualizar lista cuando se complete una descarga
        eventBus.subscribe(EventType.DOWNLOAD_COMPLETED, eventData -> {
            String downloadedVersion = eventData.getString("version");

            Platform.runLater(() -> {
                // Si estamos viendo "Instaladas", actualizar la lista
                if (currentInstalledBtn != null && currentInstalledBtn.isSelected()) {
                    loadInstalledVersions(currentVersionsList);
                }

                // Si estamos viendo "Disponibles", actualizar para mostrar (I)
                if (currentAvailableBtn != null && currentAvailableBtn.isSelected()) {
                    // Forzar recarga para actualizar el estado instalado/no instalado
                    availableVersionsLoaded = false;
                    loadAvailableVersionsLazy(currentVersionsList);
                }

                System.out.println("✓ Lista de versiones actualizada después de descargar: " + downloadedVersion);
            });
        });
    }

    private static VBox createInstanceCreationSection() {
        VBox section = new VBox(15);
        section.getStyleClass().add("version-section");
        section.getStyleClass().add("create-instance-section");

        Label sectionTitle = new Label("Crear Nueva Instancia");
        sectionTitle.getStyleClass().add("section-title");

        GridPane grid = new GridPane();
        grid.setHgap(15);
        grid.setVgap(12);
        grid.getStyleClass().add("instance-form");

        Label nameLabel = new Label("Nombre:");
        nameLabel.getStyleClass().add("form-label");

        TextField nameField = new TextField();
        nameField.setPromptText("Mi Mundo Survival");
        nameField.getStyleClass().add("form-field");

        TextFormatter<String> textFormatter = new TextFormatter<>(change -> {
            String newText = change.getControlNewText();
            return (newText.length() <= 16 && newText.length() >= 1 && newText.matches("[a-zA-Z0-9 _-]*")) ? change : null;
        });
        nameField.setTextFormatter(textFormatter);

        Label versionLabel = new Label("Versión:");
        versionLabel.getStyleClass().add("form-label");

        ComboBox<String> versionCombo = new ComboBox<>();
        versionCombo.setPromptText("Selecciona una versión");
        versionCombo.getStyleClass().add("form-combo");
        versionCombo.setMaxWidth(Double.MAX_VALUE);

        // Cargar versiones instaladas inicialmente
        taskManager.runAsync(launcher::getInstalledVersions)
                .thenAccept(versions -> Platform.runLater(() ->
                        versionCombo.setItems(FXCollections.observableArrayList(versions))
                ));

        // EVENT BUS: Actualizar combo cuando se descargue una versión
        eventBus.subscribe(EventType.DOWNLOAD_COMPLETED, eventData -> Platform.runLater(() -> taskManager.runAsync(launcher::getInstalledVersions)
                .thenAccept(versions -> Platform.runLater(() ->
                        versionCombo.setItems(FXCollections.observableArrayList(versions))
                ))));

        grid.add(nameLabel, 0, 0);
        grid.add(nameField, 1, 0);
        grid.add(versionLabel, 0, 1);
        grid.add(versionCombo, 1, 1);

        ColumnConstraints col1 = new ColumnConstraints();
        col1.setMinWidth(100);
        col1.setPrefWidth(100);

        ColumnConstraints col2 = new ColumnConstraints();
        col2.setHgrow(Priority.ALWAYS);

        grid.getColumnConstraints().addAll(col1, col2);

        HBox actionBox = new HBox(10);
        actionBox.setAlignment(Pos.CENTER_RIGHT);

        Button createButton = new Button("Crear Instancia");
        createButton.getStyleClass().add("primary-button");
        createButton.setDisable(true);

        Label statusLabel = new Label();
        statusLabel.getStyleClass().add("status-label");
        statusLabel.setVisible(false);

        nameField.textProperty().addListener((obs, old, newVal) ->
                createButton.setDisable(newVal.trim().isEmpty() || versionCombo.getValue() == null)
        );

        versionCombo.valueProperty().addListener((obs, old, newVal) ->
                createButton.setDisable(newVal == null || nameField.getText().trim().isEmpty())
        );

        createButton.setOnAction(e -> {
            String name = nameField.getText().trim();
            String version = versionCombo.getValue();

            if (instanceManager.instanceExists(name)) {
                showStatus(statusLabel, "❌ Ya existe una instancia con ese nombre", false);
                return;
            }

            createButton.setDisable(true);
            taskManager.runAsync(
                    () -> instanceManager.createInstance(name, version),
                    () -> {
                        showStatus(statusLabel, "✓ Instancia creada exitosamente", true);
                        nameField.clear();
                        versionCombo.setValue(null);
                        createButton.setDisable(false);
                    },
                    () -> {
                        showStatus(statusLabel, "❌ Error al crear la instancia", false);
                        createButton.setDisable(false);
                    }
            );
        });

        actionBox.getChildren().addAll(statusLabel, createButton);

        section.getChildren().addAll(sectionTitle, grid, actionBox);
        return section;
    }

    // ==================== LAZY LOADING HELPERS ====================

    private static void loadAvailableVersionsLazy(ListView<String> listView) {
        taskManager.runAsync(launcher::getAvailableVersions)
                .thenAccept(versions -> Platform.runLater(() -> {
                    listView.setItems(FXCollections.observableArrayList(versions));
                    listView.setCellFactory(lv -> new AvailableVersionCell());
                    availableVersionsLoaded = true;
                }))
                .exceptionally(error -> {
                    Platform.runLater(() -> showErrorPlaceholder(listView, error.getMessage()));
                    return null;
                });
    }

    private static void showLoadingPlaceholder(ListView<String> listView) {
        VBox placeholder = new VBox(15);
        placeholder.setAlignment(Pos.CENTER);
        placeholder.setPadding(new Insets(40));

        ProgressIndicator progress = new ProgressIndicator();
        progress.setMaxSize(50, 50);

        Label loadingText = new Label("Cargando versiones disponibles...");
        loadingText.getStyleClass().add("empty-state-hint");

        placeholder.getChildren().addAll(progress, loadingText);
        listView.setPlaceholder(placeholder);
        listView.setItems(FXCollections.observableArrayList());
    }

    private static void showErrorPlaceholder(ListView<String> listView, String errorMsg) {
        VBox placeholder = new VBox(15);
        placeholder.setAlignment(Pos.CENTER);
        placeholder.setPadding(new Insets(40));

        Label icon = new Label("⚠️");
        icon.setStyle("-fx-font-size: 32px;");

        Label errorText = new Label("Error al cargar versiones");
        errorText.getStyleClass().add("empty-state-title");

        Label errorDetail = new Label(errorMsg);
        errorDetail.getStyleClass().add("empty-state-hint");
        errorDetail.setWrapText(true);

        placeholder.getChildren().addAll(icon, errorText, errorDetail);
        listView.setPlaceholder(placeholder);
        listView.setItems(FXCollections.observableArrayList());
    }

    // ==================== HELPER METHODS ====================

    private static void loadInstalledVersions(ListView<String> listView) {
        List<String> installed = launcher.getInstalledVersions();
        Platform.runLater(() -> {
            listView.setItems(FXCollections.observableArrayList(installed));
            listView.setCellFactory(lv -> new InstalledVersionCell(listView));
        });
    }

    private static void loadAvailableVersions(ListView<String> listView) {
        taskManager.runAsync(launcher::getAvailableVersions)
                .thenAccept(versions -> Platform.runLater(() -> {
                    listView.setItems(FXCollections.observableArrayList(versions));
                    listView.setCellFactory(lv -> new AvailableVersionCell());
                }));
    }

    private static void showStatus(Label statusLabel, String message, boolean isSuccess) {
        statusLabel.setText(message);
        statusLabel.setStyle(isSuccess ?
                "-fx-text-fill: #90c090;" :
                "-fx-text-fill: #d08080;"
        );
        statusLabel.setVisible(true);

        if (isSuccess) {
            taskManager.runAsync(
                    () -> {
                        try {
                            Thread.sleep(3000);
                        } catch (InterruptedException ignored) {}
                    },
                    () -> statusLabel.setVisible(false),
                    e -> {}
            );
        }
    }

    // ==================== CUSTOM CELLS ====================

    private static class InstalledVersionCell extends ListCell<String> {
        private final ListView<String> parentList;

        public InstalledVersionCell(ListView<String> parentList) {
            this.parentList = parentList;
        }

        @Override
        protected void updateItem(String version, boolean empty) {
            super.updateItem(version, empty);

            if (empty || version == null) {
                setGraphic(null);
                setText(null);
                return;
            }

            HBox container = new HBox(15);
            container.setAlignment(Pos.CENTER_LEFT);
            container.setPadding(new Insets(10));
            container.getStyleClass().add("version-cell");

            Circle indicator = new Circle(6, Color.web("#90c090"));

            VBox infoBox = new VBox(3);
            HBox.setHgrow(infoBox, Priority.ALWAYS);

            Label versionLabel = new Label(version);
            versionLabel.getStyleClass().add("version-name");

            Label statusLabel = new Label("Instalada");
            statusLabel.getStyleClass().add("version-status");
            statusLabel.setStyle("-fx-text-fill: #90c090;");

            infoBox.getChildren().addAll(versionLabel, statusLabel);

            Button uninstallBtn = new Button("Desinstalar");
            uninstallBtn.getStyleClass().add("danger-button-small");
            uninstallBtn.setOnAction(e -> {
                Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
                alert.setTitle("Confirmar Desinstalación");
                alert.setHeaderText("¿Desinstalar %s?".formatted(version));
                alert.setContentText("Esta acción no se puede deshacer.");

                alert.showAndWait().ifPresent(response -> {
                    if (response == ButtonType.OK) {
                        // TODO: Implementar desinstalación real
                        taskManager.runAsync(() -> loadInstalledVersions(parentList));
                    }
                });
            });

            container.getChildren().addAll(indicator, infoBox, uninstallBtn);
            setGraphic(container);
        }
    }

    private static class AvailableVersionCell extends ListCell<String> {
        @Override
        protected void updateItem(String version, boolean empty) {
            super.updateItem(version, empty);

            if (empty || version == null) {
                setGraphic(null);
                setText(null);
                return;
            }

            HBox container = new HBox(15);
            container.setAlignment(Pos.CENTER_LEFT);
            container.setPadding(new Insets(10));
            container.getStyleClass().add("version-cell");

            boolean isInstalled = launcher.getInstalledVersions().contains(version);

            Circle indicator = new Circle(6, isInstalled ?
                    Color.web("#90c090") : Color.web("#555555"));

            VBox infoBox = new VBox(3);
            HBox.setHgrow(infoBox, Priority.ALWAYS);

            Label versionLabel = new Label(version);
            versionLabel.getStyleClass().add("version-name");

            Label typeLabel = new Label(getVersionType(version));
            typeLabel.getStyleClass().add("version-type");

            infoBox.getChildren().addAll(versionLabel, typeLabel);

            Button actionBtn = new Button(isInstalled ? "Instalada ✓" : "Instalar");
            actionBtn.getStyleClass().add(isInstalled ? "installed-button-small" : "primary-button-small");
            actionBtn.setDisable(isInstalled);

            if (!isInstalled) {
                actionBtn.setOnAction(e -> {
                    actionBtn.setDisable(true);
                    actionBtn.setText("Instalando...");
                    taskManager.runAsync(() -> launcher.downloadMinecraftVersion(version));
                    // EVENT BUS se encargará de actualizar la UI cuando termine
                });
            }

            container.getChildren().addAll(indicator, infoBox, actionBtn);
            setGraphic(container);
        }

        private String getVersionType(String version) {
            if (version.contains("w") || version.contains("-pre") || version.contains("-rc")) {
                return "Snapshot";
            } else if (version.contains("a") || version.contains("b")) {
                return "Alpha/Beta";
            }
            return "Release";
        }
    }
}