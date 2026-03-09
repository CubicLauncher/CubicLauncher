<script lang="ts">
    import { invoke } from "@tauri-apps/api/core";
    import type { InstanceDto } from "$lib/types/types";
    import InstanceDetails from "./InstanceDetails.svelte";
    import Console from "./Console.svelte";

    let { selectedInstance } = $props<{ selectedInstance: InstanceDto }>();

    function handleLaunch() {
        invoke("launch", {
            instanceName: selectedInstance.name,
        });
    }
</script>

<div class="instance-view">
    <section class="hero-section">
        <div class="instance-big-icon">
            <img src="/images/cubic.svg" alt="Icon" />
        </div>
        <div class="instance-title-area">
            <h2>{selectedInstance.name}</h2>
            <div class="last-played">Última vez jugado: Nunca</div>
            <button class="play-btn" onclick={handleLaunch}>Jugar</button>
        </div>
    </section>

    <div class="data-section">
        <InstanceDetails instance={selectedInstance} />
        <Console />
    </div>
</div>
