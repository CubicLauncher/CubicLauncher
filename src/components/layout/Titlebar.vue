<script setup lang="ts">
// Titlebar component - Visual design only
</script>

<template>
  <div class="titlebar bg-stone-900 border-b border-stone-700 flex items-center justify-between px-4 py-2 select-none">

    <!-- Center - Window title -->
    <div class="flex-1 text-center">
    </div>

    <!-- Right side - Window controls -->
    <div class="flex items-center space-x-1">
      <!-- Minimize button -->
      <button
        class="w-8 h-8 flex items-center justify-center rounded hover:bg-stone-700 transition-colors duration-200 group"
        title="Minimize"
      >
        <svg class="w-4 h-4 text-stone-400 group-hover:text-stone-200" fill="none" stroke="currentColor" viewBox="0 0 24 24">
          <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M20 12H4"/>
        </svg>
      </button>

      <!-- Maximize button -->
      <button
        class="w-8 h-8 flex items-center justify-center rounded hover:bg-stone-700 transition-colors duration-200 group"
        title="Maximize"
      >
        <svg class="w-4 h-4 text-stone-400 group-hover:text-stone-200" fill="none" stroke="currentColor" viewBox="0 0 24 24">
          <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M8 3v3a2 2 0 01-2 2H3m18 0h-3a2 2 0 01-2-2V3m0 18v-3a2 2 0 012-2h3M3 16h3a2 2 0 012 2v3"/>
        </svg>
      </button>

      <!-- Close button -->
      <button
        class="w-8 h-8 flex items-center justify-center rounded hover:bg-red-600 transition-colors duration-200 group"
        title="Close"
      >
        <svg class="w-4 h-4 text-stone-400 group-hover:text-white" fill="none" stroke="currentColor" viewBox="0 0 24 24">
          <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M6 18L18 6M6 6l12 12"/>
        </svg>
      </button>
    </div>
  </div>
</template>

<style scoped>
.titlebar {
  -webkit-app-region: drag;
}

.titlebar button {
  -webkit-app-region: no-drag;
}
</style> 