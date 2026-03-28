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
