<script lang="ts">
    import { onMount, onDestroy } from "svelte";

    type Direction = "bottom" | "top" | "left" | "right";

    interface Props {
        open?: boolean;
        onclose?: () => void;
        dismissible?: boolean;
        direction?: Direction;
        closeThreshold?: number;
        class?: string;
        style?: string;
        children?: import("svelte").Snippet;
    }

    let {
        open = $bindable(false),
        onclose,
        dismissible = true,
        direction = "bottom",
        closeThreshold = 0.25,
        class: className = "",
        style = "",
        children,
    }: Props = $props();

    const isVertical = $derived(direction === "bottom" || direction === "top");

    // --- Translate state (0 = open, ±100 = closed) ---
    let translatePct = $state(getClosedTranslate());
    let animFrameId: number;

    function getClosedTranslate(): number {
        return direction === "bottom" || direction === "right" ? 100 : -100;
    }

    // Lightweight spring animation without svelte/motion
    function animateTo(target: number, onDone?: () => void) {
        cancelAnimationFrame(animFrameId);
        const stiffness = 0.1;
        const damping = 0.7;
        let velocity = 0;

        function step() {
            const force = (target - translatePct) * stiffness;
            velocity = (velocity + force) * damping;
            translatePct += velocity;

            if (
                Math.abs(target - translatePct) < 0.1 &&
                Math.abs(velocity) < 0.1
            ) {
                translatePct = target;
                onDone?.();
                return;
            }
            animFrameId = requestAnimationFrame(step);
        }
        animFrameId = requestAnimationFrame(step);
    }

    // --- React to open prop ---
    $effect(() => {
        if (open) {
            animateTo(0);
        } else {
            animateTo(getClosedTranslate());
        }
    });

    // --- Derived styles ---
    const transformStyle = $derived(
        isVertical
            ? `translate3d(0, ${translatePct}%, 0)`
            : `translate3d(${translatePct}%, 0, 0)`,
    );

    const overlayOpacity = $derived(
        Math.max(0, 1 - Math.abs(translatePct) / 100),
    );

    const isVisible = $derived(
        open || Math.abs(translatePct) < Math.abs(getClosedTranslate()),
    );

    // --- Drag ---
    let isDragging = false;
    let dragStart = 0;
    let drawerEl: HTMLDivElement = $state() as HTMLDivElement;

    function onPointerDown(e: PointerEvent) {
        if (!dismissible) return;
        isDragging = true;
        dragStart = isVertical ? e.clientY : e.clientX;
        cancelAnimationFrame(animFrameId);
        (e.target as HTMLElement).setPointerCapture(e.pointerId);
    }

    function onPointerMove(e: PointerEvent) {
        if (!isDragging) return;
        const current = isVertical ? e.clientY : e.clientX;
        const delta = current - dragStart;
        const sign = direction === "bottom" || direction === "right" ? 1 : -1;
        const dragged = delta * sign;
        const size = isVertical
            ? drawerEl.getBoundingClientRect().height
            : drawerEl.getBoundingClientRect().width;

        translatePct =
            dragged < 0
                ? (dragged / size) * 15 // dampen over-drag
                : (dragged / size) * 100;
    }

    function onPointerUp(e: PointerEvent) {
        if (!isDragging) return;
        isDragging = false;

        const current = isVertical ? e.clientY : e.clientX;
        const delta = current - dragStart;
        const sign = direction === "bottom" || direction === "right" ? 1 : -1;
        const size = isVertical
            ? drawerEl.getBoundingClientRect().height
            : drawerEl.getBoundingClientRect().width;

        if ((delta * sign) / size > closeThreshold) {
            close();
        } else {
            animateTo(0);
        }
    }

    function close() {
        if (!dismissible) return;
        open = false;
        onclose?.();
    }

    // --- Keyboard ---
    function onKeydown(e: KeyboardEvent) {
        if (e.key === "Escape" && open && dismissible) close();
    }

    onMount(() => window.addEventListener("keydown", onKeydown));
    onDestroy(() => {
        window.removeEventListener("keydown", onKeydown);
        cancelAnimationFrame(animFrameId);
    });
</script>

{#if isVisible}
    <!-- Overlay -->
    <div
        class="drawer-overlay"
        style="opacity: {overlayOpacity};"
        role="presentation"
        onclick={() => close()}
    ></div>

    <!-- Drawer -->
    <div
        bind:this={drawerEl}
        class="drawer drawer--{direction} {className}"
        style="transform: {transformStyle}; {style}"
        role="dialog"
        aria-modal="true"
        tabindex="-1"
        onpointerdown={onPointerDown}
        onpointermove={onPointerMove}
        onpointerup={onPointerUp}
        onpointercancel={onPointerUp}
    >
        {@render children?.()}
    </div>
{/if}
