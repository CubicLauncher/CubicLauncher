module cubic.launcher.com.cubiclauncher {
    requires javafx.controls;
    requires javafx.fxml;
    requires org.kordamp.bootstrapfx.core;
    requires CLaunch;

    // Permite que JavaFX acceda a tu paquete 'ui' para lanzar la aplicación
    opens com.cubiclauncher.launcher.ui to javafx.graphics, javafx.fxml;
    opens com.cubiclauncher.launcher.ui.components to javafx.fxml, javafx.graphics;
}