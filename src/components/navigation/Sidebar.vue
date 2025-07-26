<template>
  <aside class="sidebar bg-stone-800 border-r border-stone-600 flex flex-col h-screen overflow-hidden transition-all duration-300 ease-in-out">
    <!-- Logo Section -->
    <div class="p-4 sm:p-6 border-b border-stone-600 flex-shrink-0">
      <div class="flex items-center space-x-3 sidebar-logo-container">
        <div class="w-8 h-8 sm:w-10 sm:h-10 bg-gradient-to-br from-green-400 to-green-600 rounded-lg flex items-center justify-center flex-shrink-0">
          <span class="text-white font-bold text-sm sm:text-lg">C</span>
        </div>
        <div class="min-w-0 flex-1 sidebar-text">
          <h1 class="text-white font-bold text-sm sm:text-lg truncate">CubicLauncher</h1>
          <p class="text-stone-400 text-xs sm:text-sm truncate">Minecraft</p>
        </div>
      </div>
    </div>

    <!-- Navigation -->
    <nav class="flex-1 p-2 sm:p-4 space-y-1 sm:space-y-2 overflow-y-auto">
      <button 
        v-for="item in navigationItems" 
        :key="item.id"
        @click="activeView = item.id"
        :class="[
          'w-full flex items-center space-x-2 sm:space-x-3 px-3 sm:px-4 py-2 sm:py-3 rounded-lg transition-all duration-200 text-left group sidebar-button',
          activeView === item.id 
            ? 'bg-stone-700 text-white border border-stone-500 shadow-sm' 
            : 'text-stone-400 hover:bg-stone-700 hover:text-white'
        ]"
        :title="item.label"
      >
        <component :is="item.icon" class="w-4 h-4 sm:w-5 sm:h-5 flex-shrink-0 sidebar-icon" />
        <span class="font-medium text-sm sm:text-base truncate sidebar-text">{{ item.label }}</span>
      </button>
    </nav>

    <!-- Bottom Section -->
    <div class="p-2 sm:p-4 border-t border-stone-600 flex-shrink-0">
      <button 
        class="w-full flex items-center space-x-2 sm:space-x-3 px-3 sm:px-4 py-2 sm:py-3 rounded-lg text-stone-400 hover:bg-stone-700 hover:text-white transition-all duration-200 text-left group sidebar-button"
        title="Settings"
      >
        <SettingsIcon class="w-4 h-4 sm:w-5 sm:h-5 flex-shrink-0 sidebar-icon" />
        <span class="font-medium text-sm sm:text-base truncate sidebar-text">Settings</span>
      </button>
    </div>
  </aside>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import HomeIcon from '../../assets/icons/UI/home.vue'
import ControllerIcon from '../../assets/icons/UI/controller.vue'
import SettingsIcon from '../../assets/icons/UI/settings.vue'

const activeView = ref('home')

const navigationItems = [
  {
    id: 'home',
    label: 'Home',
    icon: HomeIcon
  },
  {
    id: 'play',
    label: 'Play',
    icon: ControllerIcon
  }
]
</script>

<style scoped>
.sidebar {
  width: 256px;
  min-width: 256px;
}

/* Scrollbar Personalizada viejo */
nav::-webkit-scrollbar {
  width: 4px;
}

nav::-webkit-scrollbar-track {
  background: transparent;
}

nav::-webkit-scrollbar-thumb {
  background: #52525b;
  border-radius: 2px;
}

nav::-webkit-scrollbar-thumb:hover {
  background: #71717a;
}

/* Ensure smooth transitions */
* {
  transition-property: background-color, border-color, color, fill, stroke, opacity, box-shadow, transform, filter, backdrop-filter;
  transition-timing-function: cubic-bezier(0.4, 0, 0.2, 1);
  transition-duration: 200ms;
}

/* Collapse sidebar on smaller screens - show only icons */
@media (max-width: 1023px) {
  .sidebar {
    width: 72px;
    min-width: 72px;
  }
  
  .sidebar-text {
    opacity: 0;
    width: 0;
    overflow: hidden;
    margin-left: 0;
  }
  
  /* Center icons when collapsed */
  .sidebar-button {
    justify-content: center;
    padding-left: 0.75rem;
    padding-right: 0.75rem;
  }
  
  .sidebar-icon {
    margin: 0;
  }
  
  /* Adjust padding for collapsed state */
  .sidebar > div,
  .sidebar > nav {
    padding-left: 0.5rem;
    padding-right: 0.5rem;
  }
  
  /* Hide logo text, keep only icon */
  .sidebar > div > div > div:last-child {
    display: none;
  }
  
  /* Center the logo icon perfectly */
  .sidebar-logo-container {
    justify-content: center;
    width: 100%;
  }
  
  /* Remove space between logo icon and text when collapsed */
  .sidebar-logo-container > div:first-child {
    margin-right: 0;
  }
}

/* On very small screens, make it even more compact */
@media (max-width: 640px) {
  .sidebar {
    width: 64px;
    min-width: 64px;
  }
  
  .sidebar > div,
  .sidebar > nav {
    padding-left: 0.25rem;
    padding-right: 0.25rem;
  }
  
  .sidebar-button {
    padding-left: 0.5rem;
    padding-right: 0.5rem;
  }
}
</style> 