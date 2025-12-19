package com.cubiclauncher.launcher.core;

import com.cubiclauncher.launcher.core.InstanceManager.Instance;
import org.junit.jupiter.api.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("InstanceManager Tests")
class InstanceManagerTest {

    private InstanceManager instanceManager;

    @BeforeEach
    void setUp() {
        instanceManager = InstanceManager.getInstance();
        // Limpiar instancias existentes para cada test
        List<Instance> instances = instanceManager.getAllInstances();
        for (Instance instance : instances) {
            instanceManager.deleteInstance(instance.getName());
        }
    }

    @Test
    @DisplayName("Should return singleton instance")
    void testSingleton() {
        InstanceManager instance1 = InstanceManager.getInstance();
        InstanceManager instance2 = InstanceManager.getInstance();

        assertSame(instance1, instance2, "Should return the same instance");
    }

    // ==================== Instance Creation ====================

    @Test
    @DisplayName("Should create new instance")
    void testCreateInstance() {
        String name = "TestInstance";
        String version = "1.20.1";

        instanceManager.createInstance(name, version);

        assertTrue(instanceManager.instanceExists(name));
        Optional<Instance> instance = instanceManager.getInstance(name);
        assertTrue(instance.isPresent());
        assertEquals(name, instance.get().getName());
        assertEquals(version, instance.get().getVersion());
    }

    @Test
    @DisplayName("Should throw exception when creating duplicate instance")
    void testCreateDuplicateInstance() {
        String name = "DuplicateTest";
        instanceManager.createInstance(name, "1.20.1");

        assertThrows(IllegalArgumentException.class, () -> {
            instanceManager.createInstance(name, "1.19.4");
        });
    }

    @Test
    @DisplayName("Should save instance to disk")
    void testInstanceSavedToDisk() {
        String name = "SaveTest";
        instanceManager.createInstance(name, "1.20.1");

        Optional<Instance> instance = instanceManager.getInstance(name);
        assertTrue(instance.isPresent());

        Path instanceDir = instance.get().getInstanceDir(
                PathManager.getInstance().getInstancePath());
        assertTrue(Files.exists(instanceDir));

        Path configFile = instance.get().getInstanceConfigPath(
                PathManager.getInstance().getInstancePath());
        assertTrue(Files.exists(configFile));
    }

    // ==================== Instance Retrieval ====================

    @Test
    @DisplayName("Should get instance by name")
    void testGetInstance() {
        String name = "GetTest";
        instanceManager.createInstance(name, "1.20.1");

        Optional<Instance> instance = instanceManager.getInstance(name);
        assertTrue(instance.isPresent());
        assertEquals(name, instance.get().getName());
    }

    @Test
    @DisplayName("Should return empty optional for non-existent instance")
    void testGetNonExistentInstance() {
        Optional<Instance> instance = instanceManager.getInstance("NonExistent");
        assertFalse(instance.isPresent());
    }

    @Test
    @DisplayName("Should get all instances")
    void testGetAllInstances() {
        instanceManager.createInstance("Instance1", "1.20.1");
        instanceManager.createInstance("Instance2", "1.19.4");
        instanceManager.createInstance("Instance3", "1.18.2");

        List<Instance> instances = instanceManager.getAllInstances();
        assertEquals(3, instances.size());
    }

    @Test
    @DisplayName("Should get instance count")
    void testGetInstanceCount() {
        assertEquals(0, instanceManager.getInstanceCount());

        instanceManager.createInstance("Test1", "1.20.1");
        assertEquals(1, instanceManager.getInstanceCount());

        instanceManager.createInstance("Test2", "1.19.4");
        assertEquals(2, instanceManager.getInstanceCount());
    }

    // ==================== Instance Deletion ====================

    @Test
    @DisplayName("Should delete instance")
    void testDeleteInstance() {
        String name = "DeleteTest";
        instanceManager.createInstance(name, "1.20.1");
        assertTrue(instanceManager.instanceExists(name));

        boolean deleted = instanceManager.deleteInstance(name);
        assertTrue(deleted);
        assertFalse(instanceManager.instanceExists(name));
    }

    @Test
    @DisplayName("Should return false when deleting non-existent instance")
    void testDeleteNonExistentInstance() {
        boolean deleted = instanceManager.deleteInstance("NonExistent");
        assertFalse(deleted);
    }

    @Test
    @DisplayName("Should delete instance directory")
    void testDeleteInstanceDirectory() {
        String name = "DeleteDirTest";
        instanceManager.createInstance(name, "1.20.1");

        Optional<Instance> instance = instanceManager.getInstance(name);
        assertTrue(instance.isPresent());

        Path instanceDir = instance.get().getInstanceDir(
                PathManager.getInstance().getInstancePath());
        assertTrue(Files.exists(instanceDir));

        instanceManager.deleteInstance(name);
        assertFalse(Files.exists(instanceDir));
    }

    // ==================== Instance Renaming ====================

    @Test
    @DisplayName("Should rename instance")
    void testRenameInstance() {
        String oldName = "OldName";
        String newName = "NewName";

        instanceManager.createInstance(oldName, "1.20.1");
        boolean renamed = instanceManager.renameInstance(oldName, newName);

        assertTrue(renamed);
        assertFalse(instanceManager.instanceExists(oldName));
        assertTrue(instanceManager.instanceExists(newName));
    }

