<script setup lang="ts">
import { ref } from 'vue'

// Settings state
const settings = ref({
  general: {
    autoStart: false,
    minimizeToTray: true,
    checkUpdates: true,
    language: 'es'
  },
  performance: {
    maxMemory: 4,
    javaPath: 'auto',
    enableOptimizations: true
  },
  appearance: {
    theme: 'dark',
    accentColor: 'blue',
    showAnimations: true
  },
  minecraft: {
    customJavaArgs: '',
    enableSnapshots: false,
    keepLauncherOpen: true
  }
})

const languages = [
  { code: 'es', name: 'Español' },
  { code: 'en', name: 'English' },
  { code: 'fr', name: 'Français' },
  { code: 'de', name: 'Deutsch' }
]

const themes = [
  { value: 'dark', name: 'Oscuro' },
  { value: 'light', name: 'Claro' },
  { value: 'auto', name: 'Automático' }
]

const accentColors = [
  { value: 'blue', name: 'Azul', class: 'bg-blue-500' },
  { value: 'green', name: 'Verde', class: 'bg-green-500' },
  { value: 'purple', name: 'Púrpura', class: 'bg-purple-500' },
  { value: 'amber', name: 'Ámbar', class: 'bg-amber-500' }
]

const memoryOptions = [2, 4, 6, 8, 12, 16]

const saveSettings = () => {
  console.log('Guardando configuración...', settings.value)
  // TODO: Implement settings save functionality
}

const resetSettings = () => {
  console.log('Restableciendo configuración...')
  // TODO: Implement settings reset functionality
}
</script>

