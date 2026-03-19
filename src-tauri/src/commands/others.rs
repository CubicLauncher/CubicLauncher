use crate::core::{InstanceManager, InstancesPollingPayload};
use tauri::{AppHandle, Emitter};
use tracing::debug;
#[tauri::command]
pub fn start_polling(app: AppHandle) {
    tauri::async_runtime::spawn(async move {
        loop {
            tokio::time::sleep(std::time::Duration::from_secs(3)).await;
            debug!("Eviando polling");
            let payload = {
                let manager = InstanceManager::get();

                InstancesPollingPayload::new(
                    manager.get_running_dtos().await,
                    manager.get_all_dtos().await,
                    manager.count().await,
                )
            };

            // Emitimos un solo evento con toda la información
            let _ = app.emit("instances-update", payload);
        }
    });
}
