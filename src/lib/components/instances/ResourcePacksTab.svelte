<script lang="ts">
    import { getInstanceResourcePacks, deleteInstanceFile, addInstanceFile } from "$lib/api/cubicApi";
    import { type ModDto } from "$lib/types/types";
    import { t } from "$lib/i18n";
    import { open } from "@tauri-apps/plugin-dialog";

    let { instanceId } = $props<{ instanceId: string }>();
    let packs = $state<ModDto[]>([]);
    let isLoading = $state(false);

    async function loadPacks() {
        if (instanceId) {
            isLoading = true;
            packs = await getInstanceResourcePacks(instanceId);
            isLoading = false;
        }
    }

    $effect(() => {
        loadPacks();
    });

    async function handleDelete(pack: ModDto) {
        if (confirm(`¿Estás seguro de que deseas eliminar ${pack.filename}?`)) {
            await deleteInstanceFile(instanceId, "resourcepacks", pack.filename);
            await loadPacks();
        }
    }

    async function handleAdd() {
        const selected = await open({
            multiple: true,
            filters: [{
                name: "Resource Pack",
                extensions: ["zip"]
            }]
        });

        if (selected && Array.isArray(selected)) {
            for (const path of selected) {
                await addInstanceFile(instanceId, "resourcepacks", path);
            }
            await loadPacks();
        } else if (selected && typeof selected === "string") {
            await addInstanceFile(instanceId, "resourcepacks", selected);
            await loadPacks();
        }
    }
</script>

<div class="resources-section">
    <div class="section-header">
        <span class="section-title">{t('instanceView.resources.title')} ({packs.length})</span>
        <button class="add-btn" onclick={handleAdd}>
            {t('instanceView.resources.addBtn')}
        </button>
    </div>
    
    <div class="packs-grid">
        {#each packs as pack}
            <div class="pack-card" title={pack.filename}>
                <div class="pack-info">
                    <span class="pack-name">{pack.name}</span>
                </div>
                <button class="delete-btn" onclick={() => handleDelete(pack)} title="Eliminar">
                    <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M3 6h18"/><path d="M19 6v14c0 1-1 2-2 2H7c-1 0-2-1-2-2V6"/><path d="M8 6V4c0-1 1-2 2-2h4c1 0 2 1 2 2v2"/><line x1="10" y1="11" x2="10" y2="17"/><line x1="14" y1="11" x2="14" y2="17"/></svg>
                </button>
            </div>
        {/each}
        
        {#if packs.length === 0 && !isLoading}
            <div class="empty-state">
                {t('instanceView.resources.empty')}
            </div>
        {/if}
    </div>
</div>

