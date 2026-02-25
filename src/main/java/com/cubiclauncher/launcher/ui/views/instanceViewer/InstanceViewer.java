package com.cubiclauncher.launcher.ui.views.instanceViewer;

import com.cubiclauncher.launcher.core.InstanceManager;
import com.cubiclauncher.launcher.core.LanguageManager;
import com.cubiclauncher.launcher.core.events.EventBus;
import com.cubiclauncher.launcher.core.events.EventType;
import javafx.animation.FadeTransition;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.NumberBinding;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.layout.BorderPane;
import javafx.util.Duration;

/**
 * Panel de instance viewer
 * <p>
 * Las responsabilidades se mantienen aquí
 *  - Ciclo de vida Singleton
 *  - Subs al bus de eventos
 *  - Coordinador de componentes
 * <p>
 * La lógica visual vive en:
 *  - {@link InstanceViewerHeader}
 *  - {@link InstanceViewerContent}
 *  - {@link InstanceEditDialog}
 *  - {@link InstanceViewerUtils}
 */
public class InstanceViewer extends BorderPane {

    private static InstanceViewer instance;

    private InstanceManager.Instance currentInstance;
    private final ObjectProperty<InstanceManager.Instance> currentInstanceProperty = new SimpleObjectProperty<>();
    private final LanguageManager lm = LanguageManager.getInstance();

    private final InstanceViewerHeader  header  = new InstanceViewerHeader();
    private final InstanceViewerContent content = new InstanceViewerContent();

    // ── Constructor ───────────────────────────────────────────────────────────

    private InstanceViewer() {
        super();
        getStyleClass().add("instance-viewer");
        NumberBinding headerHeight = Bindings.min(heightProperty().multiply(0.3), 300);
        header.prefHeightProperty().bind(headerHeight);
        header.maxHeightProperty().bind(headerHeight);
        setTop(header);
        setCenter(content);

        header.setOnDeleteRequest(this::showEditDialog);

        showEmptyState();
        setupEventSubscriptions();
    }

    public static InstanceViewer getInstance() {
        if (instance == null) instance = new InstanceViewer();
        return instance;
    }

    // ── Events ────────────────────────────────────────────────────────────────

    private void setupEventSubscriptions() {
        EventBus eventBus = EventBus.get();

        eventBus.subscribe(EventType.GAME_OUTPUT, eventData -> {
            String instanceName = eventData.getString("instance_name");
            String line         = eventData.getString("line");
            if (line != null && !line.trim().isEmpty()
                    && currentInstance != null
                    && currentInstance.getName().equals(instanceName)) {
                Platform.runLater(() -> content.appendLog(line));
            }
        });

        eventBus.subscribe(EventType.INSTANCE_DELETED, eventData -> {
            String instanceName = eventData.getString("instance_name");
            if (currentInstance != null && currentInstance.getName().equals(instanceName)) {
                Platform.runLater(this::showEmptyState);
            }
        });

        eventBus.subscribe(EventType.INSTANCE_RENAME, eventData -> {
            // Sidebar handles selection refresh; nothing extra needed here for now.
        });
    }

    // ── Public API ────────────────────────────────────────────────────────────

    public void showInstance(InstanceManager.Instance inst) {
        if (this.currentInstance == inst) return;

        FadeTransition fadeOut = new FadeTransition(Duration.millis(150), this);
        fadeOut.setFromValue(1.0);
        fadeOut.setToValue(0.0);

        fadeOut.setOnFinished(e -> {
            // Actualizar la instancia actual y los datos
            this.currentInstance = inst;
            this.currentInstanceProperty.set(inst);

            if (inst != null) {
                header.update(inst);
                content.update(inst);
                content.clearLogs();
                content.appendLog(lm.get("instance.field_name")    + ": " + inst.getName());
                content.appendLog(lm.get("instance.field_version") + ": " + inst.getVersion());
                content.appendLog(lm.get("instance.ready"));
            } else {
                showEmptyState(); // Ya actualiza UI esto.
            }

            FadeTransition fadeIn = new FadeTransition(Duration.millis(200), this);
            fadeIn.setFromValue(0.0);
            fadeIn.setToValue(1.0);
            fadeIn.play();
        });

        fadeOut.play();
    }

    public void showEmptyState() {
        currentInstance = null;
        currentInstanceProperty.set(null);

        header.showEmpty();
        content.showEmpty();
        content.clearLogs();
        content.getLogsArea().appendText(lm.get("instance.select_hint") + "\n");
    }

    /** Opens the edit dialog for the given instance. */
    public void showEditDialog(InstanceManager.Instance inst) {
        InstanceEditDialog editDialog = new InstanceEditDialog(getScene().getWindow());
        editDialog.setOnSaved(this::showInstance);
        editDialog.show(inst);
    }

    // ── Console helpers (used by external callers) ────────────────────────────

    public void appendLog(String message)  { content.appendLog(message);  }
    public void appendError(String error)  { content.appendError(error);  }
    public void clearLogs()                { content.clearLogs();         }

    public ObjectProperty<InstanceManager.Instance> currentInstanceProperty() {
        return currentInstanceProperty;
    }
}