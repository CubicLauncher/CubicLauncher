package com.cubiclauncher.launcher.ui.views;

import com.cubiclauncher.launcher.ui.components.VersionCell;
import com.cubiclauncher.launcher.util.StylesLoader;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.layout.VBox;
import javafx.util.Callback;

public class VersionsView {

    public static VBox create() {
        VBox instancesBox = new VBox(10);
        instancesBox.setAlignment(Pos.CENTER);

        Label title = new Label("Versiones");
        title.getStyleClass().add("welcome-title");

        Label subtitle = new Label("Aquí se mostrará la lista de Versiones que puedes descargar del juego.");
        subtitle.getStyleClass().add("welcome-subtitle");

        ListView<String> versionsList = new ListView<>();
        ObservableList<String> versions = FXCollections.observableArrayList(
                "1.21", "1.20.4", "1.20.1", "1.19.4", "1.19.2", "1.18.2", "1.17.1", "1.16.5", "1.12.2", "1.8.9", "1.7.10"
        );
        versionsList.setItems(versions);

        versionsList.setCellFactory(new Callback<ListView<String>, ListCell<String>>() {
            @Override
            public ListCell<String> call(ListView<String> listView) {
                return new VersionCell();
            }
        });

        StylesLoader.load(instancesBox, "/com.cubiclauncher.launcher/styles/ui.main.css");

        instancesBox.getChildren().addAll(title, subtitle, versionsList);
        return instancesBox;
    }
}