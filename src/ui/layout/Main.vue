<script setup>
import Sidebar from './sidebar.vue';
</script>

<template>
  <div class="app-container">
    <Sidebar />
    <main class="main-content">
      <!-- El contenido principal se agregara aqui -->
      <h1>Bienvenido a CubicLauncher</h1>
    </main>
  </div>
</template>

<style scoped>
.app-container {
  display: flex;
  min-height: 100vh;
  background-color: var(--background-dark);
  color: var(--text-color);
  overflow: hidden;
}

.main-content {
  flex: 1;
  padding: 20px;
  margin-left: 0;
  transition: margin-left 0.3s ease;
  overflow-y: auto;
  height: 100vh;
}

/* Ajustar el contenido principal cuando la barra lateral está colapsada */
:deep(.sidebar.collapsed) + .main-content {
  margin-left: 70px; /* Igual que --sidebar-collapsed-width */
}

/* Ajustar el contenido principal cuando la barra lateral está expandida */
:deep(.sidebar:not(.collapsed)) + .main-content {
  margin-left: 250px; /* Igual que --sidebar-width */
}
</style>