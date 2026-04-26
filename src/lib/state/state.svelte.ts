import type {
  InstanceDto,
  Settings,
  Notification,
  NotificationType,
} from "../types/types";

export interface LauncherState {
  loadedInstances: InstanceDto[];
  currentInstance: InstanceDto | null;
  runningInstances: string[];
  updateProgress: number;
  settings: Settings;
  notifications: Notification[];
}

export const launcherStore = $state<LauncherState>({
  loadedInstances: [],
  currentInstance: null,
  runningInstances: [],
  notifications: [],
  updateProgress: 0, // 0 = sin update, 1-100 = descargando
  settings: {
    username: "Steve",
    user: null,
    min_memory: 1,
    max_memory: 2,
    jre8_path: "",
    jre17_path: "",
    jre21_path: "",
    language: "es",
    auto_updates: true,
    close_launcher_on_play: true,
    show_snapshots: false,
    show_alpha: false,
    force_gpu: false,
    jvm_args: "",
  },
});

export function addNotification(
  title: string,
  message: string,
  type: NotificationType = "info",
  timeout = 5000,
) {
  const id = Math.random().toString(36).substring(2, 9);
  const notification: Notification = { id, title, message, type, timeout };

  launcherStore.notifications = [...launcherStore.notifications, notification];

  return id;
}

export function removeNotification(id: string) {
  launcherStore.notifications = launcherStore.notifications.filter(
    (n) => n.id !== id,
  );
}

export function showError(title: string, message: string) {
  return addNotification(title, message, "error", 8000);
}

export function showSuccess(title: string, message: string) {
  return addNotification(title, message, "success", 4000);
}

export function showWarning(title: string, message: string) {
  return addNotification(title, message, "warning", 6000);
}

export function showInfo(title: string, message: string) {
  return addNotification(title, message, "info", 4000);
}
