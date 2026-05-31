<script lang="ts">
	import { onMount } from "svelte";
	import { invoke } from "@tauri-apps/api/core";
	import { launcherStore } from "$lib/state/state.svelte";
	import { killInst, saveSettings } from "$lib/api/launcherService";
	import { openUrl } from "$lib/api/cubicApi";
	import { t } from "$lib/i18n";
	import Select from "$lib/components/layout/Select.svelte";
	import {
		checkForUpdates,
		downloadUpdate,
		installUpdate,
	} from "$lib/api/updaterServices";
	import { listThemes } from "$lib/api/themeManager";
	import type { ThemeEntry } from "$lib/types/types";
	import UpdateSection from "./UpdateSection.svelte";
	import CollapsibleSection from "./CollapsibleSection.svelte";

	interface Props {
		onclose?: () => void;
	}

	let { onclose }: Props = $props();

	let saving = $state(false);
	let currentTab = $state("launcher");
	let checking = $state(false);
	let downloading = $state(false);
	let installing = $state(false);

	let envVarList = $state<Array<{ key: string; value: string }>>([]);

	function initEnvVars() {
		const record = launcherStore.settings.env_vars;
		const entries = Object.entries(record);
		envVarList =
			entries.length > 0
				? entries.map(([k, v]) => ({ key: k, value: v }))
				: [{ key: "", value: "" }];
	}

	function syncEnvVars() {
		const record: Record<string, string> = {};
		for (const entry of envVarList) {
			if (entry.key.trim() !== "") {
				record[entry.key.trim()] = entry.value;
			}
		}
		launcherStore.settings.env_vars = record;
	}

	function addEnvVar() {
		envVarList = [...envVarList, { key: "", value: "" }];
	}

	function removeEnvVar(index: number) {
		envVarList = envVarList.filter((_, i) => i !== index);
		syncEnvVars();
	}

	async function handleSave() {
		saving = true;
		await saveSettings();
		setTimeout(() => {
			saving = false;
		}, 1000);
	}

	async function autoDetectJava() {
		try {
			const paths: {
				jre8: string;
				jre17: string;
				jre21: string;
				jre25: string;
			} = await invoke("detect_java_paths");
			if (paths.jre8) launcherStore.settings.jre8_path = paths.jre8;
			if (paths.jre17) launcherStore.settings.jre17_path = paths.jre17;
			if (paths.jre21) launcherStore.settings.jre21_path = paths.jre21;
			if (paths.jre25) launcherStore.settings.jre25_path = paths.jre25;
		} catch (e) {
			console.error("Failed to detect java paths", e);
		}
	}

	async function handleCheckForUpdates() {
		checking = true;
		await checkForUpdates(false);
		checking = false;
	}

	async function handleDownload() {
		downloading = true;
		await downloadUpdate();
		downloading = false;
	}

	async function handleInstall() {
		installing = true;
		await installUpdate();
		installing = false;
	}

	let tabs = $derived([
		{ id: "launcher", label: t("settings.tabs.launcher") },
		{ id: "minecraft", label: t("settings.tabs.minecraft") },
		{ id: "java", label: t("settings.tabs.java") },
	]);

	const languageOptions = [
		{ value: "es", label: "Español" },
		{ value: "en", label: "English" },
	];
	let availableThemes = $state<ThemeEntry[]>([]);
	let themeOptions = $derived(
		availableThemes.map((t: ThemeEntry) => ({
			value: t.id,
			label: t.name,
		})),
	);

	async function loadThemes() {
		availableThemes = await listThemes();
	}

	onMount(() => {
		loadThemes();
		initEnvVars();
	});
	let runningInstances = $derived(
		launcherStore.loadedInstances
			.filter((i) => i.status === "started" || i.status === "starting")
			.map((i) => i.uuid),
	);

	const currentVersion = "2605d (26.5.3)";
</script>

