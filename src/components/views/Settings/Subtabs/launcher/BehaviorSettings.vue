<template>
  <div class="space-y-6">
    <!-- Ventana y comportamiento -->
    <div class="bg-stone-800 border border-stone-600 rounded-lg">
      <div class="px-6 py-4 border-b border-stone-600">
        <h3 class="text-lg font-semibold text-stone-200 flex items-center gap-2">
          <svg class="w-5 h-5 text-stone-400" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <rect width="20" height="14" x="2" y="5" rx="2" ry="2" stroke-width="2" stroke="currentColor" fill="none" />
          </svg>
          Ventana y comportamiento
        </h3>
      </div>
      <div class="p-6 space-y-6">
        <!-- Opciones de comportamiento -->
        <div class="flex flex-col gap-4">
          <label class="flex items-center gap-3">
            <input type="checkbox" class="accent-stone-600" disabled />
            <span class="text-stone-200 text-sm">Cerrar launcher al iniciar el juego</span>
          </label>
          <label class="flex items-center gap-3">
            <input type="checkbox" class="accent-stone-600" disabled />
            <span class="text-stone-200 text-sm">Mostrar logs avanzados</span>
          </label>
          <label class="flex items-center gap-3">
            <input type="checkbox" class="accent-stone-600" disabled />
            <span class="text-stone-200 text-sm">Minimizar al bandeja del sistema</span>
          </label>
          <label class="flex items-center gap-3">
            <input type="checkbox" class="accent-stone-600" disabled />
            <span class="text-stone-200 text-sm">Iniciar con Windows</span>
          </label>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
// Lógica para manejar la configuración de comportamiento
</script>

<style scoped>
.fade-enter-active, .fade-leave-active {
  transition: opacity 0.2s;
}
.fade-enter-from, .fade-leave-to {
  opacity: 0;
}
</style> 