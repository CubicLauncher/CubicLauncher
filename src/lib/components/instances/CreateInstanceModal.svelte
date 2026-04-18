<script lang="ts">
    import {
        createInstance,
        getInstalledVersions,
        getAvailableLogos,
    } from "$lib/api/cubicApi";
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
    let availableIcons = $state<string[]>([]);
    let loading = $state(false);
    let error = $state<string | null>(null);

    $effect(() => {
        if (open) {
            fetchVersions();
            fetchLogos();
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

    async function fetchLogos() {
        availableIcons = await getAvailableLogos();
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
                <button class="qm-close-btn" onclick={close}>&times;</button>
            </div>

            <div class="input-group">
                <span class="input-label">Logo de la Instancia</span>
                <div class="icon-selector">
                    {#each availableIcons as iconName}
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
                        >
                            <img src={iconPath} alt={iconName} />
                        </button>
                    {/each}
                </div>
            </div>

            <div class="modal-body">
                <div class="input-group">
                    <span class="input-label"
                        >{t("createInstance.nameLabel")}</span
                    >
                    <input
                        type="text"
                        class="text-input"
                        bind:value={name}
                        placeholder={t("createInstance.namePlaceholder")}
                        disabled={loading}
                        onkeydown={(e) => e.key === "Enter" && handleCreate()}
                    />
                </div>

                <div class="input-group">
                    <Select
                        label={t("createInstance.versionLabel")}
                        bind:value={selectedVersion}
                        options={versions.map((v) => ({ value: v, label: v }))}
                        disabled={loading || versions.length === 0}
                        placeholder={t("createInstance.noVersionsErr")}
                    />
                </div>

                {#if error}
                    <div
                        style="color: #e57373; font-size: 0.75rem; margin-top: 4px;"
                    >
                        {error}
                    </div>
                {/if}
            </div>

            <div class="modal-footer">
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
