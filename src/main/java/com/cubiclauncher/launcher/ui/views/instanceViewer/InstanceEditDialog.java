package com.cubiclauncher.launcher.ui.views.instanceViewer;

import com.cubiclauncher.launcher.core.InstanceManager;
import com.cubiclauncher.launcher.core.LanguageManager;
import com.cubiclauncher.launcher.ui.components.ModalHeader;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.geometry.Rectangle2D;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.stage.Modality;
import javafx.stage.StageStyle;
import javafx.stage.Window;

import java.io.File;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

/**
 *  Modal dialog para editar la instance y su foto
 */
public class InstanceEditDialog {

    private final LanguageManager lm = LanguageManager.getInstance();
    private final Window owner;

    /** Llamado después de un guardado exitoso */
    private Consumer<InstanceManager.Instance> onSaved;

    public InstanceEditDialog(Window owner) {
        this.owner = owner;
    }

    public void setOnSaved(Consumer<InstanceManager.Instance> onSaved) {
        this.onSaved = onSaved;
    }

    // ── Public entry point ───────────────────────────────────────────────────

    public void show(InstanceManager.Instance instance) {
        Dialog<Void> dialog = new Dialog<>();
        dialog.initOwner(owner);
        dialog.initModality(Modality.WINDOW_MODAL);
        dialog.initStyle(StageStyle.TRANSPARENT);
        dialog.setTitle(lm.get("instance.edit_title"));

        DialogPane dialogPane = dialog.getDialogPane();
        dialogPane.getStyleClass().add("editor-dialog-pane");
        dialogPane.setHeaderText(null);
        dialogPane.setGraphic(null);
        dialogPane.getButtonTypes().add(ButtonType.CLOSE);
        Node closeBtn = dialogPane.lookupButton(ButtonType.CLOSE);
        if (closeBtn != null) closeBtn.setVisible(false);
        dialogPane.setBackground(null);
        dialogPane.setPadding(Insets.EMPTY);

        // Editable state
        String[] selectedCover = { instance.getCoverImage() };

        VBox windowRoot = new VBox();
        windowRoot.getStyleClass().add("editor-window-root");
        windowRoot.setPrefWidth(500);

        ModalHeader header  = new ModalHeader(lm.get("instance.edit_title"), dialog);
        VBox        content = buildContent(instance, selectedCover);
        HBox        footer  = new HBox(15); // filled below once fields are available

        // ── Fields (kept as locals to be referenced in saveBtn action) ────────
        GridPane grid = (GridPane) ((VBox) content.getChildren().getFirst()).getChildren().getFirst();

        // Pull field references from grid (they were added at rows 0-3)
        TextField nameField    = (TextField) getGridCell(grid, 0);
        TextField versionField = (TextField) getGridCell(grid, 1);
        TextField minMemField  = (TextField) ((HBox) getGridCell(grid, 2)).getChildren().get(0);
        TextField maxMemField  = (TextField) ((HBox) getGridCell(grid, 2)).getChildren().get(1);
        TextField jvmArgsField = (TextField) getGridCell(grid, 3);

        // ── Footer ────────────────────────────────────────────────────────────
        footer.getStyleClass().add("editor-footer");
        footer.setAlignment(Pos.CENTER_RIGHT);

        Button deleteBtn = new Button(lm.get("instance.btn_delete"));
        deleteBtn.getStyleClass().add("btn-secondary");
        deleteBtn.setStyle("-fx-text-fill: #ff5555; -fx-font-size: 11px; -fx-padding: 8 20;");

        Button saveBtn = new Button(lm.get("instance.btn_save"));
        saveBtn.getStyleClass().add("play-button-primary");
        saveBtn.setStyle("-fx-font-size: 11px; -fx-padding: 8 24;");

        Region footerSpacer = new Region();
        HBox.setHgrow(footerSpacer, Priority.ALWAYS);
        footer.getChildren().addAll(deleteBtn, footerSpacer, saveBtn);

        windowRoot.getChildren().addAll(header, content, footer);
        dialogPane.setContent(windowRoot);

        // ── Actions ───────────────────────────────────────────────────────────
        saveBtn.setOnAction(e -> {
            String oldName = instance.getName();
            String newName = nameField.getText();

            instance.setVersion(versionField.getText());
            try {
                instance.setMinMemory(minMemField.getText().isEmpty() ? null : Integer.parseInt(minMemField.getText()));
                instance.setMaxMemory(maxMemField.getText().isEmpty() ? null : Integer.parseInt(maxMemField.getText()));
            } catch (NumberFormatException ignored) {
                // esto esta ignorado asi como me ignora ella
                // TODO: tomar excepciones
            }
            instance.setJvmArgs(jvmArgsField.getText());
            instance.setCoverImage(selectedCover[0]);

            if (!newName.equals(oldName)) {
                InstanceManager.getInstance().renameInstance(oldName, newName);
            } else {
                InstanceManager.getInstance().saveInstance(instance);
                if (onSaved != null) onSaved.accept(instance);
            }
            dialog.close();
        });

        deleteBtn.setOnAction(e ->
                new InstanceDeleteDialog(owner).show(instance, dialog::close));

        Platform.runLater(() -> {
            if (dialogPane.getScene() != null) {
                dialogPane.getScene().setFill(null);
                dialogPane.getScene().getStylesheets().add(
                        Objects.requireNonNull(getClass()
                                        .getResource("/com.cubiclauncher.launcher/styles/ui.main.css"))
                                .toExternalForm());
            }
            nameField.requestFocus();
        });

        dialog.showAndWait();
    }