<template>
  <div class="min-h-screen bg-stone-900 p-8">
    <!-- Header Section -->
    <div class="flex justify-between items-start mb-8">
      <div class="flex-1">
        <h1 class="text-3xl font-semibold text-white mb-2">Configuración</h1>
        <p class="text-stone-400 m-0">Personaliza tu experiencia de CubicLauncher</p>
      </div>
      
      <div class="flex gap-3">
        <button 
          @click="resetSettings"
          class="flex items-center gap-2 px-6 py-3 rounded-lg font-medium border-none cursor-pointer transition-all duration-200 ease-in-out bg-stone-800/50 text-white border border-stone-700/50 hover:bg-stone-700/50 hover:border-stone-600/50"
        >
          <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M4 4v5h.582m15.356 2A8.001 8.001 0 004.582 9m0 0H9m11 11v-5h-.581m0 0a8.003 8.003 0 01-15.357-2m15.357 2H15" />
          </svg>
          Restablecer
        </button>
        
        <button 
          @click="saveSettings"
          class="flex items-center gap-2 px-6 py-3 rounded-lg font-medium border-none cursor-pointer transition-all duration-200 ease-in-out bg-blue-500 text-white hover:bg-blue-600"
        >
          <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M5 13l4 4L19 7" />
          </svg>
          Guardar
        </button>
      </div>
    </div>

    <!-- Settings Grid -->
    <div class="grid grid-cols-1 lg:grid-cols-2 gap-8">
      <!-- General Settings -->
      <div class="bg-stone-800/30 border border-stone-700/50 rounded-lg p-6">
        <div class="flex items-center gap-3 mb-6">
          <div class="w-10 h-10 bg-blue-500/20 rounded-lg flex items-center justify-center">
            <svg class="w-5 h-5 text-blue-400" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M10.325 4.317c.426-1.756 2.924-1.756 3.35 0a1.724 1.724 0 002.573 1.066c1.543-.94 3.31.826 2.37 2.37a1.724 1.724 0 001.065 2.572c1.756.426 1.756 2.924 0 3.35a1.724 1.724 0 00-1.066 2.573c.94 1.543-.826 3.31-2.37 2.37a1.724 1.724 0 00-2.572 1.065c-.426 1.756-2.924 1.756-3.35 0a1.724 1.724 0 00-2.573-1.066c-1.543.94-3.31-.826-2.37-2.37a1.724 1.724 0 00-1.065-2.572c-1.756-.426-1.756-2.924 0-3.35a1.724 1.724 0 001.066-2.573c-.94-1.543.826-3.31 2.37-2.37.996.608 2.296.07 2.572-1.065z" />
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M15 12a3 3 0 11-6 0 3 3 0 016 0z" />
            </svg>
          </div>
          <h2 class="text-xl font-semibold text-white">General</h2>
        </div>
        
        <div class="space-y-4">
          <div class="flex items-center justify-between">
            <div>
              <div class="text-white font-medium">Iniciar con Windows</div>
              <div class="text-stone-400 text-sm">Iniciar automáticamente al arrancar el sistema</div>
            </div>
            <label class="relative inline-flex items-center cursor-pointer">
              <input type="checkbox" v-model="settings.general.autoStart" class="sr-only peer">
              <div class="w-11 h-6 bg-stone-700 peer-focus:outline-none rounded-full peer peer-checked:after:translate-x-full peer-checked:after:border-white after:content-[''] after:absolute after:top-[2px] after:left-[2px] after:bg-white after:rounded-full after:h-5 after:w-5 after:transition-all peer-checked:bg-blue-500"></div>
            </label>
          </div>
          
          <div class="flex items-center justify-between">
            <div>
              <div class="text-white font-medium">Minimizar a bandeja</div>
              <div class="text-stone-400 text-sm">Minimizar a la bandeja del sistema en lugar de cerrar</div>
            </div>
            <label class="relative inline-flex items-center cursor-pointer">
              <input type="checkbox" v-model="settings.general.minimizeToTray" class="sr-only peer">
              <div class="w-11 h-6 bg-stone-700 peer-focus:outline-none rounded-full peer peer-checked:after:translate-x-full peer-checked:after:border-white after:content-[''] after:absolute after:top-[2px] after:left-[2px] after:bg-white after:rounded-full after:h-5 after:w-5 after:transition-all peer-checked:bg-blue-500"></div>
            </label>
          </div>
          
          <div class="flex items-center justify-between">
            <div>
              <div class="text-white font-medium">Buscar actualizaciones</div>
              <div class="text-stone-400 text-sm">Verificar automáticamente nuevas versiones</div>
            </div>
            <label class="relative inline-flex items-center cursor-pointer">
              <input type="checkbox" v-model="settings.general.checkUpdates" class="sr-only peer">
              <div class="w-11 h-6 bg-stone-700 peer-focus:outline-none rounded-full peer peer-checked:after:translate-x-full peer-checked:after:border-white after:content-[''] after:absolute after:top-[2px] after:left-[2px] after:bg-white after:rounded-full after:h-5 after:w-5 after:transition-all peer-checked:bg-blue-500"></div>
            </label>
          </div>
          
          <div>
            <label class="block text-white font-medium mb-2">Idioma</label>
            <select v-model="settings.general.language" class="w-full bg-stone-800/50 border border-stone-700/50 rounded-lg px-4 py-2 text-white focus:outline-none focus:border-blue-500">
              <option v-for="lang in languages" :key="lang.code" :value="lang.code">{{ lang.name }}</option>
            </select>
          </div>
        </div>
      </div>

      <!-- Performance Settings -->
      <div class="bg-stone-800/30 border border-stone-700/50 rounded-lg p-6">
        <div class="flex items-center gap-3 mb-6">
          <div class="w-10 h-10 bg-green-500/20 rounded-lg flex items-center justify-center">
            <svg class="w-5 h-5 text-green-400" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M13 10V3L4 14h7v7l9-11h-7z" />
            </svg>
          </div>
          <h2 class="text-xl font-semibold text-white">Rendimiento</h2>
        </div>
        
        <div class="space-y-4">
          <div>
            <label class="block text-white font-medium mb-2">Memoria máxima (GB)</label>
            <select v-model="settings.performance.maxMemory" class="w-full bg-stone-800/50 border border-stone-700/50 rounded-lg px-4 py-2 text-white focus:outline-none focus:border-green-500">
              <option v-for="mem in memoryOptions" :key="mem" :value="mem">{{ mem }} GB</option>
            </select>
          </div>
          
          <div>
            <label class="block text-white font-medium mb-2">Ruta de Java</label>
            <select v-model="settings.performance.javaPath" class="w-full bg-stone-800/50 border border-stone-700/50 rounded-lg px-4 py-2 text-white focus:outline-none focus:border-green-500">
              <option value="auto">Detectar automáticamente</option>
              <option value="custom">Personalizada</option>
            </select>
          </div>
        </div>
      </div>

      <!-- Appearance Settings (Disabled) -->
      <div class="bg-stone-800/30 border border-stone-700/50 rounded-lg p-6 opacity-50 pointer-events-none relative">
        <!-- Disabled overlay -->
        <div class="absolute inset-0 bg-stone-900/20 rounded-lg flex items-center justify-center">
          <div class="bg-stone-800/90 px-4 py-2 rounded-lg border border-stone-600/50">
            <span class="text-stone-300 text-sm font-medium">Deshabilitado</span>
          </div>
        </div>
        
        <div class="flex items-center gap-3 mb-6">
          <div class="w-10 h-10 bg-purple-500/20 rounded-lg flex items-center justify-center">
            <svg class="w-5 h-5 text-purple-400" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M7 21a4 4 0 01-4-4V5a2 2 0 012-2h4a2 2 0 012 2v12a4 4 0 01-4 4zM21 5a2 2 0 00-2-2h-4a2 2 0 00-2 2v12a4 4 0 004 4h4a2 2 0 002-2V5z" />
            </svg>
          </div>
          <h2 class="text-xl font-semibold text-white">Apariencia</h2>
        </div>
        
        <div class="space-y-4">
          <div>
            <label class="block text-white font-medium mb-2">Tema</label>
            <select v-model="settings.appearance.theme" class="w-full bg-stone-800/50 border border-stone-700/50 rounded-lg px-4 py-2 text-white focus:outline-none focus:border-purple-500" disabled>
              <option v-for="theme in themes" :key="theme.value" :value="theme.value">{{ theme.name }}</option>
            </select>
          </div>
          
          <div>
            <label class="block text-white font-medium mb-2">Color de acento</label>
            <div class="grid grid-cols-4 gap-3">
              <button 
                v-for="color in accentColors" 
                :key="color.value"
                @click="settings.appearance.accentColor = color.value"
                class="flex items-center justify-center w-12 h-12 rounded-lg border-2 transition-all"
                :class="[
                  color.class,
                  settings.appearance.accentColor === color.value 
                    ? 'border-white' 
                    : 'border-stone-700/50 hover:border-stone-600/50'
                ]"
                :title="color.name"
                disabled
              >
                <svg v-if="settings.appearance.accentColor === color.value" class="w-6 h-6 text-white" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M5 13l4 4L19 7" />
                </svg>
              </button>
            </div>
          </div>
          
          <div class="flex items-center justify-between">
            <div>
              <div class="text-white font-medium">Animaciones</div>
              <div class="text-stone-400 text-sm">Mostrar animaciones y transiciones</div>
            </div>
            <label class="relative inline-flex items-center cursor-pointer">
              <input type="checkbox" v-model="settings.appearance.showAnimations" class="sr-only peer" disabled>
              <div class="w-11 h-6 bg-stone-700 peer-focus:outline-none rounded-full peer peer-checked:after:translate-x-full peer-checked:after:border-white after:content-[''] after:absolute after:top-[2px] after:left-[2px] after:bg-white after:rounded-full after:h-5 after:w-5 after:transition-all peer-checked:bg-purple-500"></div>
            </label>
          </div>
        </div>
      </div>

      <!-- Minecraft Settings -->
      <div class="bg-stone-800/30 border border-stone-700/50 rounded-lg p-6">
        <div class="flex items-center gap-3 mb-6">
          <div class="w-10 h-10 bg-amber-500/20 rounded-lg flex items-center justify-center">
            <svg class="w-5 h-5 text-amber-400" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M14.828 14.828a4 4 0 01-5.656 0M9 10h1m4 0h1m-6 4h.01M21 12a9 9 0 11-18 0 9 9 0 0118 0z" />
            </svg>
          </div>
          <h2 class="text-xl font-semibold text-white">Minecraft</h2>
        </div>
        
        <div class="space-y-4">
          <div>
            <label class="block text-white font-medium mb-2">Argumentos de Java personalizados</label>
            <textarea 
              v-model="settings.minecraft.customJavaArgs"
              placeholder="-XX:+UseG1GC -XX:+UnlockExperimentalVMOptions"
              class="w-full bg-stone-800/50 border border-stone-700/50 rounded-lg px-4 py-2 text-white focus:outline-none focus:border-amber-500 resize-none"
              rows="3"
            ></textarea>
          </div>
          
          <div class="flex items-center justify-between">
            <div>
              <div class="text-white font-medium">Snapshots</div>
              <div class="text-stone-400 text-sm">Mostrar versiones snapshot en la lista</div>
            </div>
            <label class="relative inline-flex items-center cursor-pointer">
              <input type="checkbox" v-model="settings.minecraft.enableSnapshots" class="sr-only peer">
              <div class="w-11 h-6 bg-stone-700 peer-focus:outline-none rounded-full peer peer-checked:after:translate-x-full peer-checked:after:border-white after:content-[''] after:absolute after:top-[2px] after:left-[2px] after:bg-white after:rounded-full after:h-5 after:w-5 after:transition-all peer-checked:bg-amber-500"></div>
            </label>
          </div>
          
          <div class="flex items-center justify-between">
            <div>
              <div class="text-white font-medium">Mantener launcher abierto</div>
              <div class="text-stone-400 text-sm">No cerrar el launcher al iniciar Minecraft</div>
            </div>
            <label class="relative inline-flex items-center cursor-pointer">
              <input type="checkbox" v-model="settings.minecraft.keepLauncherOpen" class="sr-only peer">
              <div class="w-11 h-6 bg-stone-700 peer-focus:outline-none rounded-full peer peer-checked:after:translate-x-full peer-checked:after:border-white after:content-[''] after:absolute after:top-[2px] after:left-[2px] after:bg-white after:rounded-full after:h-5 after:w-5 after:transition-all peer-checked:bg-amber-500"></div>
            </label>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>
