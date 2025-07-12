<template>
  <div class="space-y-6">
    <!-- Rendimiento -->
    <div class="bg-stone-800 border border-stone-600 rounded-lg">
      <div class="px-6 py-4 border-b border-stone-600">
        <h3 class="text-lg font-semibold text-stone-200 flex items-center gap-2">
          <svg class="w-5 h-5 text-stone-400" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M4 6h16M4 10h16M4 14h16M4 18h16"></path>
          </svg>
          Rendimiento
        </h3>
      </div>
      <div class="p-6 space-y-6">
        <!-- Memoria RAM -->
        <div class="flex items-center justify-between">
          <div>
            <p class="text-sm font-medium text-stone-200">Memoria RAM asignada</p>
            <p class="text-sm text-stone-400">Elige cuánta memoria RAM se asignará a Minecraft</p>
          </div>
          <div class="flex items-center gap-2">
            <input type="range" min="2" max="16" value="4" class="w-40 accent-stone-600" disabled />
            <span class="text-stone-200 text-sm">4 GB</span>
          </div>
        </div>
        
        <!-- Parámetros JVM -->
        <div class="flex items-center justify-between">
          <div>
            <p class="text-sm font-medium text-stone-200">Parámetros JVM adicionales</p>
            <p class="text-sm text-stone-400">Agrega parámetros personalizados para Java</p>
          </div>
          <input type="text" class="bg-stone-700 border border-stone-600 text-stone-200 text-sm rounded-lg px-3 py-2 w-96" placeholder="-Xmx4G -XX:+UnlockExperimentalVMOptions" disabled />
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
// Lógica para manejar la configuración de rendimiento
</script>

<style scoped>
.fade-enter-active, .fade-leave-active {
  transition: opacity 0.2s;
}
.fade-enter-from, .fade-leave-to {
  opacity: 0;
}
</style> 