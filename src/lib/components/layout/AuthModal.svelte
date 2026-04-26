<script lang="ts">
    import { t } from "$lib/i18n";
    import {
        getDeviceCode,
        authenticateWithDeviceCode,
    } from "$lib/api/cubicApi";
    import { launcherStore } from "$lib/state/state.svelte";
    import ModalBase from "./ModalBase.svelte";

    let { open = $bindable(false) } = $props<{ open: boolean }>();

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
                open = false;
            }, 2000);
        } catch (e: any) {
            console.error("Auth error:", e);
            error = e.toString();
            loading = false;
        }
    }

    $effect(() => {
        if (open) {
            // Reset state when opened
            deviceCode = null;
            loading = true;
            error = null;
            success = false;
            copied = false;
            startAuth();
        }
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

<ModalBase bind:open title={t("userMenu.authModal.title")}>
    <div class="auth-body">
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
                    {copied
                        ? "✓ " + t("userMenu.authModal.linkCopied")
                        : t("userMenu.authModal.copyLink")}
                </button>

                <div class="waiting-indicator">
                    <div class="pulse-dot"></div>
                    <p>{t("userMenu.authModal.waiting")}</p>
                </div>
            </div>
        {/if}
    </div>
</ModalBase>


