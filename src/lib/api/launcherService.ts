import { launcherStore } from "../state/state.svelte";
import { listen, type UnlistenFn } from "@tauri-apps/api/event";
import type { InstanceDto, InstancesPollingPayload } from "../types/types";
import { killInstance, fetchAll, launchInstance, getSettings, updateSettings } from "./cubicApi";
import { invoke } from "@tauri-apps/api/core";

export async function initPolling(): Promise<UnlistenFn> {
  // Sync settings first
  await syncSettings();
  
  return await listen<InstancesPollingPayload>("instances-update", (event) => {
    const { running, all } = event.payload;
    launcherStore.runningInstances = running;
    launcherStore.loadedInstances = all;
  });
}

export async function syncSettings(): Promise<void> {
    const settings = await getSettings();
    if (settings) {
        launcherStore.settings = settings;
    }
}

export async function saveSettings(): Promise<void> {
    await updateSettings(launcherStore.settings);
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

export async function deleteInst(uuid: string): Promise<void> {
  try {
    await invoke("delete_instance", { id: uuid });
    // After deletion, refresh instances
    await getVersions();
  } catch (err) {
    console.error("Error al eliminar instancia:", err);
  }
}

export async function renameInst(uuid: string, newName: string): Promise<void> {
  try {
    await invoke("rename_instance", { id: uuid, newName });
    // After rename, refresh instances
    await getVersions();
  } catch (err) {
    console.error("Error al renombrar instancia:", err);
  }
}

export async function updateInst(uuid: string, newName?: string, newVersion?: string): Promise<void> {
  try {
    await invoke("update_instance", { id: uuid, newName, newVersion });
    // After update, refresh instances
    await getVersions();
  } catch (err) {
    console.error("Error al actualizar instancia:", err);
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
