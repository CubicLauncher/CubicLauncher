<script lang="ts">
	import { getInstalledVersions } from "$lib/api/cubicApi";
	import { INSTANCE_LOGOS } from "$lib/icons/logos";
	import { deleteInst, updateInst } from "$lib/api/launcherService";
	import { launcherStore } from "$lib/state/state.svelte";
	import type { InstanceDto } from "$lib/types/types";
	import UserMenu from "./UserMenu.svelte";
	import ModalBase from "./ModalBase.svelte";
	import Select from "./Select.svelte";
	import { t } from "$lib/i18n";

	interface Props {
		selectedInstance: InstanceDto | null;
		onopenquickmenu?: () => void;
		onopenversiondownloader?: () => void;
		onopencreateinstance?: () => void;
	}

	let {
		selectedInstance = $bindable(),
		onopenquickmenu,
		onopenversiondownloader,
		onopencreateinstance,
	}: Props = $props();

	let showUserMenu = $state(false);
	let showRenameModal = $state(false);
	let showDeleteModal = $state(false);
	let instanceToActOn = $state<InstanceDto | null>(null);
	let renameInput = $state("");
	let versionInput = $state("");
	let selectedIcon = $state<string | null>(null);
	let installedVersions = $state<string[]>([]);
	let availableIcons = $state<string[]>(INSTANCE_LOGOS);
	let versionOptions = $derived(
		installedVersions.map((v) => ({ value: v, label: v })),
	);

	async function openRenameModal(instance: InstanceDto) {
		instanceToActOn = instance;
		renameInput = instance.name;
		versionInput = instance.version;
		selectedIcon = instance.icon;
		installedVersions = await getInstalledVersions();
		showRenameModal = true;
	}

	function openDeleteModal(instance: InstanceDto) {
		instanceToActOn = instance;
		showDeleteModal = true;
	}

	async function handleRename() {
		if (!instanceToActOn) return;
		const nameChanged = renameInput && renameInput !== instanceToActOn.name;
		const versionChanged =
			versionInput && versionInput !== instanceToActOn.version;

		const iconChanged = selectedIcon !== instanceToActOn.icon;

		if (nameChanged || versionChanged || iconChanged) {
			await updateInst(
				instanceToActOn.uuid,
				nameChanged ? renameInput : undefined,
				versionChanged ? versionInput : undefined,
				iconChanged ? selectedIcon : undefined,
			);

			if (selectedInstance?.uuid === instanceToActOn.uuid) {
				if (nameChanged) selectedInstance.name = renameInput;
				if (versionChanged) selectedInstance.version = versionInput;
				if (iconChanged) selectedInstance.icon = selectedIcon;
			}
		}
		showRenameModal = false;
	}

	async function handleDelete() {
		if (!instanceToActOn) return;
		await deleteInst(instanceToActOn.uuid);
		if (selectedInstance?.uuid === instanceToActOn.uuid) {
			selectedInstance = null;
		}
		showDeleteModal = false;
	}
</script>

