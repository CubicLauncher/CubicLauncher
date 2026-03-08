package com.cubiclauncher.launcher.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

public class JavaDetector {
    private static final Logger log = LoggerFactory.getLogger(JavaDetector.class);

    public static Map<String, String> detectJavaInstallations() {
        Map<String, String> detectedPaths = new HashMap<>();
        Set<Path> candidates = new HashSet<>();

        // 1. Current Java
        String currentJava = System.getProperty("java.home");
        if (currentJava != null) {
            candidates.add(Paths.get(currentJava));
        }

        // 2. PATH
        findInPath(candidates);

        // 3. OS specific locations
        String os = System.getProperty("os.name").toLowerCase();
        if (os.contains("win")) {
            searchWindows(candidates);
            searchRegistry(candidates);
        } else if (os.contains("mac")) {
            searchMacOS(candidates);
            searchMacOSUtility(candidates);
        } else {
            searchLinux(candidates);
        }

        // 4. Verify candidates
        for (Path javaHome : candidates) {
            Path executable = findJavaExecutable(javaHome);
            if (executable != null) {
                String version = getJavaMajorVersion(executable);
                if (version != null && (version.equals("8") || version.equals("17") || version.equals("21"))) {
                    // Favor higher precision/newer if multiple found for same version?
                    // For now, just first one found that is valid.
                    if (!detectedPaths.containsKey(version)) {
                        detectedPaths.put(version, executable.toString());
                        log.info("Verified Java {} at: {}", version, executable);
                    }
                }
            }
        }

        return detectedPaths;
    }

    private static void findInPath(Set<Path> candidates) {
        String pathEnv = System.getenv("PATH");
        if (pathEnv == null)
            return;

        String separator = File.pathSeparator;
        String[] paths = pathEnv.split(separator);

        for (String p : paths) {
            Path binDir = Paths.get(p);
            if (Files.exists(binDir) && Files.isDirectory(binDir)) {
                String os = System.getProperty("os.name").toLowerCase();
                String exeName = os.contains("win") ? "java.exe" : "java";
                Path javaExe = binDir.resolve(exeName);
                if (Files.exists(javaExe) && Files.isExecutable(javaExe)) {
                    // Likely a bin directory, java home is parent
                    candidates.add(binDir.getParent());
                }
            }
        }
    }

    private static void searchLinux(Set<Path> candidates) {
        String[] commonRoots = { "/usr/lib/jvm", "/usr/java", "/opt" };
        for (String root : commonRoots) {
            collectDirectories(Paths.get(root), candidates);
        }
    }

    private static void searchWindows(Set<Path> candidates) {
        String[] commonRoots = {
                "C:\\Program Files\\Java",
                "C:\\Program Files\\Eclipse Foundation",
                "C:\\Program Files\\AdoptOpenJDK",
                "C:\\Program Files\\BellSoft",
                "C:\\Program Files (x86)\\Java"
        };
        for (String root : commonRoots) {
            collectDirectories(Paths.get(root), candidates);
        }
    }

    private static void searchRegistry(Set<Path> candidates) {
        // This is a bit complex to do without JNA, skip for now or use reg query
        try {
            String[] keys = {
                    "HKLM\\SOFTWARE\\JavaSoft\\Java Runtime Environment",
                    "HKLM\\SOFTWARE\\JavaSoft\\JDK",
                    "HKLM\\SOFTWARE\\JavaSoft\\Java Development Kit"
            };
            for (String key : keys) {
                Process process = new ProcessBuilder("reg", "query", key, "/s", "/v", "JavaHome").start();
                try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        if (line.contains("JavaHome") && line.contains("REG_SZ")) {
                            String[] parts = line.split("REG_SZ");
                            if (parts.length > 1) {
                                candidates.add(Paths.get(parts[1].trim()));
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            log.trace("Registry scan failed: {}", e.getMessage());
        }
    }

    private static void searchMacOS(Set<Path> candidates) {
        collectDirectories(Paths.get("/Library/Java/JavaVirtualMachines"), candidates);
        collectDirectories(Paths.get("/System/Library/Java/JavaVirtualMachines"), candidates);
    }

    private static void searchMacOSUtility(Set<Path> candidates) {
        try {
            Process process = new ProcessBuilder("/usr/libexec/java_home", "-V").start();
            // java_home output is usually on stderr for -V
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getErrorStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    if (line.contains("/")) {
                        String path = line.substring(line.indexOf("/")).trim();
                        candidates.add(Paths.get(path));
                    }
                }
            }
        } catch (Exception e) {
            log.trace("macOS java_home utility failed: {}", e.getMessage());
        }
    }

    private static void collectDirectories(Path root, Set<Path> candidates) {
        if (!Files.exists(root) || !Files.isDirectory(root))
            return;
        try (Stream<Path> stream = Files.list(root)) {
            stream.filter(Files::isDirectory).forEach(candidates::add);
        } catch (IOException e) {
            log.warn("Error listing {}: {}", root, e.getMessage());
        }
    }

    private static Path findJavaExecutable(Path javaHome) {
        if (javaHome == null)
            return null;
        String os = System.getProperty("os.name").toLowerCase();
        String exeName = os.contains("win") ? "java.exe" : "java";

        Path[] subPaths = {
                Paths.get("bin", exeName),
                Paths.get("Contents", "Home", "bin", exeName), // macOS bundle
                Paths.get("jre", "bin", exeName)
        };

        for (Path sub : subPaths) {
            Path full = javaHome.resolve(sub);
            if (Files.exists(full) && Files.isExecutable(full)) {
                return full;
            }
        }
        return null;
    }

    private static String getJavaMajorVersion(Path javaExe) {
        try {
            ProcessBuilder pb = new ProcessBuilder(javaExe.toString(), "-version");
            pb.redirectErrorStream(true);
            Process process = pb.start();
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                String versionLine = reader.readLine();
                if (versionLine != null) {
                    // version format usually: java version "1.8.0_291" or openjdk version "17.0.1"
                    if (versionLine.contains("\"")) {
                        String versionStr = versionLine.substring(versionLine.indexOf("\"") + 1,
                                versionLine.lastIndexOf("\""));
                        if (versionStr.startsWith("1.8"))
                            return "8";
                        if (versionStr.contains(".")) {
                            return versionStr.split("\\.")[0];
                        } else {
                            return versionStr; // Likely just "17" or similar
                        }
                    }
                }
            }
            process.waitFor();
        } catch (Exception e) {
            log.debug("Failed to get version for {}: {}", javaExe, e.getMessage());
        }
        return null;
    }
}
