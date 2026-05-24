<script lang="ts">
    import {
        searchModrinthAll,
        getModrinthProjectVersions,
        getModrinthProjectVersionsAll,
        downloadMods,
        getInstanceMods,
        type ModDownloadInfo,
    } from "$lib/api/cubicApi";
    import type {
        ModrinthProject,
        ModrinthSearchResult,
        InstanceDto,
    } from "$lib/types/types";
    import { t } from "$lib/i18n";
    import Loading from "../../icons/Loading.svelte";
    import Dropdown from "../layout/Dropdown.svelte";
    import VirtualList from "../layout/VirtualList.svelte";

    let { instance } = $props<{ instance: InstanceDto }>();

    const PAGE_SIZE = 12;

    let query = $state("");
    let allHits = $state<ModrinthProject[]>([]);
    let totalHits = $state(0);
    let currentOffset = $state(0);
    let searching = $state(true);
    let loadingMore = $state(false);
    let activeCategory = $state<string | null>(null);
    let sortIndex = $state<string>("downloads");

    // Basket state
    let basket = $state<Map<string, ModrinthProject>>(new Map());

    // Details Panel state
    let selectedMod = $state<ModrinthProject | null>(null);

    // Download state
    let reviewing = $state(false);
    let resolvingDeps = $state(false);
    let downloading = $state(false);
    let downloadQueue = $state<ModDownloadInfo[]>([]);

    // Version selection state
    let selectedModVersions = $state<any[]>([]);
    let selectedVersionId = $state<string>("");
    let loadingVersions = $state(false);
    let versionSelection = $state<Map<string, string>>(new Map());

    // Installed mods tracking
    let installedModNames = $state<Set<string>>(new Set());

    const categories = [
        "Adventure",
        "Optimization",
        "Utility",
        "Magic",
        "Technology",
        "Library",
    ];

    const sortOptions = $derived([
        { value: "downloads", label: t('instanceView.downloadMods.sortDownloads') },
        { value: "relevance", label: t('instanceView.downloadMods.sortRelevance') },
        { value: "newest",    label: t('instanceView.downloadMods.sortNewest') },
        { value: "updated",   label: t('instanceView.downloadMods.sortUpdated') },
    ]);

    function getGameVersion(versionStr: string): string {
        const segments = versionStr.split('-');
        if (segments.length > 1) {
            return segments[segments.length - 1];
        }
        return versionStr;
    }

    const gameVersion = $derived(getGameVersion(instance.version));

    async function performSearch(resetResults = true) {
        if (resetResults) {
            searching = true;
            allHits = [];
            currentOffset = 0;
            totalHits = 0;
        } else {
            loadingMore = true;
        }

        try {
            const result = await searchModrinthAll(
                query,
                instance.loader,
                activeCategory,
                sortIndex,
                PAGE_SIZE,
                resetResults ? 0 : currentOffset,
            );
            if (result) {
                totalHits = result.total_hits;
                allHits = resetResults
                    ? result.hits
                    : [...allHits, ...result.hits];
                currentOffset = allHits.length;
            }
        } finally {
            searching = false;
            loadingMore = false;
        }
    }

    function handleNearEnd() {
        if (!loadingMore && !searching && allHits.length < totalHits) {
            performSearch(false);
        }
    }

    function resetState() {
        query = "";
        allHits = [];
        totalHits = 0;
        currentOffset = 0;
        searching = true;
        loadingMore = false;
        activeCategory = null;
        sortIndex = "downloads";
        basket = new Map();
        selectedMod = null;
        reviewing = false;
        resolvingDeps = false;
        downloading = false;
        downloadQueue = [];
        selectedModVersions = [];
        selectedVersionId = "";
        loadingVersions = false;
        versionSelection = new Map();
        installedModNames = new Set();
    }

    let pendingInstanceId: string | null = null;

    $effect(() => {
        const id = instance.uuid;
        pendingInstanceId = id;
        resetState();
        getInstanceMods(id).then((mods) => {
            if (pendingInstanceId !== id) return;
            installedModNames = new Set(mods.map((m) => m.name.toLowerCase()));
        });
        performSearch();
    });

    function handleCategoryClick(cat: string | null) {
        activeCategory = cat;
        performSearch(true);
    }

    function toggleBasket(project: ModrinthProject) {
        let newBasket = new Map(basket);
        let newVersionSelection = new Map(versionSelection);
        if (newBasket.has(project.project_id)) {
            newBasket.delete(project.project_id);
            newVersionSelection.delete(project.project_id);
        } else {
            newBasket.set(project.project_id, project);
            if (selectedVersionId) {
                newVersionSelection.set(project.project_id, selectedVersionId);
            }
        }
        basket = newBasket;
        versionSelection = newVersionSelection;
    }

    async function startReview() {
        reviewing = true;
        resolvingDeps = true;
        downloadQueue = [];

        try {
            // Fetch already-installed mods once to avoid re-downloading deps
            const installedMods = await getInstanceMods(instance.uuid);
            const installedFilenames = new Set(
                installedMods.map((m) => m.filename.toLowerCase()),
            );

            const queue: ModDownloadInfo[] = [];
            for (const [id, project] of basket) {
                const versions = await getModrinthProjectVersions(
                    id,
                    instance.loader,
                    gameVersion,
                );
                if (versions && versions.length > 0) {
                    let targetVersion;
                    const storedVersionId = versionSelection.get(id);
                    if (storedVersionId) {
                        targetVersion = versions.find(v => v.id === storedVersionId);
                    }
                    if (!targetVersion) {
                        targetVersion = versions[0];
                    }
                    const primaryFile =
                        targetVersion.files.find((f: any) => f.primary) ||
                        targetVersion.files[0];
                    if (
                        !queue.find((q) => q.filename === primaryFile.filename)
                    ) {
                        queue.push({
                            url: primaryFile.url,
                            filename: primaryFile.filename,
                            projectTitle: project.title,
                            iconUrl: project.icon_url || undefined,
                        });
                    }

                    if (targetVersion.dependencies) {
                        for (const dep of targetVersion.dependencies) {
                            if (
                                dep.dependency_type === "required" &&
                                dep.project_id
                            ) {
                                const depVersions =
                                    await getModrinthProjectVersions(
                                        dep.project_id,
                                        instance.loader,
                                        gameVersion,
                                    );
                                if (depVersions && depVersions.length > 0) {
                                    const depLatest = depVersions[0];
                                    const depFile =
                                        depLatest.files.find(
                                            (f: any) => f.primary,
                                        ) || depLatest.files[0];

                                    // Skip if already installed or already queued
                                    const alreadyInstalled = installedFilenames.has(
                                        depFile.filename.toLowerCase(),
                                    );
                                    const alreadyQueued = queue.find(
                                        (q) => q.filename === depFile.filename,
                                    );
                                    if (!alreadyInstalled && !alreadyQueued) {
                                        queue.push({
                                            url: depFile.url,
                                            filename: depFile.filename,
                                        });
                                    }
                                }
                            }
                        }
                    }
                }
            }
            downloadQueue = queue;
        } finally {
            resolvingDeps = false;
        }
    }

    async function confirmDownload() {
        downloading = true;
        try {
            await downloadMods(instance.uuid, downloadQueue);
            basket = new Map();
            reviewing = false;
            selectedMod = null;
        } finally {
            downloading = false;
        }
    }

    function formatNumber(num: number): string {
        if (num > 1000000) return (num / 1000000).toFixed(1) + "M";
        if (num > 1000) return (num / 1000).toFixed(1) + "K";
        return num.toString();
    }

    function isVersionCompatible(version: any): boolean {
        return version.game_versions?.some((gv: string) => getGameVersion(gv) === gameVersion);
    }

    function isModCompatible(project: ModrinthProject): boolean {
        return project.versions.some(v => v === gameVersion);
    }

    function isModInstalled(project: ModrinthProject): boolean {
        return installedModNames.has(project.title.toLowerCase());
    }

    async function loadVersions(projectId: string) {
        loadingVersions = true;
        selectedModVersions = [];
        selectedVersionId = "";
        try {
            const versions = await getModrinthProjectVersionsAll(projectId, instance.loader);
            selectedModVersions = versions;
            if (versions.length > 0) {
                const stored = versionSelection.get(projectId);
                if (stored && versions.find(v => v.id === stored)) {
                    selectedVersionId = stored;
                }
            }
        } finally {
            loadingVersions = false;
        }
    }

    function onVersionChange() {
        let newVersionSelection = new Map(versionSelection);
        if (selectedVersionId) {
            newVersionSelection.set(selectedMod!.project_id, selectedVersionId);
        } else {
            newVersionSelection.delete(selectedMod!.project_id);
        }
        versionSelection = newVersionSelection;
    }

    const versionDropdownOptions = $derived(
        selectedModVersions.map((v) => ({
            value: v.id,
            label: v.version_number,
            subtitle: isVersionCompatible(v)
                ? t('instanceView.downloadMods.compatible')
                : v.game_versions?.slice(0, 2).join(", "),
        })),
    );

    $effect(() => {
        if (selectedMod && !reviewing) {
            loadVersions(selectedMod.project_id);
        }
    });
