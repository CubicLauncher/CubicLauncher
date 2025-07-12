<template>
  <div class="space-y-6">
    <!-- Subtabs Navigation -->
    <div class="border-b border-stone-600">
      <nav class="flex space-x-8">
        <button
          v-for="tab in tabs"
          :key="tab.id"
          @click="activeTab = tab.id"
          class="py-2 px-1 border-b-2 font-medium text-sm transition-colors duration-200"
          :class="[
            activeTab === tab.id
              ? 'border-stone-400 text-stone-200'
              : 'border-transparent text-stone-400 hover:text-stone-300 hover:border-stone-500'
          ]"
        >
          <div class="flex items-center">
            <component :is="tab.icon" class="w-4 h-4 mr-2" />
            {{ tab.label }}
          </div>
        </button>
      </nav>
    </div>

    <!-- Tab Content -->
    <div class="min-h-[400px]">
      <!-- Instances Tab -->
      <div v-if="activeTab === 'instances'" class="space-y-6">
        <InstancesSettings />
      </div>

      <!-- Performance Tab -->
      <div v-if="activeTab === 'performance'" class="space-y-6">
        <PerformanceSettings />
      </div>

      <!-- Behavior Tab -->
      <div v-if="activeTab === 'behavior'" class="space-y-6">
        <BehaviorSettings />
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import InstancesSettings from '../Subtabs/launcher/InstancesSettings.vue'
import PerformanceSettings from '../Subtabs/launcher/PerformanceSettings.vue'
import BehaviorSettings from '../Subtabs/launcher/BehaviorSettings.vue'

const activeTab = ref('instances')

const tabs = [
  {
    id: 'instances',
    label: 'Instancias',
    icon: 'FolderIcon'
  },
  {
    id: 'performance',
    label: 'Rendimiento',
    icon: 'CpuIcon'
  },
  {
    id: 'behavior',
    label: 'Comportamiento',
    icon: 'SettingsIcon'
  }
]
</script>

<style scoped>
.fade-enter-active, .fade-leave-active {
  transition: opacity 0.2s;
}
.fade-enter-from, .fade-leave-to {
  opacity: 0;
}
</style> 