<div class="qm-root">
	<!-- Header -->
	<div class="qm-header">
		<span class="qm-label">{t("settings.title")}</span>
		<button type="button" class="qm-close-btn" onclick={onclose}>✕</button>
	</div>

	<!-- Tab Navigation -->
	<div class="qm-tabs">
		{#each tabs as tab (tab)}
			<button
				type="button"
				class="qm-tab-btn"
				class:active={currentTab === tab.id}
				onclick={() => (currentTab = tab.id)}
			>
				<span class="qm-tab-label">{tab.label}</span>
			</button>
		{/each}
	</div>

	<div class="qm-scroll">
		{#if currentTab === "launcher"}
			<div class="section-group">
				<CollapsibleSection
					title={t("settings.launcher.activeInstancesTitle")}
					iconSrc="/images/icons/play.svg"
					storageKey="section_instances"
				>
					{#each runningInstances as uuid (uuid)}
						{@const inst = launcherStore.loadedInstances.find(
							(i) => i.uuid === uuid,
						)}
						{#if inst}
							<div class="qm-active-card">
								<div class="qm-status-dot running"></div>
								<div class="qm-active-info">
									<span class="qm-active-name"
										>{inst.name}</span
									>
									<span class="qm-active-sub"
										>{inst.version} - {inst.loader}</span
									>
								</div>
								<button
									type="button"
									class="qm-kill-btn"
									onclick={() => killInst(inst.uuid)}
									>{t(
										"settings.launcher.killInstance",
									)}</button
								>
							</div>
						{/if}
					{:else}
						<div class="qm-empty-state">
							{t("settings.launcher.noInstances")}
						</div>
					{/each}
				</CollapsibleSection>

				<CollapsibleSection
					title={t("settings.launcher.updatesTitle")}
					iconSrc="/images/icons/download.svg"
					storageKey="section_updates"
				>
					<UpdateSection
						{currentVersion}
						pendingUpdate={launcherStore.pendingUpdate}
						updateProgress={launcherStore.updateProgress}
						updateDownloaded={launcherStore.updateDownloaded}
						{checking}
						{downloading}
						{installing}
						onCheck={handleCheckForUpdates}
						onDownload={handleDownload}
						onInstall={handleInstall}
					/>
				</CollapsibleSection>

				<CollapsibleSection
					title={t("settings.launcher.themes")}
					iconSrc="/images/icons/pencil.svg"
					storageKey="section_themes"
				>
					<Select
						id="theme"
						label={t("settings.launcher.themesActive")}
						options={themeOptions}
						bind:value={launcherStore.settings.theme}
						onchange={async () => {
							try {
								await invoke("set_theme", {
									id: launcherStore.settings.theme,
								});
							} catch (e) {
								console.error("Error setting theme:", e);
							}
						}}
					/>
					<span
						class="qm-themes-hint"
						onclick={() =>
							openUrl("https://github.com/CubicLauncher/Themes")}
						role="link"
						tabindex="0"
						onkeydown={(e) => {
							if (e.key === "Enter")
								openUrl(
									"https://github.com/CubicLauncher/Themes",
								);
						}}>{t("settings.launcher.themesSpan")}</span
					>
				</CollapsibleSection>

				<CollapsibleSection
					title={t("settings.launcher.generalTitle")}
					iconSrc="/images/icons/sliders.svg"
					storageKey="section_general"
				>
					<Select
						id="language"
						label={t("settings.launcher.language")}
						options={languageOptions}
						bind:value={launcherStore.settings.language}
						onchange={handleSave}
					/>
					<div class="qm-field-checkbox">
						<input
							type="checkbox"
							id="auto-updates"
							bind:checked={launcherStore.settings.auto_updates}
							onchange={handleSave}
						/>
						<label for="auto-updates"
							>{t("settings.launcher.autoUpdates")}</label
						>
					</div>
					<div class="qm-field-checkbox">
						<input
							type="checkbox"
							id="close-on-play"
							bind:checked={
								launcherStore.settings.close_launcher_on_play
							}
							onchange={handleSave}
						/>
						<label for="close-on-play"
							>{t("settings.launcher.closeOnPlay")}</label
						>
					</div>
					<div class="qm-field-checkbox">
						<input
							type="checkbox"
							id="discord-presence"
							bind:checked={
								launcherStore.settings.discord_presence
							}
							onchange={handleSave}
						/>
						<label for="discord-presence"
							>{t("settings.launcher.discordPresence")}</label
						>
					</div>
				</CollapsibleSection>
			</div>
		{/if}

		{#if currentTab === "minecraft"}
			<div class="section-group">
				<CollapsibleSection
					title={t("settings.minecraft.perfTitle")}
					iconSrc="/images/icons/database.svg"
					storageKey="section_performance"
				>
					<div class="qm-field-group">
						<div class="qm-field">
							<label for="min-mem"
								>{t("settings.minecraft.minRam")}</label
							>
							<input
								type="number"
								id="min-mem"
								bind:value={launcherStore.settings.min_memory}
							/>
						</div>
						<div class="qm-field">
							<label for="max-mem"
								>{t("settings.minecraft.maxRam")}</label
							>
							<input
								type="number"
								id="max-mem"
								bind:value={launcherStore.settings.max_memory}
							/>
						</div>
					</div>
				</CollapsibleSection>

				<CollapsibleSection
					title={t("settings.minecraft.optionsTitle")}
					iconSrc="/images/icons/check-square.svg"
					storageKey="section_options"
				>
					<div class="qm-field-checkbox">
						<input
							type="checkbox"
							id="show-snapshots"
							bind:checked={launcherStore.settings.show_snapshots}
							onchange={handleSave}
						/>
						<label for="show-snapshots"
							>{t("settings.minecraft.showSnapshots")}</label
						>
					</div>
					<div class="qm-field-checkbox">
						<input
							type="checkbox"
							id="show-alpha"
							bind:checked={launcherStore.settings.show_alpha}
							onchange={handleSave}
						/>
						<label for="show-alpha"
							>{t("settings.minecraft.showAlpha")}</label
						>
					</div>
				</CollapsibleSection>
			</div>
		{/if}

		{#if currentTab === "java"}
			<div class="section-group">
				<CollapsibleSection
					title={t("settings.java.runtimesTitle")}
					iconSrc="/images/icons/terminal.svg"
					storageKey="section_runtimes"
				>
					<div style="margin-bottom: 12px;">
						<button
							type="button"
							class="detect-btn"
							onclick={autoDetectJava}
							>{t("settings.java.detectPathsBtn")}</button
						>
					</div>
					<div class="qm-field">
						<label for="jre8">{t("settings.java.java8Path")}</label>
						<input
							type="text"
							id="jre8"
							bind:value={launcherStore.settings.jre8_path}
							placeholder="Path to javaw.exe"
						/>
					</div>
					<div class="qm-field">
						<label for="jre17"
							>{t("settings.java.java17Path")}</label
						>
						<input
							type="text"
							id="jre17"
							bind:value={launcherStore.settings.jre17_path}
							placeholder="Path to javaw.exe"
						/>
					</div>
					<div class="qm-field">
						<label for="jre21"
							>{t("settings.java.java21Path")}</label
						>
						<input
							type="text"
							id="jre21"
							bind:value={launcherStore.settings.jre21_path}
							placeholder="Path to javaw.exe"
						/>
					</div>
					<div class="qm-field">
						<label for="jre25"
							>{t("settings.java.java25Path")}</label
						>
						<input
							type="text"
							id="jre25"
							bind:value={launcherStore.settings.jre25_path}
							placeholder="Path to javaw.exe"
						/>
					</div>
				</CollapsibleSection>

				<CollapsibleSection
					title="Avanzado"
					iconSrc="/images/icons/settings.svg"
					storageKey="section_advanced"
				>
					<div class="qm-field">
						<label for="jvm-args"
							>{t("settings.java.jvmArgs")}</label
						>
						<textarea
							id="jvm-args"
							bind:value={launcherStore.settings.jvm_args}
							placeholder="-Xmx2G -Xms1G ..."
							style="width: 100%; background: var(--bg-input); border: 1px solid var(--border-color); color: var(--text-primary); padding: 8px 10px; border-radius: var(--border-radius-sm); font-size: 0.85rem; resize: vertical; min-height: 60px; font-family: monospace; box-shadow: inset 0 1px 2px rgba(0,0,0,0.2); box-sizing: border-box;"
						></textarea>
					</div>
					<div class="qm-field">
						<span
							style="display: block; margin-bottom: 8px; color: var(--text-secondary); font-size: 0.8rem;"
							>{t("settings.java.envVars")}</span
						>
						{#each envVarList as entry, i (entry)}
							<div
								style="display: flex; gap: 4px; align-items: center; margin-bottom: 4px;"
							>
								<input
									type="text"
									bind:value={entry.key}
									placeholder="KEY"
									oninput={syncEnvVars}
									style="flex: 1; min-width: 0; width: 0; background: var(--bg-input); border: 1px solid var(--border-color); color: var(--text-primary); padding: 4px 8px; border-radius: var(--border-radius-sm); font-size: 0.8rem; height: 28px; box-shadow: inset 0 1px 2px rgba(0,0,0,0.2); box-sizing: border-box;"
								/>
								<span
									style="color: var(--text-muted); font-size: 0.8rem; flex-shrink: 0;"
									>=</span
								>
								<input
									type="text"
									bind:value={entry.value}
									placeholder="VALUE"
									oninput={syncEnvVars}
									style="flex: 1; min-width: 0; width: 0; background: var(--bg-input); border: 1px solid var(--border-color); color: var(--text-primary); padding: 4px 8px; border-radius: var(--border-radius-sm); font-size: 0.8rem; height: 28px; box-shadow: inset 0 1px 2px rgba(0,0,0,0.2); box-sizing: border-box;"
								/>
								<button
									type="button"
									onclick={() => removeEnvVar(i)}
									style="background: none; border: none; color: var(--text-muted); cursor: pointer; padding: 2px; font-size: 1rem; line-height: 1; flex-shrink: 0;"
									>✕</button
								>
							</div>
						{/each}
						<button
							type="button"
							onclick={addEnvVar}
							style="background: none; border: 1px dashed var(--border-color); color: var(--text-secondary); cursor: pointer; padding: 4px 10px; border-radius: var(--border-radius-sm); font-size: 0.8rem; margin-top: 2px;"
							>+ {t("settings.java.envVarsAdd")}</button
						>
					</div>
				</CollapsibleSection>
			</div>
		{/if}
	</div>

	<div class="save-footer">
		<button
			type="button"
			class="qm-save-btn"
			onclick={handleSave}
			disabled={saving}
		>
			{saving ? t("settings.java.saving") : t("settings.java.saveBtn")}
		</button>
	</div>

	<!-- Footer -->
	<div class="qm-footer">
		<span class="qm-version">CubicLauncher v{currentVersion}</span>
	</div>
</div>

<style>
	.qm-root {
		height: 100%;
		display: flex;
		flex-direction: column;
		background: var(--bg-main);
		color: var(--text-primary);
		font-family: var(--font-family);
	}

	.qm-header {
		padding: 20px 20px 10px 20px;
		display: flex;
		justify-content: space-between;
		align-items: center;
		background: var(--bg-sidebar);
	}

	.qm-tabs {
		display: flex;
		padding: 0 10px;
		background: var(--bg-sidebar);
		border-bottom: 1px solid var(--border-color);
		gap: 4px;
	}

	.qm-tab-btn {
		flex: 1;
		background: none;
		border: none;
		color: var(--text-muted);
		padding: 8px 4px;
		cursor: pointer;
		font-size: 0.8rem;
		font-weight: 600;
		border-bottom: 2px solid transparent;
		transition: all 0.2s;
	}

	.qm-tab-btn.active {
		color: var(--text-primary);
		border-bottom-color: var(--accent);
	}

	.qm-label {
		font-size: 1.1rem;
		font-weight: 600;
		color: var(--text-primary);
	}

	.qm-close-btn {
		background: none;
		border: none;
		color: var(--text-muted);
		cursor: pointer;
		font-size: 1.2rem;
		transition: color 0.2s;
	}

	.qm-close-btn:hover {
		color: var(--text-primary);
	}

	.qm-scroll {
		flex: 1;
		overflow-y: auto;
	}

	.section-group {
		border: 1px solid var(--border-color);
		overflow: hidden;
		margin-bottom: 16px;
	}

	.section-group :global(.cs-root) {
		border: none;
		border-bottom: 1px solid var(--border-color);
	}

	.section-group :global(.cs-root:last-child) {
		border-bottom: none;
	}

	:global(.qm-scroll::-webkit-scrollbar) {
		width: 4px;
	}

	:global(.qm-scroll::-webkit-scrollbar-track) {
		background: transparent;
	}

	:global(.qm-scroll::-webkit-scrollbar-thumb) {
		background: var(--scrollbar-thumb);
		border-radius: 10px;
	}

	.qm-active-card {
		background: var(--bg-card);
		border-radius: var(--border-radius-sm);
		padding: 10px 12px;
		display: flex;
		align-items: center;
		gap: 12px;
		border: 1px solid var(--border-color);
		box-shadow:
			var(--shadow-sm),
			inset 0 1px 0 rgba(255, 255, 255, 0.03);
		margin-bottom: 6px;
	}

	.qm-status-dot {
		width: 8px;
		height: 8px;
		border-radius: 50%;
	}

	.qm-status-dot.running {
		background: var(--color-success);
		box-shadow: 0 0 10px rgba(var(--color-success-rgb), 0.4);
	}

	.qm-active-info {
		flex: 1;
		display: flex;
		flex-direction: column;
	}

	.qm-active-name {
		font-weight: 600;
		font-size: 0.9rem;
	}

	.qm-active-sub {
		font-size: 0.75rem;
		color: var(--text-secondary);
	}

	.qm-kill-btn {
		background: rgba(var(--color-error-rgb), 0.1);
		color: var(--color-error);
		border: 1px solid rgba(var(--color-error-rgb), 0.2);
		padding: 4px 10px;
		border-radius: var(--border-radius-sm);
		font-size: 0.75rem;
		font-weight: 600;
		cursor: pointer;
		transition: all 0.2s;
	}

	.qm-kill-btn:hover {
		background: var(--color-error);
		color: var(--accent-text);
	}

	.qm-empty-state {
		color: var(--text-muted);
		font-size: 0.85rem;
		padding: 10px 0;
	}

	.qm-field {
		margin-bottom: 15px;
	}

	.qm-field label {
		display: block;
		font-size: 0.8rem;
		color: var(--text-secondary);
		margin-bottom: 6px;
	}

	.qm-field input {
		width: 100%;
		background: var(--bg-input);
		border: 1px solid var(--border-color);
		color: var(--text-primary);
		padding: 8px 10px;
		border-radius: var(--border-radius-sm);
		font-size: 0.85rem;
		transition: border-color 0.2s;
		box-sizing: border-box;
		box-shadow: inset 0 1px 2px rgba(0, 0, 0, 0.25);
	}

	.qm-field input:focus {
		outline: none;
		border-color: var(--text-muted);
	}

	.qm-field-group {
		display: grid;
		grid-template-columns: 1fr 1fr;
		gap: 15px;
	}

	.qm-save-btn {
		width: 100%;
		background: var(--bg-card);
		color: var(--text-primary);
		border: 1px solid var(--border-color);
		padding: 10px 12px;
		border-radius: var(--border-radius-sm);
		font-family: var(--font-family);
		font-weight: 600;
		cursor: pointer;
		transition:
			background 0.15s,
			border-color 0.15s;
		box-shadow: var(--shadow-sm);
	}

	.qm-save-btn:hover:not(:disabled) {
		background: var(--bg-item-active);
		border-color: var(--border-color);
	}

	.qm-save-btn:disabled {
		opacity: 0.5;
		cursor: not-allowed;
	}

	.qm-footer {
		padding: 15px 20px;
		background: var(--bg-main);
		border-top: 1px solid var(--border-color);
		display: flex;
		justify-content: center;
	}

	.qm-version {
		font-size: 0.7rem;
		color: var(--text-muted);
		font-weight: 500;
	}

	.save-footer {
		padding: 12px 20px;
		border-top: 1px solid var(--border-color);
	}

	.detect-btn {
		background: var(--bg-input);
		border: 1px solid var(--border-color);
		color: var(--text-secondary);
		padding: 6px 12px;
		border-radius: var(--border-radius-sm);
		font-size: 0.7rem;
		font-weight: 600;
		cursor: pointer;
		transition:
			color 0.15s,
			border-color 0.15s;
	}

	.detect-btn:hover {
		color: var(--text-primary);
		border-color: var(--text-muted);
	}

	.qm-field-checkbox {
		display: flex;
		align-items: center;
		gap: 12px;
		margin-bottom: 12px;
		margin-top: 8px;
		cursor: pointer;
		user-select: none;
	}

	.qm-field-checkbox input[type="checkbox"] {
		appearance: none;
		-webkit-appearance: none;
		width: 18px;
		height: 18px;
		background: var(--bg-input);
		border: 1px solid var(--border-color);
		border-radius: var(--border-radius-sm);
		cursor: pointer;
		position: relative;
		transition: all 0.2s;
	}

	.qm-field-checkbox input[type="checkbox"]:checked {
		background: var(--accent);
		border-color: var(--accent);
	}

	.qm-field-checkbox input[type="checkbox"]:checked::after {
		content: "✓";
		position: absolute;
		top: 50%;
		left: 50%;
		transform: translate(-50%, -50%);
		color: var(--accent-text);
		font-size: 11px;
		font-weight: 800;
	}

	.qm-field-checkbox label {
		font-size: 0.85rem;
		color: var(--text-secondary);
		cursor: pointer;
		transition: color 0.2s;
	}

	.qm-field-checkbox:hover label {
		color: var(--text-primary);
	}

	.qm-field-checkbox input[type="checkbox"]:hover {
		border-color: var(--text-muted);
	}

	.qm-themes-hint {
		display: block;
		margin-top: 8px;
		font-size: 0.75rem;
		color: var(--text-secondary);
		line-height: 1.4;
		cursor: pointer;
		transition: color 0.2s;
	}

	.qm-themes-hint:hover {
		color: var(--text-primary);
	}
</style>
