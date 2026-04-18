<script lang="ts">
    import { getInstanceMods, toggleInstanceMod } from "$lib/api/cubicApi";
    import { type ModDto } from "$lib/types/types";
    import { t } from "$lib/i18n";

    let { instanceId } = $props<{ instanceId: string }>();
    let mods = $state<ModDto[]>([]);

    $effect(() => {
        if (instanceId) {
            getInstanceMods(instanceId).then((data) => {
                mods = data;
            });
        }
    });

    async function handleToggle(mod: ModDto) {
        const newEnabled = !mod.enabled;
        mod.enabled = newEnabled;
        
        await toggleInstanceMod(instanceId, mod.filename, newEnabled);
        
        mods = await getInstanceMods(instanceId);
    }
</script>

<div class="mods-section">
    <span class="section-title">{t('instanceView.mods.title')} ({mods.length})</span>
    <div class="mods-grid">
        {#each mods as mod}
            <div class="mod-card" title={mod.filename} class:disabled={!mod.enabled}>
                <div class="mod-info">
                    <span class="mod-name">{mod.name}</span>
                    <span class="mod-category"
                        >{mod.version || t('instanceView.mods.jarFile')}</span
                    >
                </div>
                <div class="mod-status-toggle">
                    <input 
                        type="checkbox" 
                        checked={mod.enabled}
                        onchange={() => handleToggle(mod)}
                    />
                </div>
            </div>
        {/each}
        {#if mods.length === 0}
            <div class="empty-mods">
                {t('instanceView.mods.empty')}
            </div>
        {/if}
    </div>
</div>
