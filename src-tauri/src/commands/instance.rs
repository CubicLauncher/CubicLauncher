use crate::core::{InstanceDto, InstanceManager, LauncherWrapper, PathManager};
use std::path::PathBuf;
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
#[tauri::command]
pub async fn get_instance_screenshot(instance_name: String) -> Option<String> {
    let instances_dir = InstanceManager::get()
        .get_all_dtos()
        .await
        .into_iter()
        .find(|i| i.name == instance_name)
        .map(|_| PathManager::get().get_instance_dir().join(&instance_name))?;

    let screenshots_dir = instances_dir.join("screenshots");

    if let Ok(entries) = std::fs::read_dir(screenshots_dir) {
        let mut screenshots: Vec<_> = entries
            .flatten()
            .filter(|e| {
                e.path()
                    .extension()
                    .map_or(false, |ext| ext.to_ascii_lowercase() == "png")
            })
            .collect();

        screenshots.sort_by_key(|e| {
            e.metadata()
                .and_then(|m| m.modified())
                .unwrap_or(std::time::SystemTime::UNIX_EPOCH)
        });

        if let Some(latest) = screenshots.last() {
            return Some(latest.path().to_string_lossy().to_string());
        }
    }
    None
}

#[tauri::command]
pub async fn get_all_instance_screenshots(instance_name: String) -> Vec<String> {
    let instances_dir = PathManager::get().get_instance_dir().join(&instance_name);
    let screenshots_dir = instances_dir.join("screenshots");

    let mut result = Vec::new();
    if let Ok(entries) = std::fs::read_dir(screenshots_dir) {
        let mut screenshots: Vec<_> = entries
            .flatten()
            .filter(|e| {
                e.path()
                    .extension()
                    .map_or(false, |ext| ext.to_ascii_lowercase() == "png")
            })
            .collect();

        screenshots.sort_by_key(|e| {
            std::cmp::Reverse(
                e.metadata()
                    .and_then(|m| m.modified())
                    .unwrap_or(std::time::SystemTime::UNIX_EPOCH),
            )
        });

        for e in screenshots {
            result.push(e.path().to_string_lossy().to_string());
        }
    }
    result
}

#[tauri::command]
pub async fn set_instance_cover_image(instance_id: String, path: String) {
    let manager = InstanceManager::get();
    if let Some(arc) = manager.get_instance(&instance_id).await {
        let mut inst = arc.write().await;
        inst.set_cover_image(Some(PathBuf::from(path)));
        let _ = inst.save_to_disk().await;
    }
}

#[tauri::command]
pub async fn reset_instance_cover_image(instance_id: String) {
    let manager = InstanceManager::get();
    if let Some(arc) = manager.get_instance(&instance_id).await {
        let mut inst = arc.write().await;
        inst.set_cover_image(None);
        let _ = inst.save_to_disk().await;
    }
}

#[tauri::command]
pub async fn get_instance_banner(instance_id: String) -> Option<String> {
    let manager = InstanceManager::get();
    let arc = manager.get_instance(&instance_id).await?;
    let (cover_image, name) = {
        let inst = arc.read().await;
        (inst.get_cover_image().map(|p| p.to_path_buf()), inst.get_name().to_string())
    };

    if let Some(path) = cover_image {
        return Some(path.to_string_lossy().to_string());
    }

    get_instance_screenshot(name).await
}

#[tauri::command]
pub async fn delete_instance(id: String) -> Result<(), String> {
    InstanceManager::get().delete_instance(&id).await
}

#[tauri::command]
pub async fn rename_instance(id: String, new_name: String) -> Result<(), String> {
    InstanceManager::get().rename_instance(&id, new_name).await
}
