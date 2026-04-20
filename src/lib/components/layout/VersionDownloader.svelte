<script lang="ts">
    import { onMount } from "svelte";
    import {
        getAvailableVersions,
        addToQueue,
        getInstalledVersions,
        getFabricVersions,
        downloadFabric,
    } from "$lib/api/cubicApi";
    import VirtualList from "./VirtualList.svelte";
    import { launcherStore } from "$lib/state/state.svelte";
    import { t } from "$lib/i18n";

    interface Props {
        onclose?: () => void;
    }

    let { onclose }: Props = $props();

    let loading = $state(true);
    let manifest = $state<any>(null);
    let fabricManifest = $state<any[]>([]);
    let installedVersions = $state<string[]>([]);
    let filter = $state("release");
    let search = $state("");

    onMount(async () => {
        const [manifestRes, installedRes, fabricRes] = await Promise.all([
            getAvailableVersions(),
            getInstalledVersions(),
            getFabricVersions(),
        ]);
        manifest = manifestRes;
        installedVersions = installedRes;
        fabricManifest = fabricRes;
        loading = false;
    });

    const filteredVersions = $derived(
        (filter === "fabric" ? fabricManifest : manifest)?.filter((v: any) => {
            if (filter === "fabric") {
                if (!v.stable) return false;
            } else {
                if (!launcherStore.settings.show_snapshots && v.type === "snapshot") return false;
                if (!launcherStore.settings.show_alpha && (v.type === "old_alpha" || v.type === "old_beta")) return false;
            }
            
            const versionId = filter === "fabric" ? v.version : v.id;
            const matchesFilter = 
                filter === "fabric" ||
                v.type === filter || 
                (filter === "alpha" && (v.type === "old_alpha" || v.type === "old_beta"));
                
            const matchesSearch = versionId
                .toLowerCase()
                .includes(search.toLowerCase());
            return matchesFilter && matchesSearch;
        }) || [],
    );

    $effect(() => {
        if (!launcherStore.settings.show_snapshots && filter === "snapshot") {
            filter = "release";
        }
        if (!launcherStore.settings.show_alpha && filter === "alpha") {
            filter = "release";
        }
    });

    async function handleDownload(versionId: string) {
        if (filter === "fabric") {
            await downloadFabric(versionId);
        } else {
            await addToQueue(versionId);
        }
        
        // Refetch installed versions
        installedVersions = await getInstalledVersions();
    }
</script>

<div class="qm-root">
    <div class="qm-header">
        <span class="qm-label">{t('versionDownloader.title')}</span>
        <button class="qm-close-btn" onclick={onclose}>✕</button>
    </div>

    <div class="qm-tabs">
        <button
            class="qm-tab-btn"
            class:active={filter === "release"}
            onclick={() => (filter = "release")}
        >
            <span class="qm-tab-label">{t('versionDownloader.tabs.releases')}</span>
        </button>
        {#if launcherStore.settings.show_snapshots}
            <button
                class="qm-tab-btn"
                class:active={filter === "snapshot"}
                onclick={() => (filter = "snapshot")}
            >
                <span class="qm-tab-label">{t('versionDownloader.tabs.snapshots')}</span>
            </button>
        {/if}
        {#if launcherStore.settings.show_alpha}
            <button
                class="qm-tab-btn"
                class:active={filter === "alpha"}
                onclick={() => (filter = "alpha")}
            >
                <span class="qm-tab-label">{t('versionDownloader.tabs.alphas')}</span>
            </button>
        {/if}
        <button
            class="qm-tab-btn"
            class:active={filter === "fabric"}
            onclick={() => (filter = "fabric")}
        >
            <span class="qm-tab-label">{t('versionDownloader.tabs.fabric')}</span>
        </button>
    </div>

    <div class="qm-search-container" style="padding: 10px 20px;">
        <input
            type="text"
            placeholder={t('versionDownloader.searchPlaceholder')}
            bind:value={search}
            style="width: 100%; background: #111; border: 1px solid #333; color: #fff; padding: 8px 12px; border-radius: 8px; font-size: 0.85rem;"
        />
    </div>

    <div class="qm-scroll" style="overflow: hidden; padding: 0;">
        {#if loading}
            <div class="qm-empty-state">{t('versionDownloader.loading')}</div>
        {:else if filteredVersions.length === 0}
            <div class="qm-empty-state">{t('versionDownloader.notFound')}</div>
        {:else}
            <VirtualList items={filteredVersions} itemHeight={66} padding={20}>
                {#snippet children(version, index)}
                    {@const isInstalled = installedVersions.includes(
                        version.id,
                    )}
                    <div
                        class="virtual-item-container"
                        style="padding: 0 20px;"
                    >
                        <div
                            class="version-item"
                            style="display: flex; align-items: center; justify-content: space-between; padding: 12px; background: rgba(255,255,255,0.02); border: 1px solid #222; border-radius: 8px; height: 58px;"
                        >
                            <div class="version-info">
                                <div
                                    style="display: flex; align-items: center; gap: 8px;"
                                >
                                    <div
                                        style="font-weight: 600; font-size: 0.9rem;"
                                    >
                                        {filter === 'fabric' ? version.version : version.id}
                                    </div>
                                    {#if isInstalled || (filter === 'fabric' && installedVersions.some(iv => iv.startsWith('fabric-loader-') && iv.endsWith(version.version)))}
                                        <span
                                            style="font-size: 0.6rem; background: rgba(0, 255, 100, 0.1); color: #00ff64; padding: 2px 6px; border-radius: 4px; font-weight: 700; text-transform: uppercase; border: 1px solid rgba(0, 255, 100, 0.2);"
                                            >{t('versionDownloader.installedTag')}</span
                                        >
                                    {/if}
                                </div>
                                <div
                                    style="font-size: 0.7rem; color: #666; text-transform: uppercase; letter-spacing: 0.5px;"
                                >
                                    {#if filter === 'fabric'}
                                        Fabric Meta • {version.stable ? 'STABLE' : 'UNSTABLE'}
                                    {:else}
                                        {version.type} • {new Date(
                                            version.releaseTime,
                                        ).toLocaleDateString()}
                                    {/if}
                                </div>
                            </div>

                            {#if isInstalled}
                                <div
                                    style="color: #00ff64; padding: 6px 14px; display: flex; align-items: center; gap: 4px;"
                                >
                                    <svg
                                        width="16"
                                        height="16"
                                        viewBox="0 0 24 24"
                                        fill="none"
                                        stroke="currentColor"
                                        stroke-width="3"
                                        stroke-linecap="round"
                                        stroke-linejoin="round"
                                        ><polyline points="20 6 9 17 4 12"
                                        ></polyline></svg
                                    >
                                </div>
                            {:else}
                                <button
                                    class="download-btn"
                                    onclick={() => handleDownload(filter === 'fabric' ? version.version : version.id)}
                                    style="background: #fff; color: #000; border: none; padding: 6px 14px; border-radius: 6px; font-size: 0.75rem; font-weight: 700; cursor: pointer; transition: all 0.2s;"
                                >
                                    {t('versionDownloader.downloadBtn')}
                                </button>
                            {/if}
                        </div>
                    </div>
                {/snippet}
            </VirtualList>
        {/if}
    </div>

    <div class="qm-footer">
        <span class="qm-version">Source: {filter === 'fabric' ? 'Fabric Meta' : 'Mojang Manifest'}</span>
    </div>
</div>
