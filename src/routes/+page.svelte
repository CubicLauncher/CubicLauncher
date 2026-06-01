<script lang="ts">
	import { onMount } from "svelte";
	import type { Component } from "svelte";
	import "../styles/App.css";
	import { launcherStore } from "$lib/state/state.svelte";
	import {
		getVersions,
		syncSettings,
		initEventListeners,
	} from "$lib/api/launcherService";
	import type { InstanceDto } from "$lib/types/types";
	import Sidebar from "$lib/components/layout/Sidebar.svelte";
	import InstanceView from "$lib/components/instances/InstanceView.svelte";
	import Drawer from "$lib/components/layout/Drawer.svelte";
	import NotificationContainer from "$lib/components/ui/NotificationContainer.svelte";
	import { initDiscordPresence } from "$lib/api/cubicApi";
	import { t } from "$lib/i18n";
	import { applyTheme } from "$lib/api/themeManager";
	import { checkForUpdates } from "$lib/api/updaterServices";
	import CreateInstanceModal from "$lib/components/instances/CreateInstanceModal.svelte";

	let selectedInstance = $state<InstanceDto | null>(null);
	let quickMenuOpen = $state(false);
	let versionDownloaderOpen = $state(false);
	let openCreateModal = $state(false);

	let SettingsComponent = $state<Component<{ onclose: () => void }> | null>(
		null,
	);
	let VersionDownloaderComponent = $state<Component<{
		onclose?: () => void;
	}> | null>(null);
	let DownloadProgressBarComponent = $state<Component | null>(null);

	onMount(async () => {
		initEventListeners();

		await Promise.all([syncSettings(), getVersions()]);

		const firstInstance = launcherStore.loadedInstances[0];
		if (firstInstance && !selectedInstance) {
			selectedInstance = firstInstance;
		}

		applyTheme(launcherStore.settings.theme);

		initDiscordPresence();

		if (launcherStore.settings.auto_updates) {
			setTimeout(() => checkForUpdates(true), 2000);
		}

		// Lazy load non-critical components after first paint
		Promise.all([
			import("$lib/components/settings/Settings.svelte"),
			import("$lib/components/layout/VersionDownloader.svelte"),
			import("$lib/components/ui/DownloadProgressBar.svelte"),
		]).then(([s, v, d]) => {
			SettingsComponent = s.default;
			VersionDownloaderComponent = v.default;
			DownloadProgressBarComponent = d.default;
		});
	});

	$effect(() => {
		const instances = launcherStore.loadedInstances;
		const sel = selectedInstance;
		if (sel) {
			const updated = instances.find((i) => i.uuid === sel.uuid);
			if (updated && updated !== sel) {
				selectedInstance = updated;
			}
		}
	});
</script>

<div class="app-container">
	<Sidebar
		bind:selectedInstance
		onopenquickmenu={() => (quickMenuOpen = true)}
		onopenversiondownloader={() => (versionDownloaderOpen = true)}
		onopencreateinstance={() => (openCreateModal = true)}
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
	<VersionDownloaderComponent
		onclose={() => (versionDownloaderOpen = false)}
	/>
</Drawer>

<CreateInstanceModal bind:open={openCreateModal} />

<NotificationContainer />

<DownloadProgressBarComponent />
