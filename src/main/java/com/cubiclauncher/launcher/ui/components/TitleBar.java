package com.cubiclauncher.launcher.ui.components;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.stage.Stage;

public class TitleBar extends HBox {

    private double xOffset = 0;
    private double yOffset = 0;

    public TitleBar(Stage primaryStage) {
        setAlignment(Pos.CENTER_RIGHT);
        setPadding(new Insets(8, 8, 8, 15));
        setSpacing(10);
        getStyleClass().add("title-bar");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button minimizeButton = new Button("—");
        minimizeButton.getStyleClass().add("title-bar-button");
        minimizeButton.setOnAction(e -> primaryStage.setIconified(true));

        Button closeButton = new Button("✕");
        closeButton.getStyleClass().addAll("title-bar-button", "close-button");
        closeButton.setOnAction(e -> primaryStage.close());

        getChildren().addAll(spacer, minimizeButton, closeButton);

        setOnMousePressed(event -> {
            xOffset = event.getSceneX();
            yOffset = event.getSceneY();
        });
        
        setOnMouseDragged(event -> {
            primaryStage.setX(event.getScreenX() - xOffset);
            primaryStage.setY(event.getScreenY() - yOffset);
        });
    }
}