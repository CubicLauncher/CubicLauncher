<script setup lang="ts">
import { ref } from 'vue'

// Mock data for instances
const instances = ref([
  {
    id: 1,
    name: 'Minecraft 1.20.4',
    version: '1.20.4',
    type: 'Vanilla',
    status: 'ready',
    lastPlayed: '2024-01-15T10:30:00Z',
    playtime: '45h 30m',
    icon: '🟢'
  },
  {
    id: 2,
    name: 'All the Mods 9',
    version: '1.20.1',
    type: 'Modpack',
    status: 'ready',
    lastPlayed: '2024-01-14T15:45:00Z',
    playtime: '82h 15m',
    icon: '🟣'
  },
  {
    id: 4,
    name: 'Better Minecraft',
    version: '1.19.2',
    type: 'Modpack',
    status: 'ready',
    lastPlayed: '2024-01-08T14:20:00Z',
    playtime: '23h 10m',
    icon: '🟡'
  }
])

const formatDate = (dateString: string) => {
  const date = new Date(dateString)
  const now = new Date()
  const diffTime = Math.abs(now.getTime() - date.getTime())
  const diffDays = Math.ceil(diffTime / (1000 * 60 * 60 * 24))
  
  if (diffDays === 1) return 'Ayer'
  if (diffDays < 7) return `Hace ${diffDays} días`
  return date.toLocaleDateString('es-ES', { day: 'numeric', month: 'short' })
}

const getStatusColor = (status: string) => {
  switch (status) {
    case 'ready': return 'text-green-400'
    case 'updating': return 'text-yellow-400'
    case 'error': return 'text-red-400'
    default: return 'text-gray-400'
  }
}

const getStatusText = (status: string) => {
  switch (status) {
    case 'ready': return 'Listo'
    case 'updating': return 'Actualizando'
    case 'error': return 'Error'
    default: return 'Desconocido'
  }
}

const playInstance = (instance: any) => {
  console.log(`Iniciando ${instance.name}...`)
  // TODO: Implement play functionality
}

const editInstance = (instance: any) => {
  console.log(`Editando ${instance.name}...`)
  // TODO: Implement edit functionality
}

const deleteInstance = (instance: any) => {
  console.log(`Eliminando ${instance.name}...`)
  // TODO: Implement delete functionality
}
</script>

