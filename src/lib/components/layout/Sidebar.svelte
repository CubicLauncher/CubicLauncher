<script lang="ts">
    import {
        createInstance,
        fetchAll,
        getInstalledVersions,
    } from "$lib/api/cubicApi";
    import {
        deleteInst,
        renameInst,
        updateInst,
    } from "$lib/api/launcherService";
    import { launcherStore } from "$lib/state/state.svelte";
    import type { InstanceDto } from "$lib/types/types";
    import UserMenu from "./UserMenu.svelte";
    import ModalBase from "./ModalBase.svelte";
    import Select from "./Select.svelte";

    let {
        selectedInstance = $bindable(),
        openCreateModal = $bindable(),
        onOpenQuickMenu,
    } = $props<{
        selectedInstance: InstanceDto | null;
        openCreateModal: boolean;
        onOpenQuickMenu?: () => void;
    }>();

    let showUserMenu = $state(false);
    let showRenameModal = $state(false);
    let showDeleteModal = $state(false);
    let instanceToActOn = $state<InstanceDto | null>(null);
    let renameInput = $state("");
    let versionInput = $state("");
    let installedVersions = $state<string[]>([]);

    async function openRenameModal(instance: InstanceDto) {
        instanceToActOn = instance;
        renameInput = instance.name;
        versionInput = instance.version;
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

        if (nameChanged || versionChanged) {
            await updateInst(
                instanceToActOn.uuid,
                nameChanged ? renameInput : undefined,
                versionChanged ? versionInput : undefined,
            );

            if (selectedInstance?.uuid === instanceToActOn.uuid) {
                if (nameChanged) selectedInstance.name = renameInput;
                if (versionChanged) selectedInstance.version = versionInput;
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
        <h1>CUBICLAUNCHER</h1>
    </div>

    <div class="sidebar-content">
        <div class="section-label">Tus Instancias</div>
        <div class="instance-list">
            {#each launcherStore.loadedInstances as instance}
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
                            {instance.name.charAt(0).toUpperCase()}
                        </div>
                        <span class="instance-name">{instance.name}</span>
                    </div>
                    <div class="instance-actions">
                        <button
                            class="action-btn"
                            onclick={(e) => {
                                e.stopPropagation();
                                openRenameModal(instance);
                            }}
                            title="Renombrar"
                        >
                            <img
                                src="/images/icons/edit.svg"
                                alt="Renombrar"
                                width="12"
                                height="12"
                                style="filter: invert(1);"
                            />
                        </button>
                        <button
                            class="action-btn delete"
                            onclick={(e) => {
                                e.stopPropagation();
                                openDeleteModal(instance);
                            }}
                            title="Eliminar"
                        >
                            <img
                                src="/images/icons/trash.svg"
                                alt="Eliminar"
                                width="12"
                                height="12"
                                style="filter: invert(30%) sepia(80%) saturate(5000%) hue-rotate(0deg) brightness(100%) contrast(100%);"
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
                    <span class="instance-name">Sin instancias</span>
                </div>
            {/if}
        </div>
    </div>

    <div class="sidebar-footer">
        <button class="footer-btn" onclick={() => (openCreateModal = true)}>
            <img
                src="/images/icons/create.svg"
                alt=""
                width="16"
                height="16"
                style="filter: invert(1);"
            />
            Crear Instancia
        </button>
        <button class="footer-btn">
            <img
                src="/images/icons/download.svg"
                alt=""
                width="16"
                height="16"
                style="filter: invert(1);"
            />
            Descargar Versiones
        </button>
        <button class="footer-btn" onclick={onOpenQuickMenu}>
            <img
                src="/images/icons/settings.svg"
                alt=""
                width="16"
                height="16"
                style="filter: invert(1);"
            />
            Ajustes
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
                    <span class="user-name">{launcherStore.settings.username}</span>
                    <img
                        src="/images/icons/edit.svg"
                        alt="Editar"
                        class="user-edit-icon"
                        width="12"
                        height="12"
                    />
                </div>
                <span class="user-status">Cracked</span>
            </div>
        </div>
    </div>
</aside>

{#if showUserMenu}
    <UserMenu onclose={() => (showUserMenu = false)} />
{/if}

<ModalBase bind:open={showRenameModal} title="Editar Instancia">
    <div class="input-group">
        <label class="input-label" for="rename-input">Nombre</label>
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
            label="Versión"
            options={installedVersions.map((v) => ({ value: v, label: v }))}
            bind:value={versionInput}
        />
    </div>

    {#snippet footer()}
        <button class="btn-secondary" onclick={() => (showRenameModal = false)}
            >Cancelar</button
        >
        <button class="btn-primary" onclick={handleRename}>Guardar</button>
    {/snippet}
</ModalBase>

<ModalBase bind:open={showDeleteModal} title="Eliminar Instancia">
    <p
        style="font-size: 0.9rem; color: var(--text-secondary); line-height: 1.4;"
    >
        ¿Estás seguro de que deseas eliminar la instancia
        <strong style="color: var(--text-primary);"
            >"{instanceToActOn?.name}"</strong
        >? Esta acción no se puede deshacer.
    </p>
    {#snippet footer()}
        <button class="btn-secondary" onclick={() => (showDeleteModal = false)}
            >Cancelar</button
        >
        <button
            class="btn-primary"
            style="background: #ff4444; color: white;"
            onclick={handleDelete}>Eliminar</button
        >
    {/snippet}
</ModalBase>
