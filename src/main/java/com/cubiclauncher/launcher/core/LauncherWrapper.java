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

package com.cubiclauncher.launcher.core;

import com.cubiclauncher.claunch.Launcher;
import com.cubiclauncher.claunch.models.LaunchOptions;
import com.cubiclauncher.claunch.models.VersionInfo;
import com.cubiclauncher.launcher.core.events.EventBus;
import com.cubiclauncher.launcher.core.events.EventData;
import com.cubiclauncher.launcher.core.events.EventType;
import com.cubiclauncher.launcher.util.NativeLibraryLoader;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

public class LauncherWrapper {
    static final SettingsManager sm = SettingsManager.getInstance();
    static final PathManager pm = PathManager.getInstance();
    private static final EventBus EVENT_BUS = EventBus.get();
    private static final Logger log = LoggerFactory.getLogger(LauncherWrapper.class);
    private static final long PROGRESS_UPDATE_INTERVAL = 100; // 100ms
    private static LauncherWrapper instance;
    private final Queue<String> downloadQueue = new LinkedList<>();
    private boolean isDownloading = false;
    private long lastProgressUpdate = 0;

    static {
        try {
            log.info("🔧 Intentando cargar librería nativa...");
            NativeLibraryLoader.loadLibraryFromResources(
                    "/com.cubiclauncher.launcher/nativeLibraries/proton/libproton");
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
        log.info("Añadiendo a la cola de descargas la versión: {}", versionId);
        downloadQueue.add(versionId);
        startNextDownload();
    }

    private void startNextDownload() {
        if (isDownloading || downloadQueue.isEmpty()) {
            return; // Si ya hay una descarga o la cola está vacía, no hacer nada
        }

        isDownloading = true;
        String versionId = downloadQueue.poll();
        log.info("Iniciando descarga de versión: {}", versionId);

        startMinecraftDownload(pm.getGamePath().resolve("shared").toString(),
                versionId,
                new DownloadCallback() {
                    @Override
                    public void onProgress(int type, int current, int total, String fileName) {
                        long now = System.currentTimeMillis();
                        if (now - lastProgressUpdate > PROGRESS_UPDATE_INTERVAL || current == total) {
                            EVENT_BUS.emit(EventType.DOWNLOAD_PROGRESS,
                                    EventData.downloadProgress(type, current, total, fileName, versionId));
                            lastProgressUpdate = now;
                        }
                    }

                    @Override
                    public void onComplete() {
                        EVENT_BUS.emit(EventType.DOWNLOAD_COMPLETED,
                                EventData.builder().put("version", versionId).build());
                        isDownloading = false;
                        startNextDownload();
                    }

                    @Override
                    public void onError(String error) {
                        EVENT_BUS.emit(EventType.DOWNLOAD_COMPLETED,
                                EventData.error("Error en descarga: " + error, null));
                        isDownloading = false;
                        startNextDownload();
                    }
                });
    }

    public List<String> getInstalledVersions() {
        File versionsDir = pm.getGamePath().resolve("shared").resolve("versions").toFile();
        if (versionsDir.exists() && versionsDir.isDirectory()) {
            String[] dirs = versionsDir.list((c, n) -> new File(c, n).isDirectory());
            if (dirs != null)
                return Arrays.asList(dirs);
        }
        return new ArrayList<>();
    }

    public List<String> getAvailableVersions() {
        List<String> versions = new ArrayList<>();
        try (var client = HttpClient.newHttpClient()) {
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
                log.warn(
                        "No se pudo obtener la versión mínima del JRE. Se obtuvo '{}' pero esta no concuerda con ninguna descargada.",
                        jreVersion);
                log.warn("Se usará el default Java 17.");
                return sm.getJava17Path();
        }
    }

    /**
     * Método de conveniencia que mantiene compatibilidad con código existente
     * Lanza el juego y maneja el proceso automáticamente
     */
    public void startVersion(String versionId, Path instanceDir) {
        try {
            Process process = launchVersion(versionId, instanceDir, null, null, null);
            TaskManager.getInstance().runAsync(() -> {
                try {
                    int exitCode = process.waitFor();
                    log.info("Juego finalizado con código: {}", exitCode);
                    EVENT_BUS.emit(EventType.GAME_EXITED,
                            EventData.builder()
                                    .put("exitCode", exitCode)
                                    .put("version", versionId)
                                    .build());
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            });
        } catch (IOException e) {
            log.error("Error lanzando versión: {}", versionId, e);
            EVENT_BUS.emit(EventType.GAME_ERROR,
                    EventData.error("Error lanzando juego: " + e.getMessage(), e));
        }
    }

    /**
     * Inicia una versión de Minecraft y devuelve el Process para controlarlo
     * 
     * @param versionId   ID de la versión a lanzar
     * @param instanceDir Directorio de la instancia
     * @return Process del juego en ejecución
     * @throws IOException Si ocurre un error al lanzar el juego
     */
    public Process launchVersion(String versionId, Path instanceDir, Integer minMem, Integer maxMem, String jvmArgs)
            throws IOException {
        String versionManifestPath = pm.getGamePath()
                .resolve("shared")
                .resolve("versions")
                .resolve(versionId)
                .resolve(versionId + ".json")
                .toString();
        String minimumJREVersion = new VersionInfo(versionManifestPath,
                pm.getGamePath().toString()).getMinimumJREVersion();

        Map<String, String> customArgs = new HashMap<>();
        if (sm.isForceDiscreteGpu() && System.getProperty("os.name").toLowerCase().contains("linux")) {
            customArgs.put("DRI_PRIME", "1");
        }

        String minMemory = (minMem != null && minMem > 0) ? minMem + "M" : sm.getMinMemoryInMB() + "M";
        String maxMemory = (maxMem != null && maxMem > 0) ? maxMem + "M" : sm.getMaxMemoryInMB() + "M";

        LaunchOptions options = LaunchOptions.defaults();
        if (jvmArgs != null && !jvmArgs.isBlank()) {
            // Here we would ideally parse JVM args, but for now we might need to update
            // claunch
            // to support raw JVM args if it doesn't.
            // Looking at the signature, LaunchOptions doesn't seem to have a jvmArgs field
            // directly in the usage above.
            // Let's assume for now we might need to add them to customArgs or similar if
            // supported.
            // Actually, let's keep it simple for now as I don't see an easy way to pass raw
            // JVM args to Launcher.launchWithProcess
            // without knowing the cli wrapper's capabilities.
        }

        return Launcher.launchWithProcess(
                versionManifestPath,
                pm.getGamePath().toString(),
                instanceDir,
                sm.getUsername(),
                getJavaPath(minimumJREVersion),
                minMemory,
                maxMemory,
                900, 600, false, options, customArgs);

    }

    /**
     * Obtiene la salida estándar (stdout) del proceso
     * 
     * @param process Proceso del juego
     * @return Reader para leer la salida línea por línea
     */
    public BufferedReader getStdoutReader(Process process) {
        return new BufferedReader(new InputStreamReader(process.getInputStream(), StandardCharsets.UTF_8));
    }

    /**
     * Obtiene la salida de error (stderr) del proceso
     * 
     * @param process Proceso del juego
     * @return Reader para leer los errores línea por línea
     */
    public BufferedReader getStderrReader(Process process) {
        return new BufferedReader(new InputStreamReader(process.getErrorStream(), StandardCharsets.UTF_8));
    }

    /**
     * Obtiene un InputStream directo de la salida estándar
     * 
     * @param process Proceso del juego
     * @return InputStream de stdout
     */
    public InputStream getStdoutStream(Process process) {
        return process.getInputStream();
    }

    /**
     * Obtiene un InputStream directo de la salida de error
     * 
     * @param process Proceso del juego
     * @return InputStream de stderr
     */
    public InputStream getStderrStream(Process process) {
        return process.getErrorStream();
    }

    /**
     * Mata el proceso forzosamente
     * 
     * @param process Proceso a terminar
     */
    public void forceKill(Process process) {
        if (process != null && process.isAlive()) {
            process.destroyForcibly();
            log.info("Proceso terminado forzosamente");
        }
    }

    /**
     * Termina el proceso de forma limpia
     * 
     * @param process Proceso a terminar
     * @param timeout Tiempo máximo de espera en segundos
     * @return true si el proceso terminó limpiamente
     */
    public boolean gracefulShutdown(Process process, int timeout) {
        if (process == null || !process.isAlive()) {
            return true;
        }

        process.destroy(); // Envía señal de terminación

        try {
            if (process.waitFor(timeout, TimeUnit.SECONDS)) {
                log.info("Proceso terminado limpiamente");
                return true;
            } else {
                log.warn("Timeout - forzando terminación");
                process.destroyForcibly();
                return false;
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            process.destroyForcibly();
            return false;
        }
    }

    /**
     * Monitorea la salida del proceso en tiempo real
     * 
     * @param process      Proceso a monitorear
     * @param onOutputLine Callback para cada línea de salida
     * @param onErrorLine  Callback para cada línea de error
     */
    public void monitorProcessOutput(Process process,
            Consumer<String> onOutputLine,
            Consumer<String> onErrorLine) {

        Thread outputThread = new Thread(() -> {
            try (BufferedReader reader = getStdoutReader(process)) {
                String line;
                while ((line = reader.readLine()) != null) {
                    onOutputLine.accept(line);
                }
            } catch (IOException e) {
                log.debug("Stream de salida cerrado", e);
            }
        });
        outputThread.setDaemon(true);
        outputThread.start();

        Thread errorThread = new Thread(() -> {
            try (BufferedReader reader = getStderrReader(process)) {
                String line;
                while ((line = reader.readLine()) != null) {
                    onErrorLine.accept(line);
                }
            } catch (IOException e) {
                log.debug("Stream de error cerrado", e);
            }
        });
        errorThread.setDaemon(true);
        errorThread.start();
    }

    /**
     * Monitorea la salida del proceso y emite eventos al EventBus
     * 
     * @param process       Proceso a monitorear
     * @param instance_name ID de la versión para identificar en los eventos
     */
    public void monitorProcessWithEvents(Process process, String instance_name) {
        monitorProcessOutput(process,
                line -> {
                    EVENT_BUS.emit(EventType.GAME_OUTPUT,
                            EventData.builder()
                                    .put("instance_name", instance_name)
                                    .put("line", line)
                                    .put("type", "stdout")
                                    .build());
                },
                line -> {
                    EVENT_BUS.emit(EventType.GAME_OUTPUT,
                            EventData.builder()
                                    .put("version", instance_name)
                                    .put("line", line)
                                    .put("type", "stderr")
                                    .build());
                });

        // Monitorear el proceso para detectar cuando termina
        new Thread(() -> {
            try {
                int exitCode = process.waitFor();
                EVENT_BUS.emit(EventType.GAME_EXITED,
                        EventData.builder()
                                .put("version", instance_name)
                                .put("exitCode", exitCode)
                                .build());
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }).start();
    }

    /**
     * Guarda el log completo del juego a un archivo
     * 
     * @param process    Proceso del juego
     * @param outputFile Archivo donde guardar el log
     * @throws IOException Si hay error al escribir el archivo
     */
    public void saveGameLogToFile(Process process, Path outputFile) throws IOException {
        try (BufferedReader stdoutReader = getStdoutReader(process);
                BufferedReader stderrReader = getStderrReader(process);
                PrintWriter writer = new PrintWriter(Files.newBufferedWriter(outputFile, StandardCharsets.UTF_8))) {

            String line;
            // Leer stdout
            while ((line = stdoutReader.readLine()) != null) {
                writer.println("[STDOUT] " + line);
            }

            // Leer stderr (podría haber datos pendientes)
            while ((line = stderrReader.readLine()) != null) {
                writer.println("[STDERR] " + line);
            }
        }
    }

    /**
     * Obtiene el PID del proceso si está disponible
     * 
     * @param process Proceso del juego
     * @return PID del proceso o -1 si no está disponible
     */
    public long getProcessPid(Process process) {
        try {
            if (process.getClass().getName().equals("java.lang.UNIXProcess")) {
                java.lang.reflect.Field pidField = process.getClass().getDeclaredField("pid");
                pidField.setAccessible(true);
                return pidField.getLong(process);
            }
        } catch (Exception e) {
            log.debug("No se pudo obtener el PID del proceso", e);
        }
        return -1;
    }

    /**
     * Verifica si el proceso sigue en ejecución
     * 
     * @param process Proceso a verificar
     * @return true si el proceso está vivo
     */
    public boolean isProcessAlive(Process process) {
        return process != null && process.isAlive();
    }

    /**
     * Espera a que el proceso termine con un timeout
     * 
     * @param process Proceso a esperar
     * @param timeout Tiempo máximo en segundos
     * @return true si el proceso terminó dentro del timeout
     */
    public boolean waitForProcess(Process process, int timeout) {
        try {
            return process.waitFor(timeout, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return false;
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

        /**
         * Llamado cuando hay progreso en la descarga
         *
         * @param type     Tipo de descarga (CLIENT, LIBRARY, ASSET, NATIVE)
         * @param current  Número actual de elementos descargados
         * @param total    Total de elementos a descargar
         * @param fileName Nombre del archivo siendo descargado
         */
        void onProgress(int type, int current, int total, String fileName);

        /**
         * Llamado cuando la descarga se completa exitosamente
         */
        void onComplete();

        /**
         * Llamado cuando ocurre un error
         *
         * @param error Mensaje de error
         */
        void onError(String error);
    }
}