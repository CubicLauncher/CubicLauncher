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

package com.cubiclauncher.launcher.ui;

import com.cubiclauncher.launcher.core.LanguageManager;
import com.cubiclauncher.launcher.core.SettingsManager;
import com.cubiclauncher.launcher.util.JavaDetector;
import com.cubiclauncher.launcher.util.StylesLoader;
import javafx.animation.FadeTransition;
import javafx.animation.ScaleTransition;
import javafx.animation.TranslateTransition;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Duration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URL;
import java.util.Map;

public class SetupWizardDialog {

    private static final Logger log = LoggerFactory.getLogger(SetupWizardDialog.class);
    private static final String CSS_PATH = "/com.cubiclauncher.launcher/styles/ui.main.css";

    private final Stage wizardStage;
    private final SettingsManager settings = SettingsManager.getInstance();
    private LanguageManager lm = LanguageManager.getInstance();

    // Escena — se mantiene como campo para que el cambio de estilo pueda acceder a
    // ella
    private Scene wizardScene;

    // Área de contenido que apila todos los paneles de pasos
    private final StackPane contentArea = new StackPane();

    // Todos los paneles de pasos
    private final Node[] steps = new Node[5];
    private int currentStep = 0;

    // Botones de navegación
    private Button prevButton;
    private Button nextButton;

    // Indicadores de paso (estilo barra de progreso)
    private final HBox stepIndicators = new HBox(8);

    // ── Etiquetas traducibles almacenadas como campos para que refreshAllLabels()
    // pueda acceder a ellas ──

    // Paso 0 – Bienvenida
    private Label welcomeTitle;
    private Label welcomeBody;
    private Label welcomeHint;

    // Paso 1 – Idioma
    private Label langTitle;
    private Label langBody;

    // Paso 2 – Estilo
    private Label styleTitle;
    private Label styleBody;
    private Label styleWarning;
    private Label styleCustomName;
    private Label styleNativeName;

    // Paso 3 – Java
    private Label javaTitle;
    private Label javaBody;
    private Button detectBtn;
    private Label javaStatusLabel;
    private Label javaSkipHint;

    // Paso 4 – Licencia
    private Label licenseTitle;
    private Label licenseBody;
    private Label licenseFinish;

    // ─────────────────────────────────────────────────────────────────────────

    public SetupWizardDialog(Stage owner) {
        wizardStage = new Stage();
        wizardStage.initModality(Modality.APPLICATION_MODAL);
        wizardStage.initOwner(owner);
        wizardStage.initStyle(StageStyle.UNDECORATED);
        wizardStage.setResizable(false);
        wizardStage.setTitle("CubicLauncher - Configuración");
    }

    /** Muestra el asistente y bloquea hasta que el usuario finalice o lo cierre. */
    public void showAndWait() {
        buildUI();
        wizardStage.showAndWait();
    }

    // ─────────────────────────────────────────────────────────────────────────
    // CONSTRUCCIÓN DE LA IU
    // ─────────────────────────────────────────────────────────────────────────

