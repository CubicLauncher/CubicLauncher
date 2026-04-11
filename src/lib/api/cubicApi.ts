import { type InstanceDto } from "../types/types";
import { invoke } from "@tauri-apps/api/core";

export async function killInstance(
  name: string,
  callback?: () => void,
  onError?: (err: unknown) => void,
): Promise<void> {
  try {
    await invoke("kill_instance", { instanceName: name });
    callback?.();
  } catch (err) {
    console.error(`Error al matar instancia ${name}:`, err);
    onError?.(err);
  }
}
export async function createInstance(
  name: string,
  version: string,
  callback?: () => void,
  onError?: (err: unknown) => void,
): Promise<void> {
  try {
    await invoke("create_instance", { name, version });
    callback?.();
  } catch (err) {
    console.error(`Error al crear instancia ${name}:`, err);
    onError?.(err);
  }
}

export async function deleteInstance(
  id: string,
  callback?: () => void,
  onError?: (err: unknown) => void,
): Promise<void> {
  try {
    await invoke("delete_instance", { id });
    callback?.();
  } catch (err) {
    console.error(`Error al eliminar instancia ${id}:`, err);
    onError?.(err);
  }
}

export async function renameInstance(
  id: string,
  newName: string,
  callback?: () => void,
  onError?: (err: unknown) => void,
): Promise<void> {
  try {
    await invoke("rename_instance", { id, newName });
    callback?.();
  } catch (err) {
    console.error(`Error al renombrar instancia ${id}:`, err);
    onError?.(err);
  }
}

export async function launchInstance(
  instance: InstanceDto,
  callback?: () => void,
  onError?: (err: unknown) => void,
): Promise<void> {
  try {
    await invoke("launch", { instanceId: instance.uuid });
    callback?.();
  } catch (err) {
    console.error(`Error al lanzar instancia ${instance.name}:`, err);
    onError?.(err);
  }
}

export async function fetchAll(
  callback?: () => void,
  onError?: (err: unknown) => void,
): Promise<InstanceDto[]> {
  try {
    const dtos = await invoke<InstanceDto[]>("get_instances");
    callback?.();
    return dtos;
  } catch (err) {
    console.error("Error en fetchAll:", err);
    onError?.(err);
    return [];
  }
}

export async function getSettings(): Promise<any> {
    try {
        return await invoke("get_settings");
    } catch (err) {
        console.error("Error al obtener settings:", err);
        return null;
    }
}

export async function updateSettings(settings: any): Promise<void> {
    try {
        await invoke("update_settings", { newSettings: settings });
    } catch (err) {
        console.error("Error al actualizar settings:", err);
    }
}
