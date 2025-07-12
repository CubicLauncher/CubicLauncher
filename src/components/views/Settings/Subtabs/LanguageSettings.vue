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
            <div class="space-y-6">
                <!-- Language Selection -->
                <div class="flex items-center justify-between">
                    <div>
                        <p class="text-sm font-medium text-stone-200">Idioma de la aplicación</p>
                        <p class="text-sm text-stone-400">Selecciona el idioma en el que se mostrará la interfaz</p>
                    </div>
                    <select class="bg-stone-700 border border-stone-600 text-stone-200 text-sm rounded-lg focus:ring-stone-500 focus:border-stone-500 block px-3 py-2">
                        <option value="es">Español</option>
                        <option value="en">English</option>
                    </select>
                </div>

                <!-- Language Preview -->
                <div class="bg-stone-700 rounded-lg p-4">
                    <h4 class="text-sm font-medium text-stone-200 mb-3">Vista previa del idioma</h4>
                    <div class="space-y-2 text-sm">
                        <div class="flex justify-between">
                            <span class="text-stone-400">Título:</span>
                            <span class="text-stone-200">Configuración</span>
                        </div>
                        <div class="flex justify-between">
                            <span class="text-stone-400">Botón:</span>
                            <span class="text-stone-200">Guardar</span>
                        </div>
                        <div class="flex justify-between">
                            <span class="text-stone-400">Mensaje:</span>
                            <span class="text-stone-200">Configuración guardada correctamente</span>
                        </div>
                    </div>
                </div>

                <!-- Additional Language Options -->
                <div class="space-y-4">
                    <h4 class="text-sm font-medium text-stone-200">Opciones adicionales</h4>
                    
                    <!-- Auto-detect Language -->
                    <div class="flex items-center justify-between">
                        <div>
                            <p class="text-sm font-medium text-stone-200">Detección automática</p>
                            <p class="text-sm text-stone-400">Detectar automáticamente el idioma del sistema</p>
                        </div>
                        <label class="relative inline-flex items-center cursor-pointer">
                            <input type="checkbox" class="sr-only peer">
                            <div class="w-11 h-6 bg-stone-600 peer-focus:outline-none peer-focus:ring-4 peer-focus:ring-stone-800 rounded-full peer peer-checked:after:translate-x-full peer-checked:after:border-white after:content-[''] after:absolute after:top-[2px] after:left-[2px] after:bg-white after:rounded-full after:h-5 after:w-5 after:transition-all peer-checked:bg-stone-400"></div>
                        </label>
                    </div>

                    <!-- Regional Format -->
                    <div class="flex items-center justify-between">
                        <div>
                            <p class="text-sm font-medium text-stone-200">Formato regional</p>
                            <p class="text-sm text-stone-400">Usar formato de fecha y números del idioma seleccionado</p>
                        </div>
                        <label class="relative inline-flex items-center cursor-pointer">
                            <input type="checkbox" checked class="sr-only peer">
                            <div class="w-11 h-6 bg-stone-400 peer-focus:outline-none peer-focus:ring-4 peer-focus:ring-stone-800 rounded-full peer peer-checked:after:translate-x-full peer-checked:after:border-white after:content-[''] after:absolute after:top-[2px] after:left-[2px] after:bg-white after:rounded-full after:h-5 after:w-5 after:transition-all peer-checked:bg-stone-400"></div>
                        </label>
                    </div>
                </div>
            </div>
        </div>
    </div>
</template>

<script setup lang="ts">
// Solo diseño visual - sin funcionalidad
</script> 
