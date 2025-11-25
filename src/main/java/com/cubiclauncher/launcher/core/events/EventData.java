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

import java.util.HashMap;
import java.util.Map;

public class EventData {
    private final Map<String, Object> data;

    private EventData() {
        this.data = new HashMap<>();
    }

    // Builder pattern
    public static Builder builder() {
        return new Builder();
    }

    public static EventData error(String message, Throwable error) {
        return builder()
                .put("message", message)
                .put("error", error)
                .put("stackTrace", error != null ? error.getStackTrace() : null)
                .build();
    }
    public static EventData instanceCreated(String instanceName, String versionId) {
        return builder()
                .put("message", instanceName)
                .put("error", versionId)
                .build();
    }
    public static EventData downloadProgress(int type, int current, int total, String filename, String versionId) {
        return builder()
                .put("type", type)
                .put("current", current)
                .put("total", total)
                .put("filename", filename)
                .put("version", versionId)
                .build();
    }

    public static EventData empty() {
        return builder().build();
    }

    public String getString(String key) {
        return (String) data.get(key);
    }

    public Integer getInt(String key) {
        return (Integer) data.get(key);
    }

    public Double getDouble(String key) {
        return (Double) data.get(key);
    }

    // === Factory methods para eventos comunes ===

    public Boolean getBoolean(String key) {
        return (Boolean) data.get(key);
    }

    // Agregar este para objetos complejos
    public Object getObject(String key) {
        return data.get(key);
    }

    public static class Builder {
        private final EventData eventData = new EventData();

        public Builder put(String key, Object value) {
            eventData.data.put(key, value);
            return this;
        }

        public EventData build() {
            return eventData;
        }
    }
}