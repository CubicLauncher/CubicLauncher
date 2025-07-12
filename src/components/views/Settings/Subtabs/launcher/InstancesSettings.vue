<template>
  <div class="space-y-6">
    <!-- Instancias y Java -->
    <div class="bg-stone-800 border border-stone-600 rounded-lg">
      <div class="px-6 py-4 border-b border-stone-600">
        <h3 class="text-lg font-semibold text-stone-200 flex items-center gap-2">
          <svg class="w-5 h-5 text-stone-400" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M3 7v4a1 1 0 001 1h3m10-5h2a2 2 0 012 2v12a2 2 0 01-2 2H7a2 2 0 01-2-2V7a2 2 0 012-2h2"></path>
          </svg>
          Instancias y Java
        </h3>
      </div>
      <div class="p-6 space-y-6">
        <!-- Carpeta de Instancias -->
        <div class="flex items-center justify-between">
          <div>
            <p class="text-sm font-medium text-stone-200">Carpeta de instancias</p>
            <p class="text-sm text-stone-400">Selecciona la carpeta donde se guardarán las instancias de Minecraft</p>
          </div>
          <div class="flex items-center gap-2">
            <input type="text" class="bg-stone-700 border border-stone-600 text-stone-200 text-sm rounded-lg px-3 py-2 w-64" placeholder="C:/Users/Usuario/MinecraftInstances" disabled />
            <button class="px-3 py-2 rounded-md bg-stone-600 text-stone-200 hover:bg-stone-500 transition-colors text-sm">Cambiar</button>
          </div>
        </div>
        
        <!-- Ruta de Java -->
        <div class="flex items-center justify-between">
          <div>
            <p class="text-sm font-medium text-stone-200">Ruta de Java</p>
            <p class="text-sm text-stone-400">Selecciona la ruta del ejecutable de Java</p>
          </div>
          <div class="flex items-center gap-2">
            <input type="text" class="bg-stone-700 border border-stone-600 text-stone-200 text-sm rounded-lg px-3 py-2 w-64" placeholder="C:/Program Files/Java/bin/javaw.exe" disabled />
            <button class="px-3 py-2 rounded-md bg-stone-600 text-stone-200 hover:bg-stone-500 transition-colors text-sm">Buscar</button>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
// Lógica para manejar la configuración de instancias
</script>

<style scoped>
.fade-enter-active, .fade-leave-active {
  transition: opacity 0.2s;
}
.fade-enter-from, .fade-leave-to {
  opacity: 0;
}
</style> 