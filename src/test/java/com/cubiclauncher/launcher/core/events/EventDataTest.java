package com.cubiclauncher.launcher.core.events;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("EventData Tests")
class EventDataTest {

    @Test
    @DisplayName("Should create empty EventData")
    void testEmptyEventData() {
        EventData data = EventData.empty();
        assertNotNull(data);
    }

    @Test
    @DisplayName("Should build EventData with string values")
    void testStringValues() {
        EventData data = EventData.builder()
                .put("name", "TestInstance")
                .put("version", "1.20.1")
                .build();

        assertEquals("TestInstance", data.getString("name"));
        assertEquals("1.20.1", data.getString("version"));
    }

    @Test
    @DisplayName("Should build EventData with integer values")
    void testIntegerValues() {
        EventData data = EventData.builder()
                .put("current", 50)
                .put("total", 100)
                .build();

        assertEquals(50, data.getInt("current"));
        assertEquals(100, data.getInt("total"));
    }

    @Test
    @DisplayName("Should build EventData with boolean values")
    void testBooleanValues() {
        EventData data = EventData.builder()
                .put("success", true)
                .put("failed", false)
                .build();

        assertTrue(data.getBoolean("success"));
        assertFalse(data.getBoolean("failed"));
    }

    @Test
    @DisplayName("Should build EventData with double values")
    void testDoubleValues() {
        EventData data = EventData.builder()
                .put("progress", 0.75)
                .build();

        assertEquals(0.75, data.getDouble("progress"));
    }

    @Test
    @DisplayName("Should create error EventData")
    void testErrorEventData() {
        Exception testException = new RuntimeException("Test error");
        EventData data = EventData.error("Something went wrong", testException);

        assertEquals("Something went wrong", data.getString("message"));
        assertEquals(testException, data.getObject("error"));
        assertNotNull(data.getObject("stackTrace"));
    }

    @Test
    @DisplayName("Should create instance created EventData")
    void testInstanceCreatedEventData() {
        EventData data = EventData.instanceCreated("MyInstance", "1.20.1");

        assertEquals("MyInstance", data.getString("message"));
        assertEquals("1.20.1", data.getString("error"));
    }

    @Test
    @DisplayName("Should create download progress EventData")
    void testDownloadProgressEventData() {
        EventData data = EventData.downloadProgress(2, 50, 100, "assets.jar", "1.20.1");

        assertEquals(2, data.getInt("type"));
        assertEquals(50, data.getInt("current"));
        assertEquals(100, data.getInt("total"));
        assertEquals("assets.jar", data.getString("filename"));
        assertEquals("1.20.1", data.getString("version"));
    }

    @Test
    @DisplayName("Should return null for non-existent keys")
    void testNonExistentKeys() {
        EventData data = EventData.empty();

        assertNull(data.getString("nonexistent"));
        assertNull(data.getInt("nonexistent"));
        assertNull(data.getBoolean("nonexistent"));
        assertNull(data.getDouble("nonexistent"));
    }

    @Test
    @DisplayName("Should retrieve complex objects")
    void testComplexObjects() {
        Object complexObject = new Object();
        EventData data = EventData.builder()
                .put("object", complexObject)
                .build();

        assertSame(complexObject, data.getObject("object"));
    }

    @Test
    @DisplayName("Should chain builder calls")
    void testBuilderChaining() {
        EventData data = EventData.builder()
                .put("key1", "value1")
                .put("key2", 42)
                .put("key3", true)
                .build();

        assertEquals("value1", data.getString("key1"));
        assertEquals(42, data.getInt("key2"));
        assertTrue(data.getBoolean("key3"));
    }
}