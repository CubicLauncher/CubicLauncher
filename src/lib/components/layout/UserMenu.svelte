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
			<div class="input-group" style="width: 100%; margin-top: 10px;">
				<label class="input-label" for="username-edit"
					>{t("userMenu.usernameLabel")}</label
				>
				<div style="display: flex; gap: 8px; align-items: center;">
					{#if editing}
						<input
							id="username-edit"
							type="text"
							bind:value={newUsername}
							onkeydown={handleKeydown}
							class="text-input"
							style="flex: 1;"
							placeholder={t("userMenu.usernamePlaceholder")}
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
				type="button"
				class="btn-danger logout-btn"
				onclick={handleLogout}
			>
				{t("userMenu.logout")}
			</button>
		{:else}
			<button
				type="button"
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
				type="button"
				class="btn-secondary"
				onclick={() => {
					editing = false;
					newUsername = launcherStore.settings.username;
				}}>Cancelar</button
			>
			<button type="button" class="btn-primary" onclick={handleSave}
				>{t("userMenu.save")}</button
			>
		{:else if !launcherStore.settings.user}
			<button
				type="button"
				class="btn-secondary"
				style="flex: 1;"
				onclick={() => (editing = true)}
				>{t("userMenu.changeNameBtn")}</button
			>
		{/if}
	{/snippet}
</ModalBase>

<AuthModal bind:open={showAuthModal} />

<style>
	.user-profile-center {
		display: flex;
		flex-direction: column;
		align-items: center;
		gap: 16px;
	}

	.avatar-wrapper {
		position: relative;
	}

	.large-avatar {
		width: 72px;
		height: 72px;
		border-radius: var(--border-radius-sm);
		background: rgba(255, 255, 255, 0.03);
		border: 1px solid var(--border);
	}

	.status-dot {
		position: absolute;
		bottom: -4px;
		right: -4px;
		width: 14px;
		height: 14px;
		background: #4caf50;
		border: 2px solid var(--bg-sidebar);
		border-radius: 50%;
		box-shadow: 0 0 10px rgba(76, 175, 80, 0.4);
	}

	.username-display-wrapper {
		display: flex;
		flex-direction: column;
		align-items: center;
		width: 100%;
		padding: 12px;
		background: rgba(255, 255, 255, 0.02);
		border: 1px solid var(--border);
		border-radius: var(--border-radius-sm);
		gap: 4px;
	}

	.username-text {
		font-size: 1.1rem;
		font-weight: 700;
		color: var(--text-primary);
	}

	.offline-tag {
		font-size: 0.65rem;
		text-transform: uppercase;
		letter-spacing: 1px;
		color: var(--text-secondary);
		font-weight: 600;
	}

	.premium-tag {
		background: var(--bg-item-active);
		border: 1px solid var(--border);
		color: var(--accent);
		padding: 2px 8px;
		border-radius: var(--border-radius-sm);
		font-size: 0.65rem;
		font-weight: 700;
		text-transform: uppercase;
		letter-spacing: 0.5px;
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
		background: var(--accent) !important;
		color: var(--bg-main) !important;
		border: none !important;
		font-weight: 700;
		padding: 10px;
		border-radius: var(--border-radius-sm);
		cursor: pointer;
		transition: all 0.2s ease;
	}

	.microsoft-btn:hover {
		opacity: 0.9;
		box-shadow: 0 4px 12px rgba(255, 255, 255, 0.1);
	}

	.logout-btn {
		width: 100%;
		display: flex;
		align-items: center;
		justify-content: center;
		gap: 10px;
	}

	.btn-danger {
		background: transparent;
		border: 1px solid var(--border);
		color: var(--text-secondary);
		padding: 8px 16px;
		border-radius: var(--border-radius-sm);
		font-size: 0.8rem;
		font-weight: 600;
		cursor: pointer;
		transition: all 0.2s ease;
		display: flex;
		align-items: center;
		justify-content: center;
		gap: 10px;
		width: 100%;
	}

	.btn-danger:hover {
		background: rgba(255, 68, 68, 0.1);
		border-color: rgba(255, 68, 68, 0.2);
		color: #ff4444;
	}
</style>
