<script lang="ts">
    import { fly, fade } from "svelte/transition";
    import { launcherStore } from "$lib/state/state.svelte";
    import { saveSettings } from "$lib/api/launcherService";
    import { t } from "$lib/i18n";
    import { showError } from "$lib/state/state.svelte";
    import { logout } from "$lib/api/cubicApi";
    import AuthModal from "./AuthModal.svelte";

    let { onclose } = $props<{ onclose: () => void }>();

    let editing = $state(false);
    let newUsername = $state(launcherStore.settings.username);
    let showAuthModal = $state(false);

    async function handleSave() {
        const usernameRegex = /^[a-zA-Z0-9_]{3,16}$/;

        if (!usernameRegex.test(newUsername)) {
            showError(
                "Nombre Inválido",
                "El nombre debe tener entre 3 y 16 caracteres y solo contener letras, números y guiones bajos (_).",
            );
            return;
        }

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

    async function handleLogout() {
        await logout();
        launcherStore.settings.user = null;
        launcherStore.settings.username = "steve";
        newUsername = "steve";
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
            <h2 class="modal-title">{t("userMenu.title")}</h2>
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
                    {#if launcherStore.settings.user}
                        <div class="status-dot premium"></div>
                    {:else}
                        <div class="status-dot"></div>
                    {/if}
                </div>

                <div class="user-info">
                    <div
                        class="input-group"
                        style="width: 100%; margin-top: 10px;"
                    >
                        <label class="input-label" for="username-edit"
                            >{t("userMenu.usernameLabel")}</label
                        >
                        <div
                            style="display: flex; gap: 8px; align-items: center;"
                        >
                            {#if editing}
                                <input
                                    id="username-edit"
                                    type="text"
                                    bind:value={newUsername}
                                    onkeydown={handleKeydown}
                                    class="text-input"
                                    style="flex: 1;"
                                    placeholder={t(
                                        "userMenu.usernamePlaceholder",
                                    )}
                                    maxlength="16"
                                />
                            {:else}
                                <div class="username-display-wrapper">
                                    <span class="username-text"
                                        >{launcherStore.settings.username}</span
                                    >
                                    {#if launcherStore.settings.user}
                                        <span class="premium-tag"
                                            >{t("userMenu.premium")}</span
                                        >
                                    {:else}
                                        <span class="offline-tag"
                                            >{t("userMenu.offline")}</span
                                        >
                                    {/if}
                                </div>
                            {/if}
                        </div>
                    </div>
                </div>
            </div>

            <div class="auth-actions">
                {#if launcherStore.settings.user}
                    <button
                        class="btn-danger logout-btn"
                        onclick={handleLogout}
                    >
                        <span class="icon">󰗼</span>
                        {t("userMenu.logout")}
                    </button>
                {:else}
                    <button
                        class="btn-primary microsoft-btn"
                        onclick={() => (showAuthModal = true)}
                    >
                        <span class="icon">󰖳</span>
                        {t("userMenu.loginMicrosoft")}
                    </button>
                {/if}
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
                <button class="btn-primary" onclick={handleSave}
                    >{t("userMenu.save")}</button
                >
            {:else if !launcherStore.settings.user}
                <button
                    class="btn-secondary"
                    style="flex: 1;"
                    onclick={() => (editing = true)}
                    >{t("userMenu.changeNameBtn")}</button
                >
            {/if}
        </div>
    </div>
</div>

{#if showAuthModal}
    <AuthModal onclose={() => (showAuthModal = false)} />
{/if}

<style>
    .user-profile-center {
        display: flex;
        flex-direction: column;
        align-items: center;
        margin-bottom: 20px;
    }

    .avatar-wrapper {
        position: relative;
        margin-bottom: 15px;
    }

    .status-dot.premium {
        background-color: #fbbf24;
        box-shadow: 0 0 10px rgba(251, 191, 36, 0.5);
    }

    .username-display-wrapper {
        display: flex;
        align-items: center;
        gap: 10px;
    }

    .premium-tag {
        background: linear-gradient(135deg, #fbbf24 0%, #d97706 100%);
        color: white;
        padding: 2px 8px;
        border-radius: 4px;
        font-size: 0.65rem;
        font-weight: bold;
        text-transform: uppercase;
    }

    .auth-actions {
        margin-top: 10px;
        width: 100%;
    }

    .microsoft-btn {
        width: 100%;
        display: flex;
        align-items: center;
        justify-content: center;
        gap: 10px;
        background: #2f2f2f;
        border: 1px solid rgba(255, 255, 255, 0.1);
    }

    .microsoft-btn:hover {
        background: #3f3f3f;
    }

    .logout-btn {
        width: 100%;
        display: flex;
        align-items: center;
        justify-content: center;
        gap: 10px;
    }

    .icon {
        font-family: "Symbols Nerd Font Mono", sans-serif;
        font-size: 1.1rem;
    }

    .btn-danger {
        background: rgba(239, 68, 68, 0.1);
        color: #ef4444;
        border: 1px solid rgba(239, 68, 68, 0.2);
    }

    .btn-danger:hover {
        background: rgba(239, 68, 68, 0.2);
    }
</style>
