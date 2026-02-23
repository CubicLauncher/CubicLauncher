package com.cubiclauncher.launcher.ui.views.InstanceViewer;

import com.cubiclauncher.launcher.core.InstanceManager;
import com.cubiclauncher.launcher.core.LanguageManager;
import com.cubiclauncher.launcher.ui.controllers.InstanceController;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.layout.GridPane;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.Stop;
import javafx.scene.shape.Rectangle;

import java.util.function.Consumer;

/**
 * Header panel for the InstanceViewer.
 * Shows the cover image as a banner background, instance name/version,
 * last played date and action buttons.
 */
public class InstanceViewerHeader extends StackPane {

    private final LanguageManager lm = LanguageManager.getInstance();

    private Label instanceName;
    private Label instanceVersion;
    private Label lastPlayedLabel;
    private Button playButton;

    // Banner background
    private ImageView bannerImageView;
    private Label     imageIcon; // fallback icon (kept for InstanceViewerUtils compat)

    /** Called when the user clicks Delete from the header. */
    private Consumer<InstanceManager.Instance> onDeleteRequest;

    public InstanceViewerHeader() {
        super();
        getStyleClass().add("instance-header");
        setMinHeight(220);
        setMaxHeight(220);
        build();
    }

    // ── Build ────────────────────────────────────────────────────────────────

    private void build() {
        // ── Layer 0: banner image (fills the whole header) ──
        bannerImageView = new ImageView();
        bannerImageView.setPreserveRatio(false); // we'll control size manually
        bannerImageView.setSmooth(true);

        // Keep image centered and "cover"-like by binding to parent size
        bannerImageView.fitWidthProperty().bind(widthProperty());
        bannerImageView.fitHeightProperty().bind(heightProperty());

        // Clip to the header bounds (optional, keeps it tidy)
        Rectangle clip = new Rectangle();
        clip.widthProperty().bind(widthProperty());
        clip.heightProperty().bind(heightProperty());
        bannerImageView.setClip(clip);

        // ── Layer 1: dark gradient overlay so text stays readable ──
        Region overlay = new Region();
        overlay.setBackground(new Background(new BackgroundFill(
                new LinearGradient(0, 0, 0, 1, true, CycleMethod.NO_CYCLE,
                        new Stop(0.0, Color.rgb(0, 0, 0, 0.10)),
                        new Stop(0.6, Color.rgb(0, 0, 0, 0.55)),
                        new Stop(1.0, Color.rgb(0, 0, 0, 0.80))),
                CornerRadii.EMPTY, Insets.EMPTY)));
        overlay.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);

        // ── Layer 2: fallback icon (shown when there is no image) ──
        imageIcon = new Label("⬢");
        imageIcon.getStyleClass().add("instance-icon");
        imageIcon.setVisible(false); // hidden by default; shown by applyCoverImage when needed

        // ── Layer 3: content (name, version, buttons) ──
        VBox contentBox = buildContentBox();

        getChildren().addAll(bannerImageView, overlay, imageIcon, contentBox);
    }

    private VBox buildContentBox() {
        VBox box = new VBox(12);
        box.setAlignment(Pos.BOTTOM_LEFT);
        box.setPadding(new Insets(0, 40, 30, 40));
        box.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
        StackPane.setAlignment(box, Pos.BOTTOM_LEFT);

        instanceName = new Label();
        instanceName.getStyleClass().add("instance-name");

        instanceVersion = new Label();
        instanceVersion.getStyleClass().add("instance-version");

        GridPane metaInfo = buildMetaGrid();
        HBox     actions  = buildActions();

        box.getChildren().addAll(instanceName, instanceVersion, metaInfo, actions);
        return box;
    }

    private GridPane buildMetaGrid() {
        GridPane metaInfo = new GridPane();
        metaInfo.getStyleClass().add("instance-meta");
        metaInfo.setHgap(32);
        metaInfo.setVgap(4);

        Label lastPlayedTitle = new Label(lm.get("instance.last_played"));
        lastPlayedTitle.getStyleClass().add("meta-label");

        lastPlayedLabel = new Label(lm.get("instance.never"));
        lastPlayedLabel.getStyleClass().add("meta-value");

        metaInfo.add(lastPlayedTitle, 0, 0);
        metaInfo.add(lastPlayedLabel, 0, 1);
        return metaInfo;
    }

    private HBox buildActions() {
        HBox actions = new HBox(10);
        actions.setAlignment(Pos.CENTER_LEFT);

        playButton = new Button(lm.get("instance.play"));
        playButton.getStyleClass().add("play-button-primary");
        playButton.setOnAction(e -> {
            InstanceManager.Instance current = (InstanceManager.Instance) playButton.getUserData();
            if (current != null) InstanceController.launchInstance(current.getName());
        });

        Button deleteButton = new Button(lm.get("instance.btn_delete"));
        deleteButton.getStyleClass().add("btn-secondary");
        deleteButton.setStyle("-fx-text-fill: #ff5555;");
        deleteButton.setOnAction(e -> {
            InstanceManager.Instance current = (InstanceManager.Instance) playButton.getUserData();
            if (current != null && onDeleteRequest != null) onDeleteRequest.accept(current);
        });

        actions.getChildren().addAll(playButton, deleteButton);
        return actions;
    }

    // ── Public API ───────────────────────────────────────────────────────────

    public void update(InstanceManager.Instance instance) {
        playButton.setUserData(instance);

        if (instance != null) {
            instanceName.setText(instance.getName());
            instanceVersion.setText(InstanceViewerUtils.formatVersion(instance.getVersion()));
            lastPlayedLabel.setText(instance.getLastPlayedFormatted());
            playButton.setDisable(false);
            // Pass bannerImageView; InstanceViewerUtils sets its image (or null)
            InstanceViewerUtils.applyCoverImage(bannerImageView, imageIcon, instance);
        } else {
            showEmpty();
        }
    }

    public void showEmpty() {
        playButton.setUserData(null);
        instanceName.setText(lm.get("instance.no_selection"));
        instanceVersion.setText("");
        lastPlayedLabel.setText(lm.get("instance.never"));
        playButton.setDisable(true);
        InstanceViewerUtils.applyCoverImage(bannerImageView, imageIcon, null);
    }

    public void setOnDeleteRequest(Consumer<InstanceManager.Instance> handler) {
        this.onDeleteRequest = handler;
    }

    /**
     * Kept for backward-compat with InstanceViewer which may call these
     * after an edit to refresh the cover.
     */
    public ImageView getImageView() { return bannerImageView; }
    public Label     getImageIcon() { return imageIcon; }
}