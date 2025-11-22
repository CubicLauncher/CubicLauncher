/*
 *
 *  * Copyright (C) 2025 Santiagolxx, Notstaff and CubicLauncher contributors
 *  *
 *  * This program is free software: you can redistribute it and/or modify
 *  * it under the terms of the GNU Affero General Public License as published by
 *  * the Free Software Foundation, either version 3 of the License, or
 *  * (at your option) any later version.
 *  *
 *  * This program is distributed in the hope that it will be useful,
 *  * but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  * GNU Affero General Public License for more details.
 *  *
 *  * You should have received a copy of the GNU Affero General Public License
 *  * along with this program.  If not, see https://www.gnu.org/licenses/.
 *
 *
 */
package com.cubiclauncher.launcher.util;

import javafx.scene.Parent;
import javafx.scene.Scene;

import java.net.URL;

public class StylesLoader {
    /**
     * Carga una hoja de estilos CSS desde la ruta de recursos especificada y la aplica a la escena.
     *
     * @param scene        La escena a la que se aplicará la hoja de estilos.
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
     *
     * @param parent       El nodo Parent (p. ej., VBox, BorderPane) al que se aplicará la hoja de estilos.
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
