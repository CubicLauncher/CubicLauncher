<script lang="ts">
    import { onMount } from "svelte";

    interface Props {
        items: any[];
        itemHeight: number;
        children: import("svelte").Snippet<[any, number]>;
        class?: string;
        padding?: number;
        onNearEnd?: () => void;
    }

    let { items, itemHeight, children, class: className = "", padding = 20, onNearEnd }: Props = $props();

    let container: HTMLDivElement = $state() as HTMLDivElement;
    let scrollTop = $state(0);
    let containerHeight = $state(0);

    const viewportHeight = $derived(containerHeight);
    const totalHeight = $derived(items.length * itemHeight + padding);
    
    // Calculate range of visible items
    const startIndex = $derived(Math.floor(scrollTop / itemHeight));
    const endIndex = $derived(Math.min(items.length - 1, Math.floor((scrollTop + viewportHeight) / itemHeight)));

    // Buffer for smoother scrolling
    const buffer = 5;
    const start = $derived(Math.max(0, startIndex - buffer));
    const end = $derived(Math.min(items.length - 1, endIndex + buffer));

    const visibleItems = $derived(
        items.slice(start, end + 1).map((item, i) => ({
            item,
            index: start + i,
            top: (start + i) * itemHeight
        }))
    );

    function handleScroll(e: Event) {
        const target = e.target as HTMLDivElement;
        scrollTop = target.scrollTop;
        if (target.scrollHeight - scrollTop - containerHeight < 500) {
            onNearEnd?.();
        }
    }

    onMount(() => {
        const resizeObserver = new ResizeObserver((entries) => {
            for (let entry of entries) {
                containerHeight = entry.contentRect.height;
            }
        });
        resizeObserver.observe(container);
        return () => resizeObserver.disconnect();
    });
</script>

<div
    bind:this={container}
    class="virtual-list-container {className}"
    onscroll={handleScroll}
    style="position: relative; overflow-y: auto; height: 100%;"
>
    <div class="virtual-list-phantom" style="height: {totalHeight}px; width: 100%; pointer-events: none;"></div>
    <div class="virtual-list-content" style="position: absolute; top: 0; left: 0; width: 100%;">
        {#each visibleItems as { item, index, top } (index)}
            <div
                class="virtual-list-item-wrapper"
                style="position: absolute; top: {top}px; left: 0; width: 100%; height: {itemHeight}px;"
            >
                {@render children(item, index)}
            </div>
        {/each}
    </div>
</div>
