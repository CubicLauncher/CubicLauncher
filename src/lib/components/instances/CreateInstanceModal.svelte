<script lang="ts">
    import { createInstance } from "$lib/api/cubicApi";
    import { t } from "$lib/i18n";

    let { open = $bindable(), oncreated } = $props<{
        open: boolean;
        oncreated?: () => void;
    }>();

    let name = $state("");
    let loading = $state(false);
    let error = $state<string | null>(null);

    async function handleCreate() {
        if (!name.trim()) {
            error = t('createInstance.emptyNameErr');
            return;
        }

        loading = true;
        error = null;

        try {
            await createInstance(
                name,
                "1.14",
                () => {
                    open = false;
                    name = "";
                    oncreated?.();
                },
                (err) => {
                    error = t('createInstance.createErr');
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
                <span class="modal-title">{t('createInstance.title')}</span>
                <button class="qm-close-btn" onclick={close}>&times;</button>
            </div>

            <div class="modal-body">
                <div class="input-group">
                    <span class="input-label">{t('createInstance.nameLabel')}</span>
                    <input
                        type="text"
                        class="text-input"
                        bind:value={name}
                        placeholder={t('createInstance.namePlaceholder')}
                        disabled={loading}
                        onkeydown={(e) => e.key === "Enter" && handleCreate()}
                    />
                </div>

                <div class="input-group">
                    <span class="input-label">{t('createInstance.versionLabel')}</span>
                    <input
                        type="text"
                        class="text-input"
                        value="1.14"
                        disabled
                    />
                    <small
                        style="font-size: 0.6rem; color: var(--text-secondary); margin-top: 4px;"
                    >
                        {t('createInstance.versionNotice')}
                    </small>
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
                    {t('createInstance.cancel')}
                </button>
                <button
                    class="btn-primary"
                    onclick={handleCreate}
                    disabled={loading}
                >
                    {loading ? t('createInstance.creatingBtn') : t('createInstance.createBtn')}
                </button>
            </div>
        </div>
    </div>
{/if}
