module cubic.launcher.com.cubiclauncher {
    requires javafx.controls;
    requires javafx.fxml;
    requires org.kordamp.bootstrapfx.core;
    requires com.google.gson;
    requires java.desktop;

    opens com.cubiclauncher.launcher.ui to javafx.graphics, javafx.fxml;
    opens com.cubiclauncher.launcher.ui.components to javafx.fxml, javafx.graphics;
    opens com.cubiclauncher.launcher.util to com.google.gson;

    exports com.cubiclauncher.launcher;
    exports com.cubiclauncher.launcher.ui;
    exports com.cubiclauncher.launcher.ui.views;
    exports com.cubiclauncher.launcher.ui.components;
    exports com.cubiclauncher.launcher.ui.controllers;
}