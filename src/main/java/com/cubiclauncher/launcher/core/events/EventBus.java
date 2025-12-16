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

package com.cubiclauncher.launcher.core.events;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;

public class EventBus {
    private static final Logger log = LoggerFactory.getLogger(EventBus.class);

    // Mapa de tipo de evento -> lista de listeners
    private final Map<EventType, List<Consumer<EventData>>> listeners = new ConcurrentHashMap<>();

    private EventBus() {
    }

    public static EventBus get() {
        return Holder.INSTANCE;
    }

    /**
     * Registrar un listener para un tipo de evento
     */
    public Subscription subscribe(EventType type, Consumer<EventData> listener) {
        listeners.computeIfAbsent(type, k -> new CopyOnWriteArrayList<>())
                .add(listener);
        return new Subscription(type, listener, this);
    }

    /**
     * Publicar un evento
     */
    public void emit(EventType type, EventData data) {
        List<Consumer<EventData>> eventListeners = listeners.get(type);

        if (eventListeners != null && !eventListeners.isEmpty()) {
            for (Consumer<EventData> listener : eventListeners) {
                try {
                    listener.accept(data);
                } catch (Exception e) {
                    log.error("Error ejecutando listener para {}", type, e);
                }
            }
        }
    }

    /**
     * Limpiar todos los listeners
     */
    public void clearAll() {
        listeners.clear();
    }

    /**
     * Eliminar un listener
     */
    public void unsubscribe(EventType type, Consumer<EventData> listener) {
        List<Consumer<EventData>> eventListeners = listeners.get(type);
        if (eventListeners != null) {
            eventListeners.remove(listener);
        }
    }

    public static class Holder {
        static final EventBus INSTANCE = new EventBus();
    }

    public record Subscription(EventType eventType, Consumer<EventData> listener, EventBus eventBus) {

        public void unsubscribe() {
                eventBus.unsubscribe(eventType, listener);
            }
        }
}