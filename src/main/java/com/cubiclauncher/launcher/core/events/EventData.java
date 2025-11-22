package com.cubiclauncher.launcher.core.events;

import java.util.HashMap;
import java.util.Map;

public class EventData {
    private final long timestamp;
    private final Map<String, Object> data;

    private EventData() {
        this.timestamp = System.currentTimeMillis();
        this.data = new HashMap<>();
    }

    public long getTimestamp() {
        return timestamp;
    }

    public <T> T get(String key) {
        return (T) data.get(key);
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

    public Boolean getBoolean(String key) {
        return (Boolean) data.get(key);
    }

    public Map<String, Object> getAll() {
        return new HashMap<>(data);
    }

    // Builder pattern
    public static Builder builder() {
        return new Builder();
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

    // === Factory methods para eventos comunes ===

    public static EventData versionEvent(String versionId) {
        return builder()
                .put("versionId", versionId)
                .build();
    }

    public static EventData downloadProgress(int type, int current, int total, String fileName) {
        return builder()
                .put("type", type)
                .put("current", current)
                .put("total", total)
                .put("fileName", fileName)
                .put("percentage", total > 0 ? (current * 100.0 / total) : 0.0)
                .build();
    }

    public static EventData gameEvent(String versionId, int value) {
        return builder()
                .put("versionId", versionId)
                .put("value", value)
                .build();
    }

    public static EventData error(String message, Throwable error) {
        return builder()
                .put("message", message)
                .put("error", error)
                .put("stackTrace", error != null ? error.getStackTrace() : null)
                .build();
    }

    public static EventData settingChange(String key, Object oldValue, Object newValue) {
        return builder()
                .put("key", key)
                .put("oldValue", oldValue)
                .put("newValue", newValue)
                .build();
    }

    public static EventData empty() {
        return builder().build();
    }
}