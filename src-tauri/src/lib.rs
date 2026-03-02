use crate::core::{Instance, LauncherWrapper};

mod core;

#[tauri::command]
async fn add_to_queue(version: String) {
    core::LauncherWrapper::get()
        .lock()
        .await
        .queue_download(version)
        .await;
}

#[tauri::command]
async fn hard_coded_launch(version: String, name: String) {
    LauncherWrapper::launch(Instance::new(name, version));
}

#[tauri::command]
fn greet(name: &str) -> String {
    format!("Hello, {}! You've been greeted from Rust!", name)
}

#[cfg_attr(mobile, tauri::mobile_entry_point)]
pub fn run() {
    tauri::Builder::default()
        .plugin(tauri_plugin_opener::init())
        .invoke_handler(tauri::generate_handler![
            greet,
            add_to_queue,
            hard_coded_launch
        ])
        .run(tauri::generate_context!())
        .expect("error while running tauri application");
}
