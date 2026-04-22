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
