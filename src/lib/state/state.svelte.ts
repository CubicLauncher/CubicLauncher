import type { InstanceDto, Settings } from "../types/types";

export interface LauncherState {
  loadedInstances: InstanceDto[];
  currentInstance: InstanceDto | null;
  runningInstances: string[];
  settings: Settings;
}

export const launcherStore = $state<LauncherState>({
  loadedInstances: [],
  currentInstance: null,
  runningInstances: [],
  settings: {
    username: "Steve",
    min_memory: 512,
    max_memory: 2048,
    jre8_path: "",
    jre17_path: "",
    jre21_path: "",
  },
});
