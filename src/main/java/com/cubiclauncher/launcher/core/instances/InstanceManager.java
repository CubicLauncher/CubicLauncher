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

package com.cubiclauncher.launcher.core.instances;

import com.cubiclauncher.launcher.core.LauncherWrapper;
import com.cubiclauncher.launcher.core.PathManager;
import com.cubiclauncher.launcher.core.TaskManager;
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
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

public class InstanceManager {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Logger log = LoggerFactory.getLogger(InstanceManager.class);
    private static final EventBus eventBus = EventBus.get();
    private final Path instancesDir;
    private final PathManager pathManager;
    private final LauncherWrapper launcherWrapper;
    private final TaskManager taskManager;
    private final List<Instance> instances;

    private static InstanceManager instance;

    private InstanceManager() {
        this.pathManager = PathManager.getInstance();
        this.instancesDir = pathManager.getInstancePath();
        this.launcherWrapper = new LauncherWrapper();
        this.taskManager = TaskManager.getInstance();
        this.instances = new ArrayList<>();
        loadInstances();
        eventBus.subscribe(EventType.INSTANCE_VERSION_NOT_INSTALLED, (eventData -> {
            taskManager.runAsync(() -> {
                launcherWrapper.downloadMinecraftVersion(eventData.getString("version"));
            });
        }));
    }

    // Singleton getter
    public static InstanceManager getInstance() {
        if (instance == null) {
            instance = new InstanceManager();
        }
        return instance;
    }

    public static class Instance {
        private final String name;
        private final String version;
        private long lastPlayed;

        // Constructor para nueva instancia
        public Instance(String name, String version) {
            this.name = name;
            this.version = version;
            this.lastPlayed = System.currentTimeMillis();
        }

        // Getters
        public String getName() {
            return name;
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
                log.info("Nueva instancia agregada: {}", instance.getName());
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
            log.info("Instancia '{}' creada con versión {}", name, version);
            eventBus.emit(EventType.INSTANCE_CREATED, EventData.empty());
        } else {
            throw new RuntimeException("No se pudo guardar la instancia: " + name);
        }
    }
    /**
     * Inicia una instancia por nombre
     */
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
                        launcherWrapper.downloadMinecraftVersion(instanceToStart.getVersion());
                    }

                    log.info("Iniciando instancia '{}' con versión {}", instanceName, instanceToStart.getVersion());

                    launcherWrapper.startVersion(
                            instanceToStart.getVersion(),
                            instanceToStart.getInstanceDir(instancesDir)
                    );

                    // Actualizar última vez jugada
                    instanceToStart.updateLastPlayed();
                    saveInstance(instanceToStart);

                    log.info("Instancia '{}' iniciada exitosamente", instanceName);
                },
                () -> {
                    log.info("Callback de éxito: Instancia '{}' procesada", instanceName);
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
            log.debug("Actualizada última vez jugada para instancia: {}", name);
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
}