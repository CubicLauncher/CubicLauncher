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
    let copiedCode = $state(false);
    let copiedLink = $state(false);

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
            copiedCode = false;
            copiedLink = false;
            startAuth();
        }
    });

    async function handleCopyCode() {
        if (deviceCode) {
            try {
                await navigator.clipboard.writeText(deviceCode.user_code);
                copiedCode = true;
                setTimeout(() => {
                    copiedCode = false;
                }, 2000);
            } catch (err) {
                console.error("Failed to copy code:", err);
            }
        }
    }

    async function handleCopyLink() {
        if (deviceCode) {
            try {
                await navigator.clipboard.writeText(
                    deviceCode.verification_uri,
                );
                copiedLink = true;
                setTimeout(() => {
                    copiedLink = false;
                }, 2000);
            } catch (err) {
                console.error("Failed to copy link:", err);
            }
        }
    }
</script>

<ModalBase bind:open title={t("userMenu.authModal.title")}>
    <div class="auth-container">
        <div class="ms-logo-wrapper">
            <svg
                class="ms-logo"
                viewBox="0 0 21 21"
                xmlns="http://www.w3.org/2000/svg"
            >
                <rect x="1" y="1" width="9" height="9" fill="#f25022" />
                <rect x="11" y="1" width="9" height="9" fill="#7fba00" />
                <rect x="1" y="11" width="9" height="9" fill="#00a4ef" />
                <rect x="11" y="11" width="9" height="9" fill="#ffb900" />
            </svg>
        </div>

        {#if loading}
            <div class="state-container">
                <div class="minimal-spinner"></div>
                <h3 class="state-title">
                    {t("userMenu.authModal.loading") || "Cargando..."}
                </h3>
                <p class="state-subtitle">Conectando con Microsoft...</p>
            </div>
        {:else if error}
            <div class="state-container">
                <div class="icon-wrapper error">
                    <svg
                        viewBox="0 0 24 24"
                        fill="none"
                        stroke="currentColor"
                        stroke-width="2"
                        ><circle cx="12" cy="12" r="10"></circle><line
                            x1="15"
                            y1="9"
                            x2="9"
                            y2="15"
                        ></line><line x1="9" y1="9" x2="15" y2="15"></line></svg
                    >
                </div>
                <h3 class="state-title">Error de autenticación</h3>
                <p class="state-subtitle error-text">
                    {t("userMenu.authModal.error")?.replace("{error}", error) ||
                        error}
                </p>
                <button class="action-btn retry" onclick={startAuth}>
                    <span>Reintentar</span>
                </button>
            </div>
        {:else if success}
            <div class="state-container">
                <div class="icon-wrapper success">
                    <svg
                        viewBox="0 0 24 24"
                        fill="none"
                        stroke="currentColor"
                        stroke-width="2"
                        ><path d="M22 11.08V12a10 10 0 1 1-5.93-9.14"
                        ></path><polyline points="22 4 12 14.01 9 11.01"
                        ></polyline></svg
                    >
                </div>
                <h3 class="state-title">¡Conectado!</h3>
                <p class="state-subtitle">
                    {t("userMenu.authModal.success") ||
                        "Tu cuenta ha sido vinculada."}
                </p>
            </div>
        {:else if deviceCode}
            <div class="state-container device-auth">
                <p class="instruction-text">
                    {t("userMenu.authModal.instruction") ||
                        "Introduce el siguiente código en la página de Microsoft para vincular tu cuenta."}
                </p>

                <div class="code-card">
                    <div class="field-group">
                        <span class="field-label">Enlace de verificación</span>
                        <div class="copy-box">
                            <div
                                class="url-display"
                                title={deviceCode.verification_uri}
                            >
                                {deviceCode.verification_uri}
                            </div>
                            <button
                                class="icon-btn {copiedLink ? 'copied' : ''}"
                                onclick={handleCopyLink}
                                title={copiedLink
                                    ? "¡Copiado!"
                                    : "Copiar enlace"}
                            >
                                {#if copiedLink}
                                    <svg
                                        viewBox="0 0 24 24"
                                        fill="none"
                                        stroke="currentColor"
                                        stroke-width="2"
                                        width="16"
                                        height="16"
                                        ><polyline points="20 6 9 17 4 12"
                                        ></polyline></svg
                                    >
                                {:else}
                                    <svg
                                        viewBox="0 0 24 24"
                                        fill="none"
                                        stroke="currentColor"
                                        stroke-width="2"
                                        width="16"
                                        height="16"
                                        ><rect
                                            x="9"
                                            y="9"
                                            width="13"
                                            height="13"
                                            rx="2"
                                            ry="2"
                                        ></rect><path
                                            d="M5 15H4a2 2 0 0 1-2-2V4a2 2 0 0 1 2-2h9a2 2 0 0 1 2 2v1"
                                        ></path></svg
                                    >
                                {/if}
                            </button>
                        </div>
                    </div>

                    <div class="field-group">
                        <span class="field-label">Código</span>
                        <div class="copy-box code-box">
                            <div class="code-display">
                                {#each deviceCode.user_code.split("") as char}
                                    <span
                                        class="code-char {char === '-'
                                            ? 'dash'
                                            : ''}">{char}</span
                                    >
                                {/each}
                            </div>
                            <button
                                class="icon-btn {copiedCode ? 'copied' : ''}"
                                onclick={handleCopyCode}
                                title={copiedCode
                                    ? "¡Copiado!"
                                    : "Copiar código"}
                            >
                                {#if copiedCode}
                                    <svg
                                        viewBox="0 0 24 24"
                                        fill="none"
                                        stroke="currentColor"
                                        stroke-width="2"
                                        width="16"
                                        height="16"
                                        ><polyline points="20 6 9 17 4 12"
                                        ></polyline></svg
                                    >
                                {:else}
                                    <svg
                                        viewBox="0 0 24 24"
                                        fill="none"
                                        stroke="currentColor"
                                        stroke-width="2"
                                        width="16"
                                        height="16"
                                        ><rect
                                            x="9"
                                            y="9"
                                            width="13"
                                            height="13"
                                            rx="2"
                                            ry="2"
                                        ></rect><path
                                            d="M5 15H4a2 2 0 0 1-2-2V4a2 2 0 0 1 2-2h9a2 2 0 0 1 2 2v1"
                                        ></path></svg
                                    >
                                {/if}
                            </button>
                        </div>
                    </div>
                </div>

                <div class="waiting-box">
                    <div class="minimal-dot"></div>
                    <div class="waiting-text">
                        <span class="status-title">Esperando autorización</span>
                        <span class="status-subtitle"
                            >{t("userMenu.authModal.waiting") ||
                                "Completa los pasos en tu navegador"}</span
                        >
                    </div>
                </div>
            </div>
        {/if}
    </div>
</ModalBase>
