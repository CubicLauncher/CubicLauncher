<template>
    <div class="space-y-6">
        <!-- Theme Selection -->
        <div class="bg-stone-800 border border-stone-600 rounded-lg">
            <div class="px-6 py-4 border-b border-stone-600">
                <h3 class="text-lg font-semibold text-stone-200 flex items-center">
                    <svg class="w-5 h-5 mr-2 text-stone-400" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                        <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M7 21a4 4 0 01-4-4V5a2 2 0 012-2h4a2 2 0 012 2v12a4 4 0 01-4 4zm0 0h12a2 2 0 002-2v-4a2 2 0 00-2-2h-2.343M11 7.343l1.657-1.657a2 2 0 012.828 0l2.829 2.829a2 2 0 010 2.828l-8.486 8.485M7 17h.01"></path>
                    </svg>
                    Modo de Apariencia
                </h3>
            </div>
            <div class="p-6">
                <p class="text-sm font-medium text-stone-200 mb-4">Selecciona el tema de la aplicación</p>
                <div class="flex flex-col gap-2">
                    <label class="flex items-center justify-between cursor-pointer rounded-lg border transition-colors duration-200 px-4 py-3 group border-stone-600 bg-stone-800 hover:bg-stone-700">
                        <div class="flex items-center gap-4">
                            <span class="w-7 h-7 rounded-full border border-stone-500 bg-stone-100" />
                            <div>
                                <div class="text-stone-200 font-medium">Claro</div>
                                <div class="text-xs text-stone-400">Fondo claro, ideal para el día</div>
                            </div>
                        </div>
                        <input type="radio" name="theme" value="light" class="sr-only" />
                    </label>

                    <label class="flex items-center justify-between cursor-pointer rounded-lg border transition-colors duration-200 px-4 py-3 group border-stone-400 bg-stone-700">
                        <div class="flex items-center gap-4">
                            <span class="w-7 h-7 rounded-full border border-stone-500 bg-stone-900" />
                            <div>
                                <div class="text-stone-200 font-medium">Oscuro</div>
                                <div class="text-xs text-stone-400">Fondo oscuro, ideal para la noche</div>
                            </div>
                        </div>
                        <input type="radio" name="theme" value="dark" checked class="sr-only" />
                        <span class="flex items-center justify-center w-6 h-6 rounded-full bg-stone-600">
                            <svg class="w-4 h-4 text-stone-200" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M5 13l4 4L19 7" />
                            </svg>
                        </span>
                    </label>

                    <label class="flex items-center justify-between cursor-pointer rounded-lg border transition-colors duration-200 px-4 py-3 group border-stone-600 bg-stone-800 hover:bg-stone-700">
                        <div class="flex items-center gap-4">
                            <span class="w-7 h-7 rounded-full border border-stone-500 bg-gradient-to-r from-stone-100 to-stone-900" />
                            <div>
                                <div class="text-stone-200 font-medium">Automático</div>
                                <div class="text-xs text-stone-400">Se adapta a tu sistema</div>
                            </div>
                        </div>
                        <input type="radio" name="theme" value="auto" class="sr-only" />
                    </label>
                </div>
            </div>
        </div>

        <!-- Color Customization -->
        <div class="bg-stone-800 border border-stone-600 rounded-lg">
            <div class="px-6 py-4 border-b border-stone-600">
                <h3 class="text-lg font-semibold text-stone-200 flex items-center">
                    <svg class="w-5 h-5 mr-2 text-stone-400" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                        <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M7 21a4 4 0 01-4-4V5a2 2 0 012-2h4a2 2 0 012 2v12a4 4 0 01-4 4zm0 0h12a2 2 0 002-2v-4a2 2 0 00-2-2h-2.343M11 7.343l1.657-1.657a2 2 0 012.828 0l2.829 2.829a2 2 0 010 2.828l-8.486 8.485M7 17h.01"></path>
                    </svg>
                    Personalización de Colores
                </h3>
            </div>
            <div class="p-6">
                <div class="space-y-6">
                    <!-- Accent Color -->
                    <div>
                        <label class="block text-sm font-medium text-stone-200 mb-3">Color de acento</label>
                        <div class="flex items-center gap-4">
                            <div class="flex gap-2">
                                <button class="w-8 h-8 rounded-full border-2 border-stone-600 bg-blue-500 transition-all" title="Azul"></button>
                                <button class="w-8 h-8 rounded-full border-2 border-stone-600 bg-green-500 transition-all" title="Verde"></button>
                                <button class="w-8 h-8 rounded-full border-2 border-stone-600 bg-purple-500 transition-all" title="Púrpura"></button>
                                <button class="w-8 h-8 rounded-full border-2 border-stone-300 scale-110 bg-red-500 transition-all" title="Rojo"></button>
                                <button class="w-8 h-8 rounded-full border-2 border-stone-600 bg-orange-500 transition-all" title="Naranja"></button>
                                <button class="w-8 h-8 rounded-full border-2 border-stone-600 bg-pink-500 transition-all" title="Rosa"></button>
                                <button class="w-8 h-8 rounded-full border-2 border-stone-600 bg-teal-500 transition-all" title="Verde azulado"></button>
                                <button class="w-8 h-8 rounded-full border-2 border-stone-600 bg-indigo-500 transition-all" title="Índigo"></button>
                            </div>
                            <span class="text-sm text-stone-400">Rojo</span>
                        </div>
                    </div>

                    <!-- Transparency -->
                    <div>
                        <label class="block text-sm font-medium text-stone-200 mb-3">
                            Transparencia de la ventana
                            <span class="text-xs text-stone-400 ml-2">25%</span>
                        </label>
                        <input type="range" value="25" min="0" max="100" class="w-full h-2 bg-stone-600 rounded-lg appearance-none cursor-pointer slider" />
                        <div class="flex justify-between text-xs text-stone-400 mt-1">
                            <span>Opaco</span>
                            <span>Transparente</span>
                        </div>
                    </div>

                    <!-- Blur Effect -->
                    <div class="flex items-center justify-between">
                        <div>
                            <p class="text-sm font-medium text-stone-200">Efecto de desenfoque</p>
                            <p class="text-sm text-stone-400">Aplicar desenfoque al fondo de la ventana</p>
                        </div>
                        <label class="relative inline-flex items-center cursor-pointer">
                            <input type="checkbox" checked class="sr-only peer">
                            <div class="w-11 h-6 bg-stone-400 peer-focus:outline-none peer-focus:ring-4 peer-focus:ring-stone-800 rounded-full peer peer-checked:after:translate-x-full peer-checked:after:border-white after:content-[''] after:absolute after:top-[2px] after:left-[2px] after:bg-white after:rounded-full after:h-5 after:w-5 after:transition-all peer-checked:bg-stone-400"></div>
                        </label>
                    </div>
                </div>
            </div>
        </div>

        <!-- Theme Preview -->
        <div class="bg-stone-800 border border-stone-600 rounded-lg">
            <div class="px-6 py-4 border-b border-stone-600">
                <h3 class="text-lg font-semibold text-stone-200">Vista previa del tema</h3>
            </div>
            <div class="p-6">
                <div class="bg-stone-700 rounded-lg p-4 border border-stone-600">
                    <div class="flex items-center justify-between mb-4">
                        <h4 class="text-stone-200 font-medium">Ejemplo de interfaz</h4>
                        <button class="px-3 py-1 bg-stone-600 text-stone-200 text-sm rounded hover:bg-stone-500 transition-colors">
                            Botón de ejemplo
                        </button>
                    </div>
                    <div class="space-y-2">
                        <div class="h-3 bg-stone-600 rounded"></div>
                        <div class="h-3 bg-stone-600 rounded w-3/4"></div>
                        <div class="h-3 bg-stone-600 rounded w-1/2"></div>
                    </div>
                </div>
            </div>
        </div>
    </div>
</template>

<script setup lang="ts">
// Solo diseño visual - sin funcionalidad
</script>

<style scoped>
.slider::-webkit-slider-thumb {
    appearance: none;
    height: 16px;
    width: 16px;
    border-radius: 50%;
    background: #a8a29e;
    cursor: pointer;
}

.slider::-moz-range-thumb {
    height: 16px;
    width: 16px;
    border-radius: 50%;
    background: #a8a29e;
    cursor: pointer;
    border: none;
}
</style> 
