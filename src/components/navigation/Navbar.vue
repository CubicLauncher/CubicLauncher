<script setup lang="ts">
import { ref, onMounted, onUnmounted } from 'vue'
import SettingsIcon from '../../assets/icons/UI/settings.vue'
import WindowControls from '../layout/WindowControls.vue'
import { navigationItems, injectNavigation } from './navigationData'
import Logo from '../../assets/logo.svg'

// Inject shared navigation state
const { activeView, setActiveView } = injectNavigation()

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
    class="flex items-center justify-between h-16 px-4 bg-stone-900/95 backdrop-blur-md border-b border-stone-600/50 sticky top-0 z-50 transition-all duration-300 ease-in-out select-none"
    :class="{ 'bg-stone-900/98 shadow-lg': isScrolled }"
    style="-webkit-app-region: drag;"
  >
    <!-- Logo Section -->
    <div class="flex items-center gap-3 flex-shrink-0">
      <img :src="Logo" alt="CubicLauncher Logo" class="w-8 h-8 flex-shrink-0" />
      <h1 class="text-lg font-medium text-white tracking-tight hidden md:block">CubicLauncher</h1>
    </div>

    <!-- Navigation Items -->
    <div class="flex items-center gap-1">
      <button 
        v-for="item in navigationItems" 
        :key="item.id"
        @click="setActiveView(item.id)"
        class="flex items-center gap-2 px-3 py-2 rounded-lg text-stone-400 bg-transparent border border-transparent cursor-pointer transition-all duration-200 ease-in-out hover:bg-white/5 hover:text-white"
        :class="{ 'bg-white/15 text-white border-white/20': activeView === item.id }"
        :title="item.label"
        style="-webkit-app-region: no-drag;"
      >
        <component :is="item.icon" class="w-4 h-4 flex-shrink-0" />
        <span class="text-sm font-medium hidden sm:block">{{ item.label }}</span>
      </button>
    </div>

    <!-- Right Section -->
    <div class="flex items-center gap-2">
      <!-- Settings Button -->
      <button 
        class="flex items-center gap-2 px-3 py-2 rounded-lg text-stone-400 bg-transparent border border-transparent cursor-pointer transition-all duration-200 ease-in-out hover:bg-white/5 hover:text-white"
        title="Settings"
        style="-webkit-app-region: no-drag;"
      >
        <SettingsIcon class="w-4 h-4" />
        <span class="text-sm font-medium hidden sm:block">Settings</span>
      </button>

      <!-- Window Controls -->
      <WindowControls />
    </div>
  </nav>
</template>

<style scoped>
/* Custom animations that Tailwind doesn't provide */
@keyframes slide-in-from-top-2 {
  from {
    opacity: 0;
    transform: translateY(-10px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.animate-in {
  animation-fill-mode: both;
}

.slide-in-from-top-2 {
  animation-name: slide-in-from-top-2;
}

/* Responsive adjustments */
@media (min-width: 640px) {
  .navbar {
    padding-left: 1.5rem;
    padding-right: 1.5rem;
  }
  
  .navbar__nav {
    gap: 0.5rem;
  }
  
  .navbar__nav-item,
  .navbar__player {
    padding-left: 1rem;
    padding-right: 1rem;
    gap: 0.75rem;
  }
}

@media (min-width: 768px) {
  .navbar__nav-item,
  .navbar__player {
    padding-left: 1.25rem;
    padding-right: 1.25rem;
  }
}
</style> 