export interface InstanceDto {
  name: string;
  version: string;
  loader: string;
  last_played: number;
  is_running: boolean;
  cover_image: string | null;
  icon: string | null;
  uuid: string;
  path: string;
}

export interface ModDto {
  name: string;
  filename: string;
  version: string | null;
  enabled: boolean;
}
export interface InstancesPollingPayload {
  running: string[];
  all: InstanceDto[];
  count: number;
}

export interface Settings {
  username: string;
  min_memory: number;
  max_memory: number;
  jre8_path: string;
  jre17_path: string;
  jre21_path: string;
  language: string;
  auto_updates: boolean;
  close_launcher_on_play: boolean;
  show_snapshots: boolean;
  show_alpha: boolean;
  force_gpu: boolean;
  jvm_args: string;
}

export type NotificationType = "error" | "info" | "success" | "warning";

export interface Notification {
  id: string;
  type: NotificationType;
  title: string;
  message: string;
  timeout?: number;
}
