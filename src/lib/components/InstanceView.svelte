<script lang="ts">
    import { invoke } from "@tauri-apps/api/core";
    import type { InstanceDto } from "$lib/types/types";
    import InstanceDetails from "./InstanceDetails.svelte";
    import Console from "./Console.svelte";
    import { launchInstance } from "$lib/api/cubicApi";

    let { selectedInstance } = $props<{ selectedInstance: InstanceDto }>();

    const formatter = new Intl.DateTimeFormat("es-ES", {
        year: "numeric",
        month: "long",
        day: "2-digit",
        hour: "2-digit",
        minute: "2-digit",
    });

    function formatDate(unix_date: number): string {
        if (unix_date < 1) {
            return "Nunca jugado";
        }
        let date = new Date(unix_date * 1000);
        return formatter.format(date);
    }
</script>

<div class="instance-view">
    <section class="hero-section">
        <div class="instance-big-icon">
            <img src="/images/cubic.svg" alt="Icon" />
        </div>
        <div class="instance-title-area">
            <h2>{selectedInstance.name}</h2>
            <div class="last-played">
                Última vez jugado: {formatDate(selectedInstance.last_played)}
            </div>
            <button class="play-btn" onclick={launchInstance(selectedInstance)}
                >Jugar</button
            >
        </div>
    </section>

    <div class="data-section">
        <InstanceDetails instance={selectedInstance} />
        <Console />
    </div>
</div>
