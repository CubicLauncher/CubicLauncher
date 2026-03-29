<script lang="ts">
    import { createInstance } from "$lib/api/cubicApi";

    let { open = $bindable(), oncreated } = $props<{
        open: boolean;
        oncreated?: () => void;
    }>();

    let name = $state("");
    let loading = $state(false);
    let error = $state<string | null>(null);

    async function handleCreate() {
        if (!name.trim()) {
            error = "El nombre no puede estar vacío";
            return;
        }

        loading = true;
        error = null;

        try {
            await createInstance(name, "1.8", () => {
                open = false;
                name = "";
                oncreated?.();
            }, (err) => {
                error = "Error al crear la instancia";
                console.error(err);
            });
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
                <span class="modal-title">Crear Nueva Instancia</span>
                <button class="qm-close-btn" onclick={close}>&times;</button>
            </div>

            <div class="modal-body">
                <div class="input-group">
                    <span class="input-label">Nombre de la Instancia</span>
                    <input
                        type="text"
                        class="text-input"
                        bind:value={name}
                        placeholder="Ej. Mi mundo 1.8"
                        disabled={loading}
                        onkeydown={(e) => e.key === "Enter" && handleCreate()}
                    />
                </div>
                
                <div class="input-group">
                    <span class="input-label">Versión</span>
                    <input
                        type="text"
                        class="text-input"
                        value="1.8"
                        disabled
                    />
                    <small style="font-size: 0.6rem; color: var(--text-secondary); margin-top: 4px;">
                        Por ahora solo está disponible la versión 1.8
                    </small>
                </div>

                {#if error}
                    <div style="color: #e57373; font-size: 0.75rem; margin-top: 4px;">
                        {error}
                    </div>
                {/if}
            </div>

            <div class="modal-footer">
                <button class="btn-secondary" onclick={close} disabled={loading}>
                    Cancelar
                </button>
                <button class="btn-primary" onclick={handleCreate} disabled={loading}>
                    {loading ? "Creando..." : "Crear"}
                </button>
            </div>
        </div>
    </div>
{/if}
