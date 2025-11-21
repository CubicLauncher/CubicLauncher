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

import com.cubiclauncher.claunch.Launcher;
import com.cubiclauncher.launcher.util.PathManager;
import com.cubiclauncher.launcher.util.SettingsManager;
import com.cubiclauncher.launcher.util.nativeLibraryLoader;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class LauncherWrapper {
    static SettingsManager sm = SettingsManager.getInstance();
    static PathManager pm = PathManager.getInstance();

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
    private native void startMinecraftDownload(String targetPath, String version);

    public void downloadMinecraftVersion(String vanillaVersionId) {
        startMinecraftDownload(pm.getGamePath().resolve("shared").toString(), vanillaVersionId);
    }

    public List<String> getInstalledVersions() {
        File versionsDir = pm.getGamePath().resolve("shared").resolve("versions").toFile();
        if (versionsDir.exists() && versionsDir.isDirectory()) {
            String[] directories = versionsDir.list((current, name) -> new File(current, name).isDirectory());
            if (directories != null) {
                return Arrays.asList(directories);
            }
        }
        return new ArrayList<>();
    }

    public List<String> getAvailableVersions() {
        List<String> versions = new ArrayList<>();
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("https://piston-meta.mojang.com/mc/game/version_manifest_v2.json"))
                .build();

        try {
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            Gson gson = new Gson();
            JsonObject json = gson.fromJson(response.body(), JsonObject.class);
            JsonArray versionsArray = json.getAsJsonArray("versions");

            versionsArray.forEach(jsonElement -> {
                JsonObject versionObj = jsonElement.getAsJsonObject();
                // Solo añadir versiones de tipo "release" (vanilla)
                if (versionObj.get("type").getAsString().equals("release")) {
                    versions.add(versionObj.get("id").getAsString());
                }
            });

        } catch (IOException | InterruptedException e) {
            versions.add("Error al cargar versiones");
        }
        return versions;
    }

    public void startVersion(String versionId) throws IOException, InterruptedException {
        Launcher.launch(
                pm.getGamePath().resolve("shared", "versions", versionId, versionId + ".json").toString(),
                pm.getGamePath().toString(),
                pm.getInstancePath().resolve("xd"),
                sm.getUsername(),
                // TODO: Agregar selectores de paths de java.
                "/usr/lib/jvm/java-21-openjdk/bin/java",
                sm.getMinMemoryInMB() + "M",
                sm.getMaxMemoryInMB() + "M",
                900,
                600,
                false);
    }
}