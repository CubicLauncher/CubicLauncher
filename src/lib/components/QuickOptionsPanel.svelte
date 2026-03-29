<script lang="ts">
    import { invoke } from "@tauri-apps/api/core";

    const options = [
        { label: "Abrir Carpeta", icon: "📂", action: () => console.log("Open folder") },
        { label: "Recursos", icon: "🎨", action: () => console.log("Resource packs") },
        { label: "Configurar", icon: "🛠️", action: () => console.log("Configure") },
        { label: "Capturas", icon: "🖼️", action: () => console.log("Screenshots") }
    ];
</script>

<div class="quick-options-section">
    <span class="section-title">Opciones rápidas</span>
    <div class="options-grid">
        {#each options as option}
            <button class="option-button" onclick={option.action}>
                <span class="option-icon">{option.icon}</span>
                <span class="option-label">{option.label}</span>
            </button>
        {/each}
    </div>
</div>

<style>
    .quick-options-section {
        flex: 2;
        display: flex;
        flex-direction: column;
    }
    .options-grid {
        display: grid;
        grid-template-columns: repeat(2, 1fr);
        grid-template-rows: repeat(2, 1fr);
        gap: 12px;
        flex: 1;
    }
    .option-button {
        background: rgba(255, 255, 255, 0.03);
        border: 1px solid var(--border);
        border-radius: 8px;
        display: flex;
        flex-direction: column;
        align-items: center;
        justify-content: center;
        gap: 8px;
        cursor: pointer;
        transition: background 0.2s ease, transform 0.1s ease;
        color: var(--text-primary);
        font-family: inherit;
    }
    .option-button:hover {
        background: rgba(255, 255, 255, 0.06);
        transform: translateY(-2px);
    }
    .option-button:active {
        transform: translateY(0);
    }
    .option-icon {
        font-size: 1.2rem;
    }
    .option-label {
        font-size: 0.75rem;
        font-weight: 600;
        text-transform: uppercase;
        letter-spacing: 0.5px;
        color: var(--text-secondary);
    }
    .option-button:hover .option-label {
        color: var(--text-primary);
    }
</style>
