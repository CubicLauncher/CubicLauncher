/*
 * Copyright (C) 2026 Santiagolxx, Notstaff and CubicLauncher contributors
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

package com.cubiclauncher.launcher.ui.views.auth;

import com.cubiclauncher.claunch.auth.AuthCallback;
import com.cubiclauncher.launcher.core.auth.AccountManagerProvider;
import com.cubiclauncher.launcher.core.LanguageManager;
import com.cubiclauncher.launcher.util.StylesLoader;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.stage.Window;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.Desktop;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

/**
 * Dialogo para el inicio de sesión con Microsoft usando el flujo de código de
 * dispositivo..
 */
public class MicrosoftLoginDialog implements AuthCallback {
    private static final Logger log = LoggerFactory.getLogger(MicrosoftLoginDialog.class);
    private final LanguageManager lm = LanguageManager.getInstance();
    private final Window owner;
    private Stage stage;

    private Label descriptionLabel;
    private Hyperlink urlLink;
    private TextField codeField;
    private Label statusLabel;
    private ProgressBar progressBar;

    public MicrosoftLoginDialog(Window owner) {
        this.owner = owner;
    }

    public void show() {
        stage = new Stage();
        stage.initOwner(owner);
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.initStyle(StageStyle.UTILITY);
        stage.setTitle(lm.get("auth.microsoft.title"));

        VBox root = new VBox(20);
        root.setPadding(new Insets(30));
        root.setAlignment(Pos.CENTER);
        root.setPrefWidth(450);
        root.getStyleClass().add("microsoft-login-root");

        descriptionLabel = new Label(lm.get("auth.microsoft.description"));
        descriptionLabel.setWrapText(true);
        descriptionLabel.setTextAlignment(javafx.scene.text.TextAlignment.CENTER);
        descriptionLabel.getStyleClass().add("progress-text");

        urlLink = new Hyperlink("https://microsoft.com/link");
        urlLink.getStyleClass().add("progress-text");
        urlLink.setOnAction(e -> openBrowser(urlLink.getText()));

        codeField = new TextField("----");
        codeField.setEditable(false);
        codeField.setAlignment(Pos.CENTER);
        codeField.setStyle(
                "-fx-font-size: 24px; -fx-font-family: 'Monospaced'; -fx-font-weight: bold; -fx-background-color: #2a2a2a; -fx-text-fill: #3a86ff;");

        Button copyButton = new Button(lm.get("auth.microsoft.copy_code"));
        copyButton.getStyleClass().add("btn-secondary");
        copyButton.setOnAction(e -> {
            Clipboard clipboard = Clipboard.getSystemClipboard();
            ClipboardContent content = new ClipboardContent();
            content.putString(codeField.getText());
            clipboard.setContent(content);
        });

        statusLabel = new Label(lm.get("auth.microsoft.waiting"));
        statusLabel.getStyleClass().add("progress-text");
        statusLabel.setStyle("-fx-font-style: italic;");

        progressBar = new ProgressBar(-1);
        progressBar.setMaxWidth(Double.MAX_VALUE);
        progressBar.setVisible(false);
        progressBar.getStyleClass().add("cdark-progress-bar");

        Button cancelButton = new Button(lm.get("auth.microsoft.cancel"));
        cancelButton.getStyleClass().add("btn-secondary");
        cancelButton.setOnAction(e -> stage.close());

        root.getChildren().addAll(descriptionLabel, urlLink, codeField, copyButton, statusLabel, progressBar,
                cancelButton);

        Scene scene = new Scene(root);
        StylesLoader.load(scene, "/com.cubiclauncher.launcher/styles/ui.main.css");
        stage.setScene(scene);

        // Iniciar proceso de autenticación
        AccountManagerProvider.getInstance().loginWithMicrosoft(this)
                .thenAccept(account -> {
                    if (account != null) {
                        Platform.runLater(() -> {
                            statusLabel.setText(lm.get("auth.microsoft.success"));
                            statusLabel.setTextFill(Color.GREEN);
                            progressBar.setVisible(false);

                            // Cerrar despues de un momento
                            new Thread(() -> {
                                try {
                                    Thread.sleep(2000);
                                } catch (InterruptedException ignored) {
                                }
                                Platform.runLater(stage::close);
                            }).start();
                        });
                    }
                });

        stage.show();
    }

    @Override
    public void onDeviceCode(String userCode, String verificationUri, int expiresIn) {
        Platform.runLater(() -> {
            codeField.setText(userCode);
            urlLink.setText(verificationUri);
            progressBar.setVisible(true);
        });
    }

    @Override
    public void onProgress(AuthCallback.AuthStep step, String message) {
        Platform.runLater(() -> statusLabel.setText(message));
    }

    @Override
    public void onError(AuthCallback.AuthStep step, String message) {
        Platform.runLater(() -> {
            String errorMsg = lm.get("auth.microsoft.error");
            if (errorMsg.contains("%s")) {
                statusLabel.setText(String.format(errorMsg, message));
            } else {
                statusLabel.setText(errorMsg + ": " + message);
            }
            statusLabel.setTextFill(Color.RED);
            progressBar.setVisible(false);
        });
    }

    @Override
    public void onSuccess(String token) {
        // El resultado final lo maneja el CompletableFuture en AccountManagerProvider
    }

    private void openBrowser(String url) {
        try {
            if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
                Desktop.getDesktop().browse(new URI(url));
            } else {
                // Fallback para Linux
                new ProcessBuilder("xdg-open", url).start();
            }
        } catch (IOException | URISyntaxException e) {
            log.error("No se pudo abrir el navegador", e);
        }
    }
}
