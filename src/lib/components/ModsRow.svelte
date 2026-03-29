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
