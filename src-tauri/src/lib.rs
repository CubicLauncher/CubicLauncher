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
    let Some(arc) = manager.get_instance(&instance_name) else {
        eprintln!("La instancia solicitada a iniciar no existe.");
        return;
    };
    drop(manager);

    LauncherWrapper::get().lock().await.launch(arc).await;
}

#[tauri::command]
async fn get_instances() -> Vec<InstanceDto> {
    InstanceManager::get().lock().await.get_all_dtos().await
}
#[tauri::command]
async fn kill_instance(instance_name: String) {
    let manager = InstanceManager::get().lock().await;
    let Some(arc) = manager.get_instance(&instance_name) else {
        eprintln!("Instancia no encontrada");
        return;
    };
    drop(manager);
    arc.lock().await.kill();
}
#[tauri::command]
async fn get_running() -> Vec<InstanceDto> {
    InstanceManager::get().lock().await.get_running_dtos().await
}
#[tauri::command]
async fn create_instance(name: String, version: String) {
    InstanceManager::get()
        .lock()
        .await
        .create_instance(name, version)
        .await;
}

#[cfg_attr(mobile, tauri::mobile_entry_point)]
pub fn run() {
    tauri::Builder::default()
        .plugin(tauri_plugin_opener::init())
        .invoke_handler(tauri::generate_handler![
            add_to_queue,
            launch,
            get_instances,
            create_instance,
            get_running,
            kill_instance
        ])
        .run(tauri::generate_context!())
        .expect("error while running tauri application");
}
