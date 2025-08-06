<script setup lang="ts">
import Navbar from './components/navigation/Navbar.vue' // No mover de directorio!
import WelcomeView from './components/views/WelcomeView.vue'
</script>

<template>
  <div class="min-h-screen bg-stone-800 text-stone-300 flex flex-col">
    <!-- Navbar with integrated titlebar -->
    <Navbar />
    
    <!-- Main Content -->
    <main class="flex-1 flex flex-col">
      <WelcomeView />
    </main>
  </div>
</template>