    // ── Content builder ───────────────────────────────────────────────────────

    private VBox buildContent(InstanceManager.Instance instance, String[] selectedCover) {
        VBox content = new VBox(25);
        content.getStyleClass().add("editor-content");
        content.setAlignment(Pos.TOP_LEFT);

        // We wrap the grid in a VBox so we can retrieve it easily
        VBox gridWrapper = new VBox();
        GridPane grid = buildFieldGrid(instance);
        gridWrapper.getChildren().add(grid);

        VBox gallerySection = buildGallerySection(instance, selectedCover);
        content.getChildren().addAll(gridWrapper, gallerySection);
        return content;
    }

    private GridPane buildFieldGrid(InstanceManager.Instance instance) {
        GridPane grid = new GridPane();
        grid.setHgap(15);
        grid.setVgap(20);
        grid.setAlignment(Pos.CENTER_LEFT);

        InstanceViewerUtils.createModalField(grid, lm.get("instance.field_name"),    instance.getName(),    0);
        InstanceViewerUtils.createModalField(grid, lm.get("instance.field_version"), instance.getVersion(), 1);

        Label ramLabel = new Label(lm.get("instance.field_ram"));
        ramLabel.getStyleClass().add("editor-field-label");

        TextField minMem = new TextField(instance.getMinMemory() != null ? String.valueOf(instance.getMinMemory()) : "");
        TextField maxMem = new TextField(instance.getMaxMemory() != null ? String.valueOf(instance.getMaxMemory()) : "");
        minMem.getStyleClass().add("editor-text-field");
        maxMem.getStyleClass().add("editor-text-field");
        minMem.setPromptText("Min");
        maxMem.setPromptText("Max");
        minMem.setPrefWidth(110);
        maxMem.setPrefWidth(110);

        HBox ramBox = new HBox(10);
        ramBox.getChildren().addAll(minMem, maxMem);
        grid.add(ramLabel, 0, 2);
        grid.add(ramBox,   1, 2);

        InstanceViewerUtils.createModalField(grid, lm.get("instance.field_jvm_args"),
                instance.getJvmArgs() != null ? instance.getJvmArgs() : "", 3);

        return grid;
    }

