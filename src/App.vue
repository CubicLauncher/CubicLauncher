<script setup lang="ts">
import Sidebar from './components/navigation/Sidebar.vue' // No mover de directorio!
import WelcomeView from './components/views/WelcomeView.vue'
import Titlebar from './components/layout/Titlebar.vue'
</script>

<template>
  <div class="min-h-screen bg-stone-800 text-stone-300 flex flex-col">
    <!-- Titlebar -->
    <Titlebar />
    
    <!-- Main Content Area -->
    <div class="flex flex-1">
      <!-- Sidebar -->
      <Sidebar />
      
      <!-- Main Content -->
      <main class="flex-1 flex flex-col">
        <WelcomeView />
      </main>
    </div>
  </div>
</template>
