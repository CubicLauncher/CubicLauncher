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

package com.cubiclauncher.launcher.core;

import javafx.application.Platform;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

/**
 * Gestor centralizado de tareas asíncronas con Virtual Threads
 */
public class TaskManager {
    private static TaskManager instance;
    private final ExecutorService executorService;
    private final ScheduledExecutorService scheduler;
    private final Logger log = LoggerFactory.getLogger(TaskManager.class);
    private final AtomicInteger activeTasks = new AtomicInteger(0);

    private TaskManager() {
        ThreadFactory factory = Thread.ofVirtual()
                .name("CubicLauncher-VT-", 0)
                .factory();

        this.executorService = Executors.newThreadPerTaskExecutor(factory);

        // Scheduler para tareas retrasadas
        this.scheduler = Executors.newScheduledThreadPool(1, r -> {
            Thread t = new Thread(r, "TaskManager-Scheduler");
            t.setDaemon(true);
            return t;
        });

        // Monitor opcional para debugging (cada 30 segundos)
        scheduler.scheduleAtFixedRate(() -> {
            int active = activeTasks.get();
            if (active > 10) {
                log.warn("Hay {} tareas activas ejecutándose", active);
            } else if (active > 0) {
                log.debug("Tareas activas: {}", active);
            }
        }, 30, 30, TimeUnit.SECONDS);
    }

    public static TaskManager getInstance() {
        if (instance == null) {
            instance = new TaskManager();
        }
        return instance;
    }

    /**
     * Ejecuta una tarea en segundo plano
     *
     * @param task La tarea a ejecutar
     */
    public void runAsync(Runnable task) {
        activeTasks.incrementAndGet();
        executorService.submit(() -> {
            try {
                task.run();
            } catch (Exception e) {
                log.error("Error en tarea asíncrona: {}", e.getMessage(), e);
            } finally {
                activeTasks.decrementAndGet();
            }
        });
    }
    /**
     * Ejecuta una tarea en segundo plano en el thread de la UI
     *
     * @param task La tarea a ejecutar en el thread de JavaFX
     */
    public void runAsyncAtJFXThread(Runnable task) {
        activeTasks.incrementAndGet();
        executorService.submit(() -> {
            try {
                Platform.runLater(task);
            } catch (Exception e) {
                log.error("Error en tarea asíncrona: {}", e.getMessage(), e);
            } finally {
                activeTasks.decrementAndGet();
            }
        });
    }
    /**
     * Ejecuta una tarea que puede lanzar excepciones, con callbacks de éxito y error
     *
     * @param task      La tarea a ejecutar (puede lanzar excepciones)
     * @param onSuccess Callback al completarse exitosamente (en el hilo de UI)
     * @param onFail    Callback al fallar (recibe la excepción, en el hilo de UI)
     */
    public void runAsync(ThrowingRunnable task, Runnable onSuccess, Consumer<Exception> onFail) {
        activeTasks.incrementAndGet();
        executorService.submit(() -> {
            try {
                task.run();
                if (onSuccess != null) {
                    Platform.runLater(onSuccess);
                }
            } catch (Exception e) {
                log.error("Error en tarea asíncrona: {}", e.getMessage(), e);
                if (onFail != null) {
                    Platform.runLater(() -> onFail.accept(e));
                }
            } finally {
                activeTasks.decrementAndGet();
            }
        });
    }

    /**
     * Ejecuta una tarea que puede lanzar excepciones, con callbacks simples
     * (cuando no necesitas acceso a la excepción en el callback de error)
     *
     * @param task         La tarea a ejecutar (puede lanzar excepciones)
     * @param onSuccess    Callback al completarse exitosamente
     * @param onFailSimple Callback al fallar (sin parámetro de excepción)
     */
    public void runAsync(ThrowingRunnable task, Runnable onSuccess, Runnable onFailSimple) {
        runAsync(task, onSuccess, e -> {
            if (onFailSimple != null) {
                onFailSimple.run();
            }
        });
    }

    /**
     * Ejecuta una tarea que retorna un resultado
     *
     * @param task La tarea que retorna un valor
     * @return CompletableFuture con el resultado
     */
    public <T> CompletableFuture<T> runAsync(Callable<T> task) {
        activeTasks.incrementAndGet();
        CompletableFuture<T> future = new CompletableFuture<>();
        executorService.submit(() -> {
            try {
                T result = task.call();
                future.complete(result);
            } catch (Exception e) {
                log.error("Error en tarea asíncrona con retorno: {}", e.getMessage(), e);
                future.completeExceptionally(e);
            } finally {
                activeTasks.decrementAndGet();
            }
        });
        return future;
    }

    /**
     * Cierra el ExecutorService cuando se cierra la aplicación
     */
    public void shutdown() {
        log.info("Cerrando TaskManager... ({} tareas activas)", activeTasks.get());

        // Cerrar el scheduler primero
        scheduler.shutdown();

        // Cerrar el executor principal
        executorService.shutdown();

        try {
            // Esperar a que terminen las tareas
            if (!executorService.awaitTermination(5, TimeUnit.SECONDS)) {
                log.warn("Timeout esperando el cierre de tareas, forzando shutdown...");
                executorService.shutdownNow();

                if (!executorService.awaitTermination(2, TimeUnit.SECONDS)) {
                    log.error("No se pudieron cerrar todas las tareas del executor");
                }
            }

            // Esperar al scheduler
            if (!scheduler.awaitTermination(2, TimeUnit.SECONDS)) {
                log.warn("Timeout esperando el cierre del scheduler, forzando...");
                scheduler.shutdownNow();
            }

        } catch (InterruptedException e) {
            log.error("Interrupción durante el cierre", e);
            executorService.shutdownNow();
            scheduler.shutdownNow();
            Thread.currentThread().interrupt();
        }

        log.info("TaskManager cerrado. Tareas restantes: {}", activeTasks.get());
    }

    /**
     * Interfaz funcional para tareas que pueden lanzar excepciones
     */
    @FunctionalInterface
    public interface ThrowingRunnable {
        void run() throws Exception;
    }
}