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

<style>
    .instance-view {
        display: flex;
        flex-direction: column;
        height: 100%;
        background: linear-gradient(180deg, rgba(0,0,0,0.2) 0%, rgba(0,0,0,0) 100%);
    }

    .tabs-nav {
        display: flex;
        gap: 12px;
        padding: 0 40px;
        border-bottom: 1px solid var(--border);
        background: rgba(255, 255, 255, 0.01);
        backdrop-filter: blur(8px);
        position: sticky;
        top: 0;
        z-index: 10;
        flex-shrink: 0;
    }

    .tab-item {
        background: transparent;
        border: none;
        color: var(--text-secondary);
        padding: 16px 4px;
        margin-right: 20px;
        font-size: 0.85rem;
        font-weight: 600;
        text-transform: uppercase;
        letter-spacing: 1px;
        cursor: pointer;
        position: relative;
        transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
        opacity: 0.7;
    }

    .tab-item:hover {
        color: var(--text-primary);
        opacity: 1;
    }

    .tab-item.active {
        color: var(--accent);
        opacity: 1;
    }

    .tab-item::after {
        content: "";
        position: absolute;
        bottom: -1px;
        left: 0;
        right: 0;
        height: 2px;
        background: var(--accent);
        transform: scaleX(0);
        transition: transform 0.3s cubic-bezier(0.4, 0, 0.2, 1);
        border-radius: 2px 2px 0 0;
        box-shadow: 0 0 10px rgba(255, 255, 255, 0.3);
    }

    .tab-item.active::after {
        transform: scaleX(1);
    }

    .tab-content {
        flex: 1;
        padding: 32px 40px;
        overflow-y: auto;
        scrollbar-gutter: stable;
    }

    .tab-pane {
        animation: slideUpFade 0.4s cubic-bezier(0.2, 0.8, 0.2, 1);
        height: 100%;
    }

    @keyframes slideUpFade {
        from {
            opacity: 0;
            transform: translateY(15px);
        }
        to {
            opacity: 1;
            transform: translateY(0);
        }
    }

    /* Global overrides to fix pre-existing constraints */
    :global(.details-panel) {
        border-right: none !important;
        padding: 0 !important;
        background: transparent !important;
    }

    :global(.instance-dashboard) {
        padding: 0 !important;
        display: block !important;
    }

    :global(.mods-section) {
        margin-bottom: 0 !important;
    }

    :global(.quick-options-section) {
        flex: none !important;
    }
</style>
