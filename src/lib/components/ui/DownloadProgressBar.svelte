<script lang="ts">
    import { listen } from "@tauri-apps/api/event";
    import { onMount } from "svelte";
    import type { AppEvent } from "$lib/types/types";
    import { getDownloadQueue } from "$lib/api/cubicApi";

    type SegmentKey = "Library" | "Asset" | "Native" | "Client";
    const SEGMENTS: SegmentKey[] = ["Library", "Asset", "Native", "Client"];

    const SEGMENT_COLORS: Record<SegmentKey, string> = {
        Library: "#4ade80",
        Asset: "#60a5fa",
        Native: "#f59e0b",
        Client: "#a78bfa",
    };

    const SEGMENT_LABELS: Record<SegmentKey, string> = {
        Library: "LIB",
        Asset: "ASSET",
        Native: "NAT",
        Client: "CLIENT",
    };

    interface SegmentProgress {
        current: number;
        total: number;
    }

    interface DownloadItem {
        version: string;
        activeType: SegmentKey | null;
        segments: Record<SegmentKey, SegmentProgress>;
        done: boolean;
    }

    function emptySegments(): Record<SegmentKey, SegmentProgress> {
        return {
            Library: { current: 0, total: 0 },
            Asset: { current: 0, total: 0 },
            Native: { current: 0, total: 0 },
            Client: { current: 0, total: 0 },
        };
    }

    let downloads = $state<Map<string, DownloadItem>>(new Map());
    let expanded = $state(false);
    let activeCount = $derived(
        [...downloads.values()].filter((d) => !d.done).length,
    );
    let doneCount = $derived(
        [...downloads.values()].filter((d) => d.done).length,
    );

    onMount(() => {
        let pollInterval: ReturnType<typeof setInterval> | null = null;

        async function syncQueue() {
            const queue = await getDownloadQueue();
            let addedNew = false;
            for (const item of queue) {
                if (!downloads.has(item.version)) {
                    downloads.set(item.version, {
                        version: item.version,
                        activeType: null,
                        segments: emptySegments(),
                        done: item.status === "done",
                    });
                    addedNew = true;
                }
            }
            if (addedNew) {
                downloads = new Map(downloads);
                expanded = true;
            }
        }

        function startPolling() {
            if (pollInterval) return;
            pollInterval = setInterval(syncQueue, 1500);
        }

        function stopPolling() {
            if (pollInterval) {
                clearInterval(pollInterval);
                pollInterval = null;
            }
        }

        syncQueue();
        startPolling();

        const unlisten = listen<AppEvent>("app-event", (event) => {
            const payload = event.payload;
            switch (payload.type) {
                case "DProgress": {
                    const { version, current, total, d_type } = payload.data;
                    const isNew = !downloads.has(version); // ← chequear ANTES del set
                    const existing = downloads.get(version) ?? {
                        version,
                        activeType: null,
                        segments: emptySegments(),
                        done: false,
                    };
                    const key = d_type as SegmentKey;
                    existing.segments[key] = { current, total };
                    existing.activeType = key;
                    existing.done = false;
                    downloads.set(version, { ...existing });
                    downloads = new Map(downloads);
                    if (isNew) expanded = true;
                    break;
                }
                case "DFinish": {
                    const { version } = payload.data;
                    const item = downloads.get(version);
                    if (item) {
                        downloads.set(version, {
                            ...item,
                            done: true,
                            activeType: null,
                        });
                        downloads = new Map(downloads);
                    }
                    setTimeout(() => {
                        downloads.delete(version);
                        downloads = new Map(downloads);
                        if (activeCount === 0) stopPolling();
                    }, 4000);
                    break;
                }
            }
        });

        return () => {
            unlisten.then((u) => u());
            stopPolling();
        };
    });

    function toggle() {
        expanded = !expanded;
    }

    function getSegmentPct(item: DownloadItem, key: SegmentKey): number {
        if (item.done) return 100;
        const s = item.segments[key];
        if (!s.total) return 0;
        return Math.round((s.current / s.total) * 100);
    }

    function getOverallPct(item: DownloadItem): number {
        if (item.done) return 100;
        const totalAll = SEGMENTS.reduce(
            (a, k) => a + item.segments[k].total,
            0,
        );
        const currentAll = SEGMENTS.reduce(
            (a, k) => a + item.segments[k].current,
            0,
        );
        return totalAll > 0 ? Math.round((currentAll / totalAll) * 100) : 0;
    }
</script>

