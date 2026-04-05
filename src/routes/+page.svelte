<script lang="ts">
    import { invoke } from "@tauri-apps/api/core";
    import { onMount } from "svelte";
    import "../styles/App.css";
    import { launcherStore } from "$lib/state/state.svelte";
    import { getVersions, initPolling } from "$lib/api/launcherService";
    import type { InstanceDto } from "$lib/types/types";
    import Sidebar from "$lib/components/layout/Sidebar.svelte";
    import InstanceView from "$lib/components/instances/InstanceView.svelte";
    import Drawer from "$lib/components/layout/Drawer.svelte";
    import Settings from "$lib/components/settings/Settings.svelte";
    import CreateInstanceModal from "$lib/components/instances/CreateInstanceModal.svelte";

    let selectedInstance = $state<InstanceDto | null>(null);
    let quickMenuOpen = $state(false);
    let openCreateModal = $state(false);

    onMount(() => {
        getVersions();
        const unlistenPromise = initPolling();
        invoke("start_polling");

        // Auto-select first instance if available
        if (launcherStore.loadedInstances.length > 0 && !selectedInstance) {
            selectedInstance = launcherStore.loadedInstances[0];
        }

        return () => {
            unlistenPromise.then((unlisten) => unlisten());
        };
    });
    function handleKeyup(e: KeyboardEvent) {
        const isLeft = e.location === 1;
        if (!isLeft) return;

        if (e.key === "Alt") quickMenuOpen = !quickMenuOpen;
        if (e.key === "Shift") quickMenuOpen = !quickMenuOpen;
    }
</script>

<svelte:window on:keyup={handleKeyup} />

<div class="app-container">
    <Sidebar
        bind:selectedInstance
        bind:openCreateModal
        onOpenQuickMenu={() => (quickMenuOpen = true)}
    />

    <main class="main-content">
        <div
            class="background-overlay"
            style="background-image: url('/images/bg.png');"
        ></div>

        {#if selectedInstance}
            <InstanceView {selectedInstance} />
        {:else}
            <div class="empty-state">
                <img
                    src="/images/cubic.svg"
                    alt="Cubic"
                    style="width: 120px; opacity: 0.1; filter: grayscale(1);"
                />
                <h2>No se ha seleccionado ninguna instancia</h2>
                <p>Elige una instancia de la barra lateral para comenzar</p>
            </div>
        {/if}
    </main>
</div>

<Drawer bind:open={quickMenuOpen} direction="right">
    <Settings onclose={() => (quickMenuOpen = false)} />
</Drawer>

<CreateInstanceModal bind:open={openCreateModal} />
