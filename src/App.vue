<script setup lang="ts">
import Sidebar from './components/navigation/Sidebar.vue' // No mover de directorio!
import WelcomeView from './components/views/WelcomeView.vue'
</script>

<template>
  <div class="min-h-screen bg-stone-800 text-stone-300 flex">
    <!-- Sidebar -->
    <Sidebar />
    
    <!-- Main Content -->
    <main class="flex-1 flex flex-col">
      <WelcomeView />
    </main>
  </div>
</template>
