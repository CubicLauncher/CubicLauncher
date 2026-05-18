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
        {#each mods as mod (mod.filename)}
            <div class="mod-card" class:disabled={!mod.enabled}>
                <div class="mod-icon">
                    {#if mod.icon}
                        <img src={mod.icon} alt={mod.name} />
                    {:else}
                        <div class="mod-icon-placeholder">📦</div>
                    {/if}
                </div>
                <div class="mod-info">
                    <div class="mod-name-row">
                        <span class="mod-name" title={mod.name}>{mod.name}</span>
                        <span class="mod-version">{mod.version || t('instanceView.mods.jarFile')}</span>
                    </div>
                    <p class="mod-description" title={mod.description}>
                        {mod.description || t('instanceView.mods.noDescription')}
                    </p>
                    {#if mod.authors && mod.authors.length > 0}
                        <span class="mod-authors" title={mod.authors.join(', ')}>
                            {t('instanceView.mods.authors')}: {mod.authors.join(', ')}
                        </span>
                    {/if}
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
