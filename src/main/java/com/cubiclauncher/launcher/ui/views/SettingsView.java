package com.cubiclauncher.launcher.ui.views;

import com.cubiclauncher.launcher.util.loadStyles;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

public class SettingsView {

    public static VBox create() {
        VBox settingsBox = new VBox(20);
        settingsBox.setAlignment(Pos.CENTER);
        settingsBox.setPadding(new Insets(20));

        Label settingsTitle = new Label("Ajustes");
        settingsTitle.getStyleClass().add("welcome-title");

        TabPane tabPane = new TabPane();
        tabPane.getStyleClass().add("settings-tab-pane"); // Mantener la clase de estilo
        VBox.setVgrow(tabPane, Priority.ALWAYS);
        tabPane.setMaxWidth(Double.MAX_VALUE);
        tabPane.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);

        // Pestaña General
        Tab generalTab = new Tab("General");
        VBox generalTabContent = new VBox(10);
        generalTabContent.setAlignment(Pos.CENTER_LEFT);
        generalTabContent.setPadding(new Insets(10));
        CheckBox autoUpdateCheckbox = new CheckBox("Habilitar actualizaciones automáticas");
        generalTabContent.getChildren().add(autoUpdateCheckbox);
        generalTab.setContent(generalTabContent);

        // Pestaña Apariencia
        Tab appearanceTab = new Tab("Apariencia");
        VBox appearanceTabContent = new VBox(10);
        appearanceTabContent.setPadding(new Insets(10));
        appearanceTabContent.getChildren().add(new Label("Aquí van los ajustes de apariencia."));
        appearanceTab.setContent(appearanceTabContent);

        // Pestaña Acerca de
        Tab aboutTab = new Tab("Acerca de");
        VBox aboutTabContent = new VBox(10);
        aboutTabContent.setPadding(new Insets(10));
        aboutTabContent.getChildren().add(new Label("Aquí va la información acerca del launcher."));
        aboutTab.setContent(aboutTabContent);

        tabPane.getTabs().addAll(generalTab, appearanceTab, aboutTab);

        settingsBox.getChildren().addAll(settingsTitle, tabPane);

        loadStyles.load(settingsBox, "/com.cubiclauncher.launcher/styles/ui.settings.css");
        
        return settingsBox;
    }
}