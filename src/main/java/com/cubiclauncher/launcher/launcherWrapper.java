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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package com.cubiclauncher.launcher;

import com.cubiclauncher.launcher.util.nativeLibraryLoader;
import java.io.IOException;

public class launcherWrapper {

    static {
        try {
            nativeLibraryLoader.loadLibraryFromResources(
                    "/com.cubiclauncher.launcher/nativeLibraries/proton/libproton.so"
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