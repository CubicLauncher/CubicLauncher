<script lang="ts">
	import { fly, fade } from "svelte/transition";
	import { onMount } from "svelte";

	interface Option {
		value: string;
		label: string;
	}

	let {
		value = $bindable(),
		options = [],
		placeholder = "Seleccionar...",
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
	let triggerEl: HTMLButtonElement;
	let dropdownStyles = $state<Record<string, string>>({});

	function updateDropdownPosition() {
		if (!triggerEl) return;
		const rect = triggerEl.getBoundingClientRect();
		dropdownStyles = {
			top: `${rect.bottom + 8}px`,
			left: `${rect.left}px`,
			width: `${rect.width}px`,
		};
	}

	function toggle() {
		if (disabled) return;
		isOpen = !isOpen;
		if (isOpen) {
			updateDropdownPosition();
		} else {
			dropdownStyles = {};
		}
	}

	function selectOption(option: Option) {
		value = option.value;
		isOpen = false;
		dropdownStyles = {};
		onchange?.(value);
	}

	function handleClickOutside(event: MouseEvent) {
		if (container && !container.contains(event.target as Node)) {
			isOpen = false;
			dropdownStyles = {};
		}
	}

	$effect(() => {
		if (isOpen) {
			const onScroll = () => {
				isOpen = false;
				dropdownStyles = {};
			};
			window.addEventListener("scroll", onScroll, true);
			return () => window.removeEventListener("scroll", onScroll, true);
		}
	});

	onMount(() => {
		window.addEventListener("click", handleClickOutside);
		return () => window.removeEventListener("click", handleClickOutside);
	});

	const selectedLabel = $derived(
		options.find((o: Option) => o.value === value)?.label || placeholder,
	);
</script>

<div class="custom-select-container" bind:this={container} {id}>
	{#if label}
		<span class="input-label">{label}</span>
	{/if}

	<button
		type="button"
		class="select-trigger"
		class:disabled
		class:open={isOpen}
		onclick={toggle}
		aria-expanded={isOpen}
		aria-haspopup="listbox"
		bind:this={triggerEl}
	>
		<span class="selected-value">{selectedLabel}</span>
		<svg
			class="chevron-icon"
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
			class="select-dropdown"
			style={dropdownStyles}
			transition:fly={{ y: 8, duration: 200 }}
			role="listbox"
		>
			{#each options as option}
				<div
					class="select-option"
					class:selected={option.value === value}
					onclick={() => selectOption(option)}
					onkeydown={(e) => e.key === "Enter" && selectOption(option)}
					role="option"
					aria-selected={option.value === value}
					tabindex="0"
				>
					{option.label}
					{#if option.value === value}
						<svg
							class="check-icon"
							width="12"
							height="12"
							viewBox="0 0 24 24"
							fill="none"
							stroke="currentColor"
							stroke-width="3"
							stroke-linecap="round"
							stroke-linejoin="round"
							><polyline points="20 6 9 17 4 12"></polyline></svg
						>
					{/if}
				</div>
			{/each}
		</div>
	{/if}
</div>

<style>
	.custom-select-container {
		position: relative;
		display: flex;
		flex-direction: column;
		gap: 6px;
		width: 100%;
	}

	.select-trigger {
		display: flex;
		align-items: center;
		justify-content: space-between;
		background: rgba(255, 255, 255, 0.03);
		border: 1px solid var(--border);
		border-radius: var(--border-radius-sm);
		padding: 10px 14px;
		color: var(--text-primary);
		font-family: inherit;
		font-size: 0.85rem;
		cursor: pointer;
		transition: all 0.2s ease;
		text-align: left;
		width: 100%;
		outline: none;
	}

	.select-trigger:hover:not(.disabled) {
		background: rgba(255, 255, 255, 0.06);
		border-color: rgba(255, 255, 255, 0.2);
	}

	.select-trigger.open {
		border-color: rgba(255, 255, 255, 0.3);
		background: rgba(255, 255, 255, 0.06);
		box-shadow: 0 0 0 2px rgba(255, 255, 255, 0.02);
	}

	.select-trigger.disabled {
		opacity: 0.5;
		cursor: not-allowed;
	}

	.selected-value {
		white-space: nowrap;
		overflow: hidden;
		text-overflow: ellipsis;
	}

	.chevron-icon {
		color: var(--text-secondary);
		transition: transform 0.2s ease;
		flex-shrink: 0;
		margin-left: 8px;
	}

	.select-trigger.open .chevron-icon {
		transform: rotate(180deg);
	}

	.select-dropdown {
		position: fixed;
		background: #121212;
		border: 1px solid var(--border);
		border-radius: var(--border-radius-sm);
		box-shadow: 0 10px 30px rgba(0, 0, 0, 0.5);
		z-index: 9999;
		max-height: 240px;
		overflow-y: auto;
		padding: 6px;
		backdrop-filter: blur(10px);
	}

	.select-option {
		display: flex;
		align-items: center;
		justify-content: space-between;
		padding: 10px 12px;
		border-radius: var(--border-radius-sm);
		color: var(--text-secondary);
		font-size: 0.85rem;
		cursor: pointer;
		transition: all 0.15s ease;
		margin-bottom: 2px;
	}

	.select-option:last-child {
		margin-bottom: 0;
	}

	.select-option:hover {
		background: rgba(255, 255, 255, 0.05);
		color: var(--text-primary);
	}

	.select-option.selected {
		background: rgba(255, 255, 255, 0.03);
		color: var(--text-primary);
		font-weight: 600;
		border: 1px solid rgba(255, 255, 255, 0.05);
	}

	.check-icon {
		color: var(--accent);
	}

	.select-dropdown:global(::-webkit-scrollbar) {
		width: 4px;
	}

	.select-dropdown:global(::-webkit-scrollbar-track) {
		background: transparent;
	}

	.select-dropdown:global(::-webkit-scrollbar-thumb) {
		background: var(--border);
		border-radius: 10px;
	}
</style>
