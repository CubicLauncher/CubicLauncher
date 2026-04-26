<script lang="ts">
    import { invoke, convertFileSrc } from "@tauri-apps/api/core";
    import { InstState, type InstanceDto } from "$lib/types/types";
    import InstanceDetails from "./InstanceDetails.svelte";
    import { launchInstance } from "$lib/api/cubicApi";
    import ModsRow from "./ModsRow.svelte";
    import QuickOptionsPanel from "./QuickOptionsPanel.svelte";
    import Loading from "../../icons/Loading.svelte";
    import Check from "../../icons/Check.svelte";
    import { t } from "$lib/i18n";
    import { killInst } from "$lib/api/launcherService";

    let { selectedInstance } = $props<{ selectedInstance: InstanceDto }>();
    let activeTab = $state("detalles");
    let screenshotUrl = $state<string | null>(null);
    let allScreenshots = $state<string[]>([]);
    let showPicker = $state(false);
    let bannerVersion = $state(0);
    let bannerState = $derived.by(() => {
        if (selectedInstance.status === InstState.Started) return "Started";
        if (selectedInstance.status === InstState.Off) return "Idle";
        if (selectedInstance.status === InstState.Error) return "Error";
        if (selectedInstance.status === InstState.Starting) return "Starting";
        return "Idle";
    });
    const supportsMods = $derived(selectedInstance.loader !== "Vanilla");

    $effect(() => {
        if (!supportsMods && activeTab === "mods") {
            activeTab = "detalles";
        }
    });

    async function fetchScreenshot() {
        if (!selectedInstance) return;

        const path = await invoke<string | null>("get_instance_banner", {
            instanceId: selectedInstance.uuid,
        });
        if (path) {
            const clean = decodeURIComponent(path);
            screenshotUrl = convertFileSrc(clean);
        } else {
            screenshotUrl = null;
        }
    }

    async function pickBanner() {
        allScreenshots = await invoke<string[]>(
            "get_all_instance_screenshots",
            {
                instanceName: selectedInstance.name,
            },
        );
        showPicker = true;
    }

    async function selectScreenshot(path: string) {
        await invoke("set_instance_cover_image", {
            instanceId: selectedInstance.uuid,
            path: path,
        });
        showPicker = false;
        bannerVersion++;
    }

    async function resetBanner() {
        await invoke("reset_instance_cover_image", {
            instanceId: selectedInstance.uuid,
        });
        bannerVersion++;
    }

    $effect(() => {
        // Tracking both selectedInstance.uuid and bannerVersion ensures we re-fetch
        // whenever the instance changes OR when the banner is manually updated/reset.
        selectedInstance.uuid;
        bannerVersion;
        fetchScreenshot();
    });

    const formatter = new Intl.DateTimeFormat("es-ES", {
        year: "numeric",
        month: "long",
        day: "2-digit",
        hour: "2-digit",
        minute: "2-digit",
    });

    function formatDate(unix_date: number): string {
        if (unix_date < 1) {
            return t("instanceView.neverPlayed");
        }
        let date = new Date(unix_date * 1000);
        return formatter.format(date);
    }
</script>

