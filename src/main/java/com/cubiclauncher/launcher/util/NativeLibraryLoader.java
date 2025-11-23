/*
 * Copyright (C) 2025 Santiagolxx, Notstaff and CubicLauncher contributors
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see https://www.gnu.org/licenses/.
 */
package com.cubiclauncher.launcher.util;

import java.io.*;
import java.nio.file.Files;

public class NativeLibraryLoader {
    /**
     * Carga una librería nativa desde resources y la copia a un archivo temporal para cargarla con System.load
     *
     * @param resourcePath Ruta completa de la librería dentro del classpath (sin "/" inicial).
     * @throws IOException Si no se encuentra el recurso o falla la copia.
     */
    @SuppressWarnings("GrazieInspection")
    public static void loadLibraryFromResources(String resourcePath) throws IOException {
        String osName = System.getProperty("os.name").toLowerCase();
        String extension = osName.contains("win") ? ".dll" : ".so";

        try (InputStream in = NativeLibraryLoader.class.getResourceAsStream(resourcePath + extension)) {
            if (in == null) throw new FileNotFoundException("Recurso no encontrado: " + resourcePath);

            File tempFile = Files.createTempFile("libtemp", extension).toFile();
            tempFile.deleteOnExit();

            try (OutputStream out = new FileOutputStream(tempFile)) {
                byte[] buffer = new byte[4096];
                int bytesRead;
                while ((bytesRead = in.read(buffer)) != -1) out.write(buffer, 0, bytesRead);
            }

            System.load(tempFile.getAbsolutePath());
        }
    }
}
