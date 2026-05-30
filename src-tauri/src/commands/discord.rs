use crate::services::discord_presence;
use tracing::info;

#[tauri::command]
pub async fn init_discord_presence() -> Result<(), String> {
    info!("Initializing Discord presence via command");
    discord_presence::init()
        .await
        .map_err(|e| e.to_string())
}
