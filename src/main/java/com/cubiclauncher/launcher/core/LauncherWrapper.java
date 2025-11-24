package com.cubiclauncher.launcher.core;

import com.cubiclauncher.claunch.Launcher;
import com.cubiclauncher.claunch.models.VersionInfo;
import com.cubiclauncher.launcher.core.events.EventBus;
import com.cubiclauncher.launcher.core.events.EventData;
import com.cubiclauncher.launcher.core.events.EventType;
import com.cubiclauncher.launcher.core.InstanceManager;
import com.cubiclauncher.launcher.util.NativeLibraryLoader;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import org.slf4j.LoggerFactory;
import org.slf4j.Logger;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class LauncherWrapper {
    static final SettingsManager sm = SettingsManager.getInstance();
    static final PathManager pm = PathManager.getInstance();
    private static final EventBus EVENT_BUS = EventBus.get();
    private static final Logger log = LoggerFactory.getLogger(LauncherWrapper.class);
    private static LauncherWrapper instance;

    static {
        try {
            log.info("🔧 Intentando cargar librería nativa...");
            NativeLibraryLoader.loadLibraryFromResources(
                    "/com.cubiclauncher.launcher/nativeLibraries/proton/libproton"
            );
            log.info("✅ Librería nativa cargada exitosamente");
        } catch (IOException e) {
            log.error("❌ Error cargando la librería nativa", e);
            throw new RuntimeException("Error cargando la librería nativa", e);
        }
    }

    // Constructor privado para Singleton
    private LauncherWrapper() {
        log.debug("LauncherWrapper inicializado");
    }

    // Método para obtener la instancia única
    public static LauncherWrapper getInstance() {
        if (instance == null) {
            instance = new LauncherWrapper();
        }
        return instance;
    }

    /**
     * funcion nativa con callback.
     */
    private native void startMinecraftDownload(String targetPath, String version, DownloadCallback callback);

    public void downloadMinecraftVersion(String versionId) {
        log.info("Iniciando descarga de versión: {}", versionId);

        startMinecraftDownload(pm.getGamePath().resolve("shared").toString(),
                versionId,
                new DownloadCallback() {
                    @Override
                    public void onProgress(int type, int current, int total, String fileName) {
                        EVENT_BUS.emit(EventType.DOWNLOAD_PROGRESS,
                                EventData.builder()
                                        .put("type", type)
                                        .put("current", current)
                                        .put("total", total)
                                        .put("fileName", fileName)
                                        .build());
                    }

                    @Override
                    public void onComplete() {
                        EVENT_BUS.emit(EventType.DOWNLOAD_COMPLETED, EventData.builder().put("version", versionId).build());
                    }

                    @Override
                    public void onError(String error) {
                        EVENT_BUS.emit(EventType.DOWNLOAD_COMPLETED,
                                EventData.error("Error en descarga: " + error, null));
                    }
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
                    .uri(URI.create("""
                            https://piston-meta.mojang.com/mc/game/version_manifest_v2.json"""))
                    .build();
            var response = client.send(request, HttpResponse.BodyHandlers.ofString());
            var json = new Gson().fromJson(response.body(), JsonObject.class);
            json.getAsJsonArray("versions").forEach(el -> {
                var obj = el.getAsJsonObject();
                if ("old_alpha".equals(obj.get("type").getAsString()) && sm.isShowAlphaVersions()) {
                    versions.add(obj.get("id").getAsString());
                }
                if ("snapshot".equals(obj.get("type").getAsString()) && sm.isShowBetaVersions()) {
                    versions.add(obj.get("id").getAsString());
                }
                if ("release".equals(obj.get("type").getAsString())) {
                    versions.add(obj.get("id").getAsString());
                }
            });
        } catch (IOException | InterruptedException e) {
            versions.add("Error al cargar versiones");
        }
        return versions;
    }

    public String getJavaPath(String jreVersion) {
        // Si jreVersion es null, usaremos el default
        if (jreVersion == null) {
            log.warn("La versión de JRE es nula. Se usará el default Java 17.");
            return sm.getJava17Path();
        }

        // Usamos switch para mayor claridad
        switch (jreVersion) {
            case "8":
                return sm.getJava8Path();
            case "17":
                return sm.getJava17Path();
            case "21":
                return sm.getJava21path();
            default:
                log.warn("No se pudo obtener la versión mínima del JRE. Se obtuvo '{}' pero esta no concuerda con ninguna descargada.", jreVersion);
                log.warn("Se usará el default Java 17.");
                return sm.getJava17Path();
        }
    }

    public void startVersion(String versionId, Path instanceDir) throws IOException, InterruptedException {
        String versionManifestPath = pm.getGamePath()
                .resolve("shared")
                .resolve("versions")
                .resolve(versionId)
                .resolve(versionId + ".json")
                .toString();
        String minimumJREVersion = new VersionInfo(versionManifestPath, pm.getGamePath().toString()).getMinimumJREVersion();

        Launcher.launch(
                versionManifestPath,
                pm.getGamePath().toString(),
                instanceDir,
                sm.getUsername(),
                getJavaPath(minimumJREVersion),
                sm.getMinMemoryInMB() + "M",
                sm.getMaxMemoryInMB() + "M",
                900, 600, false);
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

        /**
         * Llamado cuando hay progreso en la descarga
         * @param type Tipo de descarga (CLIENT, LIBRARY, ASSET, NATIVE)
         * @param current Número actual de elementos descargados
         * @param total Total de elementos a descargar
         * @param fileName Nombre del archivo siendo descargado
         */
        void onProgress(int type, int current, int total, String fileName);

        /**
         * Llamado cuando la descarga se completa exitosamente
         */
        void onComplete();

        /**
         * Llamado cuando ocurre un error
         * @param error Mensaje de error
         */
        void onError(String error);
    }
}