<script setup lang="ts">
// Component logic can be added here
</script> 

<template>
  <div class="min-h-screen bg-gradient-to-br from-stone-900 via-stone-800 to-stone-900">
    <!-- Main Content -->
    <div class="container mx-auto px-6 py-8">

      <!-- Quick Actions -->
      <div class="grid md:grid-cols-4 gap-4 mb-8">
        <button class="bg-gradient-to-r from-green-500 to-emerald-600 hover:from-green-600 hover:to-emerald-700 text-white p-6 rounded-xl transition-all duration-300 transform hover:scale-105 shadow-lg">
          <div class="text-center">
            <svg class="w-8 h-8 mx-auto mb-2" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M14.828 14.828a4 4 0 01-5.656 0M9 10h1m4 0h1m-6 4h.01M21 12a9 9 0 11-18 0 9 9 0 0118 0z"></path>
            </svg>
            <div class="font-semibold">Jugar Ahora</div>
            <div class="text-sm opacity-90">Iniciar Minecraft</div>
          </div>
        </button>

        <button class="bg-stone-800 hover:bg-stone-700 text-white p-6 rounded-xl transition-all duration-300 border border-stone-700">
          <div class="text-center">
            <svg class="w-8 h-8 mx-auto mb-2" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M19 11H5m14 0a2 2 0 012 2v6a2 2 0 01-2 2H5a2 2 0 01-2-2v-6a2 2 0 012-2m14 0V9a2 2 0 00-2-2M5 11V9a2 2 0 012-2m0 0V5a2 2 0 012-2h6a2 2 0 012 2v2M7 7h10"></path>
            </svg>
            <div class="font-semibold">Instalar</div>
            <div class="text-sm opacity-90">Nuevas versiones</div>
          </div>
        </button>

        <button class="bg-stone-800 hover:bg-stone-700 text-white p-6 rounded-xl transition-all duration-300 border border-stone-700">
          <div class="text-center">
            <svg class="w-8 h-8 mx-auto mb-2" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M4 6a2 2 0 012-2h2a2 2 0 012 2v2a2 2 0 01-2 2H6a2 2 0 01-2-2V6zM14 6a2 2 0 012-2h2a2 2 0 012 2v2a2 2 0 01-2 2h-2a2 2 0 01-2-2V6zM4 16a2 2 0 012-2h2a2 2 0 012 2v2a2 2 0 01-2 2H6a2 2 0 01-2-2v-2zM14 16a2 2 0 012-2h2a2 2 0 012 2v2a2 2 0 01-2 2h-2a2 2 0 01-2-2v-2z"></path>
            </svg>
            <div class="font-semibold">Mods</div>
            <div class="text-sm opacity-90">Gestionar mods</div>
          </div>
        </button>

        <button class="bg-stone-800 hover:bg-stone-700 text-white p-6 rounded-xl transition-all duration-300 border border-stone-700">
          <div class="text-center">
            <svg class="w-8 h-8 mx-auto mb-2" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 6.253v13m0-13C10.832 5.477 9.246 5 7.5 5S4.168 5.477 3 6.253v13C4.168 18.477 5.754 18 7.5 18s3.332.477 4.5 1.253m0-13C13.168 5.477 14.754 5 16.5 5c1.746 0 3.332.477 4.5 1.253v13C19.832 18.477 18.246 18 16.5 18c-1.746 0-3.332.477-4.5 1.253"></path>
            </svg>
            <div class="font-semibold">Guías</div>
            <div class="text-sm opacity-90">Tutoriales</div>
          </div>
        </button>
      </div>

      <!-- Main Content Grid -->
      <div class="grid lg:grid-cols-3 gap-8">
        <!-- Recent Games -->
        <div class="lg:col-span-2">
          <div class="bg-stone-800/50 backdrop-blur-sm rounded-xl p-6 border border-stone-700/50">
            <div class="flex items-center justify-between mb-6">
              <h2 class="text-xl font-semibold text-white">Juegos Recientes</h2>
            </div>
            
            <div class="space-y-4">
              <div class="flex items-center space-x-4 p-4 bg-stone-700/50 rounded-lg hover:bg-stone-700/70 transition-colors cursor-pointer">
                <div class="w-12 h-12 bg-green-500/20 rounded-lg flex items-center justify-center">
                  <svg class="w-6 h-6 text-green-400" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M14.828 14.828a4 4 0 01-5.656 0M9 10h1m4 0h1m-6 4h.01M21 12a9 9 0 11-18 0 9 9 0 0118 0z"></path>
                  </svg>
                </div>
                <div class="flex-1">
                  <div class="text-white font-medium">Minecraft 1.20.1</div>
                  <div class="text-stone-400 text-sm">Último juego: hace 2 horas</div>
                </div>
                <button class="bg-green-600 hover:bg-green-700 text-white px-3 py-1 rounded text-sm transition-colors">
                  Jugar
                </button>
              </div>

              <div class="flex items-center space-x-4 p-4 bg-stone-700/50 rounded-lg hover:bg-stone-700/70 transition-colors cursor-pointer">
                <div class="w-12 h-12 bg-blue-500/20 rounded-lg flex items-center justify-center">
                  <svg class="w-6 h-6 text-blue-400" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M4 6a2 2 0 012-2h2a2 2 0 012 2v2a2 2 0 01-2 2H6a2 2 0 01-2-2V6zM14 6a2 2 0 012-2h2a2 2 0 012 2v2a2 2 0 01-2 2h-2a2 2 0 01-2-2V6zM4 16a2 2 0 012-2h2a2 2 0 012 2v2a2 2 0 01-2 2H6a2 2 0 01-2-2v-2zM14 16a2 2 0 012-2h2a2 2 0 012 2v2a2 2 0 01-2 2h-2a2 2 0 01-2-2v-2z"></path>
                  </svg>
                </div>
                <div class="flex-1">
                  <div class="text-white font-medium">Modpack FTB</div>
                  <div class="text-stone-400 text-sm">Último juego: ayer</div>
                </div>
                <button class="bg-blue-600 hover:bg-blue-700 text-white px-3 py-1 rounded text-sm transition-colors">
                  Jugar
                </button>
              </div>

              <div class="flex items-center space-x-4 p-4 bg-stone-700/50 rounded-lg hover:bg-stone-700/70 transition-colors cursor-pointer">
                <div class="w-12 h-12 bg-purple-500/20 rounded-lg flex items-center justify-center">
                  <svg class="w-6 h-6 text-purple-400" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M19 11H5m14 0a2 2 0 012 2v6a2 2 0 01-2 2H5a2 2 0 01-2-2v-6a2 2 0 012-2m14 0V9a2 2 0 00-2-2M5 11V9a2 2 0 012-2m0 0V5a2 2 0 012-2h6a2 2 0 012 2v2M7 7h10"></path>
                  </svg>
                </div>
                <div class="flex-1">
                  <div class="text-white font-medium">Fabric 1.19.4</div>
                  <div class="text-stone-400 text-sm">Último juego: hace 3 días</div>
                </div>
                <button class="bg-purple-600 hover:bg-purple-700 text-white px-3 py-1 rounded text-sm transition-colors">
                  Jugar
                </button>
              </div>
            </div>
          </div>
        </div>

        <!-- Sidebar -->
        <div class="space-y-6">
          <!-- System Status -->
          <div class="bg-stone-800/50 backdrop-blur-sm rounded-xl p-6 border border-stone-700/50">
            <h3 class="text-lg font-semibold text-white mb-4">Estado del Sistema</h3>
            <div class="space-y-3">
              <div class="flex justify-between items-center">
                <span class="text-stone-400">RAM Disponible</span>
                <span class="text-green-400 font-medium">8.2 GB</span>
              </div>
              <div class="flex justify-between items-center">
                <span class="text-stone-400">Java</span>
                <span class="text-green-400 font-medium">✓ Instalado</span>
              </div>
              <div class="flex justify-between items-center">
                <span class="text-stone-400">Conexión</span>
                <span class="text-green-400 font-medium">✓ Online</span>
              </div>
            </div>
          </div>

          <!-- Quick Stats -->
          <div class="bg-stone-800/50 backdrop-blur-sm rounded-xl p-6 border border-stone-700/50">
            <h3 class="text-lg font-semibold text-white mb-4">Estadísticas</h3>
            <div class="space-y-3">
              <div class="flex justify-between items-center">
                <span class="text-stone-400">Tiempo jugado</span>
                <span class="text-white font-medium">127 horas</span>
              </div>
              <div class="flex justify-between items-center">
                <span class="text-stone-400">Versiones</span>
                <span class="text-white font-medium">12 instaladas</span>
              </div>
              <div class="flex justify-between items-center">
                <span class="text-stone-400">Mods</span>
                <span class="text-white font-medium">45 activos</span>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>