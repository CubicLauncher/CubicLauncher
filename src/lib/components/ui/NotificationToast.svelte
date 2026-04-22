<script lang="ts">
    import type { Notification } from "$lib/types/types";
    import { removeNotification } from "$lib/state/state.svelte";
    import { fly, fade } from "svelte/transition";
    import { onMount } from "svelte";

    let { notification }: { notification: Notification } = $props();

    let progress = $state(100);
    let startTime = Date.now();
    let interval: number;

    onMount(() => {
        if (notification.timeout && notification.timeout > 0) {
            interval = setInterval(() => {
                const elapsed = Date.now() - startTime;
                progress = Math.max(0, 100 - (elapsed / notification.timeout!) * 100);
                
                if (progress <= 0) {
                    clearInterval(interval);
                    removeNotification(notification.id);
                }
            }, 16); // ~60fps
        }
        return () => clearInterval(interval);
    });

    const colors = {
        error: "#ff4d4d",
        warning: "#ffcc00",
        info: "#00aaff",
        success: "#00ff88"
    };
</script>

<div
    class="notification-toast {notification.type}"
    in:fly={{ x: 100, duration: 400 }}
    out:fade={{ duration: 200 }}
    role="alert"
>
    <div class="icon-container">
        {#if notification.type === 'error'}
            <svg viewBox="0 0 24 24" fill="none" stroke={colors.error} stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round" class="svg-icon">
                <circle cx="12" cy="12" r="10"></circle>
                <line x1="15" y1="9" x2="9" y2="15"></line>
                <line x1="9" y1="9" x2="15" y2="15"></line>
            </svg>
        {:else if notification.type === 'warning'}
            <svg viewBox="0 0 24 24" fill="none" stroke={colors.warning} stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round" class="svg-icon">
                <path d="M10.29 3.86L1.82 18a2 2 0 0 0 1.71 3h16.94a2 2 0 0 0 1.71-3L13.71 3.86a2 2 0 0 0-3.42 0z"></path>
                <line x1="12" y1="9" x2="12" y2="13"></line>
                <line x1="12" y1="17" x2="12.01" y2="17"></line>
            </svg>
        {:else if notification.type === 'success'}
            <svg viewBox="0 0 24 24" fill="none" stroke={colors.success} stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round" class="svg-icon">
                <path d="M22 11.08V12a10 10 0 1 1-5.93-9.14"></path>
                <polyline points="22 4 12 14.01 9 11.01"></polyline>
            </svg>
        {:else}
            <svg viewBox="0 0 24 24" fill="none" stroke={colors.info} stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round" class="svg-icon">
                <circle cx="12" cy="12" r="10"></circle>
                <line x1="12" y1="16" x2="12" y2="12"></line>
                <line x1="12" y1="8" x2="12.01" y2="8"></line>
            </svg>
        {/if}
    </div>
    
    <div class="content">
        <h4 class="title">{notification.title}</h4>
        <p class="message">{notification.message}</p>
    </div>

    <button class="close-btn" onclick={() => removeNotification(notification.id)} aria-label="Cerrar">
        <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" style="width: 16px; height: 16px;">
            <line x1="18" y1="6" x2="6" y2="18"></line>
            <line x1="6" y1="6" x2="18" y2="18"></line>
        </svg>
    </button>

    {#if notification.timeout && notification.timeout > 0}
        <div class="progress-bar">
            <div class="progress-fill" style="width: {progress}%; background-color: {colors[notification.type]}"></div>
        </div>
    {/if}
</div>

<style>
    .notification-toast {
        position: relative;
        display: flex;
        align-items: flex-start;
        gap: 14px;
        min-width: 340px;
        max-width: 480px;
        padding: 18px;
        border-radius: 14px;
        background: rgba(22, 22, 28, 0.9);
        backdrop-filter: blur(16px);
        -webkit-backdrop-filter: blur(16px);
        border: 1px solid rgba(255, 255, 255, 0.08);
        box-shadow: 0 12px 40px rgba(0, 0, 0, 0.5);
        margin-bottom: 12px;
        overflow: hidden;
        color: white;
    }

    .notification-toast.error { border-left: 4px solid #ff4d4d; }
    .notification-toast.warning { border-left: 4px solid #ffcc00; }
    .notification-toast.info { border-left: 4px solid #00aaff; }
    .notification-toast.success { border-left: 4px solid #00ff88; }

    .icon-container {
        display: flex;
        align-items: center;
        justify-content: center;
        width: 36px;
        height: 36px;
        flex-shrink: 0;
    }

    .svg-icon {
        width: 24px;
        height: 24px;
    }

    .content {
        flex: 1;
        display: flex;
        flex-direction: column;
        gap: 4px;
        padding-top: 2px;
    }

    .title {
        margin: 0;
        font-size: 15px;
        font-weight: 700;
        letter-spacing: 0.2px;
        color: #fff;
    }

    .message {
        margin: 0;
        font-size: 13px;
        opacity: 0.85;
        line-height: 1.5;
        color: #e0e0e0;
    }

    .close-btn {
        background: rgba(255, 255, 255, 0.05);
        border: none;
        color: rgba(255, 255, 255, 0.5);
        border-radius: 6px;
        cursor: pointer;
        padding: 6px;
        display: flex;
        align-items: center;
        justify-content: center;
        transition: all 0.2s;
    }

    .close-btn:hover {
        background: rgba(255, 255, 255, 0.1);
        color: white;
    }

    .progress-bar {
        position: absolute;
        bottom: 0;
        left: 0;
        width: 100%;
        height: 3px;
        background: rgba(255, 255, 255, 0.05);
    }

    .progress-fill {
        height: 100%;
        transition: width 0.016s linear;
    }
</style>
