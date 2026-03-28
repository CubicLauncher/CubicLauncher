use crate::core::{InstanceDto, InstanceManager, LauncherWrapper};
use tracing::error;
#[tauri::command]
pub async fn launch(instance_id: String) {
    let manager = InstanceManager::get();
    let Some(arc) = manager.get_instance(&instance_id).await else {
        error!("Instancia no encontrada");
        return;
    };
    let inst = arc;

    LauncherWrapper::get().lock().await.launch(inst).await;
}

#[tauri::command]
pub async fn kill_instance(instance_name: String) {
    let manager = InstanceManager::get();
    let Some(arc) = manager.get_instance(&instance_name).await else {
        error!("Instancia no encontrada");
        return;
    };
    let mut inst = arc.write().await;
    inst.kill();
}

#[tauri::command]
pub async fn get_instances() -> Vec<InstanceDto> {
    InstanceManager::get().get_all_dtos().await
}
#[tauri::command]
pub async fn create_instance(name: String, version: String) {
    InstanceManager::get().create_instance(name, version).await;
}
