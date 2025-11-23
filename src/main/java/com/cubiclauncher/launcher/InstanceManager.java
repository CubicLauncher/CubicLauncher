package com.cubiclauncher.launcher;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

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

    public InstanceManager(Path launcherDir) {
        this.instancesDir = launcherDir.resolve("instances");
        this.instances = new ArrayList<>();
        loadInstances();
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

        public Path getInstanceJsonPath(Path instancesDir) {
            return instancesDir.resolve(this.name + ".json");
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
                    .filter(path -> path.toString().endsWith(".json"))
                    .forEach(this::loadInstanceFromFile);
        } catch (IOException e) {
            System.err.println("Error leyendo directorio de instancias: " + e.getMessage());
        }
    }

    private void loadInstanceFromFile(Path jsonFile) {
        try (Reader reader = new FileReader(jsonFile.toFile())) {
            Instance instance = GSON.fromJson(reader, Instance.class);
            instances.add(instance);
        } catch (IOException e) {
            System.err.println("Error cargando instancia desde " + jsonFile + ": " + e.getMessage());
        }
    }

    /**
     * Guarda una instancia en un archivo JSON
     */
    public boolean saveInstance(Instance instance) {
        try {
            Path jsonFile = instance.getInstanceJsonPath(instancesDir);
            try (Writer writer = new FileWriter(jsonFile.toFile())) {
                GSON.toJson(instance, writer);
            }

            // Si es una nueva instancia, agregarla a la lista
            if (!instances.contains(instance)) {
                instances.add(instance);
            }

            return true;
        } catch (IOException e) {
            System.err.println("Error guardando instancia: " + e.getMessage());
            return false;
        }
    }

    /**
     * Crea una nueva instancia
     */
    public Instance createInstance(String name, String version) {
        // Validar nombre único
        if (getInstance(name).isPresent()) {
            throw new IllegalArgumentException("Ya existe una instancia con el nombre: " + name);
        }

        Instance instance = new Instance(name, version);

        if (saveInstance(instance)) {
            return instance;
        } else {
            throw new RuntimeException("No se pudo guardar la instancia: " + name);
        }
    }

    /**
     * Elimina una instancia
     */
    public boolean deleteInstance(String name) {
        Optional<Instance> instanceOpt = getInstance(name);
        if (instanceOpt.isPresent()) {
            Instance instance = instanceOpt.get();

            // Eliminar archivo JSON
            Path jsonFile = instance.getInstanceJsonPath(instancesDir);
            try {
                Files.deleteIfExists(jsonFile);
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