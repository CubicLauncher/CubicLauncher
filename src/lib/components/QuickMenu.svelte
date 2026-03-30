<script lang="ts">
    import { invoke } from "@tauri-apps/api/core";
    import type { InstanceDto } from "../types/types";
    import { launcherStore } from "../state/state.svelte";
    import { killInst, saveSettings } from "../api/launcherService";

    interface Props {
        onclose?: () => void;
    }

    let { onclose }: Props = $props();

    let saving = $state(false);

    async function handleSave() {
        saving = true;
        await saveSettings();
        setTimeout(() => {
            saving = false;
        }, 1000);
    }
</script>

<div class="qm-root">
    <!-- Header -->
    <div class="qm-header">
        <span class="qm-label">Launcher Settings</span>
        <button class="qm-close-btn" onclick={onclose}>✕</button>
    </div>

    <div class="qm-scroll">
        <!-- Running instance -->
        <section class="qm-section">
            <span class="qm-section-label">Active Instances</span>
            {#if launcherStore.runningInstances.length > 0}
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
                {/each}
            {:else}
                <div class="qm-empty-state">No instances running</div>
            {/if}
        </section>

        <!-- General Settings -->
        <section class="qm-section">
            <span class="qm-section-label">General</span>
            <div class="qm-field">
                <label for="username">Username</label>
                <input
                    type="text"
                    id="username"
                    bind:value={launcherStore.settings.username}
                    placeholder="Steve"
                />
            </div>
        </section>

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

        <!-- Java Paths -->
        <section class="qm-section">
            <span class="qm-section-label">Java Runtimes</span>
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
        </section>

        <div style="padding: 10px 0 20px 0;">
            <button 
                class="qm-save-btn" 
                onclick={handleSave}
                disabled={saving}
            >
                {saving ? "Saving..." : "Save Settings"}
            </button>
        </div>
    </div>

    <!-- Footer -->
    <div class="qm-footer">
        <span class="qm-version">CubicLauncher 2604a</span>
    </div>
</div>

<style>
    .qm-root {
        height: 100%;
        display: flex;
        flex-direction: column;
        background: #0a0a0a;
        color: #eee;
        font-family: "Inter", sans-serif;
    }

    .qm-header {
        padding: 20px;
        display: flex;
        justify-content: space-between;
        align-items: center;
        border-bottom: 1px solid #222;
        background: #0f0f0f;
    }

    .qm-label {
        font-size: 1.1rem;
        font-weight: 600;
        color: #fff;
    }

    .qm-close-btn {
        background: none;
        border: none;
        color: #666;
        cursor: pointer;
        font-size: 1.2rem;
        transition: color 0.2s;
    }

    .qm-close-btn:hover {
        color: #fff;
    }

    .qm-scroll {
        flex: 1;
        overflow-y: auto;
        padding: 0 20px;
    }

    .qm-section {
        margin-top: 25px;
    }

    .qm-section-label {
        display: block;
        font-size: 0.75rem;
        font-weight: 700;
        text-transform: uppercase;
        color: #555;
        margin-bottom: 12px;
        letter-spacing: 0.05em;
    }

    .qm-active-card {
        background: #151515;
        border-radius: 12px;
        padding: 12px;
        display: flex;
        align-items: center;
        gap: 12px;
        border: 1px solid #222;
    }

    .qm-status-dot {
        width: 8px;
        height: 8px;
        border-radius: 50%;
    }

    .qm-status-dot.running {
        background: #4caf50;
        box-shadow: 0 0 10px rgba(76, 175, 80, 0.4);
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
        color: #888;
    }

    .qm-kill-btn {
        background: rgba(255, 68, 68, 0.1);
        color: #ff4444;
        border: 1px solid rgba(255, 68, 68, 0.2);
        padding: 4px 10px;
        border-radius: 6px;
        font-size: 0.75rem;
        font-weight: 600;
        cursor: pointer;
        transition: all 0.2s;
    }

    .qm-kill-btn:hover {
        background: #ff4444;
        color: #fff;
    }

    .qm-empty-state {
        color: #444;
        font-size: 0.85rem;
        padding: 10px 0;
    }

    .qm-field {
        margin-bottom: 15px;
    }

    .qm-field label {
        display: block;
        font-size: 0.8rem;
        color: #aaa;
        margin-bottom: 6px;
    }

    .qm-field input {
        width: 100%;
        background: #111;
        border: 1px solid #333;
        color: #fff;
        padding: 10px 12px;
        border-radius: 8px;
        font-size: 0.9rem;
        transition: border-color 0.2s, background 0.2s;
        box-sizing: border-box;
    }

    .qm-field input:focus {
        outline: none;
        border-color: #555;
        background: #151515;
    }

    .qm-field-group {
        display: grid;
        grid-template-columns: 1fr 1fr;
        gap: 15px;
    }

    .qm-save-btn {
        width: 100%;
        background: linear-gradient(135deg, #333, #222);
        color: #fff;
        border: 1px solid #444;
        padding: 12px;
        border-radius: 10px;
        font-weight: 600;
        cursor: pointer;
        transition: all 0.2s;
    }

    .qm-save-btn:hover:not(:disabled) {
        border-color: #666;
        transform: translateY(-1px);
        box-shadow: 0 4px 12px rgba(0,0,0,0.3);
    }

    .qm-save-btn:disabled {
        opacity: 0.5;
        cursor: not_allowed;
    }

    .qm-footer {
        padding: 15px 20px;
        background: #070707;
        border-top: 1px solid #111;
        display: flex;
        justify-content: center;
    }

    .qm-version {
        font-size: 0.7rem;
        color: #333;
        font-weight: 500;
    }

    /* Scrollbar */
    .qm-scroll::-webkit-scrollbar {
        width: 4px;
    }
    .qm-scroll::-webkit-scrollbar-track {
        background: transparent;
    }
    .qm-scroll::-webkit-scrollbar-thumb {
        background: #222;
        border-radius: 10px;
    }
</style>
