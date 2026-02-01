package com.cubiclauncher.launcher.core;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("SettingsManager Tests")
class SettingsManagerTest {

    private SettingsManager settings;

    @BeforeEach
    void setUp() {
        settings = SettingsManager.getInstance();
    }

    @Test
    @DisplayName("Should return singleton instance")
    void testSingleton() {
        SettingsManager instance1 = SettingsManager.getInstance();
        SettingsManager instance2 = SettingsManager.getInstance();

        assertSame(instance1, instance2, "Should return the same instance");
    }

    // ==================== Launcher Settings ====================

    @Test
    @DisplayName("Should set and get language")
    void testLanguage() {
        settings.setLanguage("English");
        assertEquals("English", settings.getLanguage());
    }

    @Test
    @DisplayName("Should set and get auto update")
    void testAutoUpdate() {
        settings.setAutoUpdate(false);
        assertFalse(settings.isAutoUpdate());

        settings.setAutoUpdate(true);
        assertTrue(settings.isAutoUpdate());
    }

    @Test
    @DisplayName("Should set and get error console")
    void testErrorConsole() {
        settings.setErrorConsole(true);
        assertTrue(settings.isErrorConsole());

        settings.setErrorConsole(false);
        assertFalse(settings.isErrorConsole());
    }

    @Test
    @DisplayName("Should set and get close launcher on game start")
    void testCloseLauncherOnGameStart() {
        settings.setCloseLauncherOnGameStart(true);
        assertTrue(settings.isCloseLauncherOnGameStart());

        settings.setCloseLauncherOnGameStart(false);
        assertFalse(settings.isCloseLauncherOnGameStart());
    }

    @Test
    @DisplayName("Should set and get native styles")
    void testNativeStyles() {
        settings.setNativeStyles(false);
        assertFalse(settings.isNative_styles());

        settings.setNativeStyles(true);
        assertTrue(settings.isNative_styles());
    }

    // ==================== Minecraft Settings ====================

    @Test
    @DisplayName("Should set and get show alpha versions")
    void testShowAlphaVersions() {
        settings.setShowAlphaVersions(true);
        assertTrue(settings.isShowAlphaVersions());

        settings.setShowAlphaVersions(false);
        assertFalse(settings.isShowAlphaVersions());
    }

    @Test
    @DisplayName("Should set and get show beta versions")
    void testShowBetaVersions() {
        settings.setShowBetaVersions(true);
        assertTrue(settings.isShowBetaVersions());

        settings.setShowBetaVersions(false);
        assertFalse(settings.isShowBetaVersions());
    }

    @Test
    @DisplayName("Should set and get force discrete GPU")
    void testForceDiscreteGpu() {
        settings.setForceDiscreteGpu(true);
        assertTrue(settings.isForceDiscreteGpu());

        settings.setForceDiscreteGpu(false);
        assertFalse(settings.isForceDiscreteGpu());
    }

    // ==================== User Settings ====================

    @Test
    @DisplayName("Should set and get username")
    void testUsername() {
        settings.setUsername("TestPlayer");
        assertEquals("TestPlayer", settings.getUsername());
    }

    // ==================== Java Settings ====================

    @Test
    @DisplayName("Should set and get Java 8 path")
    void testJava8Path() {
        String testPath = "/usr/lib/jvm/java-8";
        settings.setJre8_path(testPath);
        assertEquals(testPath, settings.getJava8Path());
    }

    @Test
    @DisplayName("Should set and get Java 17 path")
    void testJava17Path() {
        String testPath = "/usr/lib/jvm/java-17";
        settings.setJre17_path(testPath);
        assertEquals(testPath, settings.getJava17Path());
    }

    @Test
    @DisplayName("Should set and get Java 21 path")
    void testJava21Path() {
        String testPath = "/usr/lib/jvm/java-21";
        settings.setJre21_path(testPath);
        assertEquals(testPath, settings.getJava21path());
    }

    @Test
    @DisplayName("Should set and get JVM arguments")
    void testJvmArguments() {
        String args = "-XX:+UseG1GC -XX:MaxGCPauseMillis=200";
        settings.setJvmArguments(args);
        assertEquals(args, settings.getJvmArguments());
    }

    // ==================== Memory Settings ====================

    @Test
    @DisplayName("Should convert MB memory correctly")
    void testMemoryInMB() {
        settings.setMinMemory(512);
        settings.setMinMemoryUnit("MB");
        assertEquals(512, settings.getMinMemoryInMB());

        settings.setMaxMemory(4);
        settings.setMaxMemoryUnit("GB");
        assertEquals(4096, settings.getMaxMemoryInMB());
    }

    @Test
    @DisplayName("Should handle GB to MB conversion")
    void testGBToMBConversion() {
        settings.setMaxMemory(2);
        settings.setMaxMemoryUnit("GB");
        assertEquals(2048, settings.getMaxMemoryInMB());

        settings.setMinMemory(1);
        settings.setMinMemoryUnit("GB");
        assertEquals(1024, settings.getMinMemoryInMB());
    }

    @Test
    @DisplayName("Should handle MB unit correctly")
    void testMBUnit() {
        settings.setMinMemory(512);
        settings.setMinMemoryUnit("MB");
        assertEquals(512, settings.getMinMemoryInMB());

        settings.setMaxMemory(2048);
        settings.setMaxMemoryUnit("MB");
        assertEquals(2048, settings.getMaxMemoryInMB());
    }

    @Test
    @DisplayName("Should set memory units")
    void testMemoryUnits() {
        settings.setMinMemoryUnit("GB");
        settings.setMaxMemoryUnit("GB");

        // Units are applied in the getters through conversion
        settings.setMinMemory(1);
        assertEquals(1024, settings.getMinMemoryInMB());
    }

    @Test
    @DisplayName("Default settings should be valid")
    void testDefaultSettings() {
        assertNotNull(settings.getLanguage());
        assertNotNull(settings.getUsername());
        assertTrue(settings.getMinMemoryInMB() > 0);
        assertTrue(settings.getMaxMemoryInMB() > 0);
    }

    @Test
    @DisplayName("Max memory should be greater than min memory")
    void testMemoryLogic() {
        settings.setMinMemory(512);
        settings.setMinMemoryUnit("MB");
        settings.setMaxMemory(2);
        settings.setMaxMemoryUnit("GB");

        assertTrue(settings.getMaxMemoryInMB() > settings.getMinMemoryInMB(),
                "Max memory should be greater than min memory");
    }
}