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
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class InstanceManager {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private final Path instancesDir;
    private final List<Instance> instances;
    private static final Logger log = LoggerFactory.getLogger(InstanceManager.class);
    private static InstanceManager instance;

    // Constructor privado
    private InstanceManager() {
        this.instancesDir = PathManager.getInstance().getInstancePath();
        this.instances = new ArrayList<>();
        loadInstances();
    }

    // Singleton getter
    public static InstanceManager getInstance() {
        if (instance == null) {
            instance = new InstanceManager();
        }
        return instance;
    }

    public static class Instance {
        private String name;
        private String version;
        private String javaArgs;
        private long lastPlayed;

        // Constructor para nueva instancia
        public Instance(String name, String version) {
            this.name = name;
            this.version = version;
            this.javaArgs = "-Xmx2G -XX:+UnlockExperimentalVMOptions -XX:+UseG1GC -XX:G1NewSizePercent=20 -XX:G1ReservePercent=20 -XX:MaxGCPauseMillis=50 -XX:G1HeapRegionSize=32M";
            this.lastPlayed = System.currentTimeMillis();
        }

        // Getters y Setters
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }

        public String getVersion() { return version; }
        public void setVersion(String version) { this.version = version; }

        public String getJavaArgs() { return javaArgs; }
        public void setJavaArgs(String javaArgs) { this.javaArgs = javaArgs; }

        public long getLastPlayed() { return lastPlayed; }
        public void setLastPlayed(long lastPlayed) { this.lastPlayed = lastPlayed; }

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
    }

    // Métodos del InstanceManager

    /**
     * Carga todas las instancias desde el directorio de instancias
     */
    private void loadInstances() {
        instances.clear();

        if (!Files.exists(instancesDir)) {
            try {
                Files.createDirectories(instancesDir);
                return; // No hay instancias que cargar
            } catch (IOException e) {
                System.err.println("Error creando directorio de instancias: " + e.getMessage());
                return;
            }
        }

        try {
            Files.list(instancesDir)
                    .filter(Files::isDirectory) // Cambio: Solo directorios
                    .forEach(this::loadInstanceFromDir);
        } catch (IOException e) {
            System.err.println("Error leyendo directorio de instancias: " + e.getMessage());
        }
    }

    private void loadInstanceFromDir(Path instanceDir) {
        Path configFile = instanceDir.resolve("instance.cub");
        if (Files.exists(configFile)) {
            try (Reader reader = new FileReader(configFile.toFile())) {
                Instance instance = GSON.fromJson(reader, Instance.class);
                instances.add(instance);
            } catch (IOException e) {
                System.err.println("Error cargando instancia desde " + configFile + ": " + e.getMessage());
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
            }

            return true;
        } catch (IOException e) {
            log.error("Error guardando instancia: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Crea una nueva instancia
     */
    public Instance createInstance(String name, String version) {
        if (getInstance(name).isPresent()) {
            throw new IllegalArgumentException("Ya existe una instancia con el nombre: " + name);
        }

        Instance instance = new Instance(name, version);

        if (saveInstance(instance)) {
            EventBus.get().emit(EventType.INSTANCE_CREATED, EventData.empty());
            return instance;
        } else {
            throw new RuntimeException("No se pudo guardar la instancia: " + name);
        }
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
                return true;
            } catch (IOException e) {
                System.err.println("Error eliminando instancia: " + e.getMessage());
                return false;
            }
        }
        return false;
    }

    /**
     * Método auxiliar para eliminar directorios recursivamente
     */
    private void deleteDirectory(Path path) throws IOException {
        if (Files.exists(path)) {
            Files.walk(path)
                    .sorted((a, b) -> -a.compareTo(b)) // reverse para eliminar archivos primero
                    .forEach(p -> {
                        try {
                            Files.delete(p);
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                    });
        }
    }

    /**
     * Obtiene una instancia por nombre
     */
    public Optional<Instance> getInstance(String name) {
        return instances.stream()
                .filter(instance -> instance.getName().equals(name))
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
        getInstance(name).ifPresent(instance -> {
            instance.updateLastPlayed();
            saveInstance(instance);
        });
    }

    /**
     * Verifica si existe una instancia con el nombre dado
     */
    public boolean instanceExists(String name) {
        return getInstance(name).isPresent();
    }

    /**
     * Obtiene el directorio de instancias
     */
    public Path getInstancesDir() {
        return instancesDir;
    }

    /**
     * Obtiene el número total de instancias
     */
    public int getInstanceCount() {
        return instances.size();
    }
}