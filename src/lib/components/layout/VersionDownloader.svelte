<script lang="ts">
    import { onMount } from "svelte";
    import {
        getAvailableVersions,
        addToQueue,
        getInstalledVersions,
        getFabricVersions,
        downloadFabric,
        refreshAvailableVersions,
    } from "$lib/api/cubicApi";
    import VirtualList from "./VirtualList.svelte";
    import Select from "./Select.svelte";
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
    let fabricInstalledSet = $derived.by(() => new Set(
        installedVersions
            .filter(iv => iv.startsWith('fabric-loader-'))
            .map(iv => iv.replace('fabric-loader-', ''))
    ));
    let filter = $state("release");
    let search = $state("");
    let installStatusFilter = $state("all");
    let majorVersionFilter = $state("all");
    let fabricStabilityFilter = $state("stable");

    let loadingMojang = $state(false);
    let loadingFabric = $state(false);
    let refreshing = $state(false);

    async function refreshMojang() {
        refreshing = true;
        manifest = await refreshAvailableVersions();
        refreshing = false;
    }

    async function refreshFabric() {
        refreshing = true;
        fabricManifest = await refreshAvailableVersions();
        refreshing = false;
    }

    async function loadMojang() {
        if (manifest || loadingMojang) return;
        loadingMojang = true;
        manifest = await getAvailableVersions();
        loadingMojang = false;
    }

    async function loadFabric() {
        if (fabricManifest.length > 0 || loadingFabric) return;
        loadingFabric = true;
        fabricManifest = await getFabricVersions();
        loadingFabric = false;
    }

    onMount(async () => {
        installedVersions = await getInstalledVersions();
        loading = false;
    });

    $effect(() => {
        if (filter === "fabric") {
            loadFabric();
        } else {
            loadMojang();
        }
    });

    const isCurrentManifestLoading = $derived(
        filter === "fabric" ? loadingFabric : loadingMojang
    );

    const availableMajorVersions = $derived.by(() => {
        const source = filter === "fabric" ? fabricManifest : manifest;
        if (!source) return [];
        const versions = new Set<string>();
        source.forEach((v: any) => {
            const vid = filter === "fabric" ? v.version : v.id;
            const match = vid.match(/^1\.\d+/);
            if (match) {
                versions.add(match[0]);
            }
        });
        return Array.from(versions).sort((a, b) => {
            const aNum = parseInt(a.split('.')[1] || "0");
            const bNum = parseInt(b.split('.')[1] || "0");
            return bNum - aNum;
        });
    });

    const majorVersionOptions = $derived([
        { value: "all", label: t('versionDownloader.filters.all') },
        ...availableMajorVersions.map(v => ({ value: v, label: v }))
    ]);

    const filteredVersions = $derived(
        (filter === "fabric" ? fabricManifest : manifest)?.filter((v: any) => {
            const versionId = filter === "fabric" ? v.version : v.id;
            
            // Installed filter
            const isInstalled = installedVersions.includes(versionId) || 
                (filter === 'fabric' && fabricInstalledSet.has(versionId));
            
            if (installStatusFilter === "installed" && !isInstalled) return false;
            if (installStatusFilter === "not_installed" && isInstalled) return false;

            // Major Version Filter
            if (majorVersionFilter !== "all" && !versionId.startsWith(majorVersionFilter)) return false;

            if (filter === "fabric") {
                if (fabricStabilityFilter === "stable" && !v.stable) return false;
                if (fabricStabilityFilter === "unstable" && v.stable) return false;
            } else {
                if (!launcherStore.settings.show_snapshots && v.type === "snapshot") return false;
                if (!launcherStore.settings.show_alpha && (v.type === "old_alpha" || v.type === "old_beta")) return false;
            }
            
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
        <div style="display: flex; align-items: center; gap: 8px;">
            <button
                onclick={filter === 'fabric' ? refreshFabric : refreshMojang}
                disabled={refreshing}
                title={t('versionDownloader.refreshBtn')}
                style="background: none; border: none; color: var(--text-muted); cursor: pointer; padding: 4px; display: flex; align-items: center; border-radius: 4px; transition: color 0.2s;"
                onmouseenter={(e) => e.currentTarget.style.color = 'var(--text-primary)'}
                onmouseleave={(e) => e.currentTarget.style.color = 'var(--text-muted)'}
            >
                <svg
                    width="16"
                    height="16"
                    viewBox="0 0 24 24"
                    fill="none"
                    stroke="currentColor"
                    stroke-width="2"
                    stroke-linecap="round"
                    stroke-linejoin="round"
                    class:spin={refreshing}
                    style={refreshing ? 'animation: spin 1s linear infinite;' : ''}
                >
                    <polyline points="23 4 23 10 17 10"></polyline>
                    <path d="M20.49 15a9 9 0 1 1-2.12-9.36L23 10"></path>
                </svg>
            </button>
            <button class="qm-close-btn" onclick={onclose}>✕</button>
        </div>
    </div>

    <style>
        @keyframes spin {
            from { transform: rotate(0deg); }
            to { transform: rotate(360deg); }
        }

        .qm-root {
            height: 100%;
            display: flex;
            flex-direction: column;
            background: #0a0a0a;
            color: #eee;
            font-family: "Cantarell", sans-serif;
        }

        .qm-header {
            padding: 20px 20px 10px 20px;
            display: flex;
            justify-content: space-between;
            align-items: center;
            background: #0f0f0f;
        }

        .qm-tabs {
            display: flex;
            padding: 0 10px;
            background: #0f0f0f;
            border-bottom: 1px solid #222;
            gap: 4px;
        }

        .qm-tab-btn {
            flex: 1;
            background: none;
            border: none;
            color: #666;
            padding: 8px 4px;
            cursor: pointer;
            font-size: 0.8rem;
            font-weight: 600;
            border-bottom: 2px solid transparent;
            transition: all 0.2s;
        }

        .qm-tab-btn.active {
            color: #fff;
            border-bottom-color: #eee;
        }

        .qm-label {
            font-size: 1.1rem;
            font-weight: 600;
            color: #fff;
        }

        .qm-close-btn {
            background: none;
            border: none;
            color: #666;
            cursor: pointer;
            font-size: 1.2rem;
            transition: color 0.2s;
        }

        .qm-close-btn:hover {
            color: #fff;
        }

        .qm-scroll {
            flex: 1;
            overflow-y: auto;
            padding: 0 20px;
        }

        :global(.qm-scroll::-webkit-scrollbar) {
            width: 4px;
        }

        :global(.qm-scroll::-webkit-scrollbar-track) {
            background: transparent;
        }

        :global(.qm-scroll::-webkit-scrollbar-thumb) {
            background: #222;
            border-radius: 10px;
        }

        .qm-empty-state {
            color: #444;
            font-size: 0.85rem;
            padding: 10px 0;
        }

        .qm-footer {
            padding: 15px 20px;
            background: #070707;
            border-top: 1px solid #111;
            display: flex;
            justify-content: center;
        }

        .qm-version {
            font-size: 0.7rem;
            color: #333;
            font-weight: 500;
        }
    </style>

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

    <div class="qm-search-container" style="padding: 10px 20px; display: flex; flex-direction: column; gap: 10px;">
        <input
            type="text"
            placeholder={t('versionDownloader.searchPlaceholder')}
            bind:value={search}
            style="width: 100%; background: var(--bg-input); border: 1px solid var(--border-color); color: var(--text-primary); padding: 8px 12px; border-radius: 8px; font-size: 0.85rem;"
        />
        <div class="qm-filters-grid" style="display: grid; grid-template-columns: repeat(auto-fit, minmax(180px, 1fr)); gap: 16px; margin-top: 4px; padding-bottom: 8px;">
            <Select 
                label={t('versionDownloader.filters.installStatus')}
                options={[
                    { value: "all", label: t('versionDownloader.filters.all') },
                    { value: "installed", label: t('versionDownloader.filters.installedOnly') },
                    { value: "not_installed", label: t('versionDownloader.filters.notInstalledOnly') }
                ]}
                bind:value={installStatusFilter}
            />
            
            <Select 
                label={t('versionDownloader.filters.majorVersion')}
                options={majorVersionOptions}
                bind:value={majorVersionFilter}
            />

            {#if filter === "fabric"}
                <Select 
                    label={t('versionDownloader.filters.fabricStability')}
                    options={[
                        { value: "all", label: t('versionDownloader.filters.all') },
                        { value: "stable", label: t('versionDownloader.filters.stableOnly') },
                        { value: "unstable", label: t('versionDownloader.filters.unstableOnly') }
                    ]}
                    bind:value={fabricStabilityFilter}
                />
            {/if}
        </div>
    </div>

    <div class="qm-scroll" style="overflow: hidden; padding: 0;">
        {#if loading || isCurrentManifestLoading}
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
                            style="display: flex; align-items: center; justify-content: space-between; padding: 12px; background: var(--bg-card); border: 1px solid var(--border-color); border-radius: 8px; height: 58px;"
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
                                    {#if isInstalled || (filter === 'fabric' && fabricInstalledSet.has(version.version))}
                                        <span
                                            style="font-size: 0.6rem; background: rgba(var(--color-success-rgb), 0.1); color: var(--color-success); padding: 2px 6px; border-radius: 4px; font-weight: 700; text-transform: uppercase; border: 1px solid rgba(var(--color-success-rgb), 0.2);"
                                            >{t('versionDownloader.installedTag')}</span
                                        >
                                    {/if}
                                </div>
                                <div
                                    style="font-size: 0.7rem; color: var(--text-muted); text-transform: uppercase; letter-spacing: 0.5px;"
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
                                    style="color: var(--color-success); padding: 6px 14px; display: flex; align-items: center; gap: 4px;"
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
                                    style="background: var(--accent); color: var(--accent-text); border: none; padding: 6px 14px; border-radius: 6px; font-size: 0.75rem; font-weight: 700; cursor: pointer; transition: all 0.2s;"
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
