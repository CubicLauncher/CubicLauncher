<script lang="ts">
    import { launcherStore } from "$lib/state/state.svelte";
    import { saveSettings } from "$lib/api/launcherService";
    import { t } from "$lib/i18n";
    import { showError } from "$lib/state/state.svelte";
    import { logout } from "$lib/api/cubicApi";
    import AuthModal from "./AuthModal.svelte";
    import ModalBase from "./ModalBase.svelte";

    let { open = $bindable(false) } = $props<{ open: boolean }>();

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

<ModalBase bind:open title={t("userMenu.title")}>
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
                {t("userMenu.logout")}
            </button>
        {:else}
            <button
                class="btn-primary microsoft-btn"
                onclick={() => (showAuthModal = true)}
            >
                {t("userMenu.loginMicrosoft")}
            </button>
        {/if}
    </div>

    {#snippet footer()}
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
    {/snippet}
</ModalBase>

<AuthModal bind:open={showAuthModal} />


