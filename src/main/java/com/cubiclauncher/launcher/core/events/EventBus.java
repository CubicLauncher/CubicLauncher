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

import java.util.Map;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;

public class EventBus {
    private static final Logger log = LoggerFactory.getLogger(EventBus.class);
    private static EventBus instance;

    // Mapa de tipo de evento -> lista de listeners
    private final Map<EventType, List<Consumer<EventData>>> listeners = new ConcurrentHashMap<>();

    private EventBus() {}

    public static class Holder {
        static final EventBus INSTANCE = new EventBus();
    }

    public static EventBus get() {
        return Holder.INSTANCE;
    }

    /**
     * Registrar un listener para un tipo de evento
     */
    public void subscribe(EventType type, Consumer<EventData> listener) {
        listeners.computeIfAbsent(type, k -> new CopyOnWriteArrayList<>())
                .add(listener);
        log.debug("Listener registrado para evento: {}", type);
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
     * Desuscribir un listener
     */
    public void unsubscribe(EventType type, Consumer<EventData> listener) {
        List<Consumer<EventData>> eventListeners = listeners.get(type);
        if (eventListeners != null) {
            eventListeners.remove(listener);
        }
    }

    /**
     * Limpiar todos los listeners de un tipo
     */
    public void clearListeners(EventType type) {
        listeners.remove(type);
    }

    /**
     * Limpiar todos los listeners
     */
    public void clearAll() {
        listeners.clear();
    }

    // === Métodos helper para eventos comunes ===

    public void emitVersionDownloaded(String versionId) {
        emit(EventType.VERSION_DOWNLOADED, EventData.versionEvent(versionId));
    }

    public void emitVersionLaunched(String versionId) {
        emit(EventType.VERSION_LAUNCHED, EventData.versionEvent(versionId));
    }

    public void emitDownloadProgress(int type, int current, int total, String fileName) {
        emit(EventType.DOWNLOAD_PROGRESS, EventData.downloadProgress(type, current, total, fileName));
    }

    public void emitGameStarted(String versionId, int pid) {
        emit(EventType.GAME_STARTED, EventData.gameEvent(versionId, pid));
    }

    public void emitGameStopped(String versionId, int exitCode) {
        emit(EventType.GAME_STOPPED, EventData.gameEvent(versionId, exitCode));
    }
}