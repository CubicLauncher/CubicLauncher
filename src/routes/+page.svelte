<script lang="ts">
    import { invoke } from "@tauri-apps/api/core";
    import { createInstance } from "$lib/api/cubicApi";
    import { onMount } from "svelte";
    import Drawer from "$lib/components/Drawer.svelte";
    import QuickMenu from "$lib/components/QuickMenu.svelte";
    import "../styles/app.css";
    import { launcherStore } from "$lib/state/state.svelte";
    import { initPolling } from "$lib/api/launcherService";
    let version = $state("");
    let open = $state(false);
    let instanceName = $state("");

    onMount(() => {
        invoke("start_polling");
        const unlistenPromise = initPolling();

        return () => {
            unlistenPromise.then((unlisten) => unlisten());
        };
    });
</script>

{#each launcherStore.loadedInstances as instance}
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
<button onclick={() => (open = true)}>Open Drawer</button>

<Drawer bind:open direction="right" onclose={() => (open = false)}>
    <QuickMenu onclose={() => (open = false)} />
</Drawer>
