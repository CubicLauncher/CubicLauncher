<template>
  <div class="flex h-full min-h-0">
    <!-- Vertical Tabs -->
    <div 
      :class="[
        'border-r border-[#272727ff] bg-stone-900 flex flex-col transition-all duration-300 ease-in-out',
        isSidebarCollapsed ? 'w-16' : 'w-64'
      ]"
    >
      <div class="flex-shrink-0 p-4 border-b border-[#272727ff]">
        <h1 
          class="text-xl font-semibold text-[#d6d2d2ff] transition-all duration-300"
          :class="{ 'text-center text-lg': isSidebarCollapsed }"
        >
          <span v-if="!isSidebarCollapsed">
            {{ languageStore.getTranslation('Launcher.settings.title') }}
          </span>
          <component v-else :is="settings" class="w-6 h-6" />
        </h1>
      </div>
      
      <nav class="flex-1 flex flex-col space-y-0.5 p-2 overflow-y-auto" aria-label="Tabs">
        <button 
          v-for="tab in tabs" 
          :key="tab.id" 
          @click="activeTab = tab.id" 
          :class="[
            activeTab === tab.id
              ? 'bg-[#272727ff] text-[#d6d2d2ff] border-l-2 border-[#78716c]'
              : 'text-[#d6d2d2ff]/60 hover:text-[#d6d2d2ff] hover:bg-[#272727ff]',
            'w-full text-left px-4 py-3 text-sm font-medium transition-all duration-200 flex items-center gap-3 flex-shrink-0',
            isSidebarCollapsed ? 'justify-center px-2' : ''
          ]"
          :title="isSidebarCollapsed ? tab.name : ''"
        >
          <!-- Vue Icon Component -->
          <component 
            :is="tab.icon" 
            class="w-5 h-5 flex-shrink-0"
          />
          
          <span 
            v-if="!isSidebarCollapsed" 
            class="truncate transition-opacity duration-200"
          >
            {{ tab.name }}
          </span>
        </button>
      </nav>
      <!-- Botón goBack abajo -->
      <div class="flex-shrink-0 p-4 border-t border-[#272727ff] mt-auto">
        <div class="flex items-center justify-between mb-2">
          <button 
            @click="goBack"
            class="flex items-center gap-2 text-[#d6d2d2ff]/60 hover:text-[#d6d2d2ff] transition-colors"
            :class="{ 'justify-center': isSidebarCollapsed }"
          >
            <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M15 19l-7-7 7-7"></path>
            </svg>
            <span v-if="!isSidebarCollapsed">
              {{ launcherStore.previousView === 'instance' ? 'Back to Instance' : 'Back to Home' }}
            </span>
          </button>
        </div>
      </div>
    </div>

    <!-- Tab Content -->
    <div class="flex-1 flex flex-col min-w-0 bg-stone-950">
      <div class="flex-1 p-6 overflow-y-auto">
        <!-- Dynamic Tab Content -->
        <component 
          :is="getCurrentTabContent()" 
          v-if="getCurrentTabContent()"
          :key="activeTab"
          class="h-full"
        />
        <div v-else class="flex items-center justify-center h-64 text-stone-400">
          No content available for this tab
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, onUnmounted, computed } from "vue";
import { useLanguageStore } from "../../stores/LanguageStore";
import { useLauncherStore } from "../../stores/LauncherStore";

// Import tab content components
import LauncherSettings from "./Settings/Tabs/LauncherSettings.vue";
import GeneralSettings from "./Settings/Tabs/GeneralSettings.vue";

// Import icons (SVG components)
import controller from "../../assets/icons/UI/controller.vue";
import settings from "../../assets/icons/UI/settings.vue";

const languageStore = useLanguageStore();
const launcherStore = useLauncherStore();

const activeTab = ref("general");
const isSidebarCollapsed = ref(false);
const windowWidth = ref(window.innerWidth);

// Computed property for automatic responsive behavior
const shouldAutoCollapse = computed(() => {
  return windowWidth.value < 1024; // lg breakpoint
});

// Watch for window resize and auto-collapse when needed
const handleResize = () => {
  windowWidth.value = window.innerWidth;
  
  // Auto-collapse when screen gets smaller
  if (shouldAutoCollapse.value && !isSidebarCollapsed.value) {
    isSidebarCollapsed.value = true;
  }
  // Auto-expand when screen gets larger
  else if (!shouldAutoCollapse.value && isSidebarCollapsed.value) {
    isSidebarCollapsed.value = false;
  }
};

// Function to go back to previous view
const goBack = () => {
  launcherStore.goBack();
};

// Initialize sidebar state based on screen size
onMounted(() => {
  window.addEventListener('resize', handleResize);
  
  // Set initial state based on screen size
  if (shouldAutoCollapse.value) {
    isSidebarCollapsed.value = true;
  }
});

onUnmounted(() => {
  window.removeEventListener('resize', handleResize);
});

// Refactored tabs array with Vue icon components
const tabs = [
	{
		id: "general",
		name: "General",
		icon: controller,
		content: GeneralSettings,
	},
	{
		id: "launcher",
		name: "Launcher",
		icon: controller,
		content: LauncherSettings,
	},
];

// Method to get current tab content component
const getCurrentTabContent = () => {
	const currentTab = tabs.find((tab) => tab.id === activeTab.value);
	return currentTab?.content || null;
};
</script> 
