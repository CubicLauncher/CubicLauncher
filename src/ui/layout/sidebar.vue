<script setup lang="ts">
import { ref } from 'vue';

const navItems = [
  { id: 1, name: 'Inicio', icon: '🏠', path: '/home' },
  { id: 2, name: 'Jugar', icon: '🎮', path: '/play' },
  { id: 3, name: 'Mods', icon: '🧩', path: '/mods' },
  { id: 4, name: 'Ajustes', icon: '⚙️', path: '/settings' },
  { id: 5, name: 'Tienda', icon: '🛒', path: '/store' },
];

const activeItem = ref(1);
const isCollapsed = ref(false);

const toggleSidebar = () => {
  isCollapsed.value = !isCollapsed.value;
};
</script>

<template>
  <div class="sidebar" :class="{ 'collapsed': isCollapsed }">
    <div class="sidebar-header">
      <div class="logo">
        <span class="logo-icon">⚔️</span>
        <span class="logo-text" v-if="!isCollapsed">CubicLauncher</span>
      </div>
      <button class="toggle-btn" @click="toggleSidebar">
        {{ isCollapsed ? '→' : '←' }}
      </button>
    </div>
    
    <nav class="nav-menu">
      <div 
        v-for="item in navItems" 
        :key="item.id" 
        class="nav-item" 
        :class="{ 'active': activeItem === item.id }"
        @click="activeItem = item.id"
      >
        <span class="nav-icon">{{ item.icon }}</span>
        <span class="nav-text" v-if="!isCollapsed">{{ item.name }}</span>
      </div>
    </nav>
    
    <div class="user-profile">
      <div class="avatar">
        <span>👤</span>
      </div>
      <div class="user-info" v-if="!isCollapsed">
        <div class="username">Usuario</div>
        <div class="status">En línea</div>
      </div>
    </div>
  </div>
</template>