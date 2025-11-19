package com.cubiclauncher.launcher.util;

import javafx.scene.Parent;
import javafx.scene.Scene;

import java.net.URL;

public class StylesLoader {
    /**
     * Carga una hoja de estilos CSS desde la ruta de recursos especificada y la aplica a la escena.
     * @param scene La escena a la que se aplicará la hoja de estilos.
     * @param resourcePath La ruta al archivo CSS dentro de los recursos (p. ej., "/path/to/style.css").
     */
    public static void load(Scene scene, String resourcePath) {
        URL cssUrl = StylesLoader.class.getResource(resourcePath);
        if (cssUrl != null) {
            scene.getStylesheets().add(cssUrl.toExternalForm());
        } else {
            System.err.println("Error: No se pudo encontrar el archivo CSS en la ruta especificada: " + resourcePath);
        }
    }

    /**
     * Carga una hoja de estilos CSS desde la ruta de recursos especificada y la aplica a un nodo Parent.
     * @param parent El nodo Parent (p. ej., VBox, BorderPane) al que se aplicará la hoja de estilos.
     * @param resourcePath La ruta al archivo CSS dentro de los recursos (p. ej., "/path/to/style.css").
     */
    public static void load(Parent parent, String resourcePath) {
        URL cssUrl = StylesLoader.class.getResource(resourcePath);
        if (cssUrl != null) {
            parent.getStylesheets().add(cssUrl.toExternalForm());
        } else {
            System.err.println("Error: No se pudo encontrar el archivo CSS en la ruta especificada: " + resourcePath);
        }
    }
}
