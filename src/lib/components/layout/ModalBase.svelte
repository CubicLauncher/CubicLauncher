<script lang="ts">
    import { fade, fly } from "svelte/transition";
    import type { Snippet } from "svelte";

    let {
        open = $bindable(),
        title,
        onclose,
        children,
        footer,
    } = $props<{
        open: boolean;
        title?: string;
        onclose?: () => void;
        children?: Snippet;
        footer?: Snippet;
    }>();

    function close() {
        open = false;
        onclose?.();
    }
</script>

{#if open}
    <div
        class="modal-overlay"
        onclick={close}
        onkeydown={(e) => e.key === "Escape" && close()}
        role="presentation"
        transition:fade={{ duration: 150 }}
    >
        <div
            class="modal"
            onclick={(e) => e.stopPropagation()}
            onkeydown={(e) => e.stopPropagation()}
            role="dialog"
            aria-modal="true"
            tabindex="-1"
            transition:fly={{ y: 20, duration: 250 }}
        >
            <div class="modal-header">
                {#if title}
                    <span class="modal-title">{title}</span>
                {/if}
                <button class="action-btn" onclick={close} title="Cerrar">
                    <svg
                        width="14"
                        height="14"
                        viewBox="0 0 24 24"
                        fill="none"
                        stroke="currentColor"
                        stroke-width="2.5"
                        stroke-linecap="round"
                        stroke-linejoin="round"
                        ><line x1="18" y1="6" x2="6" y2="18"></line><line
                            x1="6"
                            y1="6"
                            x2="18"
                            y2="18"
                        ></line></svg
                    >
                </button>
            </div>

            <div class="modal-body">
                {@render children?.()}
            </div>

            {#if footer}
                <div class="modal-footer">
                    {@render footer()}
                </div>
            {/if}
        </div>
    </div>
{/if}
