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

package com.cubiclauncher.launcher.ui.components;

import com.cubiclauncher.claunch.auth.Account;
import com.cubiclauncher.launcher.core.PathManager;
import com.cubiclauncher.launcher.core.auth.AccountManagerProvider;
import com.cubiclauncher.launcher.core.LanguageManager;
import com.cubiclauncher.launcher.core.events.EventBus;
import com.cubiclauncher.launcher.core.events.EventType;
import com.cubiclauncher.launcher.ui.views.auth.MicrosoftLoginDialog;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.TextField;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.input.MouseButton;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Circle;

public class BottomBar extends HBox {
    private static final AccountManagerProvider amp = AccountManagerProvider.getInstance();
    private static final LanguageManager lm = LanguageManager.getInstance();
    private static BottomBar instance;

    private final ProgressBar progressBar;
    private final Label progressLabel;
    private final Label progressText;
    private final Label statusLabel;

    private final ImageView userAvatar;
    private final Label userName;
    private final HBox editContainer;
    private final TextField userNameField;

    private boolean isEditing = false;

    private BottomBar() {
        super(20);
        setPadding(new Insets(12, 30, 12, 30));
        setAlignment(Pos.CENTER_LEFT);
        getStyleClass().add("bottom-bar");

        // --- User profile ---
        HBox userProfile = new HBox(10);
        userProfile.setAlignment(Pos.CENTER_LEFT);

        // Avatar
        userAvatar = new ImageView();
        userAvatar.setFitWidth(28);
        userAvatar.setFitHeight(28);
        Circle clip = new Circle(14, 14, 14);
        userAvatar.setClip(clip);

        StackPane avatarContainer = new StackPane(userAvatar);
        avatarContainer.setPrefSize(28, 28);

        // Obtener nombre de la cuenta seleccionada
        String currentUsername = getSelectedUsername();
        updateAvatar(currentUsername);

        userName = new Label(currentUsername);
        userName.getStyleClass().add("user-profile");

        // --- Editing components ---
        userNameField = new TextField();
        userNameField.getStyleClass().add("user-profile-field");
        Label cancelButton = new Label("x");
        cancelButton.getStyleClass().add("cancel-edit-button");
        editContainer = new HBox(5, userNameField, cancelButton);
        editContainer.setAlignment(Pos.CENTER_LEFT);
        editContainer.setVisible(false);

        // --- Container to swap between Label and TextField ---
        StackPane userNameContainer = new StackPane(userName, editContainer);
        userNameContainer.setAlignment(Pos.CENTER_LEFT);

        userProfile.getChildren().addAll(avatarContainer, userNameContainer);

        // --- Event Handlers ---
        userName.setOnMouseClicked(event -> {
            if (isEditing)
                return;

            if (event.getButton() == MouseButton.PRIMARY) {
                showAccountMenu(event.getScreenX(), event.getScreenY());
            }
        });

        cancelButton.setOnMouseClicked(event -> cancelEdit());

        userNameField.focusedProperty().addListener((obs, oldVal, newVal) -> {
            if (!newVal && isEditing) {
                cancelEdit();
            }
        });

        // Limit to 16 characters and only allow a-z, A-Z, 0-9, _
        userNameField.textProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal.length() > 16 || !newVal.matches("[a-zA-Z0-9_]*")) {
                userNameField.setText(oldVal);
            }
        });

        userNameField.setPromptText("a-Z, 0-9, _ (3-16)");

        userNameField.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ENTER) {
                addNewAccount();
            } else if (event.getCode() == KeyCode.ESCAPE) {
                cancelEdit();
            }
        });

        // --- Spacers ---
        Region leftSpacer = new Region();
        HBox.setHgrow(leftSpacer, Priority.ALWAYS);
        Region rightSpacer = new Region();
        HBox.setHgrow(rightSpacer, Priority.ALWAYS);

        // --- Progress center ---
        VBox progressCenter = new VBox(5);
        progressCenter.setAlignment(Pos.CENTER);
        HBox.setHgrow(progressCenter, Priority.ALWAYS);

        progressText = new Label("");
        progressText.getStyleClass().add("progress-text");
        progressText.setVisible(false);

        HBox progressContainer = new HBox(10);
        progressContainer.setAlignment(Pos.CENTER);

        progressBar = new ProgressBar(0);
        progressBar.setVisible(false);
        progressBar.setPrefWidth(300);
        progressBar.setPrefHeight(16);
        progressBar.getStyleClass().add("cdark-progress-bar");

        progressLabel = new Label("");
        progressLabel.setVisible(false);
        progressLabel.getStyleClass().add("progress-percent");

        progressContainer.getChildren().addAll(progressBar, progressLabel);
        progressCenter.getChildren().addAll(progressText, progressContainer);

        statusLabel = new Label(lm.get("bottom_bar.ready"));
        statusLabel.getStyleClass().add("status-label");

        getChildren().addAll(userProfile, leftSpacer, progressCenter, rightSpacer, statusLabel);
        EventBus.get().subscribe(EventType.LANGUAGE_CHANGED,
                e -> refreshTranslations());
        // Subscribirse al cambio de cuenta para actualizar la UI
        EventBus.get().subscribe(EventType.ACCOUNT_CHANGED, e -> {
            String newUsername = e.getString("username");
            javafx.application.Platform.runLater(() -> {
                userName.setText(newUsername);
                updateAvatar(newUsername);
            });
        });
    }

    /**
     * Obtiene el nombre de usuario de la cuenta seleccionada.
     * Si no hay cuentas, retorna "Steve" como fallback.
     */
    private String getSelectedUsername() {
        Account selected = amp.getSelectedAccount();
        return selected != null ? selected.getUsername() : "Steve";
    }

    private void showAccountMenu(double screenX, double screenY) {
        ContextMenu contextMenu = new ContextMenu();
        contextMenu.getStyleClass().add("account-context-menu");

        Account selectedAccount = amp.getSelectedAccount();

        for (Account account : amp.getAccounts()) {
            String displayName = account.getUsername();
            // Mostrar tipo de cuenta si es Microsoft
            if (account.isMicrosoft()) {
                displayName += " ★";
            }
            MenuItem item = new MenuItem(displayName);
            if (selectedAccount != null && account.getUuid().equals(selectedAccount.getUuid())) {
                item.getStyleClass().add("active-account");
                item.setDisable(true);
            }
            item.setOnAction(e -> {
                amp.selectAccount(account);
                userName.setText(account.getUsername());
                updateAvatar(account.getUsername());
            });
            contextMenu.getItems().add(item);
        }

        contextMenu.getItems().add(new SeparatorMenuItem());

        MenuItem addAccount = new MenuItem(
                lm.get("bottom_bar.add_account") != null ? lm.get("bottom_bar.add_account") : "Añadir cuenta");
        addAccount.setOnAction(e -> {
            isEditing = true;
            userNameField.setText("");
            editContainer.setVisible(true);
            userName.setVisible(false);
            userNameField.requestFocus();
        });
        contextMenu.getItems().add(addAccount);

        MenuItem addMicrosoftAccount = new MenuItem(
                lm.get("bottom_bar.add_microsoft_account") != null ? lm.get("bottom_bar.add_microsoft_account")
                        : "Añadir cuenta Microsoft");
        addMicrosoftAccount.setOnAction(e -> {
            MicrosoftLoginDialog loginDialog = new MicrosoftLoginDialog(getScene().getWindow());
            loginDialog.show();
        });
        contextMenu.getItems().add(addMicrosoftAccount);

        if (amp.getAccountCount() > 1) {
            MenuItem removeAccount = new MenuItem(
                    lm.get("bottom_bar.remove_account") != null ? lm.get("bottom_bar.remove_account")
                            : "Eliminar cuenta actual");
            removeAccount.getStyleClass().add("remove-account-item");
            removeAccount.setOnAction(e -> {
                if (selectedAccount != null) {
                    amp.removeAccount(selectedAccount);
                    Account newSelected = amp.getSelectedAccount();
                    if (newSelected != null) {
                        userName.setText(newSelected.getUsername());
                        updateAvatar(newSelected.getUsername());
                    }
                }
            });
            contextMenu.getItems().add(removeAccount);
        }

        contextMenu.show(userName, screenX, screenY);
    }

    /**
     * Agrega una nueva cuenta offline usando el AccountManager.
     * Cada nueva cuenta obtiene un UUID único y persistente.
     */
    private void addNewAccount() {
        if (!isEditing)
            return;
        String newUsername = userNameField.getText().trim();
        if (newUsername.matches("[a-zA-Z0-9_]{3,16}")) {
            Account newAccount = amp.addOfflineAccount(newUsername);
            if (newAccount != null) {
                amp.selectAccount(newAccount);
                userName.setText(newUsername);
                updateAvatar(newUsername);
            }
            editContainer.setVisible(false);
            userName.setVisible(true);
            isEditing = false;
        }
        // If length or characters are not valid, we keep the editing state so the user
        // can correct it
    }

    private void cancelEdit() {
        if (!isEditing)
            return;
        editContainer.setVisible(false);
        userName.setVisible(true);
        isEditing = false;
    }

    private void updateAvatar(String username) {
        userAvatar.setVisible(false);

        String avatarUrl = "https://minotar.net/avatar/" + username + "/28";
        Image avatarImage = new Image(avatarUrl, true); // background loading

        avatarImage.progressProperty().addListener((obs, oldProgress, newProgress) -> {
            if (newProgress.doubleValue() == 1.0) {
                userAvatar.setImage(avatarImage);
                userAvatar.setVisible(true);
            }
        });

        avatarImage.errorProperty().addListener((obs, wasError, isError) -> {
            if (isError) {
                System.err.println("Failed to load avatar for: " + username);
                if (avatarImage.getException() != null
                        && !(avatarImage.getException() instanceof java.io.FileNotFoundException)) {
                    avatarImage.getException().printStackTrace();
                }
                userAvatar.setImage(new Image("/com.cubiclauncher.launcher/assets/logos/cubic.png"));
            }
        });
    }

    /**
     * Updates UI labels with current translations from LanguageManager.
     */
    public void refreshTranslations() {
        if (statusLabel != null) {
            statusLabel.setText(lm.get("bottom_bar.ready"));
        }
    }

    public static BottomBar getInstance() {
        if (instance == null) {
            instance = new BottomBar();
        }
        return instance;
    }

    // Getters para que UIBridge pueda actualizar la UI
    public ProgressBar getProgressBar() {
        return progressBar;
    }

    public Label getProgressLabel() {
        return progressLabel;
    }

    public Label getProgressText() {
        return progressText;
    }

    public Label getStatusLabel() {
        return statusLabel;
    }
}
