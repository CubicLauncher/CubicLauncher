<template>
    <div
        class="flex items-center justify-between h-9 bg-stone-800 border-b border-stone-600 px-4 titlebar z-1000"
    >
        <!-- Left spacer for balance -->
        <div class="w-24"></div>
        <div
            class="flex items-center gap-2.5 px-3 bg-stone-750/30 rounded-md border border-stone-600/30"
        >
            <div class="w-6 h-6">
                <!-- Placeholder for logo/icon -->
            </div>
            <span class="text-sm text-stone-200 select-none">
                CubicLauncher
            </span>
        </div>
        <!-- Window controls -->
        <div class="flex items-center gap-0.5 titlebar-buttons">
            <button
                class="w-7 h-7 flex items-center justify-center text-stone-400 hover:text-stone-200 hover:bg-stone-700 rounded-sm transition-all duration-75 no-drag"
                aria-label="Minimize"
            >
                <svg width="12" height="12" viewBox="0 0 12 12">
                    <path
                        d="M3 6h6"
                        stroke="currentColor"
                        stroke-width="1.5"
                        stroke-linecap="round"
                    />
                </svg>
            </button>
            <button
                class="w-7 h-7 flex items-center justify-center text-stone-400 hover:text-stone-200 hover:bg-stone-700 rounded-sm transition-all duration-75 no-drag"
                aria-label="Maximize"
            >
                <svg width="12" height="12" viewBox="0 0 12 12">
                    <rect
                        x="3"
                        y="3"
                        width="6"
                        height="6"
                        stroke="currentColor"
                        stroke-width="1.5"
                        fill="none"
                        rx="1"
                    />
                </svg>
            </button>
            <button
                class="w-7 h-7 flex items-center justify-center text-stone-400 hover:text-stone-200 hover:bg-stone-600 rounded-sm transition-all duration-75 no-drag"
                aria-label="Close"
            >
                <svg width="12" height="12" viewBox="0 0 12 12">
                    <path
                        d="M3 3l6 6M9 3l-6 6"
                        stroke="currentColor"
                        stroke-width="1.5"
                        stroke-linecap="round"
                    />
                </svg>
            </button>
        </div>
    </div>
</template>

<style scoped>
.titlebar {
    -webkit-app-region: drag;
    user-select: none;
    cursor: default;
}

.titlebar-buttons,
.no-drag {
    -webkit-app-region: no-drag;
}

/* Mejor feedback visual */
.titlebar:hover {
    cursor: move;
}

.titlebar-buttons:hover {
    cursor: default;
}

/* Asegurar que los botones tengan el cursor correcto */
.titlebar-buttons button {
    cursor: pointer;
}
</style>