    @Test
    @DisplayName("Should not rename to existing instance name")
    void testRenameToExistingName() {
        instanceManager.createInstance("Instance1", "1.20.1");
        instanceManager.createInstance("Instance2", "1.19.4");

        boolean renamed = instanceManager.renameInstance("Instance1", "Instance2");
        assertFalse(renamed);
    }

    @Test
    @DisplayName("Should return false when renaming non-existent instance")
    void testRenameNonExistentInstance() {
        boolean renamed = instanceManager.renameInstance("NonExistent", "NewName");
        assertFalse(renamed);
    }

    @Test
    @DisplayName("Should update directory when renaming")
    void testRenameUpdatesDirectory() {
        String oldName = "OldDir";
        String newName = "NewDir";

        instanceManager.createInstance(oldName, "1.20.1");
        instanceManager.renameInstance(oldName, newName);

        Optional<Instance> instance = instanceManager.getInstance(newName);
        assertTrue(instance.isPresent());

        Path instanceDir = instance.get().getInstanceDir(
                PathManager.getInstance().getInstancePath());
        assertTrue(Files.exists(instanceDir));
        assertTrue(instanceDir.toString().contains(newName));
    }

    // ==================== Instance Properties ====================

    @Test
    @DisplayName("Should check if instance exists")
    void testInstanceExists() {
        String name = "ExistsTest";
        assertFalse(instanceManager.instanceExists(name));

        instanceManager.createInstance(name, "1.20.1");
        assertTrue(instanceManager.instanceExists(name));
    }

    @Test
    @DisplayName("Should update last played timestamp")
    void testUpdateLastPlayed() throws InterruptedException {
        String name = "LastPlayedTest";
        instanceManager.createInstance(name, "1.20.1");

        Optional<Instance> instance = instanceManager.getInstance(name);
        assertTrue(instance.isPresent());
        long firstTime = instance.get().getLastPlayed();

        Thread.sleep(10); // Pequeño delay para asegurar diferencia en timestamp

        instanceManager.updateLastPlayed(name);

        Optional<Instance> updated = instanceManager.getInstance(name);
        assertTrue(updated.isPresent());
        assertTrue(updated.get().getLastPlayed() > firstTime);
    }

    @Test
    @DisplayName("Should get instances sorted by last played")
    void testGetInstancesByLastPlayed() throws InterruptedException {
        instanceManager.createInstance("Instance1", "1.20.1");
        Thread.sleep(10);
        instanceManager.createInstance("Instance2", "1.19.4");
        Thread.sleep(10);
        instanceManager.createInstance("Instance3", "1.18.2");

        List<Instance> sorted = instanceManager.getInstancesByLastPlayed();
        assertEquals(3, sorted.size());

        // El más reciente debería ser primero
        assertEquals("Instance3", sorted.get(0).getName());
        assertEquals("Instance1", sorted.get(2).getName());
    }

    // ==================== Instance Object Tests ====================

    @Test
    @DisplayName("Instance should have correct properties")
    void testInstanceProperties() {
        Instance instance = new Instance("TestInstance", "1.20.1");

        assertEquals("TestInstance", instance.getName());
        assertEquals("1.20.1", instance.getVersion());
        assertTrue(instance.getLastPlayed() > 0);
    }

    @Test
    @DisplayName("Instance should update last played")
    void testInstanceUpdateLastPlayed() throws InterruptedException {
        Instance instance = new Instance("Test", "1.20.1");
        long firstTime = instance.getLastPlayed();

        Thread.sleep(10);
        instance.updateLastPlayed();

        assertTrue(instance.getLastPlayed() > firstTime);
    }

    @Test
    @DisplayName("Instance should format last played date")
    void testInstanceLastPlayedFormatted() {
        Instance instance = new Instance("Test", "1.20.1");
        String formatted = instance.getLastPlayedFormatted();

        assertNotNull(formatted);
        assertFalse(formatted.isEmpty());
        assertTrue(formatted.matches("\\d{2}/\\d{2}/\\d{4} \\d{2}:\\d{2}"));
    }

    @Test
    @DisplayName("Instance should have correct directory path")
    void testInstanceDirectoryPath() {
        Instance instance = new Instance("TestDir", "1.20.1");
        Path instancesDir = PathManager.getInstance().getInstancePath();
        Path instanceDir = instance.getInstanceDir(instancesDir);

        assertTrue(instanceDir.toString().endsWith("TestDir"));
    }

    @Test
    @DisplayName("Instance should have correct config path")
    void testInstanceConfigPath() {
        Instance instance = new Instance("TestConfig", "1.20.1");
        Path instancesDir = PathManager.getInstance().getInstancePath();
        Path configPath = instance.getInstanceConfigPath(instancesDir);

        assertTrue(configPath.toString().endsWith("instance.cub"));
    }

    @Test
    @DisplayName("Instances with same name should be equal")
    void testInstanceEquality() {
        Instance instance1 = new Instance("Test", "1.20.1");
        Instance instance2 = new Instance("Test", "1.19.4");

        assertEquals(instance1, instance2);
        assertEquals(instance1.hashCode(), instance2.hashCode());
    }

    @Test
    @DisplayName("Instances with different names should not be equal")
    void testInstanceInequality() {
        Instance instance1 = new Instance("Test1", "1.20.1");
        Instance instance2 = new Instance("Test2", "1.20.1");

        assertNotEquals(instance1, instance2);
    }
}