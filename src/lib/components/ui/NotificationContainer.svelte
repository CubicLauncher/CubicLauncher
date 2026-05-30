<script lang="ts">
	import { launcherStore } from "$lib/state/state.svelte";
	import NotificationToast from "./NotificationToast.svelte";
</script>

<div class="notification-container">
	{#each launcherStore.notifications as notification (notification.id)}
		<NotificationToast {notification} />
	{/each}
</div>

<style>
	.notification-container {
		position: fixed;
		top: 1.5rem;
		right: 1.5rem;
		display: flex;
		flex-direction: column;
		align-items: flex-end;
		gap: 15px;
		z-index: 9999;
		pointer-events: none;
	}

	.notification-container :global(.notification-toast) {
		pointer-events: all;
	}
</style>
