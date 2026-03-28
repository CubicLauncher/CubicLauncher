import type { InstanceDto } from "../types/types";

export interface LauncherState {
  loadedInstances: InstanceDto[];
  currentInstance: InstanceDto | null;
  runningInstances: string[];
}

export const launcherStore = $state<LauncherState>({
  loadedInstances: [],
  currentInstance: null,
  runningInstances: [],
});
