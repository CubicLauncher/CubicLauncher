export interface InstanceDto {
  name: string;
  version: string;
  loader: string;
  last_played: number;
  is_running: boolean;
  cover_image: string | null;
}
export interface InstancesPollingPayload {
  running: InstanceDto[];
  all: InstanceDto[];
  count: number;
}
