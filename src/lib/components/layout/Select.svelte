<script lang="ts">
	import { fly } from "svelte/transition";
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
	let dropdownEl = $state<HTMLDivElement>();
	let dropdownStyles = $state("");

	function portal(el: HTMLElement) {
		document.body.appendChild(el);
		return {
			destroy() {
				el.remove();
			},
		};
	}

	function updateDropdownPosition() {
		const rect = triggerEl!.getBoundingClientRect();
		dropdownStyles = `top:${rect.bottom + 8}px;left:${rect.left}px;width:${rect.width}px`;
	}

	function toggle() {
		if (disabled) return;
		isOpen = !isOpen;
		if (isOpen) {
			updateDropdownPosition();
		} else {
			dropdownStyles = "";
		}
	}

	function selectOption(option: Option) {
		value = option.value;
		isOpen = false;
		dropdownStyles = "";
		onchange?.(value);
	}

	function handleClickOutside(event: MouseEvent) {
		if (container && !container.contains(event.target as Node)) {
			if (!dropdownEl || !dropdownEl.contains(event.target as Node)) {
				isOpen = false;
				dropdownStyles = "";
			}
		}
	}

	$effect(() => {
		if (isOpen) {
			const onScroll = (e: Event) => {
				if (dropdownEl && dropdownEl.contains(e.target as Node)) return;
				isOpen = false;
				dropdownStyles = "";
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
			use:portal
			bind:this={dropdownEl}
			class="select-dropdown"
			style={dropdownStyles}
			transition:fly={{ y: 8, duration: 200 }}
			role="listbox"
		>
			{#each options as option (option.value)}
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
