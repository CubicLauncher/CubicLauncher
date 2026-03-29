<script lang="ts">
    const mods = [
        { name: "OptiFine", category: "Performance" },
        { name: "Sodium", category: "Graphics" },
        { name: "Iris", category: "Shaders" },
        { name: "Lithium", category: "Optimization" },
    ];
</script>

<div class="mods-section">
    <span class="section-title">Mods Instalados ({mods.length})</span>
    <div class="mods-grid">
        {#each mods as mod}
            <div class="mod-card">
                <div class="mod-info">
                    <span class="mod-name">{mod.name}</span>
                    <span class="mod-category">{mod.category}</span>
                </div>
                <div class="mod-status-dot"></div>
            </div>
        {/each}
    </div>
</div>

<style>
    .mods-section {
        margin-bottom: 24px;
    }
    .mods-grid {
        display: grid;
        grid-template-columns: repeat(4, 1fr);
        gap: 12px;
    }
    .mod-card {
        background: rgba(255, 255, 255, 0.03);
        border: 1px solid var(--border);
        border-radius: 6px;
        padding: 12px;
        display: flex;
        justify-content: space-between;
        align-items: center;
        transition:
            background 0.2s ease,
            transform 0.1s ease;
        cursor: pointer;
    }
    .mod-card:hover {
        background: rgba(255, 255, 255, 0.06);
        transform: translateY(-2px);
    }
    .mod-info {
        display: flex;
        flex-direction: column;
        gap: 2px;
    }
    .mod-name {
        font-size: 0.85rem;
        font-weight: 600;
        color: var(--text-primary);
    }
    .mod-category {
        font-size: 0.65rem;
        color: var(--text-secondary);
        text-transform: uppercase;
        letter-spacing: 0.5px;
    }
    .mod-status-dot {
        width: 6px;
        height: 6px;
        background: #4caf50;
        border-radius: 50%;
        box-shadow: 0 0 6px rgba(76, 175, 80, 0.4);
    }
</style>
