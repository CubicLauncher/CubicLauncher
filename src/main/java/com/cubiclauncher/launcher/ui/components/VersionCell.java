package com.cubiclauncher.launcher.ui.components;

import javafx.scene.control.ListCell;

public class VersionCell extends ListCell<String> {

    @Override
    protected void updateItem(String item, boolean empty) {
        super.updateItem(item, empty);
        if (empty || item == null) {
            setText(null);
            setGraphic(null);
        } else {
            setText(item);
        }
    }
}