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
    language: "es",
    auto_updates: true,
    show_error_console: false,
    close_launcher_on_play: true,
    show_snapshots: false,
    show_alpha: false,
    force_gpu: false,
    jvm_args: "",
  },
});
