<template>
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
            <div class="space-y-8">
                <!-- Theme Selection -->
                <div class="bg-stone-700 rounded-lg p-6 border border-stone-600">
                    <div class="flex-1">
                        <h4 class="text-base font-medium text-stone-200 mb-2">Tema de la aplicación</h4>
                        <p class="text-sm text-stone-400 mb-4">Selecciona el modo de apariencia que prefieras</p>
                        <div class="flex items-center space-x-2 text-xs text-stone-500 mb-4">
                            <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M13 16h-1v-4h-1m1-4h.01M21 12a9 9 0 11-18 0 9 9 0 0118 0z"></path>
                            </svg>
                            <span>Los cambios se aplicaran de forma inmediata</span>
                        </div>
                    </div>
                    <div class="flex flex-col gap-3">
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

                <!-- Current Theme Info -->
                <div class="bg-stone-700 rounded-lg p-4 border border-stone-600">
                    <div class="flex items-center space-x-3">
                        <div class="w-10 h-10 bg-stone-600 rounded-lg flex items-center justify-center">
                            <svg class="w-5 h-5 text-stone-400" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 12l2 2 4-4m6 2a9 9 0 11-18 0 9 9 0 0118 0z"></path>
                            </svg>
                        </div>
                        <div>
                            <p class="text-sm font-medium text-stone-200">Tema actual</p>
                            <p class="text-sm text-stone-400">Oscuro</p>
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