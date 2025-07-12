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
import LanguageSettings from '../Subtabs/LanguageSettings.vue'
import ThemeSettings from '../Subtabs/ThemeSettings.vue'
import InformationSettings from '../Subtabs/InformationSettings.vue'

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

// Icon components
const LanguageIcon = {
    template: `
        <svg fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M3 5h12M9 3v2m1.048 9.5A18.022 18.022 0 016.412 9m6.088 9h7M11 21l5-10 5 10M12.751 5C11.783 10.77 8.07 15.61 3 18.129"></path>
        </svg>
    `
}

const ThemeIcon = {
    template: `
        <svg fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M7 21a4 4 0 01-4-4V5a2 2 0 012-2h4a2 2 0 012 2v12a4 4 0 01-4 4zm0 0h12a2 2 0 002-2v-4a2 2 0 00-2-2h-2.343M11 7.343l1.657-1.657a2 2 0 012.828 0l2.829 2.829a2 2 0 010 2.828l-8.486 8.485M7 17h.01"></path>
        </svg>
    `
}

const InfoIcon = {
    template: `
        <svg fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M13 16h-1v-4h-1m1-4h.01M21 12a9 9 0 11-18 0 9 9 0 0118 0z"></path>
        </svg>
    `
}
</script>

<style scoped>
.fade-enter-active, .fade-leave-active {
  transition: opacity 0.2s;
}
.fade-enter-from, .fade-leave-to {
  opacity: 0;
}
</style> 
