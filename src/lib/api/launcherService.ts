import { launcherStore } from "../state/state.svelte";
import { listen, type UnlistenFn } from "@tauri-apps/api/event";
import type { InstanceDto, InstancesPollingPayload } from "../types/types";
import { killInstance, fetchAll, launchInstance } from "./cubicApi";
import { invoke } from "@tauri-apps/api/core";

export async function initPolling(): Promise<UnlistenFn> {
  return await listen<InstancesPollingPayload>("instances-update", (event) => {
    const { running, all } = event.payload;
    launcherStore.runningInstances = running;
    launcherStore.loadedInstances = all;
  });
}
export async function killInst(uuid: string): Promise<void> {
  try {
    await killInstance(uuid, () => {
      launcherStore.runningInstances = launcherStore.runningInstances.filter(
        (item) => item !== uuid,
      );
    });
  } catch (err) {
    console.error("Error al matar instancia:", err);
  }
}
export async function getVersions(): Promise<void> {
  //  await listen<InstancesPollingPayload>("instances-update", (event) => {
  //   const { running, all } = event.payload;
  //   launcherStore.runningInstances = running;
  //   launcherStore.loadedInstances = all;
  // });
  //
  let instances: InstanceDto[] = await invoke("get_instances");

  launcherStore.loadedInstances = instances;
}
