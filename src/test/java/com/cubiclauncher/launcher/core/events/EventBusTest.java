package com.cubiclauncher.launcher.core.events;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("EventBus Tests")
class EventBusTest {

    private EventBus eventBus;

    @BeforeEach
    void setUp() {
        eventBus = EventBus.get();
        eventBus.clearAll();
    }

    @Test
    @DisplayName("Should emit and receive events")
    void testEmitAndReceive() {
        AtomicBoolean received = new AtomicBoolean(false);

        eventBus.subscribe(EventType.INSTANCE_CREATED, data -> {
            received.set(true);
        });

        eventBus.emit(EventType.INSTANCE_CREATED, EventData.empty());

        assertTrue(received.get(), "Event should have been received");
    }

    @Test
    @DisplayName("Should pass event data correctly")
    void testEventDataPassing() {
        final String expectedName = "TestInstance";
        final String expectedVersion = "1.20.1";

        eventBus.subscribe(EventType.INSTANCE_CREATED, data -> {
            assertEquals(expectedName, data.getString("message"));
            assertEquals(expectedVersion, data.getString("error"));
        });

        eventBus.emit(EventType.INSTANCE_CREATED,
                EventData.instanceCreated(expectedName, expectedVersion));
    }

    @Test
    @DisplayName("Should support multiple subscribers")
    void testMultipleSubscribers() {
        AtomicInteger callCount = new AtomicInteger(0);

        eventBus.subscribe(EventType.DOWNLOAD_PROGRESS, data -> callCount.incrementAndGet());
        eventBus.subscribe(EventType.DOWNLOAD_PROGRESS, data -> callCount.incrementAndGet());
        eventBus.subscribe(EventType.DOWNLOAD_PROGRESS, data -> callCount.incrementAndGet());

        eventBus.emit(EventType.DOWNLOAD_PROGRESS, EventData.empty());

        assertEquals(3, callCount.get(), "All three subscribers should be called");
    }

    @Test
    @DisplayName("Should clear all listeners")
    void testClearAll() {
        AtomicBoolean received = new AtomicBoolean(false);

        eventBus.subscribe(EventType.INSTANCE_DELETED, data -> received.set(true));
        eventBus.clearAll();
        eventBus.emit(EventType.INSTANCE_DELETED, EventData.empty());

        assertFalse(received.get(), "No listeners should remain after clearAll");
    }

    @Test
    @DisplayName("Should not receive events for different event types")
    void testEventTypeIsolation() {
        AtomicBoolean received = new AtomicBoolean(false);

        eventBus.subscribe(EventType.INSTANCE_CREATED, data -> received.set(true));
        eventBus.emit(EventType.INSTANCE_DELETED, EventData.empty());

        assertFalse(received.get(), "Should not receive events of different types");
    }
}