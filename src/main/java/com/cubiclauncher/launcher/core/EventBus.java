/*
 * Copyright (C) 2025 Santiagolxx, Notstaff and CubicLauncher contributors
 * AGPL-3.0 License
 */
package com.cubiclauncher.launcher.core;

import javafx.application.Platform;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * Bus de eventos simple para comunicación entre componentes.
 */
public class EventBus {
    private static final EventBus instance = new EventBus();

    private final List<Consumer<String>> onVersionDownloaded = new ArrayList<>();
    private final List<Runnable> onVersionsChanged = new ArrayList<>();

    private EventBus() {}

    public static EventBus get() { return instance; }

    // === Suscribirse a eventos ===

    public void onVersionDownloaded(Consumer<String> listener) {
        onVersionDownloaded.add(listener);
    }

    public void onVersionsChanged(Runnable listener) {
        onVersionsChanged.add(listener);
    }

    // === Emitir eventos (siempre en el hilo de UI) ===

    public void emitVersionDownloaded(String versionId) {
        Platform.runLater(() -> {
            onVersionDownloaded.forEach(l -> l.accept(versionId));
            onVersionsChanged.forEach(Runnable::run);
        });
    }

    // === Limpiar listeners (opcional, para evitar memory leaks) ===

    public void clear() {
        onVersionDownloaded.clear();
        onVersionsChanged.clear();
    }
}