<aside class="sidebar">
	<div class="sidebar-header">
		<h1 style="font-size: 0.9rem; font-weight: bold;">CUBICLAUNCHER</h1>
	</div>

	<div class="sidebar-content">
		<div class="section-label">{t("sidebar.yourInstances")}</div>
		<div class="instance-list">
			{#each launcherStore.loadedInstances as instance (instance.uuid)}
				<div
					class="instance-item"
					class:active={selectedInstance?.uuid === instance.uuid}
					onclick={() => (selectedInstance = instance)}
					onkeydown={(e) => {
						if (e.key === "Enter" || e.key === " ")
							selectedInstance = instance;
					}}
					role="button"
					tabindex="0"
					title={instance.name}
				>
					<div class="instance-info-container">
						<div class="instance-icon">
							{#if instance.icon}
								<img
									src={instance.icon}
									alt={instance.name}
									width="16"
									height="16"
								/>
							{:else}
								{instance.name.charAt(0).toUpperCase()}
							{/if}
						</div>
						<span class="instance-name">{instance.name}</span>
					</div>
					<div class="instance-actions">
						<button
							type="button"
							class="action-btn"
							onclick={(e) => {
								e.stopPropagation();
								openRenameModal(instance);
							}}
							title={t("sidebar.rename")}
						>
							<img
								src="/images/icons/edit.svg"
								alt={t("sidebar.rename")}
								width="12"
								height="12"
								style="filter: var(--icon-filter);"
							/>
						</button>
						<button
							type="button"
							class="action-btn delete"
							onclick={(e) => {
								e.stopPropagation();
								openDeleteModal(instance);
							}}
							title={t("sidebar.delete")}
						>
							<img
								src="/images/icons/trash.svg"
								alt={t("sidebar.delete")}
								width="12"
								height="12"
								style="filter: var(--icon-filter-error);"
							/>
						</button>
					</div>
				</div>
			{/each}
			{#if launcherStore.loadedInstances.length === 0}
				<div
					class="instance-item"
					style="opacity: 0.4; cursor: default;"
				>
					<span class="instance-name">{t("sidebar.noInstances")}</span
					>
				</div>
			{/if}
		</div>
	</div>

	<div class="sidebar-footer">
		<button type="button" class="footer-btn" onclick={onopencreateinstance}>
			<img
				src="/images/icons/create.svg"
				alt=""
				width="16"
				height="16"
				style="filter: var(--icon-filter);"
			/>
			{t("sidebar.createInstance")}
		</button>
		<button
			type="button"
			class="footer-btn"
			onclick={onopenversiondownloader}
		>
			<img
				src="/images/icons/download.svg"
				alt=""
				width="16"
				height="16"
				style="filter: var(--icon-filter);"
			/>
			{t("sidebar.downloadVersions")}
		</button>
		<button type="button" class="footer-btn" onclick={onopenquickmenu}>
			<img
				src="/images/icons/settings.svg"
				alt=""
				width="16"
				height="16"
				style="filter: var(--icon-filter);"
			/>
			{t("sidebar.settings")}
		</button>
		<div
			class="user-profile"
			onclick={() => (showUserMenu = true)}
			role="button"
			tabindex="0"
			onkeydown={(e) =>
				(e.key === "Enter" || e.key === " ") && (showUserMenu = true)}
			style="cursor: pointer;"
		>
			<img
				src="https://minotar.net/avatar/{launcherStore.settings
					.username}"
				alt="Avatar"
				class="user-avatar"
			/>
			<div class="user-info">
				<div class="user-name-wrapper">
					<span class="user-name"
						>{launcherStore.settings.username}</span
					>
					<img
						src="/images/icons/edit.svg"
						alt={t("userMenu.edit")}
						class="user-edit-icon"
						width="12"
						height="12"
					/>
				</div>
				<span
					class="user-status"
					class:premium={launcherStore.settings.user}
				>
					{launcherStore.settings.user
						? t("userMenu.premium")
						: t("userMenu.offline")}
				</span>
			</div>
		</div>
	</div>
</aside>

<UserMenu bind:open={showUserMenu} />

<ModalBase bind:open={showRenameModal} title={t("sidebar.modals.editTitle")}>
	<div class="input-group" style="margin-top: 12px;">
		<label class="input-label" for="icon-selector"
			>{t("createInstance.iconLabel") || "Logo de la Instancia"}</label
		>
		<div id="icon-selector" class="icon-selector" style="margin-top: 8px;">
			{#each availableIcons as iconName (iconName)}
				{@const iconPath = `/images/instances/${iconName}`}
				<button
					type="button"
					class="icon-option"
					class:selected={selectedIcon === iconPath}
					onclick={() =>
						(selectedIcon =
							selectedIcon === iconPath ? null : iconPath)}
					title={iconName}
				>
					<img src={iconPath} alt={iconName} />
				</button>
			{/each}
		</div>
	</div>

	<div class="input-group">
		<label class="input-label" for="rename-input"
			>{t("sidebar.modals.nameLabel")}</label
		>
		<input
			id="rename-input"
			type="text"
			class="text-input"
			bind:value={renameInput}
			onkeydown={(e) => e.key === "Enter" && handleRename()}
		/>
	</div>

	<div class="input-group" style="margin-top: 12px;">
		<Select
			id="version-select"
			label={t("sidebar.modals.versionLabel")}
			options={versionOptions}
			bind:value={versionInput}
		/>
	</div>

	{#snippet footer()}
		<button
			type="button"
			class="btn-secondary"
			onclick={() => (showRenameModal = false)}
			>{t("sidebar.modals.cancel")}</button
		>
		<button type="button" class="btn-primary" onclick={handleRename}
			>{t("sidebar.modals.save")}</button
		>
	{/snippet}
</ModalBase>

<ModalBase bind:open={showDeleteModal} title={t("sidebar.modals.deleteTitle")}>
	<p
		style="font-size: 0.9rem; color: var(--text-secondary); line-height: 1.4;"
	>
		{t("sidebar.modals.deleteDesc1")}
		<strong style="color: var(--text-primary);"
			>"{instanceToActOn?.name}"</strong
		>{t("sidebar.modals.deleteDesc2")}
	</p>
	{#snippet footer()}
		<button
			type="button"
			class="btn-secondary"
			onclick={() => (showDeleteModal = false)}
			>{t("sidebar.modals.cancel")}</button
		>
		<button
			type="button"
			class="btn-primary"
			style="background: var(--color-error); color: white;"
			onclick={handleDelete}>{t("sidebar.modals.deleteBtn")}</button
		>
	{/snippet}
</ModalBase>

<style>
	.sidebar {
		width: var(--sidebar-width);
		flex-shrink: 0;
		background-color: var(--bg-sidebar);
		border-right: 1px solid var(--border);
		display: flex;
		flex-direction: column;
		padding: 18px 16px;
		z-index: 10;
		user-select: none;
	}

	.sidebar-header {
		padding-bottom: 14px;
		margin-bottom: 8px;
		border-bottom: 1px solid var(--border);
	}

	.sidebar-header h1 {
		font-size: 0.65rem;
		font-weight: 700;
		letter-spacing: 2px;
		text-transform: uppercase;
		color: var(--text-secondary);
	}

	.section-label {
		font-size: 0.65rem;
		font-weight: 700;
		color: var(--text-secondary);
		text-transform: uppercase;
		letter-spacing: 1.5px;
		margin-bottom: 10px;
		display: block;
	}

	.sidebar-content {
		flex: 1;
		overflow-y: auto;
		min-height: 0;
		padding: 6px 0;
	}

	.instance-list {
		display: flex;
		flex-direction: column;
		gap: 3px;
	}

	.instance-item {
		display: flex;
		align-items: center;
		gap: 10px;
		padding: 8px 10px;
		border-radius: var(--border-radius-sm);
		cursor: pointer;
		transition:
			background 0.15s ease,
			border-color 0.15s ease;
		border: 1px solid transparent;
		background: transparent;
		color: var(--text-primary);
		width: 100%;
		text-align: left;
		font-family: "Cantarell", system-ui, sans-serif;
	}

	.instance-item:hover {
		background: rgba(255, 255, 255, 0.03);
	}

	.instance-item.active {
		background: var(--bg-item-active);
		border-color: var(--border);
	}

	.instance-icon {
		width: 22px;
		height: 22px;
		background: rgba(255, 255, 255, 0.04);
		border: 1px solid var(--border);
		border-radius: var(--border-radius-sm);
		display: flex;
		align-items: center;
		justify-content: center;
		font-size: 0.7rem;
		flex-shrink: 0;
	}

	.instance-info-container {
		display: flex;
		align-items: center;
		gap: 10px;
		flex: 1;
		min-width: 0;
	}

	.instance-name {
		font-weight: 500;
		font-size: 0.85rem;
		white-space: nowrap;
		overflow: hidden;
		text-overflow: ellipsis;
	}

	.instance-actions {
		display: flex;
		gap: 4px;
		opacity: 0;
		transition: opacity 0.2s ease;
	}

	.instance-item:hover .instance-actions {
		opacity: 1;
	}

	.sidebar-footer {
		margin-top: auto;
		padding-top: 12px;
		border-top: 1px solid var(--border);
		display: flex;
		flex-direction: column;
		gap: 6px;
	}

	.footer-btn {
		background: transparent;
		border: 1px solid var(--border);
		color: var(--text-secondary);
		padding: 8px 12px;
		border-radius: var(--border-radius-sm);
		font-size: 0.78rem;
		font-weight: 500;
		cursor: pointer;
		display: flex;
		align-items: center;
		gap: 8px;
		transition:
			background 0.15s ease,
			color 0.15s ease;
		font-family: "Cantarell", system-ui, sans-serif;
	}

	.footer-btn:hover {
		background: var(--bg-item-active);
		color: var(--text-primary);
	}

	.user-profile {
		display: flex;
		align-items: center;
		gap: 10px;
		padding: 10px 0 0;
		margin-top: 5px;
		border-top: 1px solid var(--border);
	}

	.user-avatar {
		width: 28px;
		height: 28px;
		border-radius: var(--border-radius-sm);
		background: rgba(255, 255, 255, 0.04);
		border: 1px solid var(--border);
	}

	.user-info {
		display: flex;
		flex-direction: column;
		overflow: hidden;
		gap: 2px;
	}

	.user-name {
		font-size: 0.82rem;
		font-weight: 600;
		white-space: nowrap;
		overflow: hidden;
		text-overflow: ellipsis;
	}

	.user-name-wrapper {
		display: flex;
		align-items: center;
		gap: 6px;
	}

	.user-edit-icon {
		opacity: 0;
		filter: invert(1);
		transition:
			opacity 0.2s ease,
			transform 0.2s ease;
		transform: translateX(-4px);
		pointer-events: none;
	}

	.user-profile:hover .user-edit-icon {
		opacity: 0.5;
		transform: translateX(0);
	}

	.user-status {
		font-size: 0.68rem;
		color: var(--text-secondary);
		letter-spacing: 0.3px;
		transition: color 0.2s ease;
	}

	.user-status.premium {
		color: var(--accent);
		font-weight: 600;
	}

	@media (max-width: 650px) {
		.sidebar {
			width: 70px;
			padding: 15px 10px;
		}

		.sidebar-header h1,
		.instance-name,
		.footer-btn,
		.user-info {
			display: none;
		}

		.instance-item {
			justify-content: center;
			padding: 12px 0;
		}
	}
</style>
