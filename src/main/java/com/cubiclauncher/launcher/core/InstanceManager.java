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

import com.cubiclauncher.launcher.core.events.EventBus;
import com.cubiclauncher.launcher.core.events.EventData;
import com.cubiclauncher.launcher.core.events.EventType;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

public class InstanceManager {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Logger log = LoggerFactory.getLogger(InstanceManager.class);
    private static final EventBus eventBus = EventBus.get();
    private static InstanceManager instance;
    private final PathManager pathManager = PathManager.getInstance();
    private final LauncherWrapper launcherWrapper = LauncherWrapper.getInstance();
    private final TaskManager taskManager;
    private final List<Instance> instances;
    private final Path instancesDir = pathManager.getInstancePath();

    private InstanceManager() {
        this.taskManager = TaskManager.getInstance();
        this.instances = new ArrayList<>();

        loadInstances();
        eventBus.
                subscribe(EventType.INSTANCE_VERSION_NOT_INSTALLED, (eventData -> taskManager.runAsync(() -> launcherWrapper.downloadMinecraftVersion(eventData.getString("version")))));
        eventBus.
                subscribe(EventType.REQUEST_LAUNCH_INSTANCE, (eventData -> taskManager.runAsync(() -> startInstance(eventData.getString("instance_name")))));
        eventBus.
                subscribe(EventType.REQUEST_INSTANCE_CREATION, (eventData -> taskManager.runAsync(() -> createInstance(eventData.getString("instance_name"), eventData.getString("instance_version")))));
    }

    // Singleton getter
    public static InstanceManager getInstance() {
        if (instance == null) {
            instance = new InstanceManager();
        }
        return instance;
    }

    /**
     * Carga todas las instancias desde el directorio de instancias
     */
    private void loadInstances() {
        instances.clear();

        if (!Files.exists(instancesDir)) {
            try {
                Files.createDirectories(instancesDir);
                log.info("Directorio de instancias creado: {}", instancesDir);
                return;
            } catch (IOException e) {
                log.error("Error creando directorio de instancias: {}", e.getMessage(), e);
                return;
            }
        }

        try (Stream<Path> paths = Files.list(instancesDir)) {
            paths.filter(Files::isDirectory)
                    .forEach(this::loadInstanceFromDir);

            log.info("Instancias cargadas: <{}>", getInstanceCount());
        } catch (IOException e) {
            log.error("Error leyendo directorio de instancias: {}", e.getMessage(), e);
        }
    }

    /**
     * Carga una instancia desde un directorio
     */
    private void loadInstanceFromDir(Path instanceDir) {
        Path configFile = instanceDir.resolve("instance.cub");
        if (Files.exists(configFile)) {
            try (Reader reader = new FileReader(configFile.toFile())) {
                Instance instance = GSON.fromJson(reader, Instance.class);
                if (instance != null) {
                    instances.add(instance);
                }
            } catch (IOException e) {
                log.error("Error cargando instancia desde {}: {}", configFile, e.getMessage(), e);
            }
        }
    }

    /**
     * Guarda una instancia en su directorio
     */
    public boolean saveInstance(Instance instance) {
        try {
            // Crear directorio de la instancia si no existe
            Path instanceDir = instance.getInstanceDir(instancesDir);
            Files.createDirectories(instanceDir);

            // Guardar archivo de configuración
            Path configFile = instance.getInstanceConfigPath(instancesDir);
            try (Writer writer = new FileWriter(configFile.toFile())) {
                GSON.toJson(instance, writer);
            }

            // Si es una nueva instancia, agregarla a la lista
            if (!instances.contains(instance)) {
                instances.add(instance);
                log.info("New instance: {} ({})", instance.getName(), instance.getVersion());
            } else {
                log.debug("Instancia actualizada: {}", instance.getName());
            }

            return true;
        } catch (IOException e) {
            log.error("Error guardando instancia '{}': {}", instance.getName(), e.getMessage(), e);
            return false;
        }
    }

    /**
     * Crea una nueva instancia
     */
    public void createInstance(String name, String version) {
        if (getInstance(name).isPresent()) {
            throw new IllegalArgumentException("Ya existe una instancia con el nombre: " + name);
        }

        Instance newInstance = new Instance(name, version);

        if (saveInstance(newInstance)) {
            eventBus.emit(EventType.INSTANCE_CREATED, EventData.empty());
        } else {
            throw new RuntimeException("No se pudo guardar la instancia: " + name);
        }
    }

