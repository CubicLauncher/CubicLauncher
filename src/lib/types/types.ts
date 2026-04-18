export interface InstanceDto {
  name: string;
  version: string;
  loader: string;
  last_played: number;
  is_running: boolean;
  cover_image: string | null;
  uuid: string;
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
  show_error_console: boolean;
  close_launcher_on_play: boolean;
  show_snapshots: boolean;
  show_alpha: boolean;
  force_gpu: boolean;
  auto_detect_java: boolean;
  jvm_args: string;
}
