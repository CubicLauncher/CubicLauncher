<!-- Main.vue mejorado -->
<template>
    <div class="flex h-screen">
        <Sidebar />
        <div class="flex flex-col flex-1 overflow-hidden">
            <Titlebar />
            <div class="flex-1 overflow-y-auto bg-stone-950">
                <SettingsModal v-show="store.isSettingsModalOpen" />
                <AddInstanceModal v-show="store.isAddInstanceModalOpen" />
                <InstanceView
                    v-if="store.CurrentInstance"
                    :instance="store.CurrentInstance"
                />
                <WelcomeView v-else />
            </div>
        </div>
    </div>
</template>

<script setup>
import { useLauncherStore } from "../../stores/LauncherStore";
import { defineAsyncComponent } from "vue";
import Sidebar from "../navigation/Sidebar.vue";
import WelcomeView from "../views/WelcomeView.vue";
import Titlebar from "./Titlebar.vue";
const store = useLauncherStore();

const SettingsModal = defineAsyncComponent(
    () => import("../modals/Settings/SettingsModal.vue"),
);
const AddInstanceModal = defineAsyncComponent(
    () => import("../modals/AddInstanceModal.vue"),
);
const InstanceView = defineAsyncComponent(
    () => import("../views/InstanceView.vue"),
);
</script>
