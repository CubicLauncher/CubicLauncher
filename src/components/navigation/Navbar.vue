<script setup lang="ts">
import { ref, onMounted, onUnmounted } from 'vue'
import SettingsIcon from '../../assets/icons/UI/settings.vue'
import WindowControls from '../layout/WindowControls.vue'
import { navigationItems, injectNavigation } from './navigationData'
import { useAccountModal } from '../../composables/useAccountModal'
import Logo from '../../assets/logo.svg'

// Inject shared navigation state
const { activeView, setActiveView } = injectNavigation()

// Get account modal functions
const { currentAccount, openAccountModal } = useAccountModal()

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
    class="flex items-center justify-between h-16 px-3 sm:px-4 lg:px-6 bg-stone-900/95 backdrop-blur-md border-b border-stone-600/50 sticky top-0 z-50 transition-all duration-300 ease-in-out select-none"
    :class="{ 'bg-stone-900/98 shadow-lg': isScrolled }"
    style="-webkit-app-region: drag;"
  >
    <!-- Logo Section -->
    <div class="flex items-center gap-2 sm:gap-3 flex-shrink-0 min-w-0">
      <img :src="Logo" alt="CubicLauncher Logo" class="w-7 h-7 sm:w-8 sm:h-8 flex-shrink-0" />
      <h1 class="text-base sm:text-lg font-medium text-white tracking-tight hidden sm:block truncate">CubicLauncher</h1>
    </div>

    <!-- Navigation Items -->
    <div class="flex items-center gap-1 sm:gap-2 flex-shrink-0">
      <button 
        v-for="item in navigationItems" 
        :key="item.id"
        @click="setActiveView(item.id)"
        class="flex items-center gap-1.5 sm:gap-2 px-2 sm:px-3 py-2 rounded-lg text-stone-400 bg-transparent border border-transparent cursor-pointer transition-all duration-200 ease-in-out hover:bg-white/5 hover:text-white"
        :class="{ 'bg-white/15 text-white border-white/20': activeView === item.id }"
        :title="item.label"
        style="-webkit-app-region: no-drag;"
      >
        <component :is="item.icon" class="w-4 h-4 flex-shrink-0" />
        <span class="text-xs sm:text-sm font-medium hidden md:block">{{ item.label }}</span>
      </button>
    </div>

    <!-- Right Section -->
    <div class="flex items-center gap-1 sm:gap-2 flex-shrink-0">
      <!-- Current Account Indicator -->
      <button 
        v-if="currentAccount"
        @click="openAccountModal"
        class="flex items-center gap-1.5 sm:gap-2 px-2 sm:px-3 py-2 rounded-lg text-stone-300 bg-stone-800/50 border border-stone-600/50 cursor-pointer transition-all duration-200 ease-in-out hover:bg-stone-700/50 hover:border-stone-500/50"
        title="Cambiar cuenta"
        style="-webkit-app-region: no-drag;"
      >
                 <img 
           :src="currentAccount.skin || `https://crafatar.com/avatars/${currentAccount.id}?overlay`" 
           :alt="currentAccount.username"
           class="w-4 h-4 sm:w-5 sm:h-5 rounded"
           @error="(event) => { const target = event.target as HTMLImageElement; target.src = 'https://crafatar.com/avatars/8667ba71-b85a-4004-af54-457a9734eed7?overlay'; }"
         />
        <span class="text-xs sm:text-sm font-medium hidden lg:block truncate max-w-20">{{ currentAccount.username }}</span>
        <svg class="w-3 h-3 text-stone-400 hidden sm:block" fill="none" stroke="currentColor" viewBox="0 0 24 24">
          <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M19 9l-7 7-7-7"></path>
        </svg>
      </button>

      <!-- Settings Button -->
      <button 
        @click="setActiveView('settings')"
        class="flex items-center gap-1.5 sm:gap-2 px-2 sm:px-3 py-2 rounded-lg text-stone-400 bg-transparent border border-transparent cursor-pointer transition-all duration-200 ease-in-out hover:bg-white/5 hover:text-white"
        :class="{ 'bg-white/15 text-white border-white/20': activeView === 'settings' }"
        title="Settings"
        style="-webkit-app-region: no-drag;"
      >
        <SettingsIcon class="w-4 h-4" />
        <span class="text-xs sm:text-sm font-medium hidden md:block">Settings</span>
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

/* Ensure proper spacing and prevent overflow */
nav {
  min-width: 0;
}

/* Responsive adjustments for better spacing */
@media (max-width: 640px) {
  nav {
    padding-left: 0.75rem;
    padding-right: 0.75rem;
  }
}

@media (min-width: 640px) and (max-width: 768px) {
  nav {
    padding-left: 1rem;
    padding-right: 1rem;
  }
}

@media (min-width: 768px) and (max-width: 1024px) {
  nav {
    padding-left: 1.5rem;
    padding-right: 1.5rem;
  }
}

@media (min-width: 1024px) {
  nav {
    padding-left: 2rem;
    padding-right: 2rem;
  }
}

/* Ensure buttons don't overflow */
button {
  white-space: nowrap;
  overflow: hidden;
}

/* Prevent text overflow in account indicator */
.truncate {
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
</style> 