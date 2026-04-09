<script lang="ts">
    import { createInstance, fetchAll } from "$lib/api/cubicApi";
    import { launcherStore } from "$lib/state/state.svelte";
    import type { InstanceDto } from "$lib/types/types";
    import UserMenu from "./UserMenu.svelte";

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
</script>

<aside class="sidebar">
    <div class="sidebar-header">
        <h1>CUBICLAUNCHER</h1>
    </div>

    <div class="sidebar-content">
        <div class="section-label">Tus Instancias</div>
        <div class="instance-list">
            {#each launcherStore.loadedInstances as instance}
                <button
                    class="instance-item"
                    class:active={selectedInstance?.name === instance.name}
                    onclick={() => (selectedInstance = instance)}
                    title={instance.name}
                >
                    <div class="instance-icon">
                        {instance.name.charAt(0).toUpperCase()}
                    </div>
                    <span class="instance-name">{instance.name}</span>
                </button>
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
                src="https://minotar.net/avatar/{launcherStore.settings.username}"
                alt="Avatar"
                class="user-avatar"
            />
            <div class="user-info">
                <span class="user-name">{launcherStore.settings.username}</span>
                <span class="user-status">Online</span>
            </div>
        </div>
    </div>
</aside>

{#if showUserMenu}
    <UserMenu onclose={() => (showUserMenu = false)} />
{/if}
