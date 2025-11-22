/*
 *
 *  * Copyright (C) 2025 Santiagolxx, Notstaff and CubicLauncher contributors
 *  *
 *  * This program is free software: you can redistribute it and/or modify
 *  * it under the terms of the GNU Affero General Public License as published by
 *  * the Free Software Foundation, either version 3 of the License, or
 *  * (at your option) any later version.
 *  *
 *  * This program is distributed in the hope that it will be useful,
 *  * but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  * GNU Affero General Public License for more details.
 *  *
 *  * You should have received a copy of the GNU Affero General Public License
 *  * along with this program.  If not, see https://www.gnu.org/licenses/.
 *
 *
 */

package com.cubiclauncher.launcher.util;

import javafx.application.Platform;
import java.util.concurrent.*;

/**
 * Gestor centralizado de tareas asíncronas para evitar crear threads ilimitados
 */
public class TaskManager {
    private static TaskManager instance;
    private final ExecutorService executorService;

    private TaskManager() {
        ThreadFactory factory = Thread.ofVirtual()
                .name("CubicLauncher-VT-", 0)
                .factory();

        this.executorService = Executors.newThreadPerTaskExecutor(factory);
    }

    public static TaskManager getInstance() {
        if (instance == null) {
            instance = new TaskManager();
        }
        return instance;
    }

    /**
     * Ejecuta una tarea en segundo plano
     * @param task La tarea a ejecutar
     */
    public void runAsync(Runnable task) {
        executorService.submit(() -> {
            try {
                task.run();
            } catch (Exception e) {
                e.printStackTrace();
                // Mostrar error en la UI si es necesario
                Platform.runLater(() -> System.err.println("Error en tarea asíncrona: " + e.getMessage()));
            }
        });
    }

    /**
     * Ejecuta una tarea en segundo plano y ejecuta un callback en el hilo de JavaFX
     * @param task La tarea a ejecutar
     * @param onSuccess Callback a ejecutar al completarse (en el hilo de UI)
     */
    public void runAsync(Runnable task, Runnable onSuccess) {
        executorService.submit(() -> {
            try {
                task.run();
                if (onSuccess != null) {
                    Platform.runLater(onSuccess);
                }
            } catch (Exception e) {
                e.printStackTrace();
                Platform.runLater(() -> System.err.println("Error en tarea asíncrona: " + e.getMessage()));
            }
        });
    }

    /**
     * Ejecuta una tarea que puede fallar, con manejo de errores
     */
    public void runAsync(ThrowingRunnable task, Runnable onSuccess, java.util.function.Consumer<Exception> onError) {
        executorService.submit(() -> {
            try {
                task.run();
                if (onSuccess != null) {
                    Platform.runLater(onSuccess);
                }
            } catch (Exception e) {
                e.printStackTrace();
                if (onError != null) {
                    Platform.runLater(() -> onError.accept(e));
                }
            }
        });
    }

    /**
     * Ejecuta una tarea que retorna un resultado
     */
    public <T> CompletableFuture<T> runAsync(Callable<T> task) {
        CompletableFuture<T> future = new CompletableFuture<>();
        executorService.submit(() -> {
            try {
                T result = task.call();
                future.complete(result);
            } catch (Exception e) {
                future.completeExceptionally(e);
            }
        });
        return future;
    }

    /**
     * Cierra el ExecutorService cuando se cierra la aplicación
     */
    public void shutdown() {
        executorService.shutdown();
        try {
            if (!executorService.awaitTermination(5, TimeUnit.SECONDS)) {
                executorService.shutdownNow();
            }
        } catch (InterruptedException e) {
            executorService.shutdownNow();
        }
    }

    /**
     * Interfaz funcional para tareas que pueden lanzar excepciones
     */
    @FunctionalInterface
    public interface ThrowingRunnable {
        void run();
    }
}