    private VBox buildGallerySection(InstanceManager.Instance instance, String[] selectedCover) {
        VBox section = new VBox(10);

        HBox galleryHeader = new HBox(10);
        galleryHeader.setAlignment(Pos.CENTER_LEFT);

        Label galleryLabel = new Label(lm.get("instance.gallery_title"));
        galleryLabel.getStyleClass().add("editor-field-label");

        Button openFolderBtn = new Button(lm.get("instance.open_folder"));
        openFolderBtn.getStyleClass().add("btn-secondary");
        openFolderBtn.setStyle("-fx-font-size: 9px; -fx-padding: 4 8;");
        openFolderBtn.setOnAction(e -> InstanceViewerUtils.openScreenshotsFolder(instance));

        galleryHeader.getChildren().addAll(galleryLabel, openFolderBtn);

        HBox galleryItems = new HBox(10);
        galleryItems.setPadding(new Insets(5));

        List<File> screenshots = InstanceViewerUtils.getScreenshots(instance);
        if (screenshots.isEmpty()) {
            Label noScreenshots = new Label(lm.get("instance.no_screenshots"));
            noScreenshots.setStyle("-fx-text-fill: #666; -fx-font-size: 12px;");
            galleryItems.getChildren().add(noScreenshots);
        } else {
            for (File screenshot : screenshots) {
                galleryItems.getChildren().add(createThumbnail(screenshot, selectedCover));
            }
        }

        ScrollPane galleryScroll = new ScrollPane(galleryItems);
        galleryScroll.setPrefHeight(130);
        galleryScroll.getStyleClass().add("console-scroll");
        galleryScroll.setVbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        galleryScroll.setFitToHeight(true);
        galleryScroll.setStyle("-fx-background: transparent; -fx-background-color: transparent;");

        section.getChildren().addAll(galleryHeader, galleryScroll);
        return section;
    }

    // ── Thumbnail ─────────────────────────────────────────────────────────────

    private VBox createThumbnail(File file, String[] selectedCover) {
        VBox container = new VBox();
        container.setPrefSize(100, 100);
        container.getStyleClass().add("editor-thumb-container");
        container.setAlignment(Pos.CENTER);

        Image img  = new Image(file.toURI().toString());
        ImageView thumb = new ImageView(img);
        double w = img.getWidth(), h = img.getHeight();
        double side = Math.min(w, h);
        thumb.setViewport(new Rectangle2D((w - side) / 2, (h - side) / 2, side, side));
        thumb.setFitWidth(90);
        thumb.setFitHeight(90);
        thumb.setPreserveRatio(true);

        container.getChildren().add(thumb);
        applyThumbStyle(container, file.getAbsolutePath().equals(selectedCover[0]));

        container.setOnMouseClicked(e -> {
            selectedCover[0] = file.getAbsolutePath();
            if (container.getParent() instanceof Pane parent) {
                parent.getChildren().forEach(node -> {
                    if (node instanceof VBox vb) applyThumbStyle(vb, false);
                });
            }
            applyThumbStyle(container, true);
        });

        return container;
    }

    private static void applyThumbStyle(VBox container, boolean selected) {
        if (selected) {
            container.setStyle("-fx-border-color: #3a86ff; -fx-border-width: 2; -fx-background-color: #1a1a1a;");
        } else {
            container.setStyle("-fx-border-color: #2a2a2a; -fx-border-width: 1; -fx-background-color: transparent;");
        }
    }

    // ── Helper ────────────────────────────────────────────────────────────────

    /** Retrieves a node from a GridPane by column and row index. */
    private static Node getGridCell(GridPane grid, int row) {
        for (Node n : grid.getChildren()) {
            if (GridPane.getColumnIndex(n) == 1 && GridPane.getRowIndex(n) == row) return n;
        }
        throw new IllegalStateException("Grid cell not found at col=" + 1 + " row=" + row);
    }
}