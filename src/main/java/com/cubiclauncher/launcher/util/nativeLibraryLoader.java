package com.cubiclauncher.launcher.util;

import java.io.*;
import java.nio.file.Files;

public class nativeLibraryLoader {

    /**
     * Carga una librería nativa desde resources y la copia a un archivo temporal para cargarla con System.load.
     * @param resourcePath Ruta completa de la librería dentro del classpath (sin "/" inicial).
     * @throws IOException Si no se encuentra el recurso o falla la copia.
     */
    public static void loadLibraryFromResources(String resourcePath) throws IOException {
        String osName = System.getProperty("os.name").toLowerCase();
        String extension = osName.contains("win") ? ".dll" : ".so";

        try (InputStream in = nativeLibraryLoader.class.getResourceAsStream(resourcePath)) {
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