<template>
  <div class="min-h-screen bg-stone-900 p-8">
    <!-- Header Section -->
    <div class="flex justify-between items-start mb-8">
      <div class="flex-1">
        <h1 class="text-3xl font-semibold text-white mb-2">Instancias</h1>
        <p class="text-stone-400 m-0">Gestiona tus instalaciones de Minecraft</p>
      </div>
      
      <div class="flex gap-3">
        <button class="flex items-center gap-2 px-6 py-3 rounded-lg font-medium border-none cursor-pointer transition-all duration-200 ease-in-out bg-stone-800/50 text-white border border-stone-700/50 hover:bg-stone-700/50 hover:border-stone-600/50">
          <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M4 16v1a3 3 0 003 3h10a3 3 0 003-3v-1m-4-4l-4 4m0 0l-4-4m4 4V4" />
          </svg>
          Importar
        </button>
        
        <button class="flex items-center gap-2 px-6 py-3 rounded-lg font-medium border-none cursor-pointer transition-all duration-200 ease-in-out bg-blue-500 text-white hover:bg-blue-600">
          <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 6v6m0 0v6m0-6h6m-6 0H6" />
          </svg>
          Nueva Instancia
        </button>
      </div>
    </div>

    <!-- Quick Stats -->
    <div class="flex gap-8 mb-8">
      <div class="text-center">
        <div class="text-3xl font-semibold text-white mb-1">{{ instances.length }}</div>
        <div class="text-stone-400 text-sm">Instancias</div>
      </div>
      
      <div class="text-center">
        <div class="text-3xl font-semibold text-white mb-1">{{ instances.filter(i => i.status === 'ready').length }}</div>
        <div class="text-stone-400 text-sm">Listas</div>
      </div>
      
      <div class="text-center">
        <div class="text-3xl font-semibold text-white mb-1">3</div>
        <div class="text-stone-400 text-sm">Versiones</div>
      </div>
    </div>

    <!-- Instances Grid -->
    <div class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 xl:grid-cols-4 gap-6">
      <div 
        v-for="instance in instances" 
        :key="instance.id"
        class="bg-stone-800/30 border border-stone-700/50 rounded-lg p-6 transition-all duration-200 ease-in-out hover:bg-stone-700/50 hover:border-stone-600/50"
        :class="{ 'bg-amber-900/10 border-amber-700/50': instance.status === 'updating' }"
      >
        <!-- Instance Header -->
        <div class="flex justify-between items-start mb-4">
          <div class="flex gap-3 items-center">
            <div class="text-2xl flex items-center justify-center w-10 h-10 bg-stone-800/50 rounded-lg">{{ instance.icon }}</div>
            <div>
              <h3 class="text-lg font-semibold text-white mb-1 m-0">{{ instance.name }}</h3>
              <div class="flex items-center gap-2 text-stone-400 text-sm">
                <span>{{ instance.version }}</span>
                <span class="text-stone-400/50">•</span>
                <span>{{ instance.type }}</span>
              </div>
            </div>
          </div>
          
          <div class="text-sm font-medium" :class="getStatusColor(instance.status)">
            {{ getStatusText(instance.status) }}
          </div>
        </div>

        <!-- Instance Stats -->
        <div class="flex gap-8 mb-6 py-4 border-t border-b border-stone-700/30">
          <div class="flex-1">
            <div class="text-stone-400 text-xs mb-1">Última vez</div>
            <div class="text-white font-medium text-sm">{{ formatDate(instance.lastPlayed) }}</div>
          </div>
          
          <div class="flex-1">
            <div class="text-stone-400 text-xs mb-1">Tiempo jugado</div>
            <div class="text-white font-medium text-sm">{{ instance.playtime }}</div>
          </div>
        </div>

        <!-- Instance Actions -->
        <div class="flex justify-between items-center">
          <button 
            @click="playInstance(instance)"
            class="flex items-center gap-2 px-5 py-3 bg-green-500 text-white border-none rounded-lg font-medium cursor-pointer transition-all duration-200 ease-in-out hover:bg-green-600 disabled:bg-stone-800/50 disabled:text-stone-400/70 disabled:cursor-not-allowed"
            :disabled="instance.status !== 'ready'"
          >
            <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M14.828 14.828a4 4 0 01-5.656 0M9 10h1m4 0h1m-6 4h.01M21 12a9 9 0 11-18 0 9 9 0 0118 0z" />
            </svg>
            {{ instance.status === 'ready' ? 'Jugar' : 'Actualizando...' }}
          </button>
          
          <div class="flex gap-2">
            <button 
              @click="editInstance(instance)"
              class="flex items-center justify-center w-9 h-9 bg-stone-800/50 text-stone-400 border-none rounded-md cursor-pointer transition-all duration-200 ease-in-out hover:bg-stone-700/50 hover:text-white"
              title="Editar"
            >
              <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M11 5H6a2 2 0 00-2 2v11a2 2 0 002 2h11a2 2 0 002-2v-5m-1.414-9.414a2 2 0 112.828 2.828L11.828 15H9v-2.828l8.586-8.586z" />
              </svg>
            </button>
            
            <button 
              @click="deleteInstance(instance)"
              class="flex items-center justify-center w-9 h-9 bg-stone-800/50 text-stone-400 border-none rounded-md cursor-pointer transition-all duration-200 ease-in-out hover:bg-red-500/20 hover:text-red-400"
              title="Eliminar"
            >
              <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M19 7l-.867 12.142A2 2 0 0116.138 21H7.862a2 2 0 01-1.995-1.858L5 7m5 4v6m4-6v6m1-10V4a1 1 0 00-1-1h-4a1 1 0 00-1 1v3M4 7h16" />
              </svg>
            </button>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>
