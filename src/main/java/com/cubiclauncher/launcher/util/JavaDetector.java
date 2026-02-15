package com.cubiclauncher.launcher.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

public class JavaDetector {
    private static final Logger log = LoggerFactory.getLogger(JavaDetector.class);

    public static Map<String, String> detectJavaInstallations() {
        Map<String, String> detectedPaths = new HashMap<>();
        String os = System.getProperty("os.name").toLowerCase();

        if (os.contains("win")) {
            searchWindows(detectedPaths);
        } else if (os.contains("mac")) {
            searchMacOS(detectedPaths);
        } else {
            searchLinux(detectedPaths);
        }

        return detectedPaths;
    }

    private static void searchLinux(Map<String, String> detectedPaths) {
        String[] commonPaths = {
                "/usr/lib/jvm",
                "/usr/java",
                "/opt"
        };

        for (String path : commonPaths) {
            scanDirectory(Paths.get(path), detectedPaths);
        }
    }

    private static void searchWindows(Map<String, String> detectedPaths) {
        String[] commonPaths = {
                "C:\\Program Files\\Java",
                "C:\\Program Files\\Eclipse Foundation",
                "C:\\Program Files\\AdoptOpenJDK",
                "C:\\Program Files\\BellSoft"
        };

        for (String path : commonPaths) {
            scanDirectory(Paths.get(path), detectedPaths);
        }
    }

    private static void searchMacOS(Map<String, String> detectedPaths) {
        scanDirectory(Paths.get("/Library/Java/JavaVirtualMachines"), detectedPaths);
    }

    private static void scanDirectory(Path root, Map<String, String> detectedPaths) {
        if (!Files.exists(root) || !Files.isDirectory(root)) {
            return;
        }

        try (Stream<Path> stream = Files.list(root)) {
            stream.filter(Files::isDirectory).forEach(path -> {
                String name = path.getFileName().toString().toLowerCase();
                String version = null;

                if (name.contains("8") || name.contains("1.8")) {
                    version = "8";
                } else if (name.contains("17")) {
                    version = "17";
                } else if (name.contains("21")) {
                    version = "21";
                }

                if (version != null) {
                    Path executable = findJavaExecutable(path);
                    if (executable != null) {
                        // Solo guardar si no tenemos ya uno para esa versión (o si es "mejor"?)
                        if (!detectedPaths.containsKey(version)) {
                            detectedPaths.put(version, executable.toString());
                            log.info("Detectado Java {}: {}", version, executable);
                        }
                    }
                }
            });
        } catch (IOException e) {
            log.warn("Error escaneando directorio de Java {}: {}", root, e.getMessage());
        }
    }

    private static Path findJavaExecutable(Path javaHome) {
        String os = System.getProperty("os.name").toLowerCase();
        String exeName = os.contains("win") ? "java.exe" : "java";

        // Intentar en bin/
        Path binJava = javaHome.resolve("bin").resolve(exeName);
        if (Files.exists(binJava) && Files.isExecutable(binJava)) {
            return binJava;
        }

        // Algunos bundles en macOS tienen una estructura diferente
        if (os.contains("mac")) {
            Path macHome = javaHome.resolve("Contents/Home/bin").resolve(exeName);
            if (Files.exists(macHome) && Files.isExecutable(macHome)) {
                return macHome;
            }
        }

        return null;
    }
}
