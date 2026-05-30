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

<style>
    .auth-container {
        display: flex;
        flex-direction: column;
        align-items: center;
        text-align: center;
        padding: 1rem 0.5rem;
        width: 100%;
        color: var(--text-primary, #fff);
        position: relative;
    }

    .ms-logo-wrapper {
        margin-bottom: 2rem;
        padding: 1rem;
        background: rgba(255, 255, 255, 0.03);
        border-radius: var(--border-radius-sm);
        border: 1px solid rgba(255, 255, 255, 0.05);
        box-shadow: 0 8px 32px rgba(0, 0, 0, 0.2);
        animation: slideDown 0.5s cubic-bezier(0.16, 1, 0.3, 1);
    }

    .ms-logo {
        width: 48px;
        height: 48px;
        filter: drop-shadow(0 4px 12px rgba(0, 0, 0, 0.3));
    }

    .state-container {
        display: flex;
        flex-direction: column;
        align-items: center;
        width: 100%;
        animation: fadeIn 0.4s ease;
    }

    .state-title {
        font-size: 1.4rem;
        font-weight: 700;
        margin: 0 0 0.5rem 0;
        background: linear-gradient(135deg, #fff, #aaa);
        -webkit-background-clip: text;
        -webkit-text-fill-color: transparent;
    }

    .state-subtitle {
        font-size: 0.95rem;
        color: var(--text-secondary, #a0a0a0);
        margin: 0;
        max-width: 80%;
        line-height: 1.5;
    }

    .minimal-spinner {
        width: 32px;
        height: 32px;
        border: 2px solid rgba(255, 255, 255, 0.1);
        border-top-color: var(--text-primary, #fff);
        border-radius: 50%;
        animation: spin 0.8s linear infinite;
        margin-bottom: 1.5rem;
    }

    .icon-wrapper {
        width: 64px;
        height: 64px;
        border-radius: 50%;
        display: flex;
        align-items: center;
        justify-content: center;
        margin-bottom: 1.5rem;
        position: relative;
    }

    .icon-wrapper::after {
        content: "";
        position: absolute;
        inset: -4px;
        border-radius: 50%;
        opacity: 0.3;
        z-index: -1;
    }

    .icon-wrapper svg {
        width: 32px;
        height: 32px;
    }

    .icon-wrapper.success {
        background: rgba(16, 185, 129, 0.1);
        color: #10b981;
    }

    .icon-wrapper.success::after {
        background: radial-gradient(circle, rgba(16, 185, 129, 0.5) 0%, transparent 70%);
        animation: pop 0.5s cubic-bezier(0.175, 0.885, 0.32, 1.275);
    }

    .icon-wrapper.error {
        background: rgba(239, 68, 68, 0.1);
        color: #ef4444;
    }

    .icon-wrapper.error::after {
        background: radial-gradient(circle, rgba(239, 68, 68, 0.5) 0%, transparent 70%);
    }

    .instruction-text {
        font-size: 1rem;
        color: var(--text-secondary, #e0e0e0);
        margin-bottom: 2rem;
        line-height: 1.6;
        padding: 0 1rem;
    }

    .code-card {
        background: rgba(0, 0, 0, 0.2);
        border: 1px solid rgba(255, 255, 255, 0.08);
        border-radius: var(--border-radius-sm);
        padding: 1.5rem;
        width: 100%;
        max-width: 340px;
        display: flex;
        flex-direction: column;
        gap: 1.5rem;
        box-shadow: inset 0 2px 10px rgba(255, 255, 255, 0.02), 0 15px 35px rgba(0, 0, 0, 0.3);
        position: relative;
        overflow: hidden;
    }

    .code-card::before {
        content: "";
        position: absolute;
        top: 0;
        left: -100%;
        width: 50%;
        height: 100%;
        background: linear-gradient(90deg, transparent, rgba(255, 255, 255, 0.03), transparent);
        transform: skewX(-20deg);
        animation: shine 6s infinite;
    }

    .field-group {
        display: flex;
        flex-direction: column;
        align-items: flex-start;
        width: 100%;
        gap: 0.5rem;
    }

    .field-label {
        font-size: 0.8rem;
        font-weight: 600;
        color: var(--text-secondary, #a0a0a0);
        text-transform: uppercase;
        letter-spacing: 0.5px;
    }

    .copy-box {
        display: flex;
        align-items: center;
        justify-content: space-between;
        width: 100%;
        background: rgba(255, 255, 255, 0.05);
        border: 1px solid rgba(255, 255, 255, 0.1);
        border-radius: var(--border-radius-sm);
        padding: 0.5rem;
        gap: 0.5rem;
    }

    .code-box {
        padding: 0.5rem 0.5rem 0.5rem 1rem;
    }

    .url-display {
        font-size: 0.85rem;
        color: var(--text-primary, #fff);
        white-space: nowrap;
        overflow: hidden;
        text-overflow: ellipsis;
        padding-left: 0.5rem;
        text-align: left;
        width: 100%;
    }

    .code-display {
        display: flex;
        align-items: center;
        gap: 0.2rem;
        font-family: "JetBrains Mono", "Courier New", monospace;
        font-size: 1.2rem;
        font-weight: 800;
        letter-spacing: 1px;
    }

    .code-char {
        background: rgba(255, 255, 255, 0.05);
        padding: 0.2rem 0.3rem;
        border-radius: var(--border-radius-sm);
        color: #fff;
        border: 1px solid rgba(255, 255, 255, 0.05);
    }

    .code-char.dash {
        background: transparent;
        border: none;
        color: var(--text-secondary, #666);
    }

    .icon-btn {
        display: flex;
        align-items: center;
        justify-content: center;
        background: rgba(255, 255, 255, 0.1);
        color: #fff;
        border: none;
        padding: 0.5rem;
        border-radius: var(--border-radius-sm);
        cursor: pointer;
        transition: all 0.2s;
        flex-shrink: 0;
    }

    .icon-btn:hover {
        background: rgba(255, 255, 255, 0.2);
    }

    .icon-btn.copied {
        background: #10b981;
        color: #fff;
    }

    .action-btn.retry {
        margin-top: 1.5rem;
        background: rgba(255, 255, 255, 0.05);
        color: #fff;
        border: 1px solid rgba(255, 255, 255, 0.1);
        padding: 0.75rem 2rem;
        border-radius: var(--border-radius-sm);
        font-weight: 600;
        cursor: pointer;
        transition: all 0.2s;
    }

    .action-btn.retry:hover {
        background: rgba(255, 255, 255, 0.1);
        border-color: rgba(255, 255, 255, 0.2);
    }

    .waiting-box {
        margin-top: 2rem;
        display: flex;
        align-items: center;
        gap: 1rem;
        padding: 1rem 1.5rem;
        background: rgba(255, 255, 255, 0.02);
        border-radius: var(--border-radius-sm);
        border: 1px solid rgba(255, 255, 255, 0.05);
        width: 100%;
        max-width: 340px;
    }

    .minimal-dot {
        width: 8px;
        height: 8px;
        background: var(--text-primary, #fff);
        border-radius: 50%;
        flex-shrink: 0;
        animation: simplePulse 1.5s ease-in-out infinite;
    }

    .waiting-text {
        display: flex;
        flex-direction: column;
        align-items: flex-start;
        text-align: left;
    }

    .status-title {
        font-size: 0.9rem;
        font-weight: 700;
        color: #fff;
    }

    .status-subtitle {
        font-size: 0.75rem;
        color: var(--text-secondary, #888);
        margin-top: 0.1rem;
    }

    @keyframes fadeIn {
        from { opacity: 0; transform: translateY(8px); }
        to { opacity: 1; transform: translateY(0); }
    }

    @keyframes simplePulse {
        0%, 100% { opacity: 1; }
        50% { opacity: 0.3; }
    }

    @keyframes slideDown {
        from { opacity: 0; transform: translateY(-20px); }
        to { opacity: 1; transform: translateY(0); }
    }

    @keyframes pop {
        0% { transform: scale(0.8); opacity: 0; }
        100% { transform: scale(1); opacity: 1; }
    }

    @keyframes shine {
        0% { left: -100%; }
        20% { left: 200%; }
        100% { left: 200%; }
    }

    @media (max-height: 700px) {
        .auth-container { padding: 0; }
        .ms-logo-wrapper { margin-bottom: 0.5rem; padding: 0.5rem; }
        .ms-logo { width: 32px; height: 32px; }
        .code-card { padding: 1rem; gap: 1rem; }
        .waiting-box { margin-top: 1rem; padding: 0.75rem 1rem; }
        .field-group { gap: 0.2rem; }
        .state-title { font-size: 1.2rem; margin-bottom: 0.25rem; }
        .instruction-text { margin-bottom: 1rem; font-size: 0.9rem; }
    }
</style>

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
