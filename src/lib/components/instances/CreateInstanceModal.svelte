<script lang="ts">
    import { createInstance, getInstalledVersions } from "$lib/api/cubicApi";
    import { INSTANCE_LOGOS } from "$lib/icons/logos";
    import Select from "$lib/components/layout/Select.svelte";
    import { t } from "$lib/i18n";

    let { open = $bindable(), oncreated } = $props<{
        open: boolean;
        oncreated?: () => void;
    }>();

    let name = $state("");
    let selectedVersion = $state("");
    let selectedIcon = $state<string | null>(null);
    let versions = $state<string[]>([]);
    let availableIcons = $state<string[]>(INSTANCE_LOGOS);
    let versionOptions = $derived(
        versions.map((v) => ({ value: v, label: v })),
    );
    let loading = $state(false);
    let error = $state<string | null>(null);

    $effect(() => {
        if (open) {
            fetchVersions();
        }
    });

    async function fetchVersions() {
        const rawVersions = await getInstalledVersions();
        // Sort versions descending (newest first)
        versions = rawVersions.sort((a, b) =>
            b.localeCompare(a, undefined, {
                numeric: true,
                sensitivity: "base",
            }),
        );

        if (versions.length > 0 && !selectedVersion) {
            selectedVersion = versions[0];
        }
    }

    async function handleCreate() {
        if (!name.trim()) {
            error = t("createInstance.emptyNameErr");
            return;
        }

        if (!selectedVersion) {
            error = t("createInstance.noVersionsErr");
            return;
        }

        loading = true;
        error = null;

        try {
            await createInstance(
                name,
                selectedVersion,
                selectedIcon,
                () => {
                    open = false;
                    name = "";
                    selectedIcon = null;
                    oncreated?.();
                },
                (err: unknown) => {
                    error = t("createInstance.createErr");
                    console.error(err);
                },
            );
        } finally {
            loading = false;
        }
    }

    function close() {
        if (loading) return;
        open = false;
        name = "";
        error = null;
    }
</script>

<style>
    .modal-overlay {
        position: fixed;
        inset: 0;
        background: rgba(0, 0, 0, 0.75);
        z-index: 1000;
        display: flex;
        align-items: center;
        justify-content: center;
        backdrop-filter: blur(4px);
    }

    .modal {
        background: var(--bg-sidebar);
        border: 1px solid var(--border);
        border-radius: var(--border-radius-sm);
        width: min(500px, 90vw);
        max-height: 90vh;
        overflow-y: auto;
        padding: 24px;
        display: flex;
        flex-direction: column;
        gap: 20px;
        box-shadow: 0 20px 40px rgba(0, 0, 0, 0.4);
    }

    .modal-header {
        display: flex;
        justify-content: space-between;
        align-items: center;
    }

    .modal-title {
        font-size: 1rem;
        font-weight: 700;
        letter-spacing: 0.5px;
        color: var(--text-primary);
    }

    .modal-body {
        display: flex;
        flex-direction: column;
        gap: 12px;
    }

    .modal-footer {
        display: flex;
        justify-content: flex-end;
        gap: 10px;
    }

    .divider {
        height: 1px;
        background: var(--border);
        margin: 16px 0 12px;
    }
</style>

