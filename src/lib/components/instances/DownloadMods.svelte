<script lang="ts">
    import {
        searchModrinth,
        getModrinthProjectVersions,
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
    import { onMount, tick } from "svelte";

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

    // Intersection observer sentinel
    let sentinel = $state<HTMLElement | null>(null);
    let observer: IntersectionObserver | null = null;

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
        const match = versionStr.match(/1\.\d+(\.\d+)?/);
        return match ? match[0] : versionStr;
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
            const result = await searchModrinth(
                query,
                instance.loader,
                gameVersion,
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

    function setupObserver() {
        if (observer) observer.disconnect();
        if (!sentinel) return;

        observer = new IntersectionObserver(
            async (entries) => {
                if (
                    entries[0].isIntersecting &&
                    !loadingMore &&
                    !searching &&
                    allHits.length < totalHits
                ) {
                    await performSearch(false);
                }
            },
            { rootMargin: "100px" },
        );

        observer.observe(sentinel);
    }

    $effect(() => {
        if (sentinel) {
            setupObserver();
        }
    });

    onMount(() => {
        performSearch();
        return () => observer?.disconnect();
    });

    function handleCategoryClick(cat: string | null) {
        activeCategory = cat;
        performSearch(true);
    }

    function toggleBasket(project: ModrinthProject) {
        let newBasket = new Map(basket);
        if (newBasket.has(project.project_id)) {
            newBasket.delete(project.project_id);
        } else {
            newBasket.set(project.project_id, project);
        }
        basket = newBasket;
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
                    const latest = versions[0];
                    const primaryFile =
                        latest.files.find((f: any) => f.primary) ||
                        latest.files[0];
                    if (
                        !queue.find((q) => q.filename === primaryFile.filename)
                    ) {
                        queue.push({
                            url: primaryFile.url,
                            filename: primaryFile.filename,
                        });
                    }

                    if (latest.dependencies) {
                        for (const dep of latest.dependencies) {
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
                                    <span class="dm-queue-icon">📦</span>
                                    <span class="dm-queue-filename"
                                        >{item.filename}</span
                                    >
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
                    <span class="dm-section-label">{t('instanceView.downloadMods.sortLabel')}</span>
                    <select
                        class="dm-sort-select"
                        bind:value={sortIndex}
                        onchange={() => performSearch(true)}
                    >
                        {#each sortOptions as opt}
                            <option value={opt.value}>{opt.label}</option>
                        {/each}
                    </select>
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
                        <div class="dm-grid">
                            {#each allHits as project (project.project_id)}
                                <!-- svelte-ignore a11y_click_events_have_key_events -->
                                <!-- svelte-ignore a11y_no_static_element_interactions -->
                                <div
                                    class="dm-mod-card {selectedMod?.project_id ===
                                    project.project_id
                                        ? 'selected'
                                        : ''}"
                                    onclick={() => (selectedMod = project)}
                                >
                                    <div class="dm-card-top">
                                        <div class="dm-mod-icon">
                                            {#if project.icon_url}
                                                <img
                                                    src={project.icon_url}
                                                    alt={project.title}
                                                    loading="lazy"
                                                />
                                            {:else}
                                                <span
                                                    class="dm-mod-icon-placeholder"
                                                    >📦</span
                                                >
                                            {/if}
                                        </div>
                                        <div class="dm-mod-meta">
                                            <h4
                                                class="dm-mod-title"
                                                title={project.title}
                                            >
                                                {project.title}
                                            </h4>
                                            <span class="dm-mod-author"
                                                >{t('instanceView.downloadMods.by')} {project.author}</span
                                            >
                                        </div>
                                    </div>
                                    <p class="dm-mod-desc">
                                        {project.description}
                                    </p>
                                    <div class="dm-card-bottom">
                                        <span class="dm-mod-stat"
                                            >↓ {formatNumber(
                                                project.downloads,
                                            )}</span
                                        >
                                        <button
                                            class="dm-select-btn {basket.has(
                                                project.project_id,
                                            )
                                                ? 'selected'
                                                : ''}"
                                            onclick={(e) => {
                                                e.stopPropagation();
                                                toggleBasket(project);
                                            }}
                                        >
                                            {basket.has(project.project_id)
                                                ? t('instanceView.downloadMods.selected')
                                                : t('instanceView.downloadMods.select')}
                                        </button>
                                    </div>
                                </div>
                            {/each}
                        </div>

                        <!-- Sentinel for infinite scroll -->
                        <div bind:this={sentinel} class="dm-sentinel">
                            {#if loadingMore}
                                <Loading />
                                <span>{t('instanceView.downloadMods.loadingMore')}</span>
                            {:else if allHits.length >= totalHits}
                                <span class="dm-end-label"
                                    >— {t('instanceView.downloadMods.endOfResults').replace('{count}', allHits.length.toString())} —</span
                                >
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
