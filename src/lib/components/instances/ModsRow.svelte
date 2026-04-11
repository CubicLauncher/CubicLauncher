<script lang="ts">
    import { getInstanceMods } from "$lib/api/cubicApi";
    import { type ModDto } from "$lib/types/types";

    let { instanceId } = $props<{ instanceId: string }>();
    let mods = $state<ModDto[]>([]);

    $effect(() => {
        if (instanceId) {
            getInstanceMods(instanceId).then((data) => {
                mods = data;
            });
        }
    });
</script>

<div class="mods-section">
    <span class="section-title">Mods Instalados ({mods.length})</span>
    <div class="mods-grid">
        {#each mods as mod}
            <div class="mod-card" title={mod.filename}>
                <div class="mod-info">
                    <span class="mod-name">{mod.name}</span>
                    <span class="mod-category"
                        >{mod.version || "Archivo .JAR"}</span
                    >
                </div>
                <div class="mod-status-dot"></div>
            </div>
        {/each}
        {#if mods.length === 0}
            <div class="empty-mods">No hay mods instalados en esta instancia</div>
        {/if}
    </div>
</div>

<style>
    .empty-mods {
        grid-column: 1 / -1;
        text-align: center;
        padding: 40px;
        color: var(--text-secondary);
        font-size: 0.85rem;
        background: rgba(255, 255, 255, 0.02);
        border: 1px dashed var(--border);
        border-radius: 8px;
        text-transform: uppercase;
        letter-spacing: 1px;
    }
</style>
