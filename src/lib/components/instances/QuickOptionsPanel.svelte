<script lang="ts">
    import { invoke } from "@tauri-apps/api/core";
    import { t } from "$lib/i18n";
    import type { InstanceDto } from "$lib/types/types";

    // Props
    interface Props {
        instance: InstanceDto;
    }

    let { instance }: Props = $props();

    // Interfaz para las opciones
    interface QuickOption {
        label: string;
        icon: string;
        subDir: string | null;
    }

    // Opciones usando los SVGs de la carpeta static/images/icons
    const options = $derived<QuickOption[]>([
        {
            label: t("instanceView.options.folder"),
            icon: "/images/icons/folder.svg",
            subDir: null
        },
        {
            label: t("instanceView.options.resources"),
            icon: "/images/icons/resources.svg",
            subDir: "resourcepacks"
        },
        {
            label: t("instanceView.options.screenshots"),
            icon: "/images/icons/screenshots.svg",
            subDir: "screenshots"
        },
        {
            label: t("instanceView.options.logs"),
            icon: "/images/icons/logs.svg",
            subDir: "logs"
        }
    ]);

    async function handleAction(subDir: string | null) {
        if (!instance) return;
        try {
            await invoke("open_instance_dir", { 
                id: instance.uuid, 
                subDir 
            });
        } catch (err) {
            console.error("Failed to open directory:", err);
        }
    }
</script>

<div class="quick-options-section">
    <span class="section-title">{t("instanceView.options.title")}</span>
    <div class="options-grid">
        {#each options as option}
            <button class="option-button" onclick={() => handleAction(option.subDir)}>
                <img src={option.icon} alt={option.label} class="option-icon-svg" />
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
        border-radius: 12px;
        display: flex;
        flex-direction: column;
        align-items: center;
        justify-content: center;
        gap: 12px;
        padding: 16px;
        cursor: pointer;
        transition: all 0.2s cubic-bezier(0.4, 0, 0.2, 1);
        color: var(--text-primary);
        font-family: inherit;
    }
    .option-button:hover {
        background: rgba(255, 255, 255, 0.08);
        border-color: rgba(255, 255, 255, 0.2);
        transform: translateY(-4px);
        box-shadow: 0 4px 12px rgba(0, 0, 0, 0.2);
    }
    .option-button:active {
        transform: translateY(0);
        background: rgba(255, 255, 255, 0.05);
    }
    .option-icon-svg {
        width: 32px;
        height: 32px;
        /* Invertimos el color de negro a blanco y ajustamos la opacidad */
        filter: brightness(0) invert(1) opacity(0.7);
        transition: all 0.2s ease;
    }
    .option-button:hover .option-icon-svg {
        filter: brightness(0) invert(1) opacity(1);
        transform: scale(1.1);
    }
    .option-label {
        font-size: 0.7rem;
        font-weight: 700;
        text-transform: uppercase;
        letter-spacing: 0.8px;
        color: var(--text-secondary);
        text-align: center;
    }
    .option-button:hover .option-label {
        color: var(--text-primary);
    }
</style>
