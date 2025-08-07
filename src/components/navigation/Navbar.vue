<script setup lang="ts">
import { ref, onMounted, onUnmounted } from 'vue'
import SettingsIcon from '../../assets/icons/UI/settings.vue'
import { navigationItems } from './navigationData'
import Logo from '../../assets/logo.svg'

const activeView = ref('home')
const isScrolled = ref(false)

const handleScroll = () => {
  isScrolled.value = window.scrollY > 10
}

onMounted(() => {
  window.addEventListener('scroll', handleScroll)
})

onUnmounted(() => {
  window.removeEventListener('scroll', handleScroll)
})
</script>

<template>
  <nav 
    :class="[
      'navbar bg-stone-900/95 backdrop-blur-sm border-b border-stone-700/50 flex items-center justify-between px-4 sm:px-6 py-3 sm:py-4 h-16 flex-shrink-0 titlebar sticky top-0 z-50 transition-all duration-300 ease-out',
      isScrolled ? 'shadow-lg bg-stone-900/98' : 'bg-stone-900/95'
    ]"
  >
    <!-- Logo Section -->
    <div class="flex items-center space-x-2 sm:space-x-3">
      <div class="w-7 h-7 sm:w-8 sm:h-8 flex items-center justify-center flex-shrink-0">
        <img :src="Logo" alt="CubicLauncher Logo" class="w-full h-full" />
      </div>
      <div class="hidden md:block">
        <h1 class="text-white font-medium text-lg tracking-tight">CubicLauncher</h1>
      </div>
    </div>

    <!-- Navigation Items -->
    <div class="flex items-center space-x-0.5 sm:space-x-1">
      <button 
        v-for="item in navigationItems" 
        :key="item.id"
        @click="activeView = item.id"
        :class="[
          'flex items-center space-x-1.5 sm:space-x-2.5 px-2 sm:px-3 md:px-4 py-2 sm:py-2.5 rounded-lg transition-all duration-200 text-left group navbar-button no-drag relative',
          activeView === item.id 
            ? 'bg-white/15 text-white border border-white/20' 
            : 'text-stone-400 hover:bg-white/5 hover:text-white'
        ]"
        :title="item.label"
      >
        <component :is="item.icon" class="w-4 h-4 flex-shrink-0 navbar-icon" />
        <span class="font-medium text-xs sm:text-sm hidden sm:block">{{ item.label }}</span>
        
        <!-- Active indicator -->
        <div 
          v-if="activeView === item.id"
          class="absolute bottom-0 left-1/2 transform -translate-x-1/2 w-6 h-0.5 bg-white rounded-full"
        ></div>
      </button>
    </div>

    <!-- Right Section: Settings + Window Controls -->
    <div class="flex items-center space-x-1 sm:space-x-2">
      <!-- Settings Button -->
      <button 
        class="flex items-center space-x-1.5 sm:space-x-2.5 px-2 sm:px-3 md:px-4 py-2 sm:py-2.5 rounded-lg text-stone-400 hover:bg-white/5 hover:text-white transition-all duration-200 text-left group navbar-button no-drag"
        title="Settings"
      >
        <SettingsIcon class="w-4 h-4 flex-shrink-0 navbar-icon" />
        <span class="font-medium text-xs sm:text-sm hidden sm:block">Settings</span>
      </button>

      <!-- Divider -->
      <div class="w-px h-5 sm:h-6 bg-stone-700/50 mx-0.5 sm:mx-1"></div>

      <!-- Window Controls -->
      <div class="flex items-center gap-0.5 sm:gap-1 titlebar-buttons">
        <button
          class="w-7 h-7 sm:w-8 sm:h-8 flex items-center justify-center text-stone-400 hover:text-stone-200 hover:bg-white/5 rounded-md transition-all duration-150 no-drag"
          aria-label="Minimize"
        >
          <svg width="12" height="12" sm:width="14" sm:height="14" viewBox="0 0 14 14" fill="none" class="w-3 h-3 sm:w-3.5 sm:h-3.5">
            <path
              d="M3 7h8"
              stroke="currentColor"
              stroke-width="1.5"
              stroke-linecap="round"
            />
          </svg>
        </button>
        <button
          class="w-7 h-7 sm:w-8 sm:h-8 flex items-center justify-center text-stone-400 hover:text-stone-200 hover:bg-white/5 rounded-md transition-all duration-150 no-drag"
          aria-label="Maximize"
        >
          <svg width="12" height="12" sm:width="14" sm:height="14" viewBox="0 0 14 14" fill="none" class="w-3 h-3 sm:w-3.5 sm:h-3.5">
            <rect
              x="3"
              y="3"
              width="8"
              height="8"
              stroke="currentColor"
              stroke-width="1.5"
              fill="none"
              rx="1"
            />
          </svg>
        </button>
        <button
          class="w-7 h-7 sm:w-8 sm:h-8 flex items-center justify-center text-stone-400 hover:text-red-400 hover:bg-red-500/10 rounded-md transition-all duration-150 no-drag"
          aria-label="Close"
        >
          <svg width="12" height="12" sm:width="14" sm:height="14" viewBox="0 0 14 14" fill="none" class="w-3 h-3 sm:w-3.5 sm:h-3.5">
            <path
              d="M3.5 3.5l7 7M10.5 3.5l-7 7"
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

@media (min-width: 641px) and (max-width: 768px) {
  .navbar {
    padding-left: 1rem;
    padding-right: 1rem;
  }
  
  .navbar-button {
    padding-left: 0.75rem;
    padding-right: 0.75rem;
  }
}

@media (min-width: 769px) {
  .navbar {
    padding-left: 1.5rem;
    padding-right: 1.5rem;
  }
  
  .navbar-button {
    padding-left: 1rem;
    padding-right: 1rem;
  }
}
</style> 