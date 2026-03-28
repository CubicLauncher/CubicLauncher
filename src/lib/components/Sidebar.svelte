<script lang="ts">
    import { createInstance, fetchAll } from "$lib/api/cubicApi";
    import { launcherStore } from "$lib/state/state.svelte";
    import type { InstanceDto } from "$lib/types/types";

    let { selectedInstance = $bindable() } = $props<{
        selectedInstance: InstanceDto | null;
    }>();
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
        <button class="footer-btn">Descargar Versiones</button>
        <button
            class="footer-btn"
            onclick={() => {
                // createInstance("hola", "1.16.5");
                let x = fetchAll();
                console.log(x);
            }}>asd</button
        >
        <button class="footer-btn">Ajustes</button>

        <div class="user-profile">
            <img
                src="https://media.0221.com.ar/adjuntos/357/migration/0221/032019/1553553111207.jpg"
                alt="Avatar"
                class="user-avatar"
            />
            <div class="user-info">
                <span class="user-name">Santiagolxx</span>
                <span class="user-status">Online</span>
            </div>
        </div>
    </div>
</aside>