    public void startInstance(String instanceName) {
        Optional<Instance> optionalInstance = getInstance(instanceName);

        if (optionalInstance.isEmpty()) {
            log.warn("No se encontró la instancia: {}", instanceName);
            eventBus.emit(EventType.GAME_CRASHED,
                    EventData.error("Instancia no encontrada: " + instanceName, null));
            return;
        }

        Instance instanceToStart = optionalInstance.get();

        taskManager.runAsync(
                () -> {
                    if (!launcherWrapper.getInstalledVersions().contains(instanceToStart.getVersion())) {
                        log.info("Versión {} no instalada, iniciando descarga", instanceToStart.getVersion());
                        eventBus.emit(EventType.INSTANCE_VERSION_NOT_INSTALLED, EventData.builder().put("version", instanceToStart.version).build());
                        return;
                    }

                    log.info("Iniciando instancia '{}' con versión {}", instanceName, instanceToStart.getVersion());
                    EventBus.get().emit(EventType.GAME_STARTED, EventData.builder().put("version", instanceName).build());
                    try {
                        Process process = launcherWrapper.launchVersion(
                                instanceToStart.getVersion(),
                                instanceToStart.getInstanceDir(instancesDir)
                        );
                        // Guardar el proceso en la instancia
                        instanceToStart.attachProcess(process);

                        // Actualizar última vez jugada
                        instanceToStart.updateLastPlayed();
                        saveInstance(instanceToStart);

                        // Monitorear el proceso para detectar cuando termina
                        new Thread(() -> {
                            try {
                                int exitCode = process.waitFor();
                                instanceToStart.detachProcess();
                                eventBus.emit(EventType.GAME_EXITED,
                                        EventData.builder()
                                                .put("instance", instanceName)
                                                .put("exitCode", exitCode)
                                                .build());
                            } catch (InterruptedException e) {
                                Thread.currentThread().interrupt();
                            }
                        }).start();

                        // También podemos monitorear la salida del proceso si queremos
                        launcherWrapper.monitorProcessWithEvents(process, instanceName);

                        log.info("Instancia '{}' iniciada exitosamente", instanceName);
                    } catch (IOException e) {
                        log.error("Error al lanzar la instancia '{}': {}", instanceName, e.getMessage(), e);
                        eventBus.emit(EventType.GAME_CRASHED,
                                EventData.error("Error al lanzar la instancia: " + instanceName, e));
                    }
                },
                () -> {
                    // Este es el onComplete del taskManager, no necesario en este caso
                },
                error -> {
                    log.error("Error iniciando instancia '{}': {}", instanceName, error.getMessage(), error);
                    eventBus.emit(EventType.GAME_CRASHED,
                            EventData.error("Error iniciando instancia: " + instanceName, error));
                }
        );
    }

    /**
     * Elimina una instancia (incluyendo su directorio)
     */
    public boolean deleteInstance(String name) {
        Optional<Instance> instanceOpt = getInstance(name);
        if (instanceOpt.isPresent()) {
            Instance instance = instanceOpt.get();

            // Eliminar directorio completo de la instancia
            Path instanceDir = instance.getInstanceDir(instancesDir);
            try {
                deleteDirectory(instanceDir);
                instances.remove(instance);
                log.info("Instancia '{}' eliminada", name);
                eventBus.emit(EventType.INSTANCE_DELETED, EventData.empty());
                return true;
            } catch (IOException e) {
                log.error("Error eliminando instancia '{}': {}", name, e.getMessage(), e);
                return false;
            }
        }
        log.warn("No se pudo eliminar la instancia '{}': no existe", name);
        return false;
    }