{#if open}
    <div
        class="modal-overlay"
        onclick={close}
        onkeydown={(e) => e.key === "Escape" && close()}
        role="presentation"
    >
        <div
            class="modal"
            onclick={(e) => e.stopPropagation()}
            onkeydown={(e) => e.stopPropagation()}
            role="dialog"
            aria-modal="true"
            tabindex="-1"
        >
            <div class="modal-header">
                <span class="modal-title">{t("createInstance.title")}</span>
                <button
                    class="action-btn"
                    onclick={close}
                    title="Cerrar"
                    style="background: transparent; border: none; font-size: 1.2rem; cursor: pointer; color: var(--text-secondary);"
                    >&times;</button
                >
            </div>

            <div class="modal-body" style="padding: 4px 0;">
                <div style="display: flex; gap: 20px;">
                    <!-- Left: Logo Selection -->
                    <div
                        style="display: flex; flex-direction: column; gap: 12px; width: 100px; align-items: center; flex-shrink: 0;"
                    >
                        <span
                            class="input-label"
                            style="margin: 0; text-align: center;">Logo</span
                        >
                        <!-- Large Logo Preview -->
                        <div
                            style="width: 80px; height: 80px; border-radius: 12px; background: rgba(255, 255, 255, 0.03); border: 2px dashed var(--border); display: flex; align-items: center; justify-content: center; padding: 12px; transition: all 0.2s; position: relative; overflow: hidden;"
                        >
                            {#if selectedIcon}
                                <img
                                    src={selectedIcon}
                                    alt="Logo"
                                    style="width: 100%; height: 100%; object-fit: contain; filter: drop-shadow(0 4px 8px rgba(0,0,0,0.3));"
                                />
                                <button
                                    type="button"
                                    style="position: absolute; inset: 0; background: rgba(0,0,0,0.6); border: none; color: white; opacity: 0; cursor: pointer; transition: opacity 0.2s; font-size: 0.7rem; font-weight: bold; display: flex; align-items: center; justify-content: center;"
                                    onmouseenter={(e) =>
                                        (e.currentTarget.style.opacity = "1")}
                                    onmouseleave={(e) =>
                                        (e.currentTarget.style.opacity = "0")}
                                    onclick={() => (selectedIcon = null)}
                                    >Limpiar</button
                                >
                            {:else}
                                <svg
                                    width="24"
                                    height="24"
                                    viewBox="0 0 24 24"
                                    fill="none"
                                    stroke="currentColor"
                                    stroke-width="2"
                                    stroke-linecap="round"
                                    stroke-linejoin="round"
                                    style="color: var(--text-secondary); opacity: 0.5;"
                                    ><rect
                                        x="3"
                                        y="3"
                                        width="18"
                                        height="18"
                                        rx="2"
                                        ry="2"
                                    ></rect><circle cx="8.5" cy="8.5" r="1.5"
                                    ></circle><polyline
                                        points="21 15 16 10 5 21"
                                    ></polyline></svg
                                >
                            {/if}
                        </div>
                    </div>

                    <!-- Right: Details -->
                    <div
                        style="display: flex; flex-direction: column; gap: 16px; flex: 1;"
                    >
                        <div class="input-group">
                            <span class="input-label"
                                >{t("createInstance.nameLabel")}</span
                            >
                            <input
                                type="text"
                                class="text-input"
                                bind:value={name}
                                placeholder={t(
                                    "createInstance.namePlaceholder",
                                )}
                                disabled={loading}
                                onkeydown={(e) =>
                                    e.key === "Enter" && handleCreate()}
                                style="font-size: 1rem; padding: 12px;"
                            />
                        </div>

                        <div class="input-group">
                            <Select
                                label={t("createInstance.versionLabel")}
                                bind:value={selectedVersion}
                                options={versionOptions}
                                disabled={loading || versions.length === 0}
                                placeholder={t("createInstance.noVersionsErr")}
                            />
                        </div>
                    </div>
                </div>

                <div
                    class="divider"
                    style="height: 1px; background: var(--border); margin: 16px 0 12px;"
                ></div>

                <!-- Logo Picker Grid -->
                <div class="input-group">
                    <span class="input-label">Seleccionar Icono</span>
                    <div
                        class="icon-selector"
                        style="display: grid; grid-template-columns: repeat(auto-fill, minmax(44px, 1fr)); gap: 8px; margin-top: 4px; max-height: 110px; overflow-y: auto; padding-right: 4px; padding-bottom: 4px;"
                    >
                        {#each availableIcons as iconName (iconName)}
                            {@const iconPath = `/images/instances/${iconName}`}
                            <button
                                type="button"
                                class="icon-option"
                                class:selected={selectedIcon === iconPath}
                                onclick={() =>
                                    (selectedIcon =
                                        selectedIcon === iconPath
                                            ? null
                                            : iconPath)}
                                title={iconName}
                                style="width: 100%; height: auto; aspect-ratio: 1/1; padding: 6px;"
                            >
                                <img src={iconPath} alt={iconName} />
                            </button>
                        {/each}
                    </div>
                </div>

                {#if error}
                    <div
                        style="color: var(--color-error); font-size: 0.8rem; background: rgba(var(--color-error-rgb), 0.1); border: 1px solid rgba(var(--color-error-rgb), 0.2); border-radius: 6px; padding: 10px; text-align: center; font-weight: 500; margin-top: 8px;"
                    >
                        {error}
                    </div>
                {/if}
            </div>

            <div
                class="modal-footer"
                style="margin-top: 8px; border-top: 1px solid var(--border); padding-top: 16px; display: flex; justify-content: flex-end; gap: 12px;"
            >
                <button
                    class="btn-secondary"
                    onclick={close}
                    disabled={loading}
                >
                    {t("createInstance.cancel")}
                </button>
                <button
                    class="btn-primary"
                    onclick={handleCreate}
                    disabled={loading}
                >
                    {loading
                        ? t("createInstance.creatingBtn")
                        : t("createInstance.createBtn")}
                </button>
            </div>
        </div>
    </div>
{/if}
