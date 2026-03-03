<script lang="ts">
    import { invoke } from "@tauri-apps/api/core";
    import { type InstanceDto } from "./types";
    interface Asset {
        label: string;
        progress: number; // 0–100
    }

    interface DownloadItem {
        id: number;
        name: string;
        assets: Asset[];
        status: "downloading" | "queued" | "done" | "error";
    }

    interface RecentInstance {
        id: number;
        icon: string;
        name: string;
        version: string;
        lastPlayed: string;
    }

    interface Props {
        onclose?: () => void;
    }

    let { onclose }: Props = $props();

    const downloads: DownloadItem[] = [
        // {
        //     id: 1,
        //     name: "Minecraft 1.21.4",
        //     status: "downloading",
        //     assets: [
        //         { label: "client", progress: 100 },
        //         { label: "assets", progress: 100 },
        //         { label: "libraries", progress: 0 },
        //         { label: "natives", progress: 0 },
        //     ],
        // },
        // {
        //     id: 2,
        //     name: "Java 21 (Temurin)",
        //     status: "done",
        //     assets: [
        //         { label: "jdk", progress: 100 },
        //         { label: "extras", progress: 100 },
        //         { label: "tools", progress: 100 },
        //         { label: "src", progress: 100 },
        //     ],
        // },
        // {
        //     id: 3,
        //     name: "Fabric Loader 0.16",
        //     status: "queued",
        //     assets: [
        //         { label: "loader", progress: 0 },
        //         { label: "api", progress: 0 },
        //         { label: "mixin", progress: 0 },
        //         { label: "asm", progress: 0 },
        //     ],
        // },
        // {
        //     id: 4,
        //     name: "Assets Index",
        //     status: "error",
        //     assets: [
        //         { label: "index", progress: 18 },
        //         { label: "objects", progress: 0 },
        //         { label: "virtual", progress: 0 },
        //         { label: "legacy", progress: 0 },
        //     ],
        // },
    ];

    const recents: RecentInstance[] = [
        // {
        //     id: 1,
        //     icon: "⚔️",
        //     name: "Survival 1.21.4",
        //     version: "Vanilla · 1.21.4",
        //     lastPlayed: "2h ago",
        // },
        // {
        //     id: 2,
        //     icon: "🧪",
        //     name: "Modded Testing",
        //     version: "Fabric 0.16 · 1.20.1",
        //     lastPlayed: "Yesterday",
        // },
        // {
        //     id: 3,
        //     icon: "🏗️",
        //     name: "Creative World",
        //     version: "Vanilla · 1.21.1",
        //     lastPlayed: "3 days ago",
        // },
    ];

    const statusLabel: Record<DownloadItem["status"], string> = {
        downloading: "Downloading",
        queued: "Queued",
        done: "Done",
        error: "Error",
    };
    let runningInstances = $state<{ name: string; sub: string }[]>([]);

    async function fetchRunning() {
        const dtos = await invoke<InstanceDto[]>("get_running");
        runningInstances = dtos.map((d) => ({
            name: d.name,
            sub: `Running · ${d.version}`,
        }));
    }

    $effect(() => {
        fetchRunning();
        const interval = setInterval(fetchRunning, 3000); // polling cada 3s
        return () => clearInterval(interval);
    });

    async function killInstance(name: string) {
        await invoke("kill_instance", { instanceName: name });
        runningInstances = runningInstances.filter((i) => i.name !== name);
    }

    function overallProgress(assets: Asset[]): number {
        return Math.round(
            assets.reduce((sum, a) => sum + a.progress, 0) / assets.length,
        );
    }
</script>

<div class="qm-root">
    <!-- Header -->
    <div class="qm-header">
        <span class="qm-label">Quick Menu</span>
        <button class="qm-close-btn" onclick={onclose}>✕</button>
    </div>

    <div class="qm-scroll">
        <!-- Running instance -->
        <section class="qm-section">
            <span class="qm-section-label">Running</span>
            {#if runningInstances.length > 0}
                {#each runningInstances as inst}
                    <div class="qm-active-card">
                        <div class="qm-status-dot running"></div>
                        <div class="qm-active-info">
                            <span class="qm-active-name">{inst.name}</span>
                            <span class="qm-active-sub">{inst.sub}</span>
                        </div>
                        <button
                            class="qm-kill-btn"
                            onclick={() => killInstance(inst.name)}>Kill</button
                        >
                    </div>
                {/each}
            {:else}
                <span class="qm-section-label" style="color: #333"
                    >No instances running</span
                >
            {/if}
        </section>

        <!-- Download queue -->
        <section class="qm-section">
            <span class="qm-section-label">Download Queue</span>
            <div class="qm-dl-list">
                {#each downloads as dl}
                    <div class="qm-dl-item">
                        <!-- Name + overall % + badge -->
                        <div class="qm-dl-row">
                            <span class="qm-dl-name">{dl.name}</span>
                            <div class="qm-dl-row-right">
                                {#if dl.status === "downloading"}
                                    <span class="qm-dl-pct"
                                        >{overallProgress(dl.assets)}%</span
                                    >
                                {/if}
                                <span class="qm-badge qm-badge--{dl.status}"
                                    >{statusLabel[dl.status]}</span
                                >
                            </div>
                        </div>

                        <!-- Segmented bar: one segment per asset -->
                        <div class="qm-seg-bar">
                            {#each dl.assets as asset}
                                <div
                                    class="qm-seg"
                                    title="{asset.label}: {asset.progress}%"
                                >
                                    <div
                                        class="qm-seg-fill qm-seg-fill--{dl.status}"
                                        style="width: {asset.progress}%"
                                    ></div>
                                </div>
                            {/each}
                        </div>

                        <!-- Per-asset labels aligned under each segment -->
                        <div class="qm-asset-labels">
                            {#each dl.assets as asset}
                                <span
                                    class="qm-asset-label"
                                    class:done={asset.progress === 100}
                                    class:active={asset.progress > 0 &&
                                        asset.progress < 100}
                                >
                                    {asset.label}
                                </span>
                            {/each}
                        </div>
                    </div>
                {/each}
            </div>
        </section>

        <!-- Quick actions -->
        <section class="qm-section">
            <span class="qm-section-label">Quick Actions</span>
            <!-- <div class="qm-actions-grid">
                <button class="qm-action-btn">
                    <span class="qm-action-icon">＋</span>
                    New Instance
                </button>
                <button class="qm-action-btn">
                    <span class="qm-action-icon">📂</span>
                    Open Folder
                </button>
                <button class="qm-action-btn">
                    <span class="qm-action-icon">🔄</span>
                    Check Updates
                </button>
                <button class="qm-action-btn">
                    <span class="qm-action-icon">🗑</span>
                    Clear Cache
                </button>
            </div> -->
        </section>

        <!-- Recent instances -->
        <section class="qm-section">
            <span class="qm-section-label">Recent</span>
            <div class="qm-recent-list">
                {#each recents as inst}
                    <div class="qm-recent-row">
                        <div class="qm-recent-icon">{inst.icon}</div>
                        <div class="qm-recent-info">
                            <span class="qm-recent-name">{inst.name}</span>
                            <span class="qm-recent-sub"
                                >{inst.version} · {inst.lastPlayed}</span
                            >
                        </div>
                        <button class="qm-recent-play">▶</button>
                    </div>
                {/each}
            </div>
        </section>
    </div>

    <!-- Footer -->
    <div class="qm-footer">
        <span class="qm-version">CubicLauncher 2604a</span>
        <button class="qm-settings-btn">xd</button>
    </div>
</div>
