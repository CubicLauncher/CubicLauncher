<script setup lang="ts">
import Navbar from './components/navigation/Navbar.vue' // No mover de directorio!
import WelcomeView from './components/views/WelcomeView.vue'
import InstancesView from './components/views/InstancesView.vue'
import SettingsView from './components/views/SettingsView.vue'
import { provideNavigation, useNavigation } from './components/navigation/navigationData'

// Provide navigation state to child components
provideNavigation()

// Get navigation state for this component
const { activeView } = useNavigation()
</script>

<template>
  <div class="min-h-screen bg-stone-800 text-stone-300 flex flex-col">
    <!-- Navbar with integrated titlebar -->
    <Navbar />
    
    <!-- Main Content -->
    <main class="flex-1 flex flex-col">
      <WelcomeView v-if="activeView === 'home'" />
      <InstancesView v-else-if="activeView === 'instances'" />
      <SettingsView v-else-if="activeView === 'settings'" />
    </main>
  </div>
</template>
