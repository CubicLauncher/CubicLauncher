<template>
    <div class="space-y-6">
        <!-- Version Information -->
        <div class="bg-stone-800 border border-stone-600 rounded-lg">
            <div class="px-6 py-4 border-b border-stone-600">
                <h3 class="text-lg font-semibold text-stone-200 flex items-center">
                    <svg class="w-5 h-5 mr-2 text-stone-400" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                        <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 12l2 2 4-4m6 2a9 9 0 11-18 0 9 9 0 0118 0z"></path>
                    </svg>
                    Información de Versión
                </h3>
            </div>
            <div class="p-6">
                <div class="flex items-center justify-between p-4 bg-stone-700 rounded-lg">
                    <div class="flex items-center">
                        <div class="w-10 h-10 bg-stone-600 rounded-lg flex items-center justify-center">
                            <svg class="w-5 h-5 text-stone-300" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 12l2 2 4-4m6 2a9 9 0 11-18 0 9 9 0 0118 0z"></path>
                            </svg>
                        </div>
                        <div class="ml-4">
                            <p class="text-sm font-medium text-stone-200">Versión actual</p>
                            <p class="text-sm text-stone-400">CubicLauncher v1.0.0</p>
                        </div>
                    </div>
                    <div class="flex items-center gap-2">
                        <span class="inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium bg-green-600 text-green-200">
                            Actualizado
                        </span>
                        <button class="inline-flex items-center px-3 py-1.5 rounded-md text-xs font-medium bg-stone-600 text-stone-300 hover:bg-stone-500 hover:text-stone-200 transition-colors">
                            <svg class="w-3 h-3 mr-1" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M4 4v5h.582m15.356 2A8.001 8.001 0 004.582 9m0 0H9m11 11v-5h-.581m0 0a8.003 8.003 0 01-15.357-2m15.357 2H15"></path>
                            </svg>
                            Buscar actualización
                        </button>
                    </div>
                </div>
            </div>
        </div>

        <!-- System Information -->
        <div class="bg-stone-800 border border-stone-600 rounded-lg">
            <div class="px-6 py-4 border-b border-stone-600">
                <h3 class="text-lg font-semibold text-stone-200 flex items-center">
                    <svg class="w-5 h-5 mr-2 text-stone-400" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                        <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 3v2m6-2v2M9 19v2m6-2v2M5 9H3m2 6H3m18-6h-2m2 6h-2M7 19h10a2 2 0 002-2V7a2 2 0 00-2-2H7a2 2 0 00-2 2v10a2 2 0 002 2zM9 9h6v6H9V9z"></path>
                    </svg>
                    Información del Sistema
                </h3>
            </div>
            <div class="p-6">
                <div class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
                    <div class="p-4 bg-stone-700 rounded-lg">
                        <div class="flex items-center">
                            <div class="w-8 h-8 bg-stone-600 rounded-lg flex items-center justify-center">
                                <svg class="w-4 h-4 text-stone-300" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 3v2m6-2v2M9 19v2m6-2v2M5 9H3m2 6H3m18-6h-2m2 6h-2M7 19h10a2 2 0 002-2V7a2 2 0 00-2-2H7a2 2 0 00-2 2v10a2 2 0 002 2zM9 9h6v6H9V9z"></path>
                                </svg>
                            </div>
                            <div class="ml-3">
                                <p class="text-sm font-medium text-stone-200">Sistema Operativo</p>
                                <p class="text-sm text-stone-400">Windows 10</p>
                            </div>
                        </div>
                    </div>

                    <div class="p-4 bg-stone-700 rounded-lg">
                        <div class="flex items-center">
                            <div class="w-8 h-8 bg-stone-600 rounded-lg flex items-center justify-center">
                                <svg class="w-4 h-4 text-stone-300" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 12l2 2 4-4m6 2a9 9 0 11-18 0 9 9 0 0118 0z"></path>
                                </svg>
                            </div>
                            <div class="ml-3">
                                <p class="text-sm font-medium text-stone-200">Build</p>
                                <p class="text-sm text-stone-400">v1.0.0</p>
                            </div>
                        </div>
                    </div>

                    <div class="p-4 bg-stone-700 rounded-lg">
                        <div class="flex items-center">
                            <div class="w-8 h-8 bg-stone-600 rounded-lg flex items-center justify-center">
                                <svg class="w-4 h-4 text-stone-300" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 8v4l3 3m6-3a9 9 0 11-18 0 9 9 0 0118 0z"></path>
                                </svg>
                            </div>
                            <div class="ml-3">
                                <p class="text-sm font-medium text-stone-200">Última actualización</p>
                                <p class="text-sm text-stone-400">Hace 2 días</p>
                            </div>
                        </div>
                    </div>

                    <div class="p-4 bg-stone-700 rounded-lg">
                        <div class="flex items-center">
                            <div class="w-8 h-8 bg-stone-600 rounded-lg flex items-center justify-center">
                                <svg class="w-4 h-4 text-stone-300" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M13 10V3L4 14h7v7l9-11h-7z"></path>
                                </svg>
                            </div>
                            <div class="ml-3">
                                <p class="text-sm font-medium text-stone-200">Rendimiento</p>
                                <p class="text-sm text-stone-400">Óptimo</p>
                            </div>
                        </div>
                    </div>

                    <div class="p-4 bg-stone-700 rounded-lg">
                        <div class="flex items-center justify-between">
                            <div class="flex items-center">
                                <div class="w-8 h-8 bg-stone-600 rounded-lg flex items-center justify-center">
                                    <svg class="w-4 h-4 text-stone-300" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                                        <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M10 20l4-16m4 4l4 4-4 4M6 16l-4-4 4-4"></path>
                                    </svg>
                                </div>
                                <div class="ml-3">
                                    <p class="text-sm font-medium text-stone-200">Código Fuente</p>
                                    <p class="text-sm text-stone-400">GitHub</p>
                                </div>
                            </div>
                            <a 
                                href="https://github.com/CubicLauncher/CubicLauncher"
                                target="_blank"
                                rel="noopener noreferrer"
                                class="inline-flex items-center px-3 py-1.5 rounded-md text-xs font-medium bg-stone-600 text-stone-300 hover:bg-stone-500 hover:text-stone-200 transition-colors"
                            >
                                <svg class="w-3 h-3 mr-1" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M10 6H6a2 2 0 00-2 2v10a2 2 0 002 2h10a2 2 0 002-2v-4M14 4h6m0 0v6m0-6L10 14"></path>
                                </svg>
                                Ver
                            </a>
                        </div>
                    </div>
                </div>
            </div>
        </div>

        <!-- About Section -->
        <div class="bg-stone-800 border border-stone-600 rounded-lg">
            <div class="px-6 py-4 border-b border-stone-600">
                <h3 class="text-lg font-semibold text-stone-200 flex items-center">
                    <svg class="w-5 h-5 mr-2 text-stone-400" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                        <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M13 16h-1v-4h-1m1-4h.01M21 12a9 9 0 11-18 0 9 9 0 0118 0z"></path>
                    </svg>
                    Acerca de CubicLauncher
                </h3>
            </div>
            <div class="p-6">
                <div class="space-y-4">
                    <p class="text-sm text-stone-300 leading-relaxed">
                        CubicLauncher es un launcher moderno y elegante para Minecraft, diseñado para proporcionar 
                        una experiencia de juego fluida y personalizable. Desarrollado con tecnologías modernas 
                        como Vue.js y Tauri.
                    </p>
                    
                    <div class="flex flex-wrap gap-2">
                        <span class="inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium bg-stone-600 text-stone-300">
                            Vue.js
                        </span>
                        <span class="inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium bg-stone-600 text-stone-300">
                            Tauri
                        </span>
                        <span class="inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium bg-stone-600 text-stone-300">
                            Rust
                        </span>
                        <span class="inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium bg-stone-600 text-stone-300">
                            TypeScript
                        </span>
                    </div>

                    <div class="flex gap-2">
                        <button class="inline-flex items-center px-4 py-2 rounded-md text-sm font-medium bg-stone-600 text-stone-300 hover:bg-stone-500 hover:text-stone-200 transition-colors">
                            <svg class="w-4 h-4 mr-2" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M8.228 9c.549-1.165 2.03-2 3.772-2 2.21 0 4 1.343 4 3 0 1.4-1.278 2.575-3.006 2.907-.542.104-.994.54-.994 1.093m0 3h.01M21 12a9 9 0 11-18 0 9 9 0 0118 0z"></path>
                            </svg>
                            Documentación
                        </button>
                        <button class="inline-flex items-center px-4 py-2 rounded-md text-sm font-medium bg-stone-600 text-stone-300 hover:bg-stone-500 hover:text-stone-200 transition-colors">
                            <svg class="w-4 h-4 mr-2" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M4.318 6.318a4.5 4.5 0 000 6.364L12 20.364l7.682-7.682a4.5 4.5 0 00-6.364-6.364L12 7.636l-1.318-1.318a4.5 4.5 0 00-6.364 0z"></path>
                            </svg>
                            Donar
                        </button>
                    </div>
                </div>
            </div>
        </div>
    </div>
</template>

<script setup lang="ts">
// Solo diseño visual - sin funcionalidad
</script> 
