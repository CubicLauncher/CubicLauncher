use std::sync::OnceLock;

use serde::Serialize;
use tauri::{AppHandle, Emitter};

use crate::core::InstanceDto;

static APP: OnceLock<AppHandle> = OnceLock::new();

#[derive(Clone, Serialize)]
#[serde(tag = "type", content = "data")]
pub enum AppEvent {
    InstanceStarted { id: String },
    InstanceDeleted { id: String },
    InstanceEdited { id: String },
    InstanceCreated { id: String, dto: InstanceDto },
    DProgress { version: String, progress: u32 },
    DFinish { version: String },
}

pub fn init(app: AppHandle) {
    let _ = APP.set(app);
}

pub fn emit(event: AppEvent) {
    if let Some(app) = APP.get() {
        if let Err(err) = app.emit("app-event", event) {
            tracing::warn!("failed to emit event: {}", err);
        }
    }
}
