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
            <!-- Language Tab -->
            <div v-if="activeTab === 'language'" class="space-y-6">
                <LanguageSettings />
            </div>

            <!-- Theme Tab -->
            <div v-if="activeTab === 'theme'" class="space-y-6">
                <ThemeSettings />
            </div>

            <!-- Information Tab -->
            <div v-if="activeTab === 'information'" class="space-y-6">
                <InformationSettings />
            </div>
        </div>
    </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import LanguageSettings from '../Subtabs/general/LanguageSettings.vue'
import ThemeSettings from '../Subtabs/general/ThemeSettings.vue'
import InformationSettings from '../Subtabs/general/InformationSettings.vue'

const activeTab = ref('language')

const tabs = [
    {
        id: 'language',
        label: 'Idioma',
        icon: 'LanguageIcon'
    },
    {
        id: 'theme',
        label: 'Tema',
        icon: 'ThemeIcon'
    },
    {
        id: 'information',
        label: 'Información',
        icon: 'InfoIcon'
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
