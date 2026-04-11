<script lang="ts">
    import { createInstance, fetchAll } from "$lib/api/cubicApi";
    import { deleteInst, renameInst } from "$lib/api/launcherService";
    import { launcherStore } from "$lib/state/state.svelte";
    import type { InstanceDto } from "$lib/types/types";
    import UserMenu from "./UserMenu.svelte";
    import ModalBase from "./ModalBase.svelte";

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

    function openRenameModal(instance: InstanceDto) {
        instanceToActOn = instance;
        renameInput = instance.name;
        showRenameModal = true;
    }

    function openDeleteModal(instance: InstanceDto) {
        instanceToActOn = instance;
        showDeleteModal = true;
    }

    async function handleRename() {
        if (!instanceToActOn) return;
        if (renameInput && renameInput !== instanceToActOn.name) {
            await renameInst(instanceToActOn.uuid, renameInput);
            if (selectedInstance?.uuid === instanceToActOn.uuid) {
                selectedInstance.name = renameInput;
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
                    onkeydown={(e) => { if (e.key === "Enter" || e.key === " ") selectedInstance = instance; }}
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
                            onclick={(e) => { e.stopPropagation(); openRenameModal(instance); }}
                            title="Renombrar"
                        >
                            <svg width="12" height="12" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M11 4H4a2 2 0 0 0-2 2v14a2 2 0 0 0 2 2h14a2 2 0 0 0 2-2v-7"></path><path d="M18.5 2.5a2.121 2.121 0 0 1 3 3L12 15l-4 1 1-4 9.5-9.5z"></path></svg>
                        </button>
                        <button 
                            class="action-btn delete" 
                            onclick={(e) => { e.stopPropagation(); openDeleteModal(instance); }}
                            title="Eliminar"
                        >
                            <svg width="12" height="12" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="3 6 5 6 21 6"></polyline><path d="M19 6v14a2 2 0 0 1-2 2H7a2 2 0 0 1-2-2V6m3 0V4a2 2 0 0 1 2-2h4a2 2 0 0 1 2 2v2"></path><line x1="10" y1="11" x2="10" y2="17"></line><line x1="14" y1="11" x2="14" y2="17"></line></svg>
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
            <span style="margin-right: 8px;">+</span> Crear Instancia
        </button>
        <button class="footer-btn">Descargar Versiones</button>
        <button class="footer-btn" onclick={onOpenQuickMenu}>Ajustes</button>

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
                <span class="user-name">{launcherStore.settings.username}</span>
                <span class="user-status">Cracked</span>
            </div>
        </div>
    </div>
</aside>

{#if showUserMenu}
    <UserMenu onclose={() => (showUserMenu = false)} />
{/if}

<ModalBase bind:open={showRenameModal} title="Renombrar Instancia">
    <div class="input-group">
        <label class="input-label" for="rename-input">Nuevo Nombre</label>
        <input 
            id="rename-input"
            type="text" 
            class="text-input" 
            bind:value={renameInput} 
            onkeydown={(e) => e.key === "Enter" && handleRename()}
        />
    </div>
    {#snippet footer()}
        <button class="btn-secondary" onclick={() => (showRenameModal = false)}>Cancelar</button>
        <button class="btn-primary" onclick={handleRename}>Guardar</button>
    {/snippet}
</ModalBase>

<ModalBase bind:open={showDeleteModal} title="Eliminar Instancia">
    <p style="font-size: 0.9rem; color: var(--text-secondary); line-height: 1.4;">
        ¿Estás seguro de que deseas eliminar la instancia 
        <strong style="color: var(--text-primary);">"{instanceToActOn?.name}"</strong>? 
        Esta acción no se puede deshacer.
    </p>
    {#snippet footer()}
        <button class="btn-secondary" onclick={() => (showDeleteModal = false)}>Cancelar</button>
        <button class="btn-primary" style="background: #ff4444; color: white;" onclick={handleDelete}>Eliminar</button>
    {/snippet}
</ModalBase>
