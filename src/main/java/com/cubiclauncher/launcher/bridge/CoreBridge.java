package com.cubiclauncher.launcher.core.bridge;

import com.cubiclauncher.launcher.core.events.EventBus;
import com.cubiclauncher.launcher.core.events.EventData;
import com.cubiclauncher.launcher.core.events.EventType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MarkerFactory;
import org.slf4j.Marker;

/**
 * Puente entre el Core y el EventBus.
 * El Core NO conoce la UI, solo emite eventos.
 *
 * Filosofía Rust: Los componentes del core son como módulos independientes
 * que comunican a través de canales (aquí, el EventBus).
 */
public class CoreBridge {
    private static final Logger log = LoggerFactory.getLogger(CoreBridge.class);
    private static final EventBus eventBus = EventBus.get();
    private static final Marker instanceMarker = MarkerFactory.getMarker("Instances");
    // === INSTANCE EVENTS ===

    public static void emitInstanceCreated(String name, String version) {
        log.info(instanceMarker, "Created: {} ({})", name, version);
        eventBus.emit(EventType.INSTANCE_CREATED,
                EventData.instanceCreated(name, version));
    }
}