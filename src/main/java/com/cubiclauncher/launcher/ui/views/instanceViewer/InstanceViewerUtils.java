package com.cubiclauncher.launcher.ui.views.instanceViewer;

import com.cubiclauncher.launcher.core.PathManager;
import javafx.geometry.Rectangle2D;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.Desktop;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class InstanceViewerUtils {
    private static final Logger log = LoggerFactory.getLogger(InstanceViewerUtils.class);

    private InstanceViewerUtils() {}

    public static String formatVersion(String version) {
        if (version == null) return "";
        return version.replace("-", " ").replace("loader", "").trim();
    }

    public static String extractLoader(String version) {
        if (version == null) return "Vanilla";
        if (version.contains("fabric")) return "Fabric";
        if (version.contains("forge"))  return "Forge";
        if (version.contains("quilt"))  return "Quilt";
        return "Vanilla";
    }

    public static String timestamp() {
        return java.time.LocalTime.now()
                .format(java.time.format.DateTimeFormatter.ofPattern("HH:mm:ss"));
    }

    public static VBox createInfoBox(String labelText, Label valueLabel) {
        VBox box = new VBox(4);
        box.getStyleClass().add("info-box");

        Label titleLabel = new Label(labelText);
        titleLabel.getStyleClass().add("info-label");
        valueLabel.getStyleClass().add("info-value");

        box.getChildren().addAll(titleLabel, valueLabel);
        return box;
    }

    public static void createModalField(GridPane grid, String labelText, String value, int row) {
        Label fieldLabel = new Label(labelText);
        fieldLabel.getStyleClass().add("editor-field-label");

        TextField field = new TextField(value);
        field.getStyleClass().add("editor-text-field");
        field.setPrefWidth(230);

        grid.add(fieldLabel, 0, row);
        grid.add(field, 1, row);
    }

    public static List<File> getScreenshots(com.cubiclauncher.launcher.core.InstanceManager.Instance instance) {
        List<File> screenshots = new ArrayList<>();
        File screenshotsDir = PathManager.getInstance().getInstancePath()
                .resolve(instance.getName())
                .resolve("screenshots")
                .toFile();

        if (screenshotsDir.exists() && screenshotsDir.isDirectory()) {
            File[] files = screenshotsDir.listFiles((dir, name) -> {
                String n = name.toLowerCase();
                return n.endsWith(".png") || n.endsWith(".jpg") || n.endsWith(".jpeg");
            });
            if (files != null) Collections.addAll(screenshots, files);
        }
        return screenshots;
    }

    public static void applyCoverImage(ImageView imageView, Label imageIcon,
                                       com.cubiclauncher.launcher.core.InstanceManager.Instance instance) {
        if (instance != null && instance.getCoverImage() != null) {
            File imgFile = new File(instance.getCoverImage());
            if (imgFile.exists()) {
                try {
                    Image image = new Image(imgFile.toURI().toString());
                    double width  = image.getWidth();
                    double height = image.getHeight();
                    double side   = Math.min(width, height);

                    imageView.setViewport(new Rectangle2D((width - side) / 2, (height - side) / 2, side, side));
                    imageView.setFitWidth(180);
                    imageView.setFitHeight(180);
                    imageView.setImage(image);

                    imageIcon.setVisible(false);
                    imageView.setVisible(true);
                    return;
                } catch (Exception ignored) {}
            }
        }
        imageView.setImage(null);
        imageView.setVisible(false);
        imageIcon.setVisible(true);
    }

    public static void openScreenshotsFolder(com.cubiclauncher.launcher.core.InstanceManager.Instance instance) {
        Path path = PathManager.getInstance().getInstancePath()
                .resolve(instance.getName())
                .resolve("screenshots");
        File folder = path.toFile();
        if (!folder.exists()) folder.mkdirs(); // TODO: tomar excepcion

        try {
            if (System.getProperty("os.name").toLowerCase().contains("linux")) {
                new ProcessBuilder("xdg-open", folder.getAbsolutePath()).start();
            } else if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.OPEN)) {
                Desktop.getDesktop().open(folder);
            }
        } catch (IOException e) {
            log.error(e.getLocalizedMessage()); // TODO: usar logger global
        }
    }
}