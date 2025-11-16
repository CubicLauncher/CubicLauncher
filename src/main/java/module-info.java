module cubic.launcher.com.cubiclauncher {
    requires javafx.controls;
    requires javafx.fxml;
    requires org.kordamp.bootstrapfx.core;

    // Permite que JavaFX acceda a tu paquete 'ui' para lanzar la aplicación
    opens cubic.launcher.com.cubiclauncher.ui to javafx.graphics, javafx.fxml;
    opens cubic.launcher.com.cubiclauncher.ui.components to javafx.fxml, javafx.graphics;
}