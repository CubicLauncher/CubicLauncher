import { launcherStore } from "../state/state.svelte";
import { listen, type UnlistenFn } from "@tauri-apps/api/event";
import type { InstanceDto, InstancesPollingPayload } from "../types/types";
import {
  killInstance,
  fetchRunning,
  fetchAll,
  launchInstance,
} from "./cubicApi";

export async function initPolling(): Promise<UnlistenFn> {
  return await listen<InstancesPollingPayload>("instances-update", (event) => {
    const { running, all } = event.payload;
    launcherStore.runningInstances = running;
    launcherStore.loadedInstances = all;
  });
}
export async function killInst(name: string): Promise<void> {
  try {
    await killInstance(name, () => {
      launcherStore.runningInstances = launcherStore.runningInstances.filter(
        (item) => item.name !== name,
      );
    });
  } catch (err) {
    console.error("Error al matar instancia:", err);
  }
}
