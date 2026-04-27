<script lang="ts">
    import { invoke } from "@tauri-apps/api/core";
    import { onMount } from "svelte";
    import "../styles/App.css";
    import { launcherStore } from "$lib/state/state.svelte";
    import { getVersions, initPolling } from "$lib/api/launcherService";
    import type { InstanceDto } from "$lib/types/types";
    import Sidebar from "$lib/components/layout/Sidebar.svelte";
    import InstanceView from "$lib/components/instances/InstanceView.svelte";
    import Drawer from "$lib/components/layout/Drawer.svelte";
    import Settings from "$lib/components/settings/Settings.svelte";
    import CreateInstanceModal from "$lib/components/instances/CreateInstanceModal.svelte";
    import VersionDownloader from "$lib/components/layout/VersionDownloader.svelte";
    import { t } from "$lib/i18n";
    import NotificationContainer from "$lib/components/ui/NotificationContainer.svelte";
    import DownloadProgressBar from "$lib/components/ui/DownloadProgressBar.svelte";
    import { checkForUpdates } from "$lib/api/updaterServices";

    let selectedInstance = $state<InstanceDto | null>(null);
    let quickMenuOpen = $state(false);
    let versionDownloaderOpen = $state(false);
    let openCreateModal = $state(false);

    onMount(() => {
        getVersions();
        const unlistenPromise = initPolling();
        invoke("start_polling");

        // Auto-select first instance if available
        if (launcherStore.loadedInstances.length > 0 && !selectedInstance) {
            selectedInstance = launcherStore.loadedInstances[0];
        }
        if (launcherStore.settings.auto_updates) {
            checkForUpdates(true);
        }
        return () => {
            unlistenPromise.then((unlisten) => unlisten());
        };
    });
    $effect(() => {
        const current = selectedInstance;
        if (current && launcherStore.loadedInstances.length > 0) {
            const updated = launcherStore.loadedInstances.find(
                (i) => i.uuid === current.uuid,
            );

            if (updated && updated !== current) {
                selectedInstance = updated;
            }
        }
    });
</script>

<div class="app-container">
    <Sidebar
        bind:selectedInstance
        bind:openCreateModal
        onopenquickmenu={() => (quickMenuOpen = true)}
        onopenversiondownloader={() => (versionDownloaderOpen = true)}
    />

    <main class="main-content">
        <div
            class="background-overlay"
            style="background-image: url('/images/bg.png');"
        ></div>

        {#if selectedInstance}
            <InstanceView {selectedInstance} />
        {:else}
            <div class="empty-state">
                <img
                    src="/images/cubic.svg"
                    alt="Cubic"
                    style="width: 120px; opacity: 0.1; filter: grayscale(1);"
                />
                <h2>{t("main.noInstanceTitle")}</h2>
                <p>{t("main.noInstanceDesc")}</p>
            </div>
        {/if}
    </main>
</div>

<Drawer bind:open={quickMenuOpen} direction="right">
    <Settings onclose={() => (quickMenuOpen = false)} />
</Drawer>

<Drawer bind:open={versionDownloaderOpen} direction="right">
    <VersionDownloader onclose={() => (versionDownloaderOpen = false)} />
</Drawer>

<CreateInstanceModal bind:open={openCreateModal} />

<NotificationContainer />
<DownloadProgressBar />
