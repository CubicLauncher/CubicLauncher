<script setup lang="ts">
import { ref, onMounted, onUnmounted } from 'vue'
import SettingsIcon from '../../assets/icons/UI/settings.vue'
import WindowControls from '../layout/WindowControls.vue'
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
  <nav class="navbar" :class="{ 'navbar--scrolled': isScrolled }">
    <!-- Logo Section -->
    <div class="navbar__logo">
      <img :src="Logo" alt="CubicLauncher Logo" class="navbar__logo-image" />
      <h1 class="navbar__logo-text">CubicLauncher</h1>
    </div>

    <!-- Navigation Items -->
    <div class="navbar__nav">
      <button 
        v-for="item in navigationItems" 
        :key="item.id"
        @click="activeView = item.id"
        class="navbar__nav-item"
        :class="{ 'navbar__nav-item--active': activeView === item.id }"
        :title="item.label"
      >
        <component :is="item.icon" class="navbar__nav-icon" />
        <span class="navbar__nav-label">{{ item.label }}</span>
      </button>
    </div>

    <!-- Right Section -->
    <div class="navbar__actions">
      <!-- Settings Button -->
      <button class="navbar__settings" title="Settings">
        <SettingsIcon class="navbar__nav-icon" />
        <span class="navbar__nav-label">Settings</span>
      </button>

      <!-- Window Controls -->
      <WindowControls />
    </div>
  </nav>
</template>

<style scoped>
/* Navbar Base */
.navbar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  height: 4rem;
  padding: 0 1rem;
  background: rgba(28, 25, 23, 0.95);
  backdrop-filter: blur(8px);
  border-bottom: 1px solid rgba(68, 64, 60, 0.5);
  position: sticky;
  top: 0;
  z-index: 50;
  transition: all 0.3s ease;
  
  /* Titlebar functionality */
  -webkit-app-region: drag;
  user-select: none;
}

.navbar--scrolled {
  background: rgba(28, 25, 23, 0.98);
  box-shadow: 0 10px 15px -3px rgba(0, 0, 0, 0.1);
}

/* Logo Section */
.navbar__logo {
  display: flex;
  align-items: center;
  gap: 0.75rem;
  flex-shrink: 0;
}

.navbar__logo-image {
  width: 2rem;
  height: 2rem;
  flex-shrink: 0;
}

.navbar__logo-text {
  font-size: 1.125rem;
  font-weight: 500;
  color: white;
  letter-spacing: -0.025em;
  display: none;
}

/* Navigation Items */
.navbar__nav {
  display: flex;
  align-items: center;
  gap: 0.25rem;
}

.navbar__nav-item {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  padding: 0.5rem 0.75rem;
  border-radius: 0.5rem;
  color: rgb(168, 162, 158);
  background: transparent;
  border: none;
  cursor: pointer;
  transition: all 0.2s ease;
  position: relative;
  
  /* Disable drag for buttons */
  -webkit-app-region: no-drag;
}

.navbar__nav-item:hover {
  background: rgba(255, 255, 255, 0.05);
  color: white;
}

.navbar__nav-item--active {
  background: rgba(255, 255, 255, 0.15);
  color: white;
  border: 1px solid rgba(255, 255, 255, 0.2);
}

.navbar__nav-icon {
  width: 1rem;
  height: 1rem;
  flex-shrink: 0;
}

.navbar__nav-label {
  font-size: 0.875rem;
  font-weight: 500;
  display: none;
}

/* Actions Section */
.navbar__actions {
  display: flex;
  align-items: center;
  gap: 0.5rem;
}

.navbar__settings {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  padding: 0.5rem 0.75rem;
  border-radius: 0.5rem;
  color: rgb(168, 162, 158);
  background: transparent;
  border: none;
  cursor: pointer;
  transition: all 0.2s ease;
  
  /* Disable drag for buttons */
  -webkit-app-region: no-drag;
}

.navbar__settings:hover {
  background: rgba(255, 255, 255, 0.05);
  color: white;
}

/* Window Controls - Handled by WindowControls component */

/* Responsive Design */
@media (min-width: 640px) {
  .navbar {
    padding: 0 1.5rem;
  }
  
  .navbar__nav {
    gap: 0.5rem;
  }
  
  .navbar__nav-item,
  .navbar__settings {
    padding: 0.625rem 1rem;
    gap: 0.75rem;
  }
  
  .navbar__nav-label {
    display: block;
  }
}

@media (min-width: 768px) {
  .navbar__logo-text {
    display: block;
  }
  
  .navbar__nav-item,
  .navbar__settings {
    padding: 0.625rem 1.25rem;
  }
}
</style> 