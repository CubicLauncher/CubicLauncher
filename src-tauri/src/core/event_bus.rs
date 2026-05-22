use std::sync::OnceLock;

use serde::Serialize;
use tauri::{AppHandle, Emitter};

use crate::services::InstanceDto;

static APP: OnceLock<AppHandle> = OnceLock::new();

#[derive(Clone, Serialize)]
#[serde(tag = "type", content = "data")]
pub enum AppEvent {
    InstanceEdited {
        id: String,
    },
    InstanceCreated {
        id: String,
        dto: InstanceDto,
    },
    DProgress {
        version: String,
        current: u32,
        total: u32,
        d_type: String,
    },
    DFinish {
        version: String,
    },
    STChanged,
    ThemeChanged {
        id: String,
    },
}

pub fn init(app: AppHandle) {
    let _ = APP.set(app);
}

pub fn emit(event: AppEvent) {
    if let Some(app) = APP.get()
        && let Err(err) = app.emit("app-event", event)
    {
        tracing::warn!("failed to emit event: {}", err);
    }
}
