<template>
  <BaseModal v-model="rosquilla.isSettingsModalOpen" :title="languageStore.getTranslation('Launcher.settings.title')">
    <div class="flex">
      <!-- Vertical Tabs -->
      <div class="w-56 border-r border-[#272727ff]">
        <nav class="flex flex-col space-y-0.5" aria-label="Tabs">
          <button 
            v-for="tab in tabs" 
            :key="tab.id" 
            @click="activeTab = tab.id" 
            :class="[
              activeTab === tab.id
                ? 'bg-[#272727ff] text-[#d6d2d2ff] border-l-2 border-[#78716c]'
                : 'text-[#d6d2d2ff]/60 hover:text-[#d6d2d2ff] hover:bg-[#272727ff]',
              'w-full text-left px-4 py-2.5 text-sm font-medium transition-colors flex items-center gap-2'
            ]"
          >
            <!-- Vue Icon Component -->
            <component 
              :is="tab.icon" 
              class="w-5 h-5"
            />
            
            {{ tab.name }}
          </button>
        </nav>
      </div>

      <!-- Tab Content -->
      <div class="p-2">
        <!-- Dynamic Tab Content -->
        <component 
          :is="getCurrentTabContent()" 
          v-if="getCurrentTabContent()"
          :key="activeTab"
        />
        <div v-else class="flex items-center justify-center h-64 text-stone-400">
          No content available for this tab
        </div>
      </div>
    </div>
  </BaseModal>
</template>

<script setup lang="ts">
import { ref } from "vue";
import { useLanguageStore } from "../../../stores/LanguageStore";
import { useLauncherStore } from "../../../stores/LauncherStore";
import BaseModal from "../BaseModal.vue";

// Import tab content components
import LauncherSettings from "./Tabs/LauncherSettings.vue";
import AccountSettings from "./Tabs/AccountSettings.vue";

// Import icons (SVG components)
import controller from "../../../assets/icons/UI/controller.vue";


const rosquilla = useLauncherStore();
const languageStore = useLanguageStore();

const activeTab = ref("general");

// Refactored tabs array with Vue icon components
const tabs = [
	{
		id: "general",
		name: "General",
		icon: controller,
		content: LauncherSettings,
	},
	{
		id: "game",
		name: "Game",
		icon: controller,
		content: LauncherSettings,
	},
	{
		id: "launcher",
		name: "Launcher",
		icon: controller,
		content: LauncherSettings,
	},
	{
		id: "accounts",
		name: "Luis roscasel",
		icon: controller,
		content: AccountSettings,
	},
];

// Method to get current tab content component
const getCurrentTabContent = () => {
	const currentTab = tabs.find((tab) => tab.id === activeTab.value);
	return currentTab?.content || null;
};
</script>