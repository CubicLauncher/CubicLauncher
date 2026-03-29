<script lang="ts">
    import { invoke } from "@tauri-apps/api/core";
    import type { InstanceDto } from "$lib/types/types";
    import InstanceDetails from "./InstanceDetails.svelte";
    import { launchInstance } from "$lib/api/cubicApi";
    import ModsRow from "./ModsRow.svelte";
    import ScreenshotsPanel from "./ScreenshotsPanel.svelte";
    import QuickOptionsPanel from "./QuickOptionsPanel.svelte";

    let { selectedInstance } = $props<{ selectedInstance: InstanceDto }>();
    let activeTab = $state("detalles");

    const formatter = new Intl.DateTimeFormat("es-ES", {
        year: "numeric",
        month: "long",
        day: "2-digit",
        hour: "2-digit",
        minute: "2-digit",
    });

    function formatDate(unix_date: number): string {
        if (unix_date < 1) {
            return "Nunca jugado";
        }
        let date = new Date(unix_date * 1000);
        return formatter.format(date);
    }
</script>

<div class="instance-view">
    <section class="hero-section">
        <div class="instance-big-icon">
            <img src="/images/cubic.svg" alt="Icon" />
        </div>
        <div class="instance-title-area">
            <h2>{selectedInstance.name}</h2>
            <div class="last-played">
                Última vez jugado: {formatDate(selectedInstance.last_played)}
            </div>
            <button
                class="play-btn"
                onclick={() => launchInstance(selectedInstance)}
                >Jugar</button
            >
        </div>
    </section>

    <div class="tabs-nav">
        <button 
            class="tab-item {activeTab === 'detalles' ? 'active' : ''}" 
            onclick={() => activeTab = 'detalles'}
        >
            Detalles
        </button>
        <button 
            class="tab-item {activeTab === 'mods' ? 'active' : ''}" 
            onclick={() => activeTab = 'mods'}
        >
            Mods
        </button>
        <button 
            class="tab-item {activeTab === 'capturas' ? 'active' : ''}" 
            onclick={() => activeTab = 'capturas'}
        >
            Capturas
        </button>
        <button 
            class="tab-item {activeTab === 'opciones' ? 'active' : ''}" 
            onclick={() => activeTab = 'opciones'}
        >
            Opciones
        </button>
    </div>

    <div class="tab-content">
        {#if activeTab === 'detalles'}
            <div class="tab-pane">
                <InstanceDetails instance={selectedInstance} />
            </div>
        {:else if activeTab === 'mods'}
            <div class="tab-pane">
                <ModsRow />
            </div>
        {:else if activeTab === 'capturas'}
            <div class="tab-pane">
                <ScreenshotsPanel />
            </div>
        {:else if activeTab === 'opciones'}
            <div class="tab-pane">
                <QuickOptionsPanel />
            </div>
        {/if}
    </div>
</div>

