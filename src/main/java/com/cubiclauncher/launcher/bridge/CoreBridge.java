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

package com.cubiclauncher.launcher.bridge;

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