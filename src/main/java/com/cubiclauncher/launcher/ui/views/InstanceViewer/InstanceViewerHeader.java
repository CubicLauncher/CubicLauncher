package com.cubiclauncher.launcher.ui.views.InstanceViewer;

import com.cubiclauncher.launcher.core.InstanceManager;
import com.cubiclauncher.launcher.core.LanguageManager;
import com.cubiclauncher.launcher.ui.controllers.InstanceController;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.geometry.Rectangle2D;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.Stop;
import javafx.scene.shape.Rectangle;

import java.io.File;
import java.util.Objects;
import java.util.function.Consumer;

public class InstanceViewerHeader extends StackPane {

    private final LanguageManager lm = LanguageManager.getInstance();

    private Label instanceName;
    private Label instanceVersion;
    private Label lastPlayedLabel;
    private Button playButton;

    private ImageView bannerImageView;

    private Consumer<InstanceManager.Instance> onDeleteRequest;

    public InstanceViewerHeader() {
        super();
        getStyleClass().add("instance-header");
        setMinHeight(300);
        setMaxHeight(300);
        build();
    }

    // ── Build ────────────────────────────────────────────────────────────────

    private void build() {
        // ── Layer 0: banner ──
        bannerImageView = new ImageView();
        bannerImageView.setPreserveRatio(true);
        bannerImageView.setSmooth(true);

        // El clip define el área visible del banner
        Rectangle bannerClip = new Rectangle();
        bannerClip.widthProperty().bind(widthProperty());
        bannerClip.heightProperty().bind(heightProperty());
        bannerImageView.setClip(bannerClip);

        widthProperty().addListener((obs, o, n) -> updateBannerViewport());
        heightProperty().addListener((obs, o, n) -> updateBannerViewport());

        // ── Layer 1: gradient overlay ──
        Region overlay = getRegion();

        // ── Layer 2: content row ──
        HBox contentRow = new HBox(24);
        contentRow.setAlignment(Pos.BOTTOM_LEFT);
        contentRow.setPadding(new Insets(0, 40, 28, 40));
        contentRow.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
        StackPane.setAlignment(contentRow, Pos.BOTTOM_LEFT);

        contentRow.getChildren().addAll(buildSquare(), buildInfoSection());

        getChildren().addAll(bannerImageView, overlay, contentRow);
    }

    private static Region getRegion() {
        Region overlay = new Region();
        overlay.setBackground(new Background(new BackgroundFill(
                new LinearGradient(0, 0, 0, 1, true, CycleMethod.NO_CYCLE,
                        new Stop(0.0, Color.rgb(0, 0, 0, 0.10)),
                        new Stop(0.30, Color.rgb(0, 0, 0, 0.55)),
                        new Stop(0.75, Color.rgb(0, 0, 0, 0.55)),
                        new Stop(1.0, Color.rgb(0, 0, 0, 0.85))),
                CornerRadii.EMPTY, Insets.EMPTY)));
        overlay.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
        return overlay;
    }

    private void updateBannerViewport() {
        Image img = bannerImageView.getImage();
        if (img == null || img.getWidth() == 0 || img.getHeight() == 0) return;

        double containerW = getWidth();
        double containerH = getHeight();
        if (containerW <= 0 || containerH <= 0) return;

        double imgW = img.getWidth();
        double imgH = img.getHeight();

        double scale = Math.max(containerW / imgW, containerH / imgH);

        double visibleW = containerW / scale;
        double visibleH = containerH / scale;

        double x = (imgW - visibleW) / 2.0;
        double y = (imgH - visibleH) / 2.0;

        bannerImageView.setFitWidth(containerW);
        bannerImageView.setFitHeight(containerH);
        bannerImageView.setViewport(new Rectangle2D(x, y, visibleW, visibleH));
    }

    private StackPane buildSquare() {
        StackPane square = new StackPane();
        square.getStyleClass().add("instance-image");
        square.setMinSize(100, 100);
        square.setMaxSize(100, 100);

        Label imageIcon = new Label();
        imageIcon.getStyleClass().add("instance-icon");

        ImageView icon = new ImageView(
                new Image(Objects.requireNonNull(
                        getClass().getResourceAsStream(
                                "/com.cubiclauncher.launcher/assets/logos/cubic.png"
                        )
                ))
        );
        icon.setFitWidth(72);
        icon.setFitHeight(72);
        icon.setPreserveRatio(true);
        icon.setSmooth(true);

        square.getChildren().add(icon);
        return square;
    }

    private VBox buildInfoSection() {
        VBox info = new VBox(8);
        info.setAlignment(Pos.BOTTOM_LEFT);
        info.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(info, Priority.ALWAYS);

        instanceName = new Label();
        instanceName.getStyleClass().add("instance-name");

        instanceVersion = new Label();
        instanceVersion.getStyleClass().add("instance-version");

        GridPane metaInfo = buildMetaGrid();
        HBox     actions  = buildActions();

        info.getChildren().addAll(instanceName, instanceVersion, metaInfo, actions);
        return info;
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
            applyBannerImage(instance);
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
        bannerImageView.setImage(null);
        bannerImageView.setViewport(null);
    }

    private void applyBannerImage(InstanceManager.Instance instance) {
        if (instance == null || instance.getCoverImage() == null || instance.getCoverImage().isBlank()) {
            bannerImageView.setImage(null);
            bannerImageView.setViewport(null);
            return;
        }
        try {
            File f = new File(instance.getCoverImage());
            if (f.exists()) {
                Image img = new Image(f.toURI().toString(), true);
                bannerImageView.setImage(img);
                // Una vez cargada la imagen, calcular el viewport
                img.progressProperty().addListener((obs, o, progress) -> {
                    if (progress.doubleValue() >= 1.0) updateBannerViewport();
                });
                // Por si ya estaba en caché
                updateBannerViewport();
            } else {
                bannerImageView.setImage(null);
                bannerImageView.setViewport(null);
            }
        } catch (Exception ex) {
            bannerImageView.setImage(null);
            bannerImageView.setViewport(null);
        }
    }

    public void setOnDeleteRequest(Consumer<InstanceManager.Instance> handler) {
        this.onDeleteRequest = handler;
    }
}