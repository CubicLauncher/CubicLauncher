<script setup lang="ts">
import { ref } from 'vue'
import SettingsIcon from '../../assets/icons/UI/settings.vue'
import { navigationItems } from './navigationData'
import Logo from '../../assets/logo.svg'

const activeView = ref('home')
</script>

<template>
  <nav class="navbar bg-stone-800 border-b border-stone-600 flex items-center justify-between px-4 py-3 h-16 flex-shrink-0 titlebar">
    <!-- Logo Section -->
    <div class="flex items-center space-x-3">
      <div class="w-8 h-8 flex items-center justify-center flex-shrink-0">
        <img :src="Logo" alt="CubicLauncher Logo" class="w-full h-full" />
      </div>
      <div class="hidden sm:block">
        <h1 class="text-white font-semibold text-lg">CubicLauncher</h1>
      </div>
    </div>

    <!-- Navigation Items -->
    <div class="flex items-center space-x-1 sm:space-x-2">
      <button 
        v-for="item in navigationItems" 
        :key="item.id"
        @click="activeView = item.id"
        :class="[
          'flex items-center space-x-2 px-3 sm:px-4 py-2 rounded-lg transition-all duration-200 text-left group navbar-button no-drag',
          activeView === item.id 
            ? 'bg-stone-700 text-white border border-stone-500 shadow-sm' 
            : 'text-stone-400 hover:bg-stone-700 hover:text-white'
        ]"
        :title="item.label"
      >
        <component :is="item.icon" class="w-4 h-4 sm:w-5 sm:h-5 flex-shrink-0 navbar-icon" />
        <span class="font-medium text-sm sm:text-base hidden sm:block">{{ item.label }}</span>
      </button>
    </div>

    <!-- Right Section: Settings + Window Controls -->
    <div class="flex items-center space-x-2">
      <!-- Settings Button -->
      <button 
        class="flex items-center space-x-2 px-3 sm:px-4 py-2 rounded-lg text-stone-400 hover:bg-stone-700 hover:text-white transition-all duration-200 text-left group navbar-button no-drag"
        title="Settings"
      >
        <SettingsIcon class="w-4 h-4 sm:w-5 sm:h-5 flex-shrink-0 navbar-icon" />
        <span class="font-medium text-sm sm:text-base hidden sm:block">Settings</span>
      </button>

      <!-- Window Controls -->
      <div class="flex items-center gap-0.5 titlebar-buttons">
        <button
          class="w-7 h-7 flex items-center justify-center text-stone-400 hover:text-stone-200 hover:bg-stone-700 rounded-sm transition-all duration-75 no-drag"
          aria-label="Minimize"
        >
          <svg width="12" height="12" viewBox="0 0 12 12">
            <path
              d="M3 6h6"
              stroke="currentColor"
              stroke-width="1.5"
              stroke-linecap="round"
            />
          </svg>
        </button>
        <button
          class="w-7 h-7 flex items-center justify-center text-stone-400 hover:text-stone-200 hover:bg-stone-700 rounded-sm transition-all duration-75 no-drag"
          aria-label="Maximize"
        >
          <svg width="12" height="12" viewBox="0 0 12 12">
            <rect
              x="3"
              y="3"
              width="6"
              height="6"
              stroke="currentColor"
              stroke-width="1.5"
              fill="none"
              rx="1"
            />
          </svg>
        </button>
        <button
          class="w-7 h-7 flex items-center justify-center text-stone-400 hover:text-stone-200 hover:bg-stone-600 rounded-sm transition-all duration-75 no-drag"
          aria-label="Close"
        >
          <svg width="12" height="12" viewBox="0 0 12 12">
            <path
              d="M3 3l6 6M9 3l-6 6"
              stroke="currentColor"
              stroke-width="1.5"
              stroke-linecap="round"
            />
          </svg>
        </button>
      </div>
    </div>
  </nav>
</template>

<style scoped>
.navbar {
  min-height: 64px;
}

/* Titlebar functionality */
.titlebar {
  -webkit-app-region: drag;
  user-select: none;
  cursor: default;
}

.titlebar-buttons,
.no-drag {
  -webkit-app-region: no-drag;
}

/* Mejor feedback visual */
.titlebar:hover {
  cursor: move;
}

.titlebar-buttons:hover {
  cursor: default;
}

/* Asegurar que los botones tengan el cursor correcto */
.titlebar-buttons button {
  cursor: pointer;
}

/* Transiciones suaves */
* {
  transition-property: background-color, border-color, color, fill, stroke, opacity, box-shadow, transform, filter, backdrop-filter;
  transition-timing-function: cubic-bezier(0.4, 0, 0.2, 1);
  transition-duration: 200ms;
}

/* Responsive adjustments */
@media (max-width: 640px) {
  .navbar {
    padding-left: 0.75rem;
    padding-right: 0.75rem;
  }
  
  .navbar-button {
    padding-left: 0.5rem;
    padding-right: 0.5rem;
  }
}
</style> 