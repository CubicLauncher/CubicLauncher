use crate::core::{Instance, InstanceDto, InstanceManager, LauncherWrapper};

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
async fn launch(instance_name: String) {
    let manager = InstanceManager::get().lock().await;
    let instance: Option<&Instance> = manager.get_instance(&instance_name);
    if instance.is_none() {
        eprintln!("La instancia solicitada a iniciar no existe.");
        return;
    }
    match instance {
        Some(i) => LauncherWrapper::get().lock().await.launch(i).await,
        None => eprintln!("Instancia no encontrada: {}", instance_name),
    }
}

#[tauri::command]
async fn get_instances() -> Vec<InstanceDto> {
    InstanceManager::get().lock().await.get_all::<InstanceDto>()
}
#[tauri::command]
async fn create_instance(name: String, version: String) {
    InstanceManager::get()
        .lock()
        .await
        .create_instance(name, version);
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
            launch,
            get_instances,
            create_instance
        ])
        .run(tauri::generate_context!())
        .expect("error while running tauri application");
}
