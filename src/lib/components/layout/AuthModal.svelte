<script lang="ts">
    import { fly, fade } from "svelte/transition";
    import { t } from "$lib/i18n";
    import {
        getDeviceCode,
        authenticateWithDeviceCode,
    } from "$lib/api/cubicApi";
    import { launcherStore } from "$lib/state/state.svelte";
    import { onMount } from "svelte";

    let { onclose } = $props<{ onclose: () => void }>();

    let deviceCode = $state<any>(null);
    let loading = $state(true);
    let error = $state<string | null>(null);
    let success = $state(false);
    let copied = $state(false);

    async function startAuth() {
        try {
            loading = true;
            error = null;
            deviceCode = await getDeviceCode();
            loading = false;

            // Start polling
            const user = await authenticateWithDeviceCode(
                deviceCode.device_code,
                deviceCode.interval,
                deviceCode.expires_in,
            );

            launcherStore.settings.user = user;
            launcherStore.settings.username = user.username;
            success = true;

            setTimeout(() => {
                onclose();
            }, 2000);
        } catch (e: any) {
            console.error("Auth error:", e);
            error = e.toString();
            loading = false;
        }
    }

    onMount(() => {
        startAuth();
    });

    async function handleCopyLink() {
        if (deviceCode) {
            try {
                await navigator.clipboard.writeText(
                    deviceCode.verification_uri,
                );
                copied = true;
                setTimeout(() => {
                    copied = false;
                }, 2000);
            } catch (err) {
                console.error("Failed to copy:", err);
            }
        }
    }
</script>

<div
    class="modal-overlay"
    onclick={onclose}
    onkeydown={(e) => e.key === "Escape" && onclose()}
    role="button"
    tabindex="-1"
    transition:fade={{ duration: 200 }}
>
    <div
        class="modal"
        onclick={(e) => e.stopPropagation()}
        onkeydown={(e) => e.stopPropagation()}
        role="dialog"
        tabindex="0"
        transition:fly={{ y: 20, duration: 300, opacity: 0 }}
    >
        <div class="modal-header">
            <h2 class="modal-title">{t("userMenu.authModal.title")}</h2>
            <button class="btn-secondary close-btn" onclick={onclose}>✕</button>
        </div>

        <div class="modal-body auth-body">
            {#if loading}
                <div class="loader-container">
                    <div class="loader"></div>
                    <p>{t("versionDownloader.loading")}</p>
                </div>
            {:else if error}
                <div class="error-container">
                    <p class="error-text">
                        {t("userMenu.authModal.error").replace(
                            "{error}",
                            error,
                        )}
                    </p>
                    <button class="btn-primary" onclick={startAuth}
                        >Reintentar</button
                    >
                </div>
            {:else if success}
                <div class="success-container">
                    <div class="success-icon">✓</div>
                    <p>{t("userMenu.authModal.success")}</p>
                </div>
            {:else if deviceCode}
                <div class="auth-instructions">
                    <p>{t("userMenu.authModal.instruction")}</p>

                    <div class="code-display">
                        {deviceCode.user_code}
                    </div>

                    <button
                        class="btn-primary copy-btn {copied ? 'copied' : ''}"
                        onclick={handleCopyLink}
                    >
                        <span class="icon">{copied ? "󰄬" : "󰆏"}</span>
                        {copied
                            ? t("userMenu.authModal.linkCopied")
                            : t("userMenu.authModal.copyLink")}
                    </button>

                    <div class="waiting-indicator">
                        <div class="pulse-dot"></div>
                        <p>{t("userMenu.authModal.waiting")}</p>
                    </div>
                </div>
            {/if}
        </div>
    </div>
</div>

<style>
    .auth-body {
        display: flex;
        flex-direction: column;
        align-items: center;
        text-align: center;
        padding: 20px;
        min-height: 250px;
        justify-content: center;
    }

    .code-display {
        background: rgba(0, 0, 0, 0.3);
        padding: 15px 30px;
        border-radius: 8px;
        font-size: 2rem;
        font-weight: bold;
        letter-spacing: 5px;
        margin: 20px 0;
        color: var(--accent-color, #3b82f6);
        border: 2px solid rgba(59, 130, 246, 0.3);
    }

    .auth-instructions p {
        margin-bottom: 15px;
        opacity: 0.9;
        line-height: 1.5;
    }

    .copy-btn {
        display: flex;
        align-items: center;
        gap: 10px;
        transition: all 0.3s ease;
    }

    .copy-btn.copied {
        background: #10b981;
        border-color: #10b981;
    }

    .icon {
        font-family: "Symbols Nerd Font Mono", sans-serif;
        font-size: 1.1rem;
    }

    .waiting-indicator {
        margin-top: 25px;
        display: flex;
        align-items: center;
        gap: 10px;
        opacity: 0.7;
    }

    .pulse-dot {
        width: 8px;
        height: 8px;
        background-color: var(--accent-color, #3b82f6);
        border-radius: 50%;
        animation: pulse 1.5s infinite;
    }

    @keyframes pulse {
        0% {
            transform: scale(0.95);
            box-shadow: 0 0 0 0 rgba(59, 130, 246, 0.7);
        }
        70% {
            transform: scale(1);
            box-shadow: 0 0 0 10px rgba(59, 130, 246, 0);
        }
        100% {
            transform: scale(0.95);
            box-shadow: 0 0 0 0 rgba(59, 130, 246, 0);
        }
    }

    .loader {
        border: 3px solid rgba(255, 255, 255, 0.1);
        border-top: 3px solid var(--accent-color, #3b82f6);
        border-radius: 50%;
        width: 40px;
        height: 40px;
        animation: spin 1s linear infinite;
        margin-bottom: 15px;
    }

    @keyframes spin {
        0% {
            transform: rotate(0deg);
        }
        100% {
            transform: rotate(360deg);
        }
    }

    .success-icon {
        font-size: 3rem;
        color: #10b981;
        margin-bottom: 15px;
    }

    .error-text {
        color: #ef4444;
        margin-bottom: 15px;
    }

    .close-btn {
        padding: 4px 8px;
        font-size: 0.7rem;
    }
</style>
