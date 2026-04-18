<script lang="ts">
    import { fly, fade } from "svelte/transition";
    import { launcherStore } from "$lib/state/state.svelte";
    import { saveSettings } from "$lib/api/launcherService";
    import { t } from "$lib/i18n";

    let { onclose } = $props<{ onclose: () => void }>();

    let editing = $state(false);
    let newUsername = $state(launcherStore.settings.username);

    async function handleSave() {
        launcherStore.settings.username = newUsername;
        await saveSettings();
        editing = false;
    }

    function handleKeydown(e: KeyboardEvent) {
        if (e.key === "Enter") handleSave();
        if (e.key === "Escape") {
            editing = false;
            newUsername = launcherStore.settings.username;
        }
    }
</script>

<div
    class="modal-overlay"
    onclick={onclose}
    onkeydown={(e) => e.key === "Escape" && onclose()}
    role="button"
    tabindex="-1"
    transition:fade={{ duration: 200 }}
>
    <div
        class="modal"
        onclick={(e) => e.stopPropagation()}
        onkeydown={(e) => e.stopPropagation()}
        role="dialog"
        tabindex="0"
        transition:fly={{ y: 20, duration: 300, opacity: 0 }}
    >
        <div class="modal-header">
            <h2 class="modal-title">{t('userMenu.title')}</h2>
            <button
                class="btn-secondary"
                style="padding: 4px 8px; font-size: 0.7rem;"
                onclick={onclose}>✕</button
            >
        </div>

        <div class="modal-body">
            <div class="user-profile-center">
                <div class="avatar-wrapper">
                    <img
                        src="https://minotar.net/avatar/{launcherStore.settings
                            .username}/64"
                        alt="Avatar"
                        class="large-avatar"
                    />
                    <div class="status-dot"></div>
                </div>

                <div class="input-group" style="width: 100%; margin-top: 10px;">
                    <label class="input-label" for="username-edit"
                        >{t('userMenu.usernameLabel')}</label
                    >
                    <div style="display: flex; gap: 8px;">
                        {#if editing}
                            <input
                                id="username-edit"
                                type="text"
                                bind:value={newUsername}
                                onkeydown={handleKeydown}
                                class="text-input"
                                style="flex: 1;"
                                placeholder={t('userMenu.usernamePlaceholder')}
                            />
                        {:else}
                            <div class="username-display-wrapper">
                                <span class="username-text"
                                    >{launcherStore.settings.username}</span
                                >
                                <span class="offline-tag">{t('userMenu.offline')}</span>
                            </div>
                        {/if}
                    </div>
                </div>
            </div>
        </div>

        <div class="modal-footer">
            {#if editing}
                <button
                    class="btn-secondary"
                    onclick={() => {
                        editing = false;
                        newUsername = launcherStore.settings.username;
                    }}>Cancelar</button
                >
                <button class="btn-primary" onclick={handleSave}>{t('userMenu.save')}</button
                >
            {:else}
                <button
                    class="btn-secondary"
                    style="flex: 1;"
                    onclick={() => (editing = true)}>{t('userMenu.changeNameBtn')}</button
                >
            {/if}
        </div>
    </div>
</div>
