<script lang="ts">
    import { fly } from "svelte/transition";
    import { onMount } from "svelte";

    interface Option {
        value: string;
        label: string;
        subtitle?: string;
    }

    let {
        value = $bindable(),
        options = [] as Option[],
        placeholder = "Select...",
        disabled = false,
        label,
        id,
        onchange,
    } = $props<{
        value: string;
        options: Option[];
        placeholder?: string;
        disabled?: boolean;
        label?: string;
        id?: string;
        onchange?: (value: string) => void;
    }>();

    let isOpen = $state(false);
    let container: HTMLDivElement;

    function toggle() {
        if (disabled) return;
        isOpen = !isOpen;
    }

    function selectOption(option: Option) {
        value = option.value;
        isOpen = false;
        onchange?.(value);
    }

    function handleClickOutside(event: MouseEvent) {
        if (container && !container.contains(event.target as Node)) {
            isOpen = false;
        }
    }

    onMount(() => {
        window.addEventListener("click", handleClickOutside);
        return () => window.removeEventListener("click", handleClickOutside);
    });

    const selectedLabel = $derived(
        (options as Option[]).find((o) => o.value === value)?.label || placeholder,
    );
</script>

<div class="dd-container" bind:this={container} {id}>
    {#if label}
        <span class="dd-label">{label}</span>
    {/if}

    <button
        type="button"
        class="dd-trigger"
        class:dd-disabled={disabled}
        class:dd-open={isOpen}
        onclick={toggle}
        aria-expanded={isOpen}
        aria-haspopup="listbox"
    >
        <span class="dd-selected">{selectedLabel}</span>
        <svg
            class="dd-chevron"
            class:dd-open={isOpen}
            width="12"
            height="12"
            viewBox="0 0 16 16"
            fill="none"
            stroke="currentColor"
            stroke-width="2"
            stroke-linecap="round"
            stroke-linejoin="round"
        >
            <path d="M4 6l4 4 4-4" />
        </svg>
    </button>

    {#if isOpen}
        <div
            class="dd-dropdown"
            transition:fly={{ y: 8, duration: 200 }}
            role="listbox"
        >
            {#each options as option}
                <div
                    class="dd-option"
                    class:dd-selected={option.value === value}
                    onclick={() => selectOption(option)}
                    onkeydown={(e) => e.key === "Enter" && selectOption(option)}
                    role="option"
                    aria-selected={option.value === value}
                    tabindex="0"
                >
                    <div class="dd-option-content">
                        <span class="dd-option-label">{option.label}</span>
                        {#if option.subtitle}
                            <span class="dd-option-subtitle">{option.subtitle}</span>
                        {/if}
                    </div>
                    {#if option.value === value}
                        <svg
                            class="dd-check"
                            width="12"
                            height="12"
                            viewBox="0 0 24 24"
                            fill="none"
                            stroke="currentColor"
                            stroke-width="3"
                            stroke-linecap="round"
                            stroke-linejoin="round"
                        >
                            <polyline points="20 6 9 17 4 12" />
                        </svg>
                    {/if}
                </div>
            {/each}
        </div>
    {/if}
</div>
