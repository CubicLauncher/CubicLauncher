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

import com.cubiclauncher.launcher.core.LanguageManager;
import com.cubiclauncher.launcher.util.StylesLoader;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.image.Image;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import java.util.Objects;

public class ErrorConsoleView {
    private static ErrorConsoleView instance;
    private Stage stage;
    private TextArea textArea;
    private final LanguageManager lm = LanguageManager.getInstance();

    private ErrorConsoleView() {
        init();
    }

    public static ErrorConsoleView getInstance() {
        if (instance == null) {
            instance = new ErrorConsoleView();
        }
        return instance;
    }

    private void init() {
        Platform.runLater(() -> {
            stage = new Stage();
            stage.setTitle(lm.get("launcher.debug_console.title"));

            VBox root = new VBox(10);
            root.setPadding(new Insets(10));
            root.getStyleClass().add("console-root");

            Label title = new Label(lm.get("settings.error_console"));
            title.getStyleClass().add("section-title");

            textArea = new TextArea();
            textArea.setEditable(false);
            textArea.setWrapText(true);
            textArea.getStyleClass().add("console-area");
            VBox.setVgrow(textArea, Priority.ALWAYS);

            Button clearButton = new Button(lm.get("instance.clear"));
            clearButton.setOnAction(e -> textArea.clear());

            Button copyButton = new Button(lm.get("instance.copy"));
            copyButton.setOnAction(e -> {
                javafx.scene.input.Clipboard clipboard = javafx.scene.input.Clipboard.getSystemClipboard();
                javafx.scene.input.ClipboardContent content = new javafx.scene.input.ClipboardContent();
                content.putString(textArea.getText());
                clipboard.setContent(content);
            });

            HBox buttons = new HBox(10, clearButton, copyButton);

            root.getChildren().addAll(title, textArea, buttons);

            Scene scene = new Scene(root, 800, 500);
            StylesLoader.load(scene, "/com.cubiclauncher.launcher/styles/ui.main.css");

            stage.setScene(scene);
            stage.getIcons().add(
                    new Image(Objects.requireNonNull(getClass().getResourceAsStream("/com.cubiclauncher.launcher/assets/logos/cdark.png"))));
        });
    }

    public void show() {
        Platform.runLater(() -> {
            if (stage != null) {
                stage.show();
                stage.toFront();
            }
        });
    }

    public void hide() {
        Platform.runLater(() -> {
            if (stage != null) {
                stage.hide();
            }
        });
    }

    public boolean isShowing() {
        return stage != null && stage.isShowing();
    }

    public void log(String message) {
        Platform.runLater(() -> {
            if (textArea != null) {
                textArea.appendText(message + "\n");
                textArea.setScrollTop(Double.MAX_VALUE);
            }
        });
    }
}
