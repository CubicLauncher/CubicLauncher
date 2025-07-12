<template>
    <div class="bg-stone-800 border border-stone-600 rounded-lg">
        <div class="px-6 py-4 border-b border-stone-600">
            <h3 class="text-lg font-semibold text-stone-200 flex items-center">
                <svg class="w-5 h-5 mr-2 text-stone-400" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M3 5h12M9 3v2m1.048 9.5A18.022 18.022 0 016.412 9m6.088 9h7M11 21l5-10 5 10M12.751 5C11.783 10.77 8.07 15.61 3 18.129"></path>
                </svg>
                Configuración de Idioma
            </h3>
        </div>
        <div class="p-6">
            <div class="space-y-8">
                <!-- Language Selection -->
                <div class="bg-stone-700 rounded-lg p-6 border border-stone-600">
                    <div class="flex items-center justify-between">
                        <div class="flex-1">
                            <h4 class="text-base font-medium text-stone-200 mb-2">Idioma de la aplicación</h4>
                            <p class="text-sm text-stone-400 mb-4">Selecciona el idioma en el que se mostrará la interfaz de usuario</p>
                            <div class="flex items-center space-x-2 text-xs text-stone-500">
                                <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M13 16h-1v-4h-1m1-4h.01M21 12a9 9 0 11-18 0 9 9 0 0118 0z"></path>
                                </svg>
                                <span>Los cambios se aplicaran de forma inmediata</span>
                            </div>
                        </div>
                        <div class="ml-6">
                            <select class="bg-stone-600 border border-stone-500 text-stone-200 text-sm rounded-lg focus:ring-stone-500 focus:border-stone-500 block px-4 py-3 min-w-[140px]">
                                <option value="es">🇪🇸 Español</option>
                                <option value="en">🇺🇸 English</option>
                            </select>
                        </div>
                    </div>
                </div>

                <!-- Current Language Info -->
                <div class="bg-stone-700 rounded-lg p-4 border border-stone-600">
                    <div class="flex items-center space-x-3">
                        <div class="w-10 h-10 bg-stone-600 rounded-lg flex items-center justify-center">
                            <svg class="w-5 h-5 text-stone-400" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 12l2 2 4-4m6 2a9 9 0 11-18 0 9 9 0 0118 0z"></path>
                            </svg>
                        </div>
                        <div>
                            <p class="text-sm font-medium text-stone-200">Idioma actual</p>
                            <p class="text-sm text-stone-400">Español (es-ES)</p>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    </div>
</template>

<script setup lang="ts">
// Solo diseño visual - sin funcionalidad
</script> 