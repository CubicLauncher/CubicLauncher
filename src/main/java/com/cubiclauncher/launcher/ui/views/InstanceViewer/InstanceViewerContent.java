package com.cubiclauncher.launcher.ui.views.InstanceViewer;

import com.cubiclauncher.launcher.core.InstanceManager;
import com.cubiclauncher.launcher.core.LanguageManager;
import com.cubiclauncher.launcher.core.PathManager;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;

/**
 * Center content panel for the InstanceViewer.
 * Contains two cards side-by-side: Details and Console.
 */
public class InstanceViewerContent extends HBox {

    private final LanguageManager lm = LanguageManager.getInstance();

    // Details card labels
    private Label versionLabel;
    private Label loaderLabel;
    private Label locationLabel;

    // Console
    private TextArea logsArea;

    public InstanceViewerContent() {
        super(32);
        getStyleClass().add("instance-content");
        setPadding(new Insets(32, 40, 40, 20));
        build();
    }

    // ── Build ────────────────────────────────────────────────────────────────

    private void build() {
        getChildren().addAll(buildDetailsCard(), buildLogsCard());
    }

    private VBox buildDetailsCard() {
        VBox card = new VBox(12);
        card.getStyleClass().add("details-card");
        card.setPrefWidth(400);

        Label title = new Label(lm.get("instance.details"));
        title.getStyleClass().add("section-title");

        VBox grid = new VBox(16);
        grid.getStyleClass().add("details-grid");

        versionLabel  = new Label("-");
        loaderLabel   = new Label("-");
        locationLabel = new Label("-");

        grid.getChildren().addAll(
                InstanceViewerUtils.createInfoBox(lm.get("instance.version"),  versionLabel),
                InstanceViewerUtils.createInfoBox(lm.get("instance.loader"),   loaderLabel),
                InstanceViewerUtils.createInfoBox(lm.get("instance.location"), locationLabel)
        );

        card.getChildren().addAll(title, grid);
        return card;
    }

    private VBox buildLogsCard() {
        VBox card = new VBox(0);
        card.getStyleClass().add("logs-card");
        HBox.setHgrow(card, Priority.ALWAYS);

        HBox logsHeader = buildLogsHeader();

        logsArea = new TextArea();
        logsArea.getStyleClass().add("console-area");
        logsArea.setEditable(false);
        logsArea.setWrapText(true);
        logsArea.setStyle("-fx-background-color: transparent; -fx-control-inner-background: transparent;");

        ScrollPane scroll = new ScrollPane(logsArea);
        scroll.getStyleClass().add("console-scroll");
        scroll.setFitToWidth(true);
        scroll.setFitToHeight(true);
        scroll.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        scroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scroll.setStyle("-fx-background: transparent; -fx-background-color: transparent;");

        VBox container = new VBox(0);
        container.getStyleClass().add("logs-container");
        container.getChildren().addAll(logsHeader, scroll);
        VBox.setVgrow(scroll, Priority.ALWAYS);

        card.getChildren().add(container);
        return card;
    }

    private HBox buildLogsHeader() {
        HBox header = new HBox(8);
        header.setAlignment(Pos.CENTER_LEFT);

        Label title = new Label(lm.get("instance.console"));
        title.getStyleClass().add("section-title");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button copyBtn = new Button(lm.get("instance.copy"));
        copyBtn.getStyleClass().add("btn-secondary");
        copyBtn.setOnAction(e -> {
            if (!logsArea.getText().isEmpty()) {
                javafx.scene.input.Clipboard cb = javafx.scene.input.Clipboard.getSystemClipboard();
                javafx.scene.input.ClipboardContent cc = new javafx.scene.input.ClipboardContent();
                cc.putString(logsArea.getText());
                cb.setContent(cc);
            }
        });

        Button clearBtn = new Button(lm.get("instance.clear"));
        clearBtn.getStyleClass().add("btn-secondary");
        clearBtn.setOnAction(e -> {
            logsArea.clear();
            appendLog(lm.get("instance.console_cleared"));
        });

        header.getChildren().addAll(title, spacer, copyBtn, clearBtn);
        return header;
    }

    // ── Public API ───────────────────────────────────────────────────────────

    public void update(InstanceManager.Instance instance) {
        if (instance != null) {
            versionLabel.setText(instance.getVersion());
            loaderLabel.setText(InstanceViewerUtils.extractLoader(instance.getVersion()));
            locationLabel.setText(
                    PathManager.getInstance().getInstancePath().resolve(instance.getName()).toString());
        } else {
            showEmpty();
        }
    }

    public void showEmpty() {
        versionLabel.setText("-");
        loaderLabel.setText("-");
        locationLabel.setText("-");
    }

    public void appendLog(String message) {
        String ts = InstanceViewerUtils.timestamp();
        javafx.application.Platform.runLater(() ->
                logsArea.appendText("[" + ts + "] " + message + "\n"));
    }

    public void appendError(String error) {
        String ts = InstanceViewerUtils.timestamp();
        javafx.application.Platform.runLater(() ->
                logsArea.appendText("[" + ts + "] ERROR: " + error + "\n"));
    }

    public void clearLogs() {
        logsArea.clear();
    }

    public TextArea getLogsArea() {
        return logsArea;
    }
}