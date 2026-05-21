<script lang="ts">
    import { onMount } from "svelte";
    import "../styles/App.css";
    import { launcherStore } from "$lib/state/state.svelte";
    import { getVersions, syncSettings, initEventListeners } from "$lib/api/launcherService";
    import type { InstanceDto } from "$lib/types/types";
    import Sidebar from "$lib/components/layout/Sidebar.svelte";
    import InstanceView from "$lib/components/instances/InstanceView.svelte";
    import Drawer from "$lib/components/layout/Drawer.svelte";
    import NotificationContainer from "$lib/components/ui/NotificationContainer.svelte";
    import { t } from "$lib/i18n";
    import { applyTheme } from "$lib/api/themeManager";
    import { checkForUpdates } from "$lib/api/updaterServices";

    let selectedInstance = $state<InstanceDto | null>(null);
    let quickMenuOpen = $state(false);
    let versionDownloaderOpen = $state(false);
    let openCreateModal = $state(false);

    let SettingsComponent = $state<any>(null);
    let CreateInstanceModalComponent = $state<any>(null);
    let VersionDownloaderComponent = $state<any>(null);
    let DownloadProgressBarComponent = $state<any>(null);

    onMount(async () => {
        initEventListeners();

        const [settings, _] = await Promise.all([
            syncSettings(),
            getVersions(),
        ]);

        const firstInstance = launcherStore.loadedInstances[0];
        if (firstInstance && !selectedInstance) {
            selectedInstance = firstInstance;
        }

        applyTheme(launcherStore.settings.theme);

        if (launcherStore.settings.auto_updates) {
            setTimeout(() => checkForUpdates(true), 2000);
        }

        // Lazy load non-critical components after first paint
        Promise.all([
            import("$lib/components/settings/Settings.svelte"),
            import("$lib/components/instances/CreateInstanceModal.svelte"),
            import("$lib/components/layout/VersionDownloader.svelte"),
            import("$lib/components/ui/DownloadProgressBar.svelte"),
        ]).then(([s, c, v, d]) => {
            SettingsComponent = s.default;
            CreateInstanceModalComponent = c.default;
            VersionDownloaderComponent = v.default;
            DownloadProgressBarComponent = d.default;
        });
    });

    $effect(() => {
        const current = selectedInstance;
        if (!current) return;

        const updated = launcherStore.loadedInstances.find(
            (i) => i.uuid === current.uuid,
        );
        if (!updated) return;

        if (updated.status !== current.status) {
            current.status = updated.status;
        }
        if (updated.name !== current.name) {
            current.name = updated.name;
        }
        if (updated.version !== current.version) {
            current.version = updated.version;
        }
        if (updated.icon !== current.icon) {
            current.icon = updated.icon;
        }
        if (updated.cover_image !== current.cover_image) {
            current.cover_image = updated.cover_image;
        }
        if (updated.last_played !== current.last_played) {
            current.last_played = updated.last_played;
        }
        if (updated.loader !== current.loader) {
            current.loader = updated.loader;
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
        <div class="background-overlay"></div>

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
    <SettingsComponent onclose={() => (quickMenuOpen = false)} />
</Drawer>

<Drawer bind:open={versionDownloaderOpen} direction="right">
    <VersionDownloaderComponent onclose={() => (versionDownloaderOpen = false)} />
</Drawer>

<CreateInstanceModalComponent bind:open={openCreateModal} />

<NotificationContainer />

<DownloadProgressBarComponent />
