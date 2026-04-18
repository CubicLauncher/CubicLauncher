<script lang="ts">
    import { invoke } from "@tauri-apps/api/core";
    import type { InstanceDto } from "$lib/types/types";
    import { launcherStore } from "$lib/state/state.svelte";
    import { killInst, saveSettings } from "$lib/api/launcherService";
    import { t } from "$lib/i18n";
    import Select from "$lib/components/layout/Select.svelte";

    interface Props {
        onclose?: () => void;
    }

    let { onclose }: Props = $props();

    let saving = $state(false);
    let currentTab = $state("launcher");

    async function handleSave() {
        saving = true;
        await saveSettings();
        setTimeout(() => {
            saving = false;
        }, 1000);
    }

    async function autoDetectJava() {
        try {
            const paths: { jre8: string; jre17: string; jre21: string } =
                await invoke("detect_java_paths");
            if (paths.jre8) launcherStore.settings.jre8_path = paths.jre8;
            if (paths.jre17) launcherStore.settings.jre17_path = paths.jre17;
            if (paths.jre21) launcherStore.settings.jre21_path = paths.jre21;
        } catch (e) {
            console.error("Failed to detect java paths", e);
        }
    }

    let tabs = $derived([
        { id: "launcher", label: t("settings.tabs.launcher") },
        { id: "minecraft", label: t("settings.tabs.minecraft") },
        { id: "java", label: t("settings.tabs.java") },
    ]);

    let languageOptions = [
        { value: "es", label: "Español" },
        { value: "en", label: "English" },
    ];
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
            <!-- Running instance -->
            <section class="qm-section">
                <span class="qm-section-label"
                    >{t("settings.launcher.activeInstancesTitle")}</span
                >
                {#each launcherStore.runningInstances as uuid}
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
                    />
                </div>
                <div class="qm-field-checkbox">
                    <input
                        type="checkbox"
                        id="auto-updates"
                        bind:checked={launcherStore.settings.auto_updates}
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
                    />
                    <label for="show-alpha"
                        >{t("settings.minecraft.showAlpha")}</label
                    >
                </div>
                <div class="qm-field-checkbox">
                    <input
                        type="checkbox"
                        id="force-gpu"
                        bind:checked={launcherStore.settings.force_gpu}
                    />
                    <label for="force-gpu"
                        >{t("settings.minecraft.forceGpu")}</label
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
                    <label for="jvm-args">{t("settings.java.jvmArgs")}</label>
                    <textarea
                        id="jvm-args"
                        bind:value={launcherStore.settings.jvm_args}
                        placeholder="-Xmx2G -Xms1G ..."
                        style="width: 100%; background: #111; border: 1px solid #333; color: #fff; padding: 10px 12px; border-radius: 8px; font-size: 0.9rem; resize: vertical; min-height: 60px;"
                    ></textarea>
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
        <span class="qm-version">CubicLauncher 2605a-RS</span>
    </div>
</div>
