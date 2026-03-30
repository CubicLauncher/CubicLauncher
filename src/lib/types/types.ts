export interface InstanceDto {
  name: string;
  version: string;
  loader: string;
  last_played: number;
  is_running: boolean;
  cover_image: string | null;
  uuid: string;
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
}
