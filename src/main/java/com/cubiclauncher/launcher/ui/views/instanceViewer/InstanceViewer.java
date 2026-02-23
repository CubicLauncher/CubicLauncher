package com.cubiclauncher.launcher.ui.views.instanceViewer;

import com.cubiclauncher.launcher.core.InstanceManager;
import com.cubiclauncher.launcher.core.LanguageManager;
import com.cubiclauncher.launcher.core.events.EventBus;
import com.cubiclauncher.launcher.core.events.EventType;
import javafx.application.Platform;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.layout.BorderPane;

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
 *  - {@link InstanceDeleteDialog}
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

        setTop(header);
        setCenter(content);

        // Header delete button → show delete dialog
        header.setOnDeleteRequest(inst ->
                new InstanceDeleteDialog(getScene().getWindow()).show(inst, null));

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
            showEmptyState();
        }
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