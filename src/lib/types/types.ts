export interface InstanceDto {
  name: string;
  version: string;
  loader: string;
  last_played: number;
  status: InstState;
  cover_image: string | null;
  icon: string | null;
  uuid: string;
  path: string;
}

export enum InstState {
  Started = "started",
  Error = "error",
  Starting = "starting",
  Off = "off",
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
  user: MinecraftUser | null;
  min_memory: number;
  max_memory: number;
  jre8_path: string;
  jre17_path: string;
  jre21_path: string;
  jre25_path: string;
  language: string;
  auto_updates: boolean;
  close_launcher_on_play: boolean;
  show_snapshots: boolean;
  show_alpha: boolean;
  force_gpu: boolean;
  jvm_args: string;
}

export type AccountType = "Cracked" | "Microsoft";

export interface MinecraftUser {
  username: string;
  uuid: string;
  access_token: string;
  refresh_token: string | null;
  user_type: AccountType;
}

export interface DeviceCode {
  user_code: string;
  device_code: string;
  verification_uri: string;
  expires_in: number;
  interval: number;
}

export type NotificationType = "error" | "info" | "success" | "warning";

export interface Notification {
  id: string;
  type: NotificationType;
  title: string;
  message: string;
  timeout?: number;
}

export type AppEvent =
  | {
      type: "InstanceStarted";
      data: {
        id: string;
      };
    }
  | {
      type: "InstanceDeleted";
      data: {
        id: string;
      };
    }
  | {
      type: "InstanceEdited";
      data: {
        id: string;
      };
    }
  | {
      type: "InstanceCreated";
      data: {
        id: string;
        dto: InstanceDto;
      };
    }
  | {
      type: "DProgress";
      data: {
        version: string;
        current: number;
        total: number;
        d_type: string;
      };
    }
  | {
      type: "DFinish";
      data: {
        version: string;
      };
    };
