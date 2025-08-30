import type { BackendRes, Instance, MinecraftVersion } from "./lib/types";

export interface IsettingsLauncherData {
  version: string;
  build: string;
  platform: string;
}

// Datos falsos para versiones de Minecraft
const FAKE_MINECRAFT_VERSIONS: MinecraftVersion[] = [
  {
    id: "1.20.4",
    type: "release",
    url: "https://fake-url.com/1.20.4.json",
    time: "2023-12-07T12:56:18+00:00",
    releaseTime: "2023-12-07T12:43:13+00:00",
    sha1: "fake-sha1-hash-1204",
    complianceLevel: 1,
  },
  {
    id: "1.20.3",
    type: "release",
    url: "https://fake-url.com/1.20.3.json",
    time: "2023-12-05T13:21:44+00:00",
    releaseTime: "2023-12-05T13:05:32+00:00",
    sha1: "fake-sha1-hash-1203",
    complianceLevel: 1,
  },
  {
    id: "1.20.2",
    type: "release",
    url: "https://fake-url.com/1.20.2.json",
    time: "2023-09-21T11:45:23+00:00",
    releaseTime: "2023-09-21T11:32:10+00:00",
    sha1: "fake-sha1-hash-1202",
    complianceLevel: 1,
  },
  {
    id: "1.20.1",
    type: "release",
    url: "https://fake-url.com/1.20.1.json",
    time: "2023-06-12T12:36:14+00:00",
    releaseTime: "2023-06-12T12:25:51+00:00",
    sha1: "fake-sha1-hash-1201",
    complianceLevel: 1,
  },
  {
    id: "24w07a",
    type: "snapshot",
    url: "https://fake-url.com/24w07a.json",
    time: "2024-02-14T15:23:45+00:00",
    releaseTime: "2024-02-14T15:12:30+00:00",
    sha1: "fake-sha1-hash-24w07a",
    complianceLevel: 1,
  },
];

// Datos falsos para instancias
const FAKE_INSTANCES: Instance[] = [
  {
    name: "Vanilla 1.20.4",
    loader: {
      loader: "Vanilla",
      version: "1.20.4",
    },
    game: {
      version: "1.20.4",
    },
    lastPlayed: "2024-01-15T10:30:00.000Z",
  },
  {
    name: "Fabric Modded",
    loader: {
      loader: "Fabric",
      version: "0.15.3",
    },
    game: {
      version: "1.20.1",
    },
    lastPlayed: "2024-01-10T14:45:00.000Z",
  },
];

// Datos falsos del launcher
const FAKE_LAUNCHER_DATA: IsettingsLauncherData = {
  version: "1.2.3",
  build: "2024.01.15",
  platform: "Windows 11",
};

// Simulador de delay para hacer más realista
const delay = (ms: number) => new Promise((resolve) => setTimeout(resolve, ms));

// Funciones de ventana (simuladas)
export function closeLauncher() {
  console.log("Launcher cerrado (simulado)");
  // Simular error ocasional
  if (Math.random() < 0.05) {
    console.error("Error simulado al cerrar launcher");
    return { success: false, errorType: "WINDOW_CLOSE_ERROR" };
  }
  return { success: true };
}

export function hideLauncher() {
  console.log("Launcher ocultado (simulado)");
  // Simular error ocasional
  if (Math.random() < 0.05) {
    console.error("Error simulado al ocultar launcher");
    return { success: false, errorType: "WINDOW_HIDE_ERROR" };
  }
  return { success: true };
}

export function maximizeLauncher() {
  console.log("Launcher maximizado (simulado)");
  // Simular error ocasional
  if (Math.random() < 0.05) {
    console.error("Error simulado al maximizar launcher");
    return { success: false, errorType: "WINDOW_MAXIMIZE_ERROR" };
  }
  return { success: true };
}

// Obtener versiones de Minecraft (simulado)
export async function GetVersions(): Promise<MinecraftVersion[]> {
  await delay(800); // Simular latencia de red

  console.log("Versiones de Minecraft obtenidas (simulado)");
  return FAKE_MINECRAFT_VERSIONS;
}

// Guardar instancia (simulado)
export async function SaveInstance(instance: Instance): Promise<BackendRes> {
  await delay(500); // Simular latencia

  // Simular error ocasional (10% de probabilidad)
  if (Math.random() < 0.1) {
    console.error("Error simulado al guardar instancia:", instance.name);
    return {
      success: false,
      errorType: "INVALID_INSTANCE",
      error: "Error simulado al guardar la instancia",
    };
  }

  console.log("Instancia guardada (simulado):", instance.name);
  return {
    success: true,
    data: instance,
  };
}

// Obtener datos del launcher (simulado)
export async function getLauncherData(): Promise<BackendRes> {
  await delay(300); // Simular latencia

  // Simular error ocasional (5% de probabilidad)
  if (Math.random() < 0.05) {
    console.error("Error simulado al obtener datos del launcher");
    return {
      success: false,
      errorType: "GENERIC_FILESYSTEM_ERROR",
    };
  }

  console.log("Datos del launcher obtenidos (simulado)");
  return {
    success: true,
    data: FAKE_LAUNCHER_DATA,
  };
}