<div class="dl-tray" class:expanded>
    <button class="dl-tray-tab" onclick={toggle}>
        <div class="dl-tray-tab-left">
            {#if activeCount > 0}
                <span class="dl-tray-spinner"></span>
                <span>{activeCount} descargando</span>
            {:else if doneCount > 0}
                <svg
                    width="13"
                    height="13"
                    viewBox="0 0 24 24"
                    fill="none"
                    stroke="currentColor"
                    stroke-width="2.5"
                    stroke-linecap="round"
                    stroke-linejoin="round"
                    ><polyline points="20 6 9 17 4 12" /></svg
                >
                <span>{doneCount} completadas</span>
            {:else}
                <svg
                    width="13"
                    height="13"
                    viewBox="0 0 24 24"
                    fill="none"
                    stroke="currentColor"
                    stroke-width="2"
                    stroke-linecap="round"
                    stroke-linejoin="round"
                    ><path
                        d="M12 15V3m0 12l-4-4m4 4l4-4M2 17l.621 2.485A2 2 0 0 0 4.561 21h14.878a2 2 0 0 0 1.94-1.515L22 17"
                    /></svg
                >
                <span>Sin descargas</span>
            {/if}
        </div>
        <svg
            class="dl-tray-chevron"
            width="11"
            height="11"
            viewBox="0 0 16 16"
            fill="none"
            stroke="currentColor"
            stroke-width="2"
            stroke-linecap="round"
            stroke-linejoin="round"
        >
            <path d="M4 6l4 4 4-4" />
        </svg>
    </button>

    <div class="dl-tray-body">
        {#if downloads.size === 0}
            <div class="dl-tray-empty">
                <svg
                    width="26"
                    height="26"
                    viewBox="0 0 24 24"
                    fill="none"
                    stroke="currentColor"
                    stroke-width="1.5"
                    stroke-linecap="round"
                    stroke-linejoin="round"
                    ><path
                        d="M12 15V3m0 12l-4-4m4 4l4-4M2 17l.621 2.485A2 2 0 0 0 4.561 21h14.878a2 2 0 0 0 1.94-1.515L22 17"
                    /></svg
                >
                <span class="dl-tray-empty-title">No hay descargas activas</span
                >
                <span class="dl-tray-empty-sub"
                    >Las versiones que descargues aparecerán aquí</span
                >
            </div>
        {:else}
            {#each [...downloads.values()] as item (item.version)}
                {@const overall = getOverallPct(item)}
                <div class="dl-tray-item" class:done={item.done}>
                    <div class="dl-tray-item-header">
                        <div class="dl-tray-item-left">
                            {#if item.done}
                                <svg
                                    width="10"
                                    height="10"
                                    viewBox="0 0 24 24"
                                    fill="none"
                                    stroke="var(--color-success)"
                                    stroke-width="2.5"
                                    stroke-linecap="round"
                                    stroke-linejoin="round"
                                    ><polyline points="20 6 9 17 4 12" /></svg
                                >
                            {:else}
                                <span class="dl-tray-spinner-sm"></span>
                            {/if}
                            <span class="dl-tray-version">{item.version}</span>
                        </div>
                        <span class="dl-tray-pct" class:done={item.done}
                            >{overall}%</span
                        >
                    </div>

                    <div class="dl-tray-segments">
                        {#each SEGMENTS as key}
                            {@const pct = getSegmentPct(item, key)}
                            {@const color = SEGMENT_COLORS[key]}
                            {@const isActive =
                                item.activeType === key && !item.done}
                            <div class="dl-tray-seg">
                                <div class="dl-tray-seg-header">
                                    <span
                                        class="dl-tray-seg-label"
                                        style:color={pct > 0 || isActive
                                            ? color
                                            : "var(--text-muted)"}
                                    >
                                        {SEGMENT_LABELS[key]}
                                    </span>
                                    <span
                                        class="dl-tray-seg-pct"
                                        style:color={pct > 0 ? color : "var(--text-muted)"}
                                    >
                                        {pct}%
                                    </span>
                                </div>
                                <div class="dl-tray-seg-track">
                                    <div
                                        class="dl-tray-seg-fill"
                                        class:active={isActive}
                                        style:width="{pct}%"
                                        style:background={color}
                                    ></div>
                                </div>
                            </div>
                        {/each}
                    </div>

                    <div class="dl-tray-overall-track">
                        <div
                            class="dl-tray-overall-fill"
                            style:width="{overall}%"
                        ></div>
                    </div>
                </div>
            {/each}
        {/if}
    </div>
</div>
