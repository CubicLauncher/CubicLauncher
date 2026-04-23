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

