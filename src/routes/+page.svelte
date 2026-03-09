<script lang="ts">
    import { invoke } from "@tauri-apps/api/core";
    import { onMount } from "svelte";
    import "../styles/App.css";
    import { launcherStore } from "$lib/state/state.svelte";
    import { initPolling } from "$lib/api/launcherService";
    import type { InstanceDto } from "$lib/types/types";
    import Sidebar from "$lib/components/Sidebar.svelte";
    import InstanceView from "$lib/components/InstanceView.svelte";

    let selectedInstance = $state<InstanceDto | null>(null);

    onMount(() => {
        invoke("start_polling");
        const unlistenPromise = initPolling();

        // Auto-select first instance if available
        if (launcherStore.loadedInstances.length > 0 && !selectedInstance) {
            selectedInstance = launcherStore.loadedInstances[0];
        }

        return () => {
            unlistenPromise.then((unlisten) => unlisten());
        };
    });
</script>

<div class="app-container">
    <Sidebar bind:selectedInstance />

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