    private void buildUI() {
        steps[0] = buildWelcomeStep();
        steps[1] = buildLanguageStep();
        steps[2] = buildStyleStep();
        steps[3] = buildJavaStep();
        steps[4] = buildLicenseStep();

        for (int i = 0; i < steps.length; i++) {
            steps[i].setVisible(i == 0);
            steps[i].setOpacity(i == 0 ? 1.0 : 0.0);
            contentArea.getChildren().add(steps[i]);
        }

        contentArea.setMinSize(600, 380);
        contentArea.setPrefSize(600, 380);

        // ── Pie de página de navegación ──────────────────────────────────────────────
        prevButton = new Button();
        prevButton.getStyleClass().add("wizard-nav-btn");
        prevButton.setText(lm.get("wizard.back"));
        prevButton.setVisible(false);
        prevButton.setOnAction(e -> navigate(-1));

        nextButton = new Button();
        nextButton.getStyleClass().addAll("wizard-nav-btn", "wizard-nav-btn-primary");
        nextButton.setText(lm.get("wizard.next"));
        nextButton.setOnAction(e -> navigate(1));

        stepIndicators.setAlignment(Pos.CENTER);
        buildIndicators();

        HBox footer = new HBox(16, prevButton, stepIndicators, nextButton);
        footer.setAlignment(Pos.CENTER);
        footer.setPadding(new Insets(16, 24, 20, 24));
        footer.getStyleClass().add("wizard-footer");

        // ── Raíz ──────────────────────────────────────────────────────────
        VBox root = new VBox(contentArea, footer);
        root.getStyleClass().add("wizard-root");
        VBox.setVgrow(contentArea, Priority.ALWAYS);

        wizardScene = new Scene(root, 640, 480);
        wizardScene.setFill(Color.TRANSPARENT);

        applyStylesheet(settings.isNative_styles());

        wizardStage.setScene(wizardScene);

        // Animación de entrada
        root.setScaleX(0.85);
        root.setScaleY(0.85);
        root.setOpacity(0);
        ScaleTransition scaleIn = new ScaleTransition(Duration.millis(280), root);
        scaleIn.setFromX(0.85);
        scaleIn.setToX(1.0);
        scaleIn.setFromY(0.85);
        scaleIn.setToY(1.0);
        FadeTransition fadeIn = new FadeTransition(Duration.millis(280), root);
        fadeIn.setFromValue(0);
        fadeIn.setToValue(1);
        Platform.runLater(() -> {
            scaleIn.play();
            fadeIn.play();
        });
    }

    // ─────────────────────────────────────────────────────────────────────────
    // NAVEGACIÓN
    // ─────────────────────────────────────────────────────────────────────────

    private void navigate(int direction) {
        int nextStep = currentStep + direction;
        if (nextStep < 0 || nextStep >= steps.length)
            return;

        Node outgoing = steps[currentStep];
        Node incoming = steps[nextStep];

        double slideOutX = direction > 0 ? -60 : 60;
        double slideInX = direction > 0 ? 60 : -60;

        incoming.setTranslateX(slideInX);
        incoming.setOpacity(0);
        incoming.setVisible(true);

        FadeTransition fadeOut = new FadeTransition(Duration.millis(160), outgoing);
        fadeOut.setFromValue(1);
        fadeOut.setToValue(0);

        TranslateTransition slideOut = new TranslateTransition(Duration.millis(160), outgoing);
        slideOut.setToX(slideOutX);

        FadeTransition fadeIn = new FadeTransition(Duration.millis(200), incoming);
        fadeIn.setFromValue(0);
        fadeIn.setToValue(1);

        TranslateTransition slideIn = new TranslateTransition(Duration.millis(200), incoming);
        slideIn.setFromX(slideInX);
        slideIn.setToX(0);

        fadeOut.setOnFinished(e -> {
            outgoing.setVisible(false);
            outgoing.setTranslateX(0);
            fadeIn.play();
            slideIn.play();
        });
        fadeOut.play();
        slideOut.play();

        currentStep = nextStep;
        updateControls();
    }

    private void updateControls() {
        lm = LanguageManager.getInstance();
        prevButton.setVisible(currentStep > 0);
        prevButton.setText(lm.get("wizard.back"));

        boolean isLast = (currentStep == steps.length - 1);
        nextButton.setText(isLast
                ? lm.get("wizard.finish")
                : lm.get("wizard.next"));

        if (isLast) {
            nextButton.getStyleClass().add("wizard-nav-btn-finish");
            nextButton.setOnAction(e -> finish());
        } else {
            nextButton.getStyleClass().remove("wizard-nav-btn-finish");
            nextButton.setOnAction(e -> navigate(1));
        }
        buildIndicators();
    }

    private void finish() {
        settings.setFirstLaunch(false);
        log.info("Asistente de configuración finalizado. Ajustes guardados.");
        wizardStage.close();
    }

    // ─────────────────────────────────────────────────────────────────────────
    // INDICADORES DE PASO
    // ─────────────────────────────────────────────────────────────────────────

