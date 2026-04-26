<script lang="ts">
    import { listen } from "@tauri-apps/api/event";
    import { onMount } from "svelte";
    import { fade, slide } from "svelte/transition";
    import { t } from "$lib/i18n";

    let progress = $state<{
        version: string;
        current: number;
        total: number;
        type: string;
    } | null>(null);

    let visible = $state(false);

    onMount(() => {
        const unlistenProgress = listen("download-progress", (event: any) => {
            progress = event.payload;
            visible = true;
        });

        const unlistenFinished = listen("download-finished", () => {
            progress = null;
            visible = false;
        });

        return () => {
            unlistenProgress.then((u) => u());
            unlistenFinished.then((u) => u());
        };
    });

    const percent = $derived(
        progress && progress.total > 0
            ? Math.round((progress.current / progress.total) * 100)
            : 0
    );
</script>

{#if visible && progress}
    <div class="progress-container" in:slide={{ axis: 'y' }} out:fade>
        <div class="progress-info">
            <div class="info-left">
                <span class="version-name">{progress.version}</span>
                <span class="progress-type">{progress.type}</span>
            </div>
            <span class="percent">{percent}%</span>
        </div>
        <div class="progress-track">
            <div class="progress-fill" style="width: {percent}%"></div>
        </div>
        <div class="progress-details">
            {progress.current} / {progress.total} {t('downloadProgress.items')}
        </div>
    </div>
{/if}
