<template>
  <div class="window-controls">
    <button class="window-controls__button" @click="minimize" aria-label="Minimize">
      <svg viewBox="0 0 14 14" class="window-controls__icon">
        <path d="M3 7h8" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" />
      </svg>
    </button>
    
    <button class="window-controls__button" @click="maximize" aria-label="Maximize">
      <svg viewBox="0 0 14 14" class="window-controls__icon">
        <rect x="3" y="3" width="8" height="8" stroke="currentColor" stroke-width="1.5" fill="none" rx="1" />
      </svg>
    </button>
    
    <button class="window-controls__button window-controls__button--close" @click="close" aria-label="Close">
      <svg viewBox="0 0 14 14" class="window-controls__icon">
        <path d="M3.5 3.5l7 7M10.5 3.5l-7 7" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" />
      </svg>
    </button>
  </div>
</template>

<script setup lang="ts">
const minimize = () => {
  // TODO: Implement minimize functionality with Tauri
  console.log('Minimize window')
}

const maximize = () => {
  // TODO: Implement maximize functionality with Tauri
  console.log('Maximize window')
}

const close = () => {
  // TODO: Implement close functionality with Tauri
  console.log('Close window')
}
</script>

<style scoped>
.window-controls {
  display: flex;
  align-items: center;
  gap: 0.125rem;
  padding-left: 0.375rem;
  border-left: 1px solid rgba(68, 64, 60, 0.5);
  margin-left: 0.375rem;
  
  /* Disable drag for controls */
  -webkit-app-region: no-drag;
}

.window-controls__button {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 1.75rem;
  height: 1.75rem;
  color: rgb(168, 162, 158);
  background: transparent;
  border: none;
  border-radius: 0.25rem;
  cursor: pointer;
  transition: all 0.15s ease;
}

.window-controls__button:hover {
  background: rgba(255, 255, 255, 0.05);
  color: rgb(229, 231, 235);
}

.window-controls__button--close:hover {
  background: rgba(239, 68, 68, 0.1);
  color: rgb(248, 113, 113);
}

.window-controls__icon {
  width: 0.75rem;
  height: 0.75rem;
  fill: none;
}

/* Responsive Design */
@media (min-width: 640px) {
  .window-controls {
    gap: 0.25rem;
    padding-left: 0.5rem;
    margin-left: 0.5rem;
  }
  
  .window-controls__button {
    width: 2rem;
    height: 2rem;
    border-radius: 0.375rem;
  }
  
  .window-controls__icon {
    width: 0.875rem;
    height: 0.875rem;
  }
}

@media (min-width: 768px) {
  .window-controls {
    gap: 0.5rem;
  }
  
  .window-controls__button {
    width: 2.25rem;
    height: 2.25rem;
  }
  
  .window-controls__icon {
    width: 1rem;
    height: 1rem;
  }
}
</style>
