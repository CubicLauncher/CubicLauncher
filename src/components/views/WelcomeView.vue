<script setup lang="ts">
// Component logic can be added here
</script> 

<template>
  <div class="min-h-screen bg-gradient-to-br from-stone-900 via-stone-800 to-stone-900">
    <!-- Main Content -->
    <div class="container mx-auto px-4 sm:px-6 py-6 sm:py-8">
      <!-- Quick Actions -->
      <div class="grid grid-cols-2 sm:grid-cols-4 gap-3 sm:gap-4 mb-6 sm:mb-8">
        <button class="bg-gradient-to-r from-green-500 to-emerald-600 hover:from-green-600 hover:to-emerald-700 text-white p-4 sm:p-6 rounded-xl transition-all duration-300 transform hover:scale-105 shadow-lg">
          <div class="text-center">
            <svg class="w-6 h-6 sm:w-8 sm:h-8 mx-auto mb-2" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M14.828 14.828a4 4 0 01-5.656 0M9 10h1m4 0h1m-6 4h.01M21 12a9 9 0 11-18 0 9 9 0 0118 0z"></path>
            </svg>
            <div class="font-semibold text-sm sm:text-base">Jugar Ahora</div>
            <div class="text-xs sm:text-sm opacity-90">Iniciar Minecraft</div>
          </div>
        </button>

        <button class="bg-stone-800 hover:bg-stone-700 text-white p-4 sm:p-6 rounded-xl transition-all duration-300 border border-stone-700">
          <div class="text-center">
            <svg class="w-6 h-6 sm:w-8 sm:h-8 mx-auto mb-2" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M19 11H5m14 0a2 2 0 012 2v6a2 2 0 01-2 2H5a2 2 0 01-2-2v-6a2 2 0 012-2m14 0V9a2 2 0 00-2-2M5 11V9a2 2 0 012-2m0 0V5a2 2 0 012-2h6a2 2 0 012 2v2M7 7h10"></path>
            </svg>
            <div class="font-semibold text-sm sm:text-base">Instalar</div>
            <div class="text-xs sm:text-sm opacity-90">Nuevas versiones</div>
          </div>
        </button>

        <button class="bg-stone-800 hover:bg-stone-700 text-white p-4 sm:p-6 rounded-xl transition-all duration-300 border border-stone-700">
          <div class="text-center">
            <svg class="w-6 h-6 sm:w-8 sm:h-8 mx-auto mb-2" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M4 6a2 2 0 012-2h2a2 2 0 012 2v2a2 2 0 01-2 2H6a2 2 0 01-2-2V6zM14 6a2 2 0 012-2h2a2 2 0 012 2v2a2 2 0 01-2 2h-2a2 2 0 01-2-2V6zM4 16a2 2 0 012-2h2a2 2 0 012 2v2a2 2 0 01-2 2H6a2 2 0 01-2-2v-2zM14 16a2 2 0 012-2h2a2 2 0 012 2v2a2 2 0 01-2 2h-2a2 2 0 01-2-2v-2z"></path>
            </svg>
            <div class="font-semibold text-sm sm:text-base">Mods</div>
            <div class="text-xs sm:text-sm opacity-90">Gestionar mods</div>
          </div>
        </button>

        <button class="bg-stone-800 hover:bg-stone-700 text-white p-4 sm:p-6 rounded-xl transition-all duration-300 border border-stone-700">
          <div class="text-center">
            <svg class="w-6 h-6 sm:w-8 sm:h-8 mx-auto mb-2" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 6.253v13m0-13C10.832 5.477 9.246 5 7.5 5S4.168 5.477 3 6.253v13C4.168 18.477 5.754 18 7.5 18s3.332.477 4.5 1.253m0-13C13.168 5.477 14.754 5 16.5 5c1.746 0 3.332.477 4.5 1.253v13C19.832 18.477 18.246 18 16.5 18c-1.746 0-3.332.477-4.5 1.253"></path>
            </svg>
            <div class="font-semibold text-sm sm:text-base">Guías</div>
            <div class="text-xs sm:text-sm opacity-90">Tutoriales</div>
          </div>
        </button>
      </div>

      <!-- Main Content Grid -->
      <div class="grid grid-cols-1 lg:grid-cols-3 gap-6 lg:gap-8">
        <!-- Content -->
      </div>
    </div>
  </div>
</template>