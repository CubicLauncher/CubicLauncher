<script setup lang="ts">
import { ref, provide } from 'vue'
import Navbar from './components/navigation/Navbar.vue' // No mover de directorio!
import WelcomeView from './components/views/WelcomeView.vue'
import InstancesView from './components/views/InstancesView.vue'

const activeView = ref('home')

// Provide activeView to child components
provide('activeView', activeView)

const setActiveView = (viewId: string) => {
  activeView.value = viewId
}

// Provide the setter function
provide('setActiveView', setActiveView)
</script>

<template>
  <div class="min-h-screen bg-stone-800 text-stone-300 flex flex-col">
    <!-- Navbar with integrated titlebar -->
    <Navbar />
    
    <!-- Main Content -->
    <main class="flex-1 flex flex-col">
      <WelcomeView v-if="activeView === 'home'" />
      <InstancesView v-else-if="activeView === 'instances'" />
    </main>
  </div>
</template>
