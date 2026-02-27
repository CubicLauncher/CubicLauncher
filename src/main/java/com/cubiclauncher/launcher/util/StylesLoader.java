package com.cubiclauncher.launcher.util;

import javafx.scene.Scene;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;

public final class StylesLoader {

    private StylesLoader() {}

    private static String resolve(String path) {
        URL url;

        // 1. ¿Existe como archivo del sistema?
        File file = new File(path);
        if (file.exists() && file.isFile()) {
            try {
                url = file.toURI().toURL();
            } catch (MalformedURLException e) {
                throw new IllegalArgumentException(
                        "Ruta de archivo CSS inválida: " + path, e
                );
            }
        } else {
            // 2. Intentar como resource del classpath
            url = StylesLoader.class.getResource(path);
            if (url == null) {
                throw new IllegalArgumentException(
                        "No se encontró el CSS ni como archivo ni como resource: " + path
                );
            }
        }

        return url.toExternalForm();
    }

    public static void load(Scene scene, String path) {
        if (scene == null) return;

        String css = resolve(path);
        if (!scene.getStylesheets().contains(css)) {
            scene.getStylesheets().add(css);
        }
    }

    public static void unload(Scene scene, String path) {
        if (scene == null) return;

        String css = resolve(path);
        scene.getStylesheets().remove(css);
    }

    public static boolean isLoaded(Scene scene, String path) {
        if (scene == null) return false;

        String css = resolve(path);
        return scene.getStylesheets().contains(css);
    }
}