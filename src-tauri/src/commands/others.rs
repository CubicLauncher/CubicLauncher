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
                    manager.get_running_ids().await,
                    manager.get_all_dtos().await,
                    manager.count().await,
                )
            };

            // Emitimos un solo evento con toda la información
            let _ = app.emit("instances-update", payload);
        }
    });
}

#[tauri::command]
pub fn open_url(url: String) -> Result<(), String> {
    #[cfg(target_os = "windows")]
    {
        std::process::Command::new("cmd")
            .args(["/C", "start", &url])
            .spawn()
            .map_err(|e| e.to_string())?;
    }
    #[cfg(target_os = "macos")]
    {
        std::process::Command::new("open")
            .arg(&url)
            .spawn()
            .map_err(|e| e.to_string())?;
    }
    #[cfg(target_os = "linux")]
    {
        std::process::Command::new("xdg-open")
            .arg(&url)
            .spawn()
            .map_err(|e| e.to_string())?;
    }
    Ok(())
}
