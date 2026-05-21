<script lang="ts">
    import { onMount } from "svelte";
    import { invoke } from "@tauri-apps/api/core";
    import { launcherStore } from "$lib/state/state.svelte";
    import { killInst, saveSettings } from "$lib/api/launcherService";
    import { t } from "$lib/i18n";
    import Select from "$lib/components/layout/Select.svelte";
    import {
        checkForUpdates,
        downloadUpdate,
        installUpdate,
    } from "$lib/api/updaterServices";
    import { listThemes } from "$lib/api/themeManager";
    import type { ThemeEntry } from "$lib/types/types";

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
      envVarList = entries.length > 0
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
        // If we get here, install failed (relaunch would have fired)
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

    const currentVersion = "2605c (26.5.2)";
</script>

<div class="qm-root">
    <!-- Header -->
    <div class="qm-header">
        <span class="qm-label">{t("settings.title")}</span>
        <button class="qm-close-btn" onclick={onclose}>✕</button>
    </div>

    <!-- Tab Navigation -->
    <div class="qm-tabs">
        {#each tabs as tab}
            <button
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
            <!-- Running instances -->
            <section class="qm-section">
                <span class="qm-section-label"
                    >{t("settings.launcher.activeInstancesTitle")}</span
                >
                {#each runningInstances as uuid}
                    {@const inst = launcherStore.loadedInstances.find(
                        (i) => i.uuid === uuid,
                    )}
                    {#if inst}
                        <div class="qm-active-card">
                            <div class="qm-status-dot running"></div>
                            <div class="qm-active-info">
                                <span class="qm-active-name">{inst.name}</span>
                                <span class="qm-active-sub"
                                    >{inst.version} - {inst.loader}</span
                                >
                            </div>
                            <button
                                class="qm-kill-btn"
                                onclick={() => killInst(inst.uuid)}
                                >{t("settings.launcher.killInstance")}</button
                            >
                        </div>
                    {/if}
                {:else}
                    <div class="qm-empty-state">
                        {t("settings.launcher.noInstances")}
                    </div>
                {/each}
            </section>

            <!-- Updates section -->
            <section class="qm-section">
                <span class="qm-section-label"
                    >{t("settings.launcher.updatesTitle")}</span
                >

                <!-- Version info row -->
                <div class="update-version-row">
                    <div class="update-version-info">
                        <span class="update-version-label"
                            >{t("settings.launcher.currentVersion")}</span
                        >
                        <span class="update-version-value"
                            >v{currentVersion}</span
                        >
                    </div>
                    {#if launcherStore.pendingUpdate}
                        <div class="update-version-info">
                            <span class="update-version-label"
                                >{t("settings.launcher.available")}</span
                            >
                            <span class="update-version-value update-available"
                                >v{launcherStore.pendingUpdate.version}</span
                            >
                        </div>
                    {:else}
                        <div class="update-version-info">
                            <span class="update-version-label"
                                >{t("settings.launcher.status")}</span
                            >
                            <span class="update-version-value update-ok"
                                >{t("settings.launcher.updateOk")}</span
                            >
                        </div>
                    {/if}
                </div>

                {#if launcherStore.pendingUpdate?.body}
                    <div class="update-notes">
                        <span class="update-notes-label"
                            >{t("settings.launcher.notes")}</span
                        >
                        <p class="update-notes-text">
                            {launcherStore.pendingUpdate.body}
                        </p>
                    </div>
                {/if}

                <!-- Progress bar (visible while downloading) -->
                {#if launcherStore.updateProgress > 0 && launcherStore.updateProgress < 100}
                    <div class="update-progress-wrap">
                        <div class="update-progress-track">
                            <div
                                class="update-progress-fill"
                                style="width: {launcherStore.updateProgress}%"
                            ></div>
                        </div>
                        <span class="update-progress-pct"
                            >{launcherStore.updateProgress}%</span
                        >
                    </div>
                {/if}

                <!-- Action buttons -->
                <div class="update-actions">
                    <!-- Check for updates -->
                    <button
                        class="update-btn secondary"
                        onclick={handleCheckForUpdates}
                        disabled={checking || downloading || installing}
                    >
                        {#if checking}
                            <span class="update-btn-spinner"></span>
                            {t("settings.launcher.checking")}
                        {:else}
                            {t("settings.launcher.searchBtn")}
                        {/if}
                    </button>

                    {#if launcherStore.pendingUpdate}
                        {#if !launcherStore.updateDownloaded}
                            <!-- Download only -->
                            <button
                                class="update-btn secondary"
                                onclick={handleDownload}
                                disabled={checking || downloading || installing}
                            >
                                {#if downloading}
                                    <span class="update-btn-spinner"></span>
                                    Descargando...
                                {:else}
                                    Solo descargar
                                {/if}
                            </button>

                            <!-- Download + Install -->
                            <button
                                class="update-btn primary"
                                onclick={async () => {
                                    await handleDownload();
                                    if (launcherStore.updateDownloaded)
                                        await handleInstall();
                                }}
                                disabled={checking || downloading || installing}
                            >
                                {#if downloading || installing}
                                    <span class="update-btn-spinner light"
                                    ></span>
                                    {downloading
                                        ? t("settings.launcher.downloading")
                                        : t("settings.launcher.installing")}
                                {:else}
                                    {t("settings.launcher.downloadInstallBtn")}
                                {/if}
                            </button>
                        {:else}
                            <!-- Already downloaded, just install -->
                            <button
                                class="update-btn primary"
                                onclick={handleInstall}
                                disabled={installing}
                            >
                                {#if installing}
                                    <span class="update-btn-spinner light"
                                    ></span>
                                    {t("settings.launcher.installing")}
                                {:else}
                                    {t("settings.launcher.installUpdateBtn")}
                                {/if}
                            </button>
                        {/if}
                    {/if}
                </div>
            </section>

            <!-- Themes -->
            <section class="qm-section">
                <span class="qm-section-label">Temas</span>
                <div class="qm-field">
                    <Select
                        id="theme"
                        label="Tema activo"
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
                </div>
            </section>

            <!-- General Settings -->
            <section class="qm-section">
                <span class="qm-section-label"
                    >{t("settings.launcher.generalTitle")}</span
                >
                <div class="qm-field">
                    <Select
                        id="language"
                        label={t("settings.launcher.language")}
                        options={languageOptions}
                        bind:value={launcherStore.settings.language}
                        onchange={handleSave}
                    />
                </div>
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
            </section>
        {/if}

        {#if currentTab === "minecraft"}
            <!-- Performance -->
            <section class="qm-section">
                <span class="qm-section-label"
                    >{t("settings.minecraft.perfTitle")}</span
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
            </section>

            <section class="qm-section">
                <span class="qm-section-label"
                    >{t("settings.minecraft.optionsTitle")}</span
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
            </section>
        {/if}

        {#if currentTab === "java"}
            <!-- Java Paths -->
            <section class="qm-section">
                <div
                    style="display: flex; justify-content: space-between; align-items: center; margin-top: 20px; margin-bottom: 10px;"
                >
                    <span class="qm-section-label" style="margin: 0;"
                        >{t("settings.java.runtimesTitle")}</span
                    >
                    <button
                        class="qm-save-btn"
                        style="width: auto; padding: 6px 12px; margin: 0; font-size: 0.85rem;"
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
                    <label for="jre17">{t("settings.java.java17Path")}</label>
                    <input
                        type="text"
                        id="jre17"
                        bind:value={launcherStore.settings.jre17_path}
                        placeholder="Path to javaw.exe"
                    />
                </div>
                <div class="qm-field">
                    <label for="jre21">{t("settings.java.java21Path")}</label>
                    <input
                        type="text"
                        id="jre21"
                        bind:value={launcherStore.settings.jre21_path}
                        placeholder="Path to javaw.exe"
                    />
                </div>
                <div class="qm-field">
                    <label for="jre25">{t("settings.java.java25Path")}</label>
                    <input
                        type="text"
                        id="jre25"
                        bind:value={launcherStore.settings.jre25_path}
                        placeholder="Path to javaw.exe"
                    />
                </div>
                <div class="qm-field">
                    <label for="jvm-args">{t("settings.java.jvmArgs")}</label>
                    <textarea
                        id="jvm-args"
                        bind:value={launcherStore.settings.jvm_args}
                        placeholder="-Xmx2G -Xms1G ..."
                        style="width: 100%; background: var(--bg-input); border: 1px solid var(--border-color); color: var(--text-primary); padding: 10px 12px; border-radius: 8px; font-size: 0.9rem; resize: vertical; min-height: 60px;"
                    ></textarea>
                </div>
                <div class="qm-field">
                    <span style="display: block; margin-bottom: 6px; color: var(--text-primary); font-size: 0.9rem;">{t("settings.java.envVars")}</span>
                    {#each envVarList as entry, i}
                        <div
                            style="display: flex; gap: 4px; align-items: center; margin-bottom: 4px;"
                        >
                            <input
                                type="text"
                                bind:value={entry.key}
                                placeholder="KEY"
                                oninput={syncEnvVars}
                                style="flex: 1; min-width: 0; width: 0; background: var(--bg-input); border: 1px solid var(--border-color); color: var(--text-primary); padding: 4px 6px; border-radius: 4px; font-size: 0.8rem; height: 28px;"
                            />
                            <span
                                style="color: var(--text-secondary); font-size: 0.8rem; flex-shrink: 0;"
                                >=</span
                            >
                            <input
                                type="text"
                                bind:value={entry.value}
                                placeholder="VALUE"
                                oninput={syncEnvVars}
                                style="flex: 1; min-width: 0; width: 0; background: var(--bg-input); border: 1px solid var(--border-color); color: var(--text-primary); padding: 4px 6px; border-radius: 4px; font-size: 0.8rem; height: 28px;"
                            />
                            <button
                                onclick={() => removeEnvVar(i)}
                                style="background: none; border: none; color: var(--text-secondary); cursor: pointer; padding: 2px; font-size: 1rem; line-height: 1; flex-shrink: 0;"
                                >✕</button
                            >
                        </div>
                    {/each}
                    <button
                        onclick={addEnvVar}
                        style="background: none; border: 1px dashed var(--border-color); color: var(--text-secondary); cursor: pointer; padding: 4px 10px; border-radius: 4px; font-size: 0.8rem; margin-top: 2px;"
                        >+ {t("settings.java.envVarsAdd")}</button
                    >
                </div>
            </section>
        {/if}

        <div style="padding: 20px 0;">
            <button class="qm-save-btn" onclick={handleSave} disabled={saving}>
                {saving
                    ? t("settings.java.saving")
                    : t("settings.java.saveBtn")}
            </button>
        </div>
    </div>

    <!-- Footer -->
    <div class="qm-footer">
        <span class="qm-version">CubicLauncher v{currentVersion}</span>
    </div>
</div>
