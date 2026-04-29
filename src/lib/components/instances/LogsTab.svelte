<script lang="ts">
    import { getInstanceLogs, readInstanceLog, deleteInstanceFile } from "$lib/api/cubicApi";
    import { t } from "$lib/i18n";

    let { instanceId } = $props<{ instanceId: string }>();
    let logs = $state<string[]>([]);
    let selectedLog = $state<string | null>(null);
    let logContent = $state<string>("");
    let isLoading = $state(false);

    async function loadLogs() {
        if (instanceId) {
            logs = await getInstanceLogs(instanceId);
        }
    }

    $effect(() => {
        loadLogs();
    });

    async function handleSelectLog(filename: string) {
        selectedLog = filename;
        isLoading = true;
        logContent = await readInstanceLog(instanceId, filename);
        isLoading = false;
    }

    async function handleDelete(filename: string) {
        if (confirm(`¿Estás seguro de que deseas eliminar ${filename}?`)) {
            await deleteInstanceFile(instanceId, "logs", filename);
            if (selectedLog === filename) {
                selectedLog = null;
                logContent = "";
            }
            await loadLogs();
        }
    }
</script>

<div class="logs-section">
    <div class="logs-sidebar">
        <span class="section-title">{t('instanceView.logs.title')}</span>
        <div class="logs-list">
            {#each logs as log}
                <div 
                    class="log-item" 
                    class:active={selectedLog === log}
                    role="button"
                    tabindex="0"
                    onclick={() => handleSelectLog(log)}
                    onkeydown={(e) => e.key === 'Enter' && handleSelectLog(log)}
                >
                    <span class="log-filename">{log}</span>
                    <span 
                        class="delete-btn" 
                        role="button"
                        tabindex="0"
                        onclick={(e) => { e.stopPropagation(); handleDelete(log); }} 
                        onkeydown={(e) => { if (e.key === 'Enter') { e.stopPropagation(); handleDelete(log); } }}
                        title="Eliminar"
                    >✕</span>
                </div>
            {/each}
            {#if logs.length === 0}
                <div class="empty-logs">{t('instanceView.logs.empty')}</div>
            {/if}
        </div>
    </div>
    
    <div class="log-viewer">
        {#if selectedLog}
            <div class="viewer-header">
                <h3>{selectedLog}</h3>
            </div>
            {#if isLoading}
                <div class="loading-state">Cargando...</div>
            {:else}
                <pre class="log-content">{logContent}</pre>
            {/if}
        {:else}
            <div class="select-prompt">
                {t('instanceView.logs.selectLog')}
            </div>
        {/if}
    </div>
</div>

