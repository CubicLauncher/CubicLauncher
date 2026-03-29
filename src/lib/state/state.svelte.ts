import type { InstanceDto } from "../types/types";

export interface LauncherState {
  loadedInstances: InstanceDto[];
  currentInstance: InstanceDto | null;
  runningInstances: string[];
  userName: string;
}

export const launcherStore = $state<LauncherState>({
  loadedInstances: [],
  currentInstance: null,
  runningInstances: [],
  userName: "Santiagolxx",
});
