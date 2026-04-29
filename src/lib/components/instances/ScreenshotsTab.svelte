<script lang="ts">
    import { invoke, convertFileSrc } from "@tauri-apps/api/core";
    import { deleteInstanceFile } from "$lib/api/cubicApi";
    import { t } from "$lib/i18n";

    let { instance } = $props<{ instance: any }>();
    let screenshots = $state<string[]>([]);
    let selectedImage = $state<string | null>(null);

    async function loadScreenshots() {
        if (instance) {
            screenshots = await invoke<string[]>("get_all_instance_screenshots", {
                instanceName: instance.name,
            });
        }
    }

    $effect(() => {
        loadScreenshots();
    });

    async function handleDelete(path: string) {
        const filename = path.split(/[\\/]/).pop();
        if (filename && confirm(`¿Estás seguro de que deseas eliminar esta captura?`)) {
            await deleteInstanceFile(instance.uuid, "screenshots", filename);
            if (selectedImage === path) selectedImage = null;
            await loadScreenshots();
        }
    }
</script>

<div class="screenshots-section">
    <div class="section-header">
        <span class="section-title">{t('instanceView.screenshots.title')} ({screenshots.length})</span>
    </div>

    <div class="screenshots-grid">
        {#each screenshots as path}
            <div 
                class="screenshot-card" 
                role="button"
                tabindex="0"
                onclick={() => selectedImage = path}
                onkeydown={(e) => e.key === 'Enter' && (selectedImage = path)}
            >
                <img src={convertFileSrc(path)} alt="Screenshot" />
                <div class="overlay">
                    <button class="delete-btn" onclick={(e) => { e.stopPropagation(); handleDelete(path); }} title="Eliminar">
                        <svg xmlns="http://www.w3.org/2000/svg" width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M3 6h18"/><path d="M19 6v14c0 1-1 2-2 2H7c-1 0-2-1-2-2V6"/><path d="M8 6V4c0-1 1-2 2-2h4c1 0 2 1 2 2v2"/><line x1="10" y1="11" x2="10" y2="17"/><line x1="14" y1="11" x2="14" y2="17"/></svg>
                    </button>
                </div>
            </div>
        {/each}
        
        {#if screenshots.length === 0}
            <div class="empty-state">
                {t('instanceView.screenshots.empty')}
            </div>
        {/if}
    </div>
</div>

{#if selectedImage}
    <div 
        class="image-viewer-overlay" 
        role="button"
        tabindex="0"
        onclick={() => selectedImage = null}
        onkeydown={(e) => e.key === 'Escape' && (selectedImage = null)}
    >
        <div class="viewer-container" role="dialog" aria-modal="true" tabindex="-1" onclick={(e) => e.stopPropagation()} onkeydown={(e) => e.stopPropagation()}>
            <img src={convertFileSrc(selectedImage)} alt="Full size" />
            <button class="close-btn" onclick={() => selectedImage = null}>✕</button>
        </div>
    </div>
{/if}

