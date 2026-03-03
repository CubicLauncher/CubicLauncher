<script lang="ts">
    import { invoke } from "@tauri-apps/api/core";
    import { onMount } from "svelte";
    // Espejo del InstanceDto de Rust
    interface InstanceDto {
        name: string;
        version: string;
        loader: string;
        last_played: number;
        is_running: boolean;
        cover_image: string | null;
    }
    let instances: InstanceDto[] = $state([]);
    let version = $state("");
    let instanceName = $state("");
    onMount(async () => {
        instances = await invoke("get_instances");
    });

    async function createInstance(name: string, version: string) {
        await invoke("create_instance", { name, version });
        instances = await invoke("get_instances");
    }
</script>

{#each instances as instance}
    <div>
        <h3>{instance.name}</h3>
        <p>{instance.version} — {instance.loader}</p>
        <button
            onclick={() =>
                invoke("launch", {
                    instanceName: instance.name,
                })}
        >
            Jugar
        </button>
    </div>
{/each}
<div>
    <label>
        version
        <input type="text" bind:value={version} />
    </label>
    <label>
        nombre
        <input type="text" bind:value={instanceName} />
    </label>
    <button onclick={() => createInstance(instanceName, version)}>crear</button>
</div>
