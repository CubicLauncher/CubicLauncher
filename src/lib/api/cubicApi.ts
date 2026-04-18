import { type InstanceDto, type ModDto } from "../types/types";
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
  icon: string | null,
  callback?: () => void,
  onError?: (err: unknown) => void,
): Promise<void> {
  try {
    await invoke("create_instance", { name, version, icon });
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

export async function updateInstance(
  id: string,
  newName?: string,
  newVersion?: string,
  callback?: () => void,
  onError?: (err: unknown) => void,
): Promise<void> {
  try {
    await invoke("update_instance", { id, newName, newVersion });
    callback?.();
  } catch (err) {
    console.error(`Error al actualizar instancia ${id}:`, err);
    onError?.(err);
  }
}

export async function getInstalledVersions(): Promise<string[]> {
  try {
    return await invoke<string[]>("get_installed_versions");
  } catch (err) {
    console.error("Error al obtener versiones instaladas:", err);
    return [];
  }
}

export async function getInstanceMods(id: string): Promise<ModDto[]> {
  try {
    return await invoke<ModDto[]>("get_instance_mods", { id });
  } catch (err) {
    console.error(`Error al obtener mods de instancia ${id}:`, err);
    return [];
  }
}

export async function toggleInstanceMod(
  id: string,
  filename: string,
  enable: boolean
): Promise<void> {
  try {
    await invoke("toggle_instance_mod", { id, filename, enable });
  } catch (err) {
    console.error(`Error al hacer toggle de mod ${filename}:`, err);
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
export async function getAvailableVersions(): Promise<any> {
    try {
        return await invoke("get_available_versions");
    } catch (err) {
        console.error("Error al obtener versiones disponibles:", err);
        return null;
    }
}

export async function addToQueue(version: string): Promise<void> {
    try {
        await invoke("add_to_queue", { version });
    } catch (err) {
        console.error(`Error al agregar ${version} a la cola:`, err);
    }
}

export async function getAvailableLogos(): Promise<string[]> {
    try {
        return await invoke<string[]>("get_available_logos");
    } catch (err) {
        console.error("Error al obtener logos disponibles:", err);
        return [];
    }
}
