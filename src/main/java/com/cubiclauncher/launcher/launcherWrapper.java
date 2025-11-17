package com.cubiclauncher.launcher;

import com.cubiclauncher.launcher.util.nativeLibraryLoader;
import java.io.IOException;

public class launcherWrapper {

    static {
        try {
            nativeLibraryLoader.loadLibraryFromResources(
                    "com/cubiclauncher/launcher/nativeLibraries/proton/libcproton.so"
            );
        } catch (IOException e) {
            throw new RuntimeException("Error cargando la librería nativa", e);
        }
    }

    /**
     * Método nativo expuesto por la librería Rust.
     */
    public native void startMinecraftDownload(String targetPath, String version);
}