</script>

<div class="dm-root">
    {#if reviewing}
        <!-- ─── REVIEW PANE ─── -->
        <div class="dm-review">
            <div class="dm-review-header">
                <div>
                    <span class="dm-section-label">{t('instanceView.downloadMods.sectionLabel')}</span>
                    <h2 class="dm-review-title">{t('instanceView.downloadMods.reviewTitle')}</h2>
                </div>
                <button
                    class="dm-back-btn"
                    onclick={() => (reviewing = false)}
                    disabled={downloading}
                >
                    {t('instanceView.downloadMods.back')}
                </button>
            </div>

            <div class="dm-review-body">
                {#if resolvingDeps}
                    <div class="dm-center-state">
                        <Loading />
                        <p>{t('instanceView.downloadMods.resolvingDeps')}</p>
                    </div>
                {:else if downloadQueue.length === 0}
                    <div class="dm-center-state">
                        <p>{t('instanceView.downloadMods.allInstalled')}</p>
                        <span style="font-size:0.75rem; opacity:0.5;">{t('instanceView.downloadMods.allInstalledSub')}</span>
                    </div>
                {:else}
                    <div class="dm-queue-box">
                        <p class="dm-queue-subtitle">
                            {downloadQueue.length}
                            {downloadQueue.length === 1
                                ? t('instanceView.downloadMods.file_one')
                                : t('instanceView.downloadMods.file_other')} para descargar:
                        </p>
                        <div class="dm-queue-list">
                            {#each downloadQueue as item}
                                <div class="dm-queue-item">
                                    {#if item.iconUrl}
                                        <img src={item.iconUrl} alt="" class="dm-queue-icon-img" />
                                    {:else}
                                        <span class="dm-queue-icon">📦</span>
                                    {/if}
                                    <div class="dm-queue-item-info">
                                        {#if item.projectTitle}
                                            <span class="dm-queue-title">{item.projectTitle}</span>
                                        {/if}
                                        <span class="dm-queue-filename">{item.filename}</span>
                                    </div>
                                </div>
                            {/each}
                        </div>
                    </div>

                    <div class="dm-review-footer">
                        <span class="dm-review-count">
                            <strong>{downloadQueue.length}</strong>
                            {downloadQueue.length !== 1
                                ? t('instanceView.downloadMods.file_other')
                                : t('instanceView.downloadMods.file_one')}
                        </span>
                        <button
                            class="dm-primary-btn"
                            onclick={confirmDownload}
                            disabled={downloading}
                        >
                            {#if downloading}
                                <Loading /> {t('instanceView.downloadMods.downloading')}
                            {:else}
                                {t('instanceView.downloadMods.confirmDownload')}
                            {/if}
                        </button>
                    </div>
                {/if}
            </div>
        </div>
    {:else}
        <!-- ─── BROWSE LAYOUT ─── -->
        <div class="dm-layout">
            <!-- Left Sidebar -->
            <aside class="dm-sidebar">
                <div class="dm-sidebar-top">
                    <span class="dm-section-label">{t('instanceView.downloadMods.categoriesLabel')}</span>
                    <button
                        class="dm-cat-btn {activeCategory === null
                            ? 'active'
                            : ''}"
                        onclick={() => handleCategoryClick(null)}
                    >
                        {t('instanceView.downloadMods.allCategories')}
                    </button>
                    {#each categories as cat}
                        <button
                            class="dm-cat-btn {activeCategory === cat
                                ? 'active'
                                : ''}"
                            onclick={() => handleCategoryClick(cat)}
                        >
                            {cat}
                        </button>
                    {/each}
                </div>

                <div class="dm-sidebar-middle">
                    <Dropdown
                        label={t('instanceView.downloadMods.sortLabel')}
                        bind:value={sortIndex}
                        options={sortOptions}
                        onchange={() => performSearch(true)}
                    />
                </div>

                <div class="dm-basket-card">
                    <div class="dm-basket-header">
                        <span class="dm-basket-label">{t('instanceView.downloadMods.selectionLabel')}</span>
                        <span class="dm-basket-badge">{basket.size}</span>
                    </div>
                    <p class="dm-basket-desc">{t('instanceView.downloadMods.selectionDesc')}</p>
                    <button
                        class="dm-primary-btn dm-full-width"
                        disabled={basket.size === 0}
                        onclick={startReview}
                    >
                        {t('instanceView.downloadMods.reviewBtn')}
                    </button>
                </div>
            </aside>

            <!-- Main -->
            <main class="dm-main">
                <div class="dm-search-bar-wrap">
                    <span class="dm-search-icon">
                        <svg
                            width="15"
                            height="15"
                            viewBox="0 0 24 24"
                            fill="none"
                            stroke="currentColor"
                            stroke-width="2"
                        >
                            <circle cx="11" cy="11" r="8" /><path
                                d="m21 21-4.35-4.35"
                            />
                        </svg>
                    </span>
                    <input
                        class="dm-search-input"
                        type="text"
                        bind:value={query}
                        placeholder={t('instanceView.downloadMods.searchPlaceholder')}
                        onkeydown={(e) =>
                            e.key === "Enter" && performSearch(true)}
                    />
                    {#if query}
                        <button
                            class="dm-search-clear"
                            onclick={() => {
                                query = "";
                                performSearch(true);
                            }}>×</button
                        >
                    {/if}
                </div>

                {#if totalHits > 0 && !searching}
                    <div class="dm-results-meta">
                        <span>{t('instanceView.downloadMods.resultsFound').replace('{count}', totalHits.toLocaleString())}</span>
                    </div>
                {/if}

                <div class="dm-results-area">
                    {#if searching}
                        <div class="dm-center-state">
                            <Loading />
                            <p>{t('instanceView.downloadMods.searching')}</p>
                        </div>
                    {:else if allHits.length > 0}
                        <div class="dm-vlist-wrap">
                            <VirtualList
                                items={allHits}
                                itemHeight={130}
                                onNearEnd={handleNearEnd}
                            >
                                {#snippet children(project)}
                                    <!-- svelte-ignore a11y_click_events_have_key_events -->
                                    <!-- svelte-ignore a11y_no_static_element_interactions -->
                                    <div
                                        class="dm-mod-card-v {selectedMod?.project_id ===
                                        project.project_id
                                            ? 'selected'
                                            : ''}"
                                        onclick={() => (selectedMod = project)}
                                    >
                                        <div class="dm-mod-icon-v">
                                            {#if project.icon_url}
                                                <img
                                                    src={project.icon_url}
                                                    alt={project.title}
                                                    loading="lazy"
                                                />
                                            {:else}
                                                <span class="dm-mod-icon-placeholder">📦</span>
                                            {/if}
                                        </div>
                                        <div class="dm-mod-body-v">
                                            <div class="dm-mod-top-v">
                                                <h4 class="dm-mod-title-v" title={project.title}>
                                                    {project.title}
                                                </h4>
                                                <div class="dm-mod-badges-v">
                                                    {#if isModInstalled(project)}
                                                        <span class="dm-installed-badge">{t('instanceView.downloadMods.installed')}</span>
                                                    {/if}
                                                    {#if !isModCompatible(project)}
                                                        <span class="dm-incompat-badge" title={t('instanceView.downloadMods.noCompatibleVersions').replace('{version}', gameVersion)}>{t('instanceView.downloadMods.noVersionCompat').replace('{version}', gameVersion)}</span>
                                                    {/if}
                                                </div>
                                            </div>
                                            <span class="dm-mod-author-v">
                                                {t('instanceView.downloadMods.by')} {project.author}
                                            </span>
                                            <p class="dm-mod-desc-v">{project.description}</p>
                                        </div>
                                        <div class="dm-mod-actions-v">
                                            <span class="dm-mod-stat">↓ {formatNumber(project.downloads)}</span>
                                            <button
                                                class="dm-select-btn {basket.has(project.project_id) ? 'selected' : ''}"
                                                onclick={(e) => { e.stopPropagation(); toggleBasket(project); }}
                                            >
                                                {basket.has(project.project_id) ? t('instanceView.downloadMods.selected') : t('instanceView.downloadMods.select')}
                                            </button>
                                        </div>
                                    </div>
                                {/snippet}
                            </VirtualList>
                            {#if loadingMore}
                                <div class="dm-vlist-loading">
                                    <Loading class="dm-spinning" />
                                    <span>{t('instanceView.downloadMods.loadingMore')}</span>
                                </div>
                            {:else if allHits.length >= totalHits && totalHits > 0}
                                <div class="dm-vlist-end">
                                    <span class="dm-end-label">— {t('instanceView.downloadMods.endOfResults').replace('{count}', allHits.length.toString())} —</span>
                                </div>
                            {/if}
                        </div>
                    {:else}
                        <div class="dm-center-state">
                            <p>{t('instanceView.downloadMods.noResults')}</p>
                            <button
                                class="dm-ghost-btn"
                                onclick={() => {
                                    query = "";
                                    activeCategory = null;
                                    performSearch(true);
                                }}
                            >
                                {t('instanceView.downloadMods.clearFilters')}
                            </button>
                        </div>
                    {/if}
                </div>
            </main>

            <!-- Details Panel -->
            {#if selectedMod}
                <aside class="dm-details">
                    <button
                        class="dm-close-btn"
                        aria-label={t('instanceView.downloadMods.closeDetails')}
                        onclick={() => (selectedMod = null)}
                    >
                        <svg
                            width="14"
                            height="14"
                            viewBox="0 0 24 24"
                            fill="none"
                            stroke="currentColor"
                            stroke-width="2.5"
                        >
                            <path d="M18 6 6 18M6 6l12 12" />
                        </svg>
                    </button>
                    <div class="dm-details-scroll">
                        <div class="dm-details-icon">
                            {#if selectedMod.icon_url}
                                <img
                                    src={selectedMod.icon_url}
                                    alt={selectedMod.title}
                                />
                            {:else}
                                <span>📦</span>
                            {/if}
                        </div>
                        <h3 class="dm-details-title">{selectedMod.title}</h3>
                        <p class="dm-details-author">{t('instanceView.downloadMods.by')} {selectedMod.author}</p>

                        <div class="dm-details-stat-row">
                            <div class="dm-details-stat">
                                <span class="dm-details-stat-label"
                                    >{t('instanceView.downloadMods.downloads')}</span
                                >
                                <span class="dm-details-stat-value"
                                    >{formatNumber(selectedMod.downloads)}</span
                                >
                            </div>
                        </div>

                        <div class="dm-tags">
                            {#each selectedMod.categories.slice(0, 4) as cat}
                                <span class="dm-tag">{cat}</span>
                            {/each}
                        </div>

                        <!-- Version Selector -->
                        <div class="dm-details-version-row">
                            <span class="dm-details-version-label">{t('instanceView.downloadMods.versionLabel')}</span>
                            {#if loadingVersions}
                                <span class="dm-loading-versions">{t('instanceView.downloadMods.loadingVersions')}</span>
                            {:else if selectedModVersions.length === 0}
                                <span class="dm-no-versions-msg">{t('instanceView.downloadMods.noCompatibleVersions').replace('{version}', gameVersion)}</span>
                                <span class="dm-loading-versions">{t('instanceView.downloadMods.noCompatibleDesc')}</span>
                            {:else}
                                <Dropdown
                                    bind:value={selectedVersionId}
                                    options={versionDropdownOptions}
                                    placeholder={t('instanceView.downloadMods.anyVersion')}
                                    onchange={onVersionChange}
                                />
                            {/if}
                        </div>

                        <p class="dm-details-desc">{selectedMod.description}</p>

                        <button
                            class="dm-primary-btn dm-full-width {basket.has(
                                selectedMod.project_id,
                            )
                                ? 'dm-btn-remove'
                                : ''}"
                            onclick={() => toggleBasket(selectedMod!)}
                        >
                            {basket.has(selectedMod.project_id)
                                ? t('instanceView.downloadMods.removeSelection')
                                : t('instanceView.downloadMods.selectToDownload')}
                        </button>
                    </div>
                </aside>
            {/if}
        </div>
    {/if}
</div>