<div class="instance-view">
    {#if showPicker}
        <div
            class="screenshot-picker-overlay"
            role="button"
            tabindex="0"
            onclick={() => (showPicker = false)}
            onkeydown={(e) => e.key === "Escape" && (showPicker = false)}
        >
            <div
                class="screenshot-picker-modal"
                role="dialog"
                aria-modal="true"
                tabindex="-1"
                onclick={(e) => e.stopPropagation()}
                onkeydown={(e) => e.stopPropagation()}
            >
                <div class="picker-header">
                    <h3>{t("instanceView.pickBannerTitle")}</h3>
                    <button
                        class="close-btn"
                        onclick={() => (showPicker = false)}>✕</button
                    >
                </div>
                <div class="picker-content">
                    {#if allScreenshots.length === 0}
                        <div class="empty-picker">
                            {t("instanceView.noScreenshots")}
                        </div>
                    {:else}
                        <div class="picker-grid">
                            {#each allScreenshots as path}
                                <button
                                    class="picker-item"
                                    onclick={() => selectScreenshot(path)}
                                >
                                    <img
                                        src={convertFileSrc(path)}
                                        alt="Screenshot"
                                    />
                                </button>
                            {/each}
                        </div>
                    {/if}
                </div>
            </div>
        </div>
    {/if}
    <section
        class="hero-section"
        style={screenshotUrl
            ? `background-image: linear-gradient(to bottom, rgba(0, 0, 0, 0.2), rgba(0, 0, 0, 0.8)), url(${screenshotUrl})`
            : "background: linear-gradient(180deg, rgba(255, 255, 255, 0.02) 0%, rgba(0, 0, 0, 0) 100%);"}
    >
        <img
            class="instance-big-icon"
            src={selectedInstance.icon || "/images/cubic.svg"}
            alt="Icon"
        />
        <div class="instance-title-area">
            <h2>{selectedInstance.name}</h2>
            <div class="last-played">
                {t("instanceView.lastPlayed").replace(
                    "{date}",
                    formatDate(selectedInstance.last_played),
                )}
            </div>
            {#if bannerState == "Started"}
                <button
                    class="play-btn"
                    onclick={() => killInst(selectedInstance.uuid)}
                    >{t("instanceView.close")}</button
                >
            {:else if bannerState == "Starting"}
                <button class="play-btn" disabled
                    >{t("instanceView.playBtn")}</button
                >
            {:else}
                <button
                    class="play-btn"
                    onclick={() => launchInstance(selectedInstance)}
                    >{t("instanceView.playBtn")}</button
                >
            {/if}
        </div>

        <div class="banner-controls">
            <button
                class="banner-btn"
                onclick={pickBanner}
                title={t("instanceView.changeBannerTitle")}
            >
                <svg
                    xmlns="http://www.w3.org/2000/svg"
                    width="16"
                    height="16"
                    viewBox="0 0 24 24"
                    fill="none"
                    stroke="currentColor"
                    stroke-width="2"
                    stroke-linecap="round"
                    stroke-linejoin="round"
                    ><path
                        d="M23 19a2 2 0 0 1-2 2H3a2 2 0 0 1-2-2V8a2 2 0 0 1 2-2h4l2-3h6l2 3h4a2 2 0 0 1 2 2z"
                    /><circle cx="12" cy="13" r="4" /></svg
                >
                <span>{t("instanceView.changeBanner")}</span>
            </button>
        </div>
    </section>
    {#if bannerState}
        {#if bannerState === "Idle"}
            <div class="banner-status">
                <span>{t("instanceView.status.idle")}</span>
            </div>
        {:else if bannerState === "Started"}
            <div class="banner-status-started">
                <div class="banner-status-row">
                    <Check class="banner-status-icon" />
                    <span>{t("instanceView.status.started")}</span>
                </div>
            </div>
        {:else if bannerState === "Starting"}
            <div class="banner-status-starting">
                <div class="banner-status-row">
                    <Loading class="banner-status-icon-rotate" />
                    <span>{t("instanceView.status.starting")}</span>
                </div>
            </div>
        {/if}
    {/if}

    <div class="tabs-nav">
        <button
            class="tab-item {activeTab === 'detalles' ? 'active' : ''}"
            onclick={() => (activeTab = "detalles")}
        >
            {t("instanceView.tabs.details")}
        </button>
        <button
            class="tab-item {activeTab === 'mods' ? 'active' : ''}"
            onclick={() => supportsMods && (activeTab = "mods")}
            disabled={!supportsMods}
        >
            {t("instanceView.tabs.mods")}
        </button>
        <button
            class="tab-item {activeTab === 'opciones' ? 'active' : ''}"
            onclick={() => (activeTab = "opciones")}
        >
            {t("instanceView.tabs.options")}
        </button>
    </div>

    <div class="tab-content">
        {#if activeTab === "detalles"}
            <div class="tab-pane">
                <InstanceDetails instance={selectedInstance} />
            </div>
        {:else if activeTab === "mods"}
            <div class="tab-pane">
                <ModsRow instanceId={selectedInstance.uuid} />
            </div>
        {:else if activeTab === "opciones"}
            <div class="tab-pane">
                <QuickOptionsPanel instance={selectedInstance} />
            </div>
        {/if}
    </div>
</div>