    /**
     * Renombra una instancia
     */
    public boolean renameInstance(String oldName, String newName) {
        if (instanceExists(newName)) {
            log.warn("No se puede renombrar la instancia a '{}': ya existe", newName);
            return false;
        }

        Optional<Instance> instanceOpt = getInstance(oldName);
        if (instanceOpt.isPresent()) {
            Instance instance = instanceOpt.get();
            String originalName = instance.getName();
            Path oldDir = instance.getInstanceDir(instancesDir);

            instance.setName(newName);
            Path newDir = instance.getInstanceDir(instancesDir);

            try {
                // 1. Rename directory
                Files.move(oldDir, newDir, StandardCopyOption.REPLACE_EXISTING);

                // 2. Save updated instance file
                if (!saveInstance(instance)) {
                    // saveInstance failed and logged the error. Revert the directory move.
                    log.warn("Falló el guardado de la configuración, revirtiendo el renombrado del directorio de la instancia.");
                    try {
                        Files.move(newDir, oldDir, StandardCopyOption.REPLACE_EXISTING);
                    } catch (IOException revertEx) {
                        log.error("¡FALLO CRÍTICO! No se pudo revertir el renombrado del directorio. El directorio '{}' debe ser renombrado a '{}' manualmente.", newDir, oldDir, revertEx);
                    }
                    instance.setName(originalName); // Revert name on object
                    return false;
                }

                // 3. Success
                log.info("Instancia '{}' renombrada a '{}'", oldName, newName);
                eventBus.emit(EventType.INSTANCE_RENAME, EventData.empty());
                return true;

            } catch (IOException e) {
                // This catches failure on the first Files.move
                log.error("Error renombrando el directorio de la instancia '{}': {}", oldName, e.getMessage(), e);
                instance.setName(originalName); // Revert name on object
                return false;
            }
        }
        log.warn("No se pudo renombrar la instancia '{}': no existe", oldName);
        return false;
    }

    /**
     * Método auxiliar para eliminar directorios recursivamente
     */
    private void deleteDirectory(Path path) throws IOException {
        if (Files.exists(path)) {
            try (Stream<Path> walk = Files.walk(path)) {
                walk.sorted((a, b) -> -a.compareTo(b)) // reverse para eliminar archivos primero
                        .forEach(p -> {
                            try {
                                Files.delete(p);
                            } catch (IOException e) {
                                throw new UncheckedIOException(e);
                            }
                        });
            }
        }
    }

    /**
     * Obtiene una instancia por nombre
     */
    public Optional<Instance> getInstance(String name) {
        return instances.stream()
                .filter(inst -> inst.getName().equals(name))
                .findFirst();
    }

    /**
     * Obtiene todas las instancias
     */
    public List<Instance> getAllInstances() {
        return new ArrayList<>(instances);
    }

    /**
     * Obtiene instancias ordenadas por última vez jugadas (más recientes primero)
     */
    public List<Instance> getInstancesByLastPlayed() {
        List<Instance> sorted = new ArrayList<>(instances);
        sorted.sort((a, b) -> Long.compare(b.getLastPlayed(), a.getLastPlayed()));
        return sorted;
    }

    /**
     * Actualiza la última vez jugada de una instancia
     */
    public void updateLastPlayed(String name) {
        getInstance(name).ifPresent(inst -> {
            inst.updateLastPlayed();
            saveInstance(inst);
        });
    }

    /**
     * Verifica si existe una instancia con el nombre dado
     */
    public boolean instanceExists(String name) {
        return getInstance(name).isPresent();
    }

    /**
     * Obtiene el número total de instancias
     */
    public int getInstanceCount() {
        return instances.size();
    }

    public static class Instance {
        private String name;
        private final String version;
        private long lastPlayed;
        private transient Process process;

        // Constructor para nueva instancia
        public Instance(String name, String version) {
            this.name = name;
            this.version = version;
            this.lastPlayed = System.currentTimeMillis();
        }

        public void attachProcess(Process process) {
            this.process = process;
        }

        public void detachProcess() {
            this.process = null;
        }

        public Process getProcess() {
            return process;
        }

        // Getters
        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getVersion() {
            return version;
        }

        public long getLastPlayed() {
            return lastPlayed;
        }

        public void setLastPlayed(long lastPlayed) {
            this.lastPlayed = lastPlayed;
        }

        public void updateLastPlayed() {
            this.lastPlayed = System.currentTimeMillis();
        }
        public String getLastPlayedFormatted() {
            Instant instant = Instant.ofEpochMilli(this.lastPlayed);
            LocalDateTime dateTime = LocalDateTime.ofInstant(instant, ZoneId.systemDefault());
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
            return dateTime.format(formatter);
        }

        // Cambio: Ahora crea un directorio con el nombre y usa instance.cub
        public Path getInstanceDir(Path instancesDir) {
            return instancesDir.resolve(this.name);
        }

        public Path getInstanceConfigPath(Path instancesDir) {
            return getInstanceDir(instancesDir).resolve("instance.cub");
        }

        @Override
        public String toString() {
            return String.format("Instance{name='%s', version='%s', lastPlayed=%d}",
                    name, version, lastPlayed);
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Instance instance = (Instance) o;
            return name.equals(instance.name);
        }

        @Override
        public int hashCode() {
            return name.hashCode();
        }
    }
}