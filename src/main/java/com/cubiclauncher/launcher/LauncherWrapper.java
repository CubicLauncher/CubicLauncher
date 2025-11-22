/*
 * Copyright (C) 2025 Santiagolxx, Notstaff and CubicLauncher contributors
 * AGPL-3.0 License
 */
package com.cubiclauncher.launcher;

import com.cubiclauncher.claunch.Launcher;
import com.cubiclauncher.launcher.core.EventBus;
import com.cubiclauncher.launcher.core.PathManager;
import com.cubiclauncher.launcher.core.SettingsManager;
import com.cubiclauncher.launcher.util.nativeLibraryLoader;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import jdk.jfr.Event;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
    static final SettingsManager sm = SettingsManager.getInstance();
    static final PathManager pm = PathManager.getInstance();
    private static final EventBus EVENT_BUS = EventBus.get();
    private static final Logger log = LoggerFactory.getLogger(LauncherWrapper.class);
    static {
        try {
            nativeLibraryLoader.loadLibraryFromResources(
                    "/com.cubiclauncher.launcher/nativeLibraries/proton/libproton"
            );
        } catch (IOException e) {
            throw new RuntimeException("Error cargando la librería nativa", e);
        }
    }

    /**
     * Callback para progreso de descargas.
     * Se define acá mismo, sin clase extra.
     */
    public interface DownloadCallback {
        int TYPE_CLIENT = 0;
        int TYPE_LIBRARY = 1;
        int TYPE_ASSET = 2;
        int TYPE_NATIVE = 3;

        void onProgress(int type, int current, int total, String fileName);
        void onComplete();
        void onError(String error);

        static String getTypeName(int type) {
            return switch (type) {
                case TYPE_CLIENT -> "Cliente";
                case TYPE_LIBRARY -> "Librería";
                case TYPE_ASSET -> "Asset";
                case TYPE_NATIVE -> "Nativo";
                default -> "Desconocido";
            };
        }
    }

    /**
     * Método nativo con callback.
     */
    private native void startMinecraftDownload(String targetPath, String version, DownloadCallback callback);

    /**
     * Descarga con callback.
     */
    public void downloadMinecraftVersion(String versionId, DownloadCallback callback) {
        startMinecraftDownload(pm.getGamePath().resolve("shared").toString(), versionId, callback);
    }

    /**
     * Descarga sin callback (log a consola).
     */
    public void downloadMinecraftVersion(String versionId) {
        downloadMinecraftVersion(versionId, new DownloadCallback() {
            @Override
            public void onProgress(int type, int current, int total, String fileName) {
                log.info("{}: {}/{}", DownloadCallback.getTypeName(type), current, total);
            }
            @Override
            public void onComplete() { System.out.println("Descarga completada"); EVENT_BUS.emitVersionDownloaded(versionId); }
            @Override
            public void onError(String error) { System.err.println("Error: " + error); }
        });
    }

    public List<String> getInstalledVersions() {
        File versionsDir = pm.getGamePath().resolve("shared").resolve("versions").toFile();
        if (versionsDir.exists() && versionsDir.isDirectory()) {
            String[] dirs = versionsDir.list((c, n) -> new File(c, n).isDirectory());
            if (dirs != null) return Arrays.asList(dirs);
        }
        return new ArrayList<>();
    }

    public List<String> getAvailableVersions() {
        List<String> versions = new ArrayList<>();
        try {
            var client = HttpClient.newHttpClient();
            var request = HttpRequest.newBuilder()
                    .uri(URI.create("https://piston-meta.mojang.com/mc/game/version_manifest_v2.json"))
                    .build();
            var response = client.send(request, HttpResponse.BodyHandlers.ofString());
            var json = new Gson().fromJson(response.body(), JsonObject.class);
            json.getAsJsonArray("versions").forEach(el -> {
                var obj = el.getAsJsonObject();
                if ("release".equals(obj.get("type").getAsString())) {
                    versions.add(obj.get("id").getAsString());
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
                "/usr/lib/jvm/java-21-openjdk/bin/java",
                sm.getMinMemoryInMB() + "M",
                sm.getMaxMemoryInMB() + "M",
                900, 600, false);
    }
}