    private void buildIndicators() {
        stepIndicators.getChildren().clear();
        for (int i = 0; i < steps.length; i++) {
            Region dot = new Region();
            dot.setPrefSize(8, 8);
            dot.setMinSize(8, 8);
            dot.getStyleClass().add(i == currentStep ? "wizard-dot-active" : "wizard-dot");
            stepIndicators.getChildren().add(dot);
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // CAMBIO DE HOJA DE ESTILOS (aplicado inmediatamente)
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Añade o elimina la hoja de estilos CubicDark de la escena actual del
     * asistente.
     * Se llama tanto durante la construcción inicial como cuando el usuario
     * selecciona una tarjeta de estilo.
     */
    private void applyStylesheet(boolean nativeStyles) {
        if (wizardScene == null)
            return;
        URL cssUrl = StylesLoader.class.getResource(CSS_PATH);
        if (cssUrl == null) {
            log.warn("No se encontró el CSS en {}", CSS_PATH);
            return;
        }
        String cssExtern = cssUrl.toExternalForm();
        if (nativeStyles) {
            wizardScene.getStylesheets().remove(cssExtern);
        } else {
            if (!wizardScene.getStylesheets().contains(cssExtern)) {
                wizardScene.getStylesheets().add(cssExtern);
            }
        }
        log.info("Estilo aplicado inmediatamente: nativeStyles={}", nativeStyles);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // ACTUALIZACIÓN DE IDIOMA (aplicado inmediatamente a todas las etiquetas
    // almacenadas)
    // ─────────────────────────────────────────────────────────────────────────

    private void refreshAllLabels() {
        lm = LanguageManager.getInstance();

        // Botones de navegación
        prevButton.setText(lm.get("wizard.back"));
        boolean isLast = (currentStep == steps.length - 1);
        nextButton.setText(isLast ? lm.get("wizard.finish") : lm.get("wizard.next"));

        // Paso 0
        if (welcomeTitle != null)
            welcomeTitle.setText(lm.get("wizard.welcome.title"));
        if (welcomeBody != null)
            welcomeBody.setText(lm.get("wizard.welcome.body"));
        if (welcomeHint != null)
            welcomeHint.setText(lm.get("wizard.welcome.hint"));

        // Paso 1
        if (langTitle != null)
            langTitle.setText(lm.get("wizard.language.title"));
        if (langBody != null)
            langBody.setText(lm.get("wizard.language.body"));

        // Paso 2
        if (styleTitle != null)
            styleTitle.setText(lm.get("wizard.style.title"));
        if (styleBody != null)
            styleBody.setText(lm.get("wizard.style.body"));
        if (styleWarning != null)
            styleWarning.setText(lm.get("wizard.style.warning"));
        if (styleCustomName != null)
            styleCustomName.setText(lm.get("wizard.style.custom"));
        if (styleNativeName != null)
            styleNativeName.setText(lm.get("wizard.style.native"));

        // Paso 3
        if (javaTitle != null)
            javaTitle.setText(lm.get("wizard.java.title"));
        if (javaBody != null)
            javaBody.setText(lm.get("wizard.java.body"));
        if (detectBtn != null)
            detectBtn.setText(lm.get("wizard.java.detect"));
        if (javaSkipHint != null)
            javaSkipHint.setText(lm.get("wizard.java.skip_hint"));

        // Paso 4
        if (licenseTitle != null)
            licenseTitle.setText(lm.get("wizard.license.title"));
        if (licenseBody != null)
            licenseBody.setText(lm.get("wizard.license.body"));
        if (licenseFinish != null)
            licenseFinish.setText(lm.get("wizard.license.enjoy"));
    }

    // ─────────────────────────────────────────────────────────────────────────
    // PASO 0 – BIENVENIDA
    // ─────────────────────────────────────────────────────────────────────────

    private Node buildWelcomeStep() {
        VBox box = new VBox(20);
        box.setAlignment(Pos.CENTER);
        box.setPadding(new Insets(40, 60, 30, 60));
        box.getStyleClass().add("wizard-step");

        welcomeTitle = new Label(lm.get("wizard.welcome.title"));
        welcomeTitle.getStyleClass().add("wizard-step-title");
        welcomeTitle.setWrapText(true);
        welcomeTitle.setTextAlignment(javafx.scene.text.TextAlignment.CENTER);

        welcomeBody = new Label(lm.get("wizard.welcome.body"));
        welcomeBody.getStyleClass().add("wizard-step-body");
        welcomeBody.setWrapText(true);
        welcomeBody.setTextAlignment(javafx.scene.text.TextAlignment.CENTER);
        welcomeBody.setMaxWidth(460);

        welcomeHint = new Label(lm.get("wizard.welcome.hint"));
        welcomeHint.getStyleClass().add("wizard-step-hint");
        welcomeHint.setWrapText(true);
        welcomeHint.setTextAlignment(javafx.scene.text.TextAlignment.CENTER);

        box.getChildren().addAll(welcomeTitle, welcomeBody, welcomeHint);
        return box;
    }

    // ─────────────────────────────────────────────────────────────────────────
    // PASO 1 – IDIOMA
    // ─────────────────────────────────────────────────────────────────────────

    private Node buildLanguageStep() {
        VBox box = new VBox(24);
        box.setAlignment(Pos.CENTER);
        box.setPadding(new Insets(40, 60, 30, 60));
        box.getStyleClass().add("wizard-step");

        langTitle = new Label(lm.get("wizard.language.title"));
        langTitle.getStyleClass().add("wizard-step-title");

        langBody = new Label(lm.get("wizard.language.body"));
        langBody.getStyleClass().add("wizard-step-body");
        langBody.setWrapText(true);
        langBody.setTextAlignment(javafx.scene.text.TextAlignment.CENTER);
        langBody.setMaxWidth(400);

        HBox langCards = new HBox(16);
        langCards.setAlignment(Pos.CENTER);

        // Se usan botones normales con una referencia compartida para evitar problemas
        // de deselección
        Button[] selected = new Button[1];
        Button esBtn = createLangSelectCard("Español", "es_es", selected);
        Button enBtn = createLangSelectCard("English", "en_us", selected);

        // Inicializar selección
        Button initialLang = "es_es".equals(settings.getLanguage()) ? esBtn : enBtn;
        initialLang.getStyleClass().add("wizard-lang-card-selected");
        selected[0] = initialLang;

        langCards.getChildren().addAll(esBtn, enBtn);
        box.getChildren().addAll(langTitle, langBody, langCards);
        return box;
    }

    private Button createLangSelectCard(String name, String code, Button[] selected) {
        Label nameLbl = new Label(name);
        nameLbl.getStyleClass().add("wizard-lang-name");
        VBox inner = new VBox(6, nameLbl);
        inner.setAlignment(Pos.CENTER);
        inner.setPadding(new Insets(10, 20, 10, 20));

        Button btn = new Button();
        btn.setGraphic(inner);
        btn.getStyleClass().add("wizard-lang-card");

        btn.setOnAction(e -> {
            if (selected[0] != null && selected[0] != btn) {
                selected[0].getStyleClass().remove("wizard-lang-card-selected");
            }
            if (!btn.getStyleClass().contains("wizard-lang-card-selected")) {
                btn.getStyleClass().add("wizard-lang-card-selected");
            }
            selected[0] = btn;

            // Aplicar idioma inmediatamente
            settings.setLanguage(code);
            LanguageManager.getInstance().loadLanguage(code);
            lm = LanguageManager.getInstance();
            refreshAllLabels();
            log.info("Idioma cambiado inmediatamente a: {}", code);
        });

        return btn;
    }

    // ─────────────────────────────────────────────────────────────────────────
    // PASO 2 – ESTILO DE INTERFAZ
    // ─────────────────────────────────────────────────────────────────────────

    private Node buildStyleStep() {
        VBox box = new VBox(24);
        box.setAlignment(Pos.CENTER);
        box.setPadding(new Insets(40, 60, 30, 60));
        box.getStyleClass().add("wizard-step");

        styleTitle = new Label(lm.get("wizard.style.title"));
        styleTitle.getStyleClass().add("wizard-step-title");

        styleBody = new Label(lm.get("wizard.style.body"));
        styleBody.getStyleClass().add("wizard-step-body");
        styleBody.setWrapText(true);
        styleBody.setTextAlignment(javafx.scene.text.TextAlignment.CENTER);
        styleBody.setMaxWidth(400);

        HBox styleCards = new HBox(16);
        styleCards.setAlignment(Pos.CENTER);

        Button[] selected = new Button[1];
        Button customBtn = createStyleSelectCard(false, selected);
        Button nativeBtn = createStyleSelectCard(true, selected);

        Button initiallyActive = settings.isNative_styles() ? nativeBtn : customBtn;
        initiallyActive.getStyleClass().add("wizard-lang-card-selected");
        selected[0] = initiallyActive;

        styleCards.getChildren().addAll(customBtn, nativeBtn);

        styleWarning = new Label(lm.get("wizard.style.warning"));
        styleWarning.getStyleClass().add("wizard-step-hint");
        styleWarning.setWrapText(true);
        styleWarning.setTextAlignment(javafx.scene.text.TextAlignment.CENTER);

        box.getChildren().addAll(styleTitle, styleBody, styleCards, styleWarning);
        return box;
    }

    /**
     * Crea una tarjeta de selección de estilo.
     * Utiliza {@link Button} normales para evitar errores de deselección.
     * Cambia la hoja de estilos del asistente <em>inmediatamente</em> al hacer
     * clic.
     */
    private Button createStyleSelectCard(boolean nativeStyle, Button[] selected) {
        // Etiqueta de nombre — almacenada como campo para que refreshAllLabels() pueda
        // actualizarla
        Label nameLbl = new Label(lm.get(nativeStyle ? "wizard.style.native" : "wizard.style.custom"));
        nameLbl.getStyleClass().add("wizard-lang-name");
        if (nativeStyle)
            styleNativeName = nameLbl;
        else
            styleCustomName = nameLbl;

        VBox inner = new VBox(6, nameLbl);
        inner.setAlignment(Pos.CENTER);
        inner.setPadding(new Insets(10, 20, 10, 20));

        Button btn = new Button();
        btn.setGraphic(inner);
        btn.getStyleClass().add("wizard-lang-card");

        btn.setOnAction(e -> {
            // Deseleccionar anterior
            if (selected[0] != null && selected[0] != btn) {
                selected[0].getStyleClass().remove("wizard-lang-card-selected");
            }
            // Seleccionar este
            if (!btn.getStyleClass().contains("wizard-lang-card-selected")) {
                btn.getStyleClass().add("wizard-lang-card-selected");
            }
            selected[0] = btn;

            // Aplicar ajuste y CSS inmediatamente
            settings.setNativeStyles(nativeStyle);
            applyStylesheet(nativeStyle);
            log.info("Estilo cambiado inmediatamente: nativeStyles={}", nativeStyle);
        });

        return btn;
    }

    // ─────────────────────────────────────────────────────────────────────────
    // PASO 3 – CONFIGURACIÓN DE JAVA
    // ─────────────────────────────────────────────────────────────────────────

    private Node buildJavaStep() {
        VBox box = new VBox(20);
        box.setAlignment(Pos.CENTER);
        box.setPadding(new Insets(40, 60, 30, 60));
        box.getStyleClass().add("wizard-step");

        javaTitle = new Label(lm.get("wizard.java.title"));
        javaTitle.getStyleClass().add("wizard-step-title");

        javaBody = new Label(lm.get("wizard.java.body"));
        javaBody.getStyleClass().add("wizard-step-body");
        javaBody.setWrapText(true);
        javaBody.setTextAlignment(javafx.scene.text.TextAlignment.CENTER);
        javaBody.setMaxWidth(400);

        javaStatusLabel = new Label(lm.get("wizard.java.status.idle"));
        javaStatusLabel.getStyleClass().add("wizard-java-status");
        javaStatusLabel.setWrapText(true);
        javaStatusLabel.setTextAlignment(javafx.scene.text.TextAlignment.CENTER);
        javaStatusLabel.setMaxWidth(440);

        detectBtn = new Button(lm.get("wizard.java.detect"));
        detectBtn.getStyleClass().addAll("wizard-nav-btn", "wizard-nav-btn-primary");
        detectBtn.setOnAction(e -> {
            detectBtn.setDisable(true);
            javaStatusLabel.setText(lm.get("wizard.java.status.detecting"));
            javaStatusLabel.getStyleClass().removeAll("wizard-java-status-ok", "wizard-java-status-warn");

            new Thread(() -> {
                Map<String, String> found = JavaDetector.detectJavaInstallations();
                Platform.runLater(() -> {
                    detectBtn.setDisable(false);
                    if (found.isEmpty()) {
                        javaStatusLabel.getStyleClass().add("wizard-java-status-warn");
                        javaStatusLabel.setText(lm.get("wizard.java.status.not_found"));
                    } else {
                        javaStatusLabel.getStyleClass().add("wizard-java-status-ok");
                        StringBuilder sb = new StringBuilder(lm.get("wizard.java.status.found") + "\n");
                        found.forEach((ver, path) -> {
                            sb.append("Java ").append(ver).append(": ").append(path).append("\n");
                            switch (ver) {
                                case "8" -> settings.setJre8_path(path);
                                case "17" -> settings.setJre17_path(path);
                                case "21" -> settings.setJre21_path(path);
                            }
                        });
                        javaStatusLabel.setText(sb.toString().trim());
                    }
                });
            }, "java-detector").start();
        });

        javaSkipHint = new Label(lm.get("wizard.java.skip_hint"));
        javaSkipHint.getStyleClass().add("wizard-step-hint");

        box.getChildren().addAll(javaTitle, javaBody, detectBtn, javaStatusLabel, javaSkipHint);
        return box;
    }

    // ─────────────────────────────────────────────────────────────────────────
    // PASO 4 – CÓDIGO ABIERTO Y LICENCIA
    // ─────────────────────────────────────────────────────────────────────────

    private Node buildLicenseStep() {
        VBox box = new VBox(18);
        box.setAlignment(Pos.CENTER);
        box.setPadding(new Insets(40, 60, 30, 60));
        box.getStyleClass().add("wizard-step");

        licenseTitle = new Label(lm.get("wizard.license.title"));
        licenseTitle.getStyleClass().add("wizard-step-title");

        licenseBody = new Label(lm.get("wizard.license.body"));
        licenseBody.getStyleClass().add("wizard-step-body");
        licenseBody.setWrapText(true);
        licenseBody.setTextAlignment(javafx.scene.text.TextAlignment.CENTER);
        licenseBody.setMaxWidth(460);

        VBox licenseCard = new VBox(8);
        licenseCard.getStyleClass().add("wizard-license-card");
        licenseCard.setAlignment(Pos.CENTER_LEFT);
        licenseCard.setMaxWidth(460);

        Label licTitle = new Label("GNU Affero General Public License v3.0 (AGPL-3.0)");
        licTitle.getStyleClass().add("wizard-license-title");
        licTitle.setWrapText(true);

        Label licDesc = new Label(lm.get("wizard.license.description"));
        licDesc.getStyleClass().add("wizard-step-hint");
        licDesc.setWrapText(true);

        Hyperlink licLink = new Hyperlink("github.com/CubicLauncher/CubicLauncher");
        licLink.getStyleClass().add("wizard-license-link");
        licLink.setOnAction(e -> {
            try {
                java.awt.Desktop.getDesktop()
                        .browse(new java.net.URI("https://github.com/CubicLauncher/CubicLauncher"));
            } catch (Exception ex) {
                log.warn("No se pudo abrir el navegador: {}", ex.getMessage());
            }
        });

        licenseCard.getChildren().addAll(licTitle, licDesc, licLink);

        licenseFinish = new Label(lm.get("wizard.license.enjoy"));
        licenseFinish.getStyleClass().add("wizard-step-body");
        licenseFinish.setWrapText(true);
        licenseFinish.setTextAlignment(javafx.scene.text.TextAlignment.CENTER);

        box.getChildren().addAll(licenseTitle, licenseBody, licenseCard, licenseFinish);
        return box;
    }
}
