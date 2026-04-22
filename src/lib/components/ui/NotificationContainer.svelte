<script lang="ts">
    import { launcherStore } from "$lib/state/state.svelte";
    import NotificationToast from "./NotificationToast.svelte";
    import { flip } from "svelte/animate";
</script>

<div class="notification-container">
    {#each launcherStore.notifications as notification (notification.id)}
        <div animate:flip={{ duration: 300 }}>
            <NotificationToast {notification} />
        </div>
    {/each}
</div>

<style>
    .notification-container {
        position: fixed;
        top: 24px;
        right: 24px;
        z-index: 9999;
        display: flex;
        flex-direction: column;
        align-items: flex-end;
        pointer-events: none;
    }

    /* Enable pointer events for the toasts themselves */
    .notification-container :global(.notification-toast) {
        pointer-events: auto;
    }
</style>
