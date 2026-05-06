use std::sync::OnceLock;

use serde::Serialize;
use tauri::ipc::Channel;

use crate::core::InstanceDto;

static CHANNEL: OnceLock<Channel<AppEvent>> = OnceLock::new();

#[derive(Serialize, Clone)]
pub enum AppEvent {
    InstanceStarted { id: String },
    InstanceDeleted { id: String },
    InstanceEdited { id: String },
    InstanceCreated { id: String, dto: InstanceDto },
    DProgress { version: String, progress: u32 },
    DFinish { version: String },
}

impl std::fmt::Display for AppEvent {
    fn fmt(&self, f: &mut std::fmt::Formatter<'_>) -> std::fmt::Result {
        match self {
            AppEvent::InstanceStarted { id } => write!(f, "InstanceStarted({id})"),
            AppEvent::InstanceDeleted { id } => write!(f, "InstanceDeleted({id})"),
            AppEvent::InstanceEdited { id } => write!(f, "InstanceEdited({id})"),
            AppEvent::InstanceCreated { id, dto: _ } => write!(f, "InstanceCreated({id})"),
            AppEvent::DProgress { version, progress } => {
                write!(f, "DProgress({version}/{progress})")
            }
            AppEvent::DFinish { version } => write!(f, "DFinish({version})"),
        }
    }
}

pub fn activate(channel: Channel<AppEvent>) {
    CHANNEL.set(channel).ok();
}

pub fn emit(event: AppEvent) {
    match CHANNEL.get() {
        Some(ch) => {
            ch.send(event)
                .unwrap_or_else(|e| tracing::warn!("Error sending event: {}", e));
        }
        None => tracing::debug!("EventBus no esta activo, tirando: {}", event),
    }
}
