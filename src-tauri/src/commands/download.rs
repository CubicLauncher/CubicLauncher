use crate::core::LauncherWrapper;

#[tauri::command]
pub async fn add_to_queue(version: String) {
    LauncherWrapper::get()
        .lock()
        .await
        .queue_download(version)
        .await;
}
