package com.cubiclauncher.launcher.ui.views.InstanceViewer;

import com.cubiclauncher.launcher.core.InstanceManager;
import com.cubiclauncher.launcher.core.LanguageManager;
import com.cubiclauncher.launcher.ui.components.ModalHeader;
import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.StageStyle;
import javafx.stage.Window;

import java.util.Objects;

/**
 * Modal dialog que consulta al usuario si quiere eliminar esta instance
 */
public class InstanceDeleteDialog {

    private final LanguageManager lm = LanguageManager.getInstance();
    private final Window owner;

    public InstanceDeleteDialog(Window owner) {
        this.owner = owner;
    }

    /**
     * Muestra el dialog, si el usuario acepta la instancia es eliminada
     * {@code onSuccess} es llamado (Podría ser nulo).
     */
    public void show(InstanceManager.Instance instance, Runnable onSuccess) {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.initOwner(owner);
        dialog.initModality(Modality.WINDOW_MODAL);
        dialog.initStyle(StageStyle.TRANSPARENT);
        dialog.setTitle(lm.get("instance.confirm_delete_title"));

        DialogPane dialogPane = dialog.getDialogPane();
        dialogPane.getStyleClass().add("editor-dialog-pane");
        dialogPane.setHeaderText(null);
        dialogPane.setGraphic(null);
        dialogPane.setBackground(null);
        dialogPane.setPadding(javafx.geometry.Insets.EMPTY);
        dialogPane.getButtonTypes().add(ButtonType.CLOSE);

        Node closeBtn = dialogPane.lookupButton(ButtonType.CLOSE);
        if (closeBtn != null) closeBtn.setVisible(false);

        VBox windowRoot = new VBox();
        windowRoot.getStyleClass().add("editor-window-root");
        windowRoot.setPrefWidth(400);

        ModalHeader header  = new ModalHeader(lm.get("instance.confirm_delete_title"), dialog);
        VBox        content = buildContent(instance);
        HBox        footer  = buildFooter(dialog);

        windowRoot.getChildren().addAll(header, content, footer);
        dialogPane.setContent(windowRoot);

        Platform.runLater(() -> {
            if (dialogPane.getScene() != null) {
                dialogPane.getScene().setFill(null);
                dialogPane.getScene().getStylesheets().add(
                        Objects.requireNonNull(getClass()
                                        .getResource("/com.cubiclauncher.launcher/styles/ui.main.css"))
                                .toExternalForm());
            }
        });

        dialog.showAndWait().ifPresent(type -> {
            if (type == ButtonType.OK) {
                InstanceManager.getInstance().deleteInstance(instance.getName());
                if (onSuccess != null) onSuccess.run();
            }
        });
    }

    // ── Builders ─────────────────────────────────────────────────────────────

    private VBox buildContent(InstanceManager.Instance instance) {
        VBox content = new VBox(15);
        content.getStyleClass().add("editor-content");
        content.setAlignment(Pos.CENTER_LEFT);

        Label headerText = new Label(lm.get("instance.confirm_delete_header", instance.getName()));
        headerText.getStyleClass().add("section-title");
        headerText.setWrapText(true);

        Label bodyText = new Label(lm.get("instance.confirm_delete_content"));
        bodyText.getStyleClass().add("info-label");
        bodyText.setWrapText(true);
        bodyText.setStyle("-fx-font-size: 14px;");

        content.getChildren().addAll(headerText, bodyText);
        return content;
    }

    private HBox buildFooter(Dialog<ButtonType> dialog) {
        HBox footer = new HBox(15);
        footer.getStyleClass().add("editor-footer");
        footer.setAlignment(Pos.CENTER_RIGHT);

        Button btnCancel = new Button(lm.get("instance.btn_cancel", "Cancelar"));
        btnCancel.getStyleClass().add("btn-secondary");
        btnCancel.setOnAction(e -> dialog.setResult(ButtonType.CANCEL));

        Button btnDelete = new Button(lm.get("instance.btn_delete"));
        btnDelete.getStyleClass().add("btn-secondary");

        String baseStyle   = "-fx-text-fill: #ff5555; -fx-border-color: #ff5555; -fx-background-color: rgba(255,85,85,0.1);";
        String hoverStyle  = "-fx-text-fill: #ff5555; -fx-border-color: #ff5555; -fx-background-color: rgba(255,85,85,0.2);";
        btnDelete.setStyle(baseStyle);
        btnDelete.setOnMouseEntered(e -> btnDelete.setStyle(hoverStyle));
        btnDelete.setOnMouseExited (e -> btnDelete.setStyle(baseStyle));
        btnDelete.setOnAction(e -> dialog.setResult(ButtonType.OK));

        footer.getChildren().addAll(btnCancel, btnDelete);
        return footer;
    }
}