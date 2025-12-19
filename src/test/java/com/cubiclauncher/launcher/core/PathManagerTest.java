package com.cubiclauncher.launcher.core;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.condition.EnabledOnOs;
import org.junit.jupiter.api.condition.OS;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("PathManager Tests")
class PathManagerTest {

    @Test
    @DisplayName("Should return singleton instance")
    void testSingleton() {
        PathManager instance1 = PathManager.getInstance();
        PathManager instance2 = PathManager.getInstance();

        assertSame(instance1, instance2, "Should return the same instance");
    }

    @Test
    @DisplayName("Should create settings directory")
    void testSettingsPathExists() {
        PathManager pathManager = PathManager.getInstance();
        Path settingsPath = pathManager.getSettingsPath();

        assertNotNull(settingsPath, "Settings path should not be null");
        assertTrue(Files.exists(settingsPath), "Settings directory should exist");
        assertTrue(Files.isDirectory(settingsPath), "Settings path should be a directory");
    }

    @Test
    @DisplayName("Should create game directory")
    void testGamePathExists() {
        PathManager pathManager = PathManager.getInstance();
        Path gamePath = pathManager.getGamePath();

        assertNotNull(gamePath, "Game path should not be null");
        assertTrue(Files.exists(gamePath), "Game directory should exist");
        assertTrue(Files.isDirectory(gamePath), "Game path should be a directory");
    }

    @Test
    @DisplayName("Should create instance directory")
    void testInstancePathExists() {
        PathManager pathManager = PathManager.getInstance();
        Path instancePath = pathManager.getInstancePath();

        assertNotNull(instancePath, "Instance path should not be null");
        assertTrue(Files.exists(instancePath), "Instance directory should exist");
        assertTrue(Files.isDirectory(instancePath), "Instance path should be a directory");
    }

    @Test
    @EnabledOnOs(OS.WINDOWS)
    @DisplayName("Should use correct Windows paths")
    void testWindowsPaths() {
        PathManager pathManager = PathManager.getInstance();
        String settingsPath = pathManager.getSettingsPath().toString();

        assertTrue(settingsPath.contains("CubicLauncher"),
                "Windows path should contain CubicLauncher");
        assertTrue(settingsPath.contains("AppData") || settingsPath.contains("APPDATA"),
                "Windows path should use AppData");
    }

    @Test
    @EnabledOnOs(OS.LINUX)
    @DisplayName("Should use correct Linux paths")
    void testLinuxPaths() {
        PathManager pathManager = PathManager.getInstance();
        String settingsPath = pathManager.getSettingsPath().toString();

        assertTrue(settingsPath.contains(".cubic"),
                "Linux path should contain .cubic");
        assertTrue(settingsPath.contains(System.getProperty("user.home")),
                "Linux path should be in user home");
    }

    @Test
    @EnabledOnOs(OS.MAC)
    @DisplayName("Should use correct macOS paths")
    void testMacPaths() {
        PathManager pathManager = PathManager.getInstance();
        String settingsPath = pathManager.getSettingsPath().toString();

        assertTrue(settingsPath.contains("Library"),
                "macOS path should contain Library");
        assertTrue(settingsPath.contains("Application Support"),
                "macOS path should contain Application Support");
        assertTrue(settingsPath.contains("CubicLauncher"),
                "macOS path should contain CubicLauncher");
    }

    @Test
    @DisplayName("Instance path should be subdirectory of settings")
    void testInstancePathStructure() {
        PathManager pathManager = PathManager.getInstance();
        Path instancePath = pathManager.getInstancePath();

        assertTrue(instancePath.toString().contains("instances"),
                "Instance path should contain 'instances'");
    }

    @Test
    @DisplayName("Should create directories on first access")
    void testDirectoryCreation() {
        PathManager pathManager = PathManager.getInstance();

        // All directories should exist after getInstance
        assertTrue(Files.exists(pathManager.getSettingsPath()));
        assertTrue(Files.exists(pathManager.getGamePath()));
        assertTrue(Files.exists(pathManager.getInstancePath()));
    }

    @Test
    @DisplayName("Paths should be writable")
    void testPathsAreWritable() {
        PathManager pathManager = PathManager.getInstance();

        assertTrue(Files.isWritable(pathManager.getSettingsPath()),
                "Settings path should be writable");
        assertTrue(Files.isWritable(pathManager.getGamePath()),
                "Game path should be writable");
        assertTrue(Files.isWritable(pathManager.getInstancePath()),
                "Instance path should be writable");
    }
}