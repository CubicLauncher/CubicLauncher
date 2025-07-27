<script setup lang="ts">
// Component logic can be added here
</script> 

<template>
  <div class="flex-1 flex flex-col">
    <!-- Header -->
    <header class="p-6 border-b border-stone-600">
      <h2 class="text-2xl font-bold text-white">Welcome</h2>
      <p class="text-stone-400 mt-1">Your Minecraft launcher</p>
    </header>

    <!-- Main Content -->
    <div class="flex-1 p-8">
      <div class="max-w-4xl mx-auto">
        <!-- Hero Section -->
        <div class="text-center mb-12">
          <div class="w-24 h-24 bg-gradient-to-br from-green-400 to-green-600 rounded-2xl mx-auto mb-6 flex items-center justify-center">
            <span class="text-white font-bold text-3xl">C</span>
          </div>
          <h1 class="text-4xl font-bold text-white mb-4">CubicLauncher</h1>
          <p class="text-stone-400 text-lg">
            Modern Minecraft launcher
          </p>
        </div>

        <!-- Content Area -->
        <div class="bg-stone-700 rounded-xl p-8 border border-stone-600">
          <h3 class="text-white font-semibold text-lg mb-4">Content Area</h3>
          <p class="text-stone-400">Your content will go here...</p>
        </div>
      </div>
    </div>
  </div>
</template>