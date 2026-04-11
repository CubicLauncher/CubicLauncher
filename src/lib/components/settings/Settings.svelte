<script lang="ts">
    import { invoke } from "@tauri-apps/api/core";
    import type { InstanceDto } from "$lib/types/types";
    import { launcherStore } from "$lib/state/state.svelte";
    import { killInst, saveSettings } from "$lib/api/launcherService";

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

    const tabs = [
        { id: "launcher", label: "Launcher" },
        { id: "minecraft", label: "Minecraft" },
        { id: "java", label: "Java" },
    ];
</script>

<div class="qm-root">
    <!-- Header -->
    <div class="qm-header">
        <span class="qm-label">Ajustes</span>
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
                <span class="qm-section-label">Active Instances</span>
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
                                onclick={() => killInst(inst.uuid)}>Kill</button
                            >
                        </div>
                    {/if}
                {:else}
                    <div class="qm-empty-state">No instances running</div>
                {/each}
            </section>

            <!-- General Settings -->
            <section class="qm-section">
                <span class="qm-section-label">General</span>
                <div class="qm-field">
                    <label for="language">Idioma</label>
                    <select
                        id="language"
                        bind:value={launcherStore.settings.language}
                    >
                        <option value="es">Español</option>
                        <option value="en">English</option>
                    </select>
                </div>
                <div class="qm-field-checkbox">
                    <input
                        type="checkbox"
                        id="auto-updates"
                        bind:checked={launcherStore.settings.auto_updates}
                    />
                    <label for="auto-updates">Actualizaciones Automáticas</label
                    >
                </div>
                <div class="qm-field-checkbox">
                    <input
                        type="checkbox"
                        id="error-console"
                        bind:checked={launcherStore.settings.show_error_console}
                    />
                    <label for="error-console">Consola de errores</label>
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
                        >Cerrar el launcher al abrir el juego</label
                    >
                </div>
            </section>
        {/if}

        {#if currentTab === "minecraft"}
            <!-- Performance -->
            <section class="qm-section">
                <span class="qm-section-label">Performance (RAM)</span>
                <div class="qm-field-group">
                    <div class="qm-field">
                        <label for="min-mem">Minimum (MB)</label>
                        <input
                            type="number"
                            id="min-mem"
                            bind:value={launcherStore.settings.min_memory}
                        />
                    </div>
                    <div class="qm-field">
                        <label for="max-mem">Maximum (MB)</label>
                        <input
                            type="number"
                            id="max-mem"
                            bind:value={launcherStore.settings.max_memory}
                        />
                    </div>
                </div>
            </section>

            <section class="qm-section">
                <span class="qm-section-label">Opciones de Minecraft</span>
                <div class="qm-field-checkbox">
                    <input
                        type="checkbox"
                        id="show-beta"
                        bind:checked={launcherStore.settings.show_beta}
                    />
                    <label for="show-beta">Mostrar versiones Betas</label>
                </div>
                <div class="qm-field-checkbox">
                    <input
                        type="checkbox"
                        id="show-alpha"
                        bind:checked={launcherStore.settings.show_alpha}
                    />
                    <label for="show-alpha">Mostrar versiones Alpha</label>
                </div>
                <div class="qm-field-checkbox">
                    <input
                        type="checkbox"
                        id="force-gpu"
                        bind:checked={launcherStore.settings.force_gpu}
                    />
                    <label for="force-gpu">Forzar uso de la Gpu</label>
                </div>
            </section>
        {/if}

        {#if currentTab === "java"}
            <!-- Java Paths -->
            <section class="qm-section">
                <span class="qm-section-label">Configuración de Java</span>
                <div class="qm-field-checkbox">
                    <input
                        type="checkbox"
                        id="auto-detect-java"
                        bind:checked={launcherStore.settings.auto_detect_java}
                    />
                    <label for="auto-detect-java"
                        >Detectar automáticamente la versión de Java</label
                    >
                </div>

                <span class="qm-section-label" style="margin-top: 20px;"
                    >Java Runtimes</span
                >
                <div class="qm-field">
                    <label for="jre8">Java 8 Path</label>
                    <input
                        type="text"
                        id="jre8"
                        bind:value={launcherStore.settings.jre8_path}
                        placeholder="Path to javaw.exe"
                    />
                </div>
                <div class="qm-field">
                    <label for="jre17">Java 17 Path</label>
                    <input
                        type="text"
                        id="jre17"
                        bind:value={launcherStore.settings.jre17_path}
                        placeholder="Path to javaw.exe"
                    />
                </div>
                <div class="qm-field">
                    <label for="jre21">Java 21 Path</label>
                    <input
                        type="text"
                        id="jre21"
                        bind:value={launcherStore.settings.jre21_path}
                        placeholder="Path to javaw.exe"
                    />
                </div>
                <div class="qm-field">
                    <label for="jvm-args">Argumento JVM</label>
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
                {saving ? "Saving..." : "Save Settings"}
            </button>
        </div>
    </div>

    <!-- Footer -->
    <div class="qm-footer">
        <span class="qm-version">CubicLauncher 2604a-RS</span>
    </div>
</div>
