import { type InstanceDto } from "../types";
import { invoke } from "@tauri-apps/api/core";

export async function fetchRunning(
  callback?: () => void,
  onError?: (err: unknown) => void,
): Promise<InstanceDto[]> {
  try {
    const dtos = await invoke<InstanceDto[]>("get_running");
    callback?.();
    return dtos;
  } catch (err) {
    console.error("Error en fetchRunning:", err);
    onError?.(err);
    return [];
  }
}

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

export async function launchInstance(
  instance: InstanceDto,
  callback?: () => void,
  onError?: (err: unknown) => void,
): Promise<void> {
  try {
    await invoke("launch", { instanceName: instance.name });
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
