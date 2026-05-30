<script lang="ts">
	import { slide } from "svelte/transition";
	import type { Snippet } from "svelte";

	let {
		title,
		iconSrc,
		storageKey,
		defaultOpen = true,
		children,
	}: {
		title: string;
		iconSrc?: string;
		storageKey?: string;
		defaultOpen?: boolean;
		children: Snippet;
	} = $props();

	function loadSaved(key: string | undefined, fallback: boolean): boolean {
		if (!key) return fallback;
		try {
			const saved = localStorage.getItem(key);
			if (saved !== null) return saved === "true";
		} catch {
			// localStorage not available
		}
		return fallback;
	}

	// svelte-ignore state_referenced_locally
	let open = $state(loadSaved(storageKey, defaultOpen));

	$effect(() => {
		if (storageKey) {
			try {
				localStorage.setItem(storageKey, String(open));
			} catch {
				// localStorage not available
			}
		}
	});
</script>

<div class="cs-root">
	<button
		type="button"
		class="cs-header"
		class:expanded={open}
		onclick={() => (open = !open)}
		aria-expanded={open}
	>
		<span class="cs-header-left">
			{#if iconSrc}
				<span class="cs-icon">
					<img
						src={iconSrc}
						width="18"
						height="18"
						alt=""
						style="filter: var(--icon-filter); display: block;"
					/>
				</span>
			{/if}
			<span class="cs-title">{title}</span>
		</span>
		<svg
			class="cs-chevron"
			class:open
			width="16"
			height="16"
			viewBox="0 0 24 24"
			fill="none"
			stroke="currentColor"
			stroke-width="2"
			stroke-linecap="round"
			stroke-linejoin="round"
		>
			<path d="M6 9l6 6 6-6" />
		</svg>
	</button>
	{#if open}
		<div class="cs-content" transition:slide={{ duration: 150 }}>
			{@render children()}
		</div>
	{/if}
</div>

<style>
	.cs-root {
		background: var(--bg-card);
	}

	.cs-header {
		display: flex;
		align-items: center;
		justify-content: space-between;
		width: 100%;
		background: none;
		border: none;
		border-bottom: 1px solid transparent;
		color: inherit;
		padding: 10px 14px;
		cursor: pointer;
		user-select: none;
		transition: border-color 0.2s;
	}

	.cs-header.expanded {
		border-bottom-color: var(--border-color);
	}

	.cs-header-left {
		display: flex;
		align-items: center;
		gap: 8px;
		min-width: 0;
	}

	.cs-icon {
		display: flex;
		align-items: center;
		color: var(--text-muted);
		flex-shrink: 0;
		line-height: 0;
	}

	.cs-title {
		font-size: 0.75rem;
		font-weight: 700;
		text-transform: uppercase;
		color: var(--text-primary);
		letter-spacing: 0.05em;
		text-align: left;
		white-space: nowrap;
		overflow: hidden;
		text-overflow: ellipsis;
	}

	.cs-chevron {
		color: var(--accent);
		transition: transform 0.2s;
		flex-shrink: 0;
	}

	.cs-chevron.open {
		transform: rotate(180deg);
	}

	.cs-content {
		padding: 6px 14px 14px 14px;
		overflow: hidden;
	}
</style>
