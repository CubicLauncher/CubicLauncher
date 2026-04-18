use tauri::Manager;
use crate::core::{InstanceDto, InstanceManager, LauncherWrapper, PathManager};
use std::path::PathBuf;
use tracing::error;
#[tauri::command]
pub async fn launch(app: tauri::AppHandle, instance_id: String) -> Result<(), String> {
    let manager = InstanceManager::get();
    let Some(arc) = manager.get_instance(&instance_id).await else {
        return Err("Instancia no encontrada".to_string());
    };
    let inst = arc;

    let result = LauncherWrapper::get().lock().await.launch(inst.clone()).await;
    
    if result.is_ok() {
        let settings = crate::core::SettingsManager::get().lock().unwrap();
        if settings.close_launcher_on_play {
            // Get all windows (usually just one) and hide them
            let windows = app.webview_windows();
            for window in windows.values() {
                let _ = window.hide();
            }

            // Spawn a task to monitor the instance and show windows back
            let app_clone = app.clone();
            tokio::spawn(async move {
                loop {
                    tokio::time::sleep(tokio::time::Duration::from_millis(1000)).await;
                    let finished = inst.write().await.check_and_detach();
                    if finished {
                        let windows = app_clone.webview_windows();
                        for window in windows.values() {
                            let _ = window.show();
                            let _ = window.set_focus();
                        }
                        break;
                    }
                }
            });
        }
    }
    
    result
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
pub async fn create_instance(name: String, version: String, icon: Option<String>) {
    InstanceManager::get().create_instance(name, version, icon).await;
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
    InstanceManager::get().update_instance(&id, Some(new_name), None, None).await
}

#[tauri::command]
pub async fn update_instance(
    id: String,
    new_name: Option<String>,
    new_version: Option<String>,
    new_icon: Option<Option<String>>,
) -> Result<(), String> {
    InstanceManager::get()
        .update_instance(&id, new_name, new_version, new_icon)
        .await
}

#[tauri::command]
pub async fn get_available_logos() -> Vec<String> {
    let mut logos = Vec::new();
    let paths = ["static/images/instances", "../static/images/instances"];
    
    for path in paths {
        if let Ok(entries) = std::fs::read_dir(path) {
            for entry in entries.flatten() {
                if let Some(name) = entry.file_name().to_str() {
                    if name.ends_with(".ico") || name.ends_with(".png") || name.ends_with(".svg") {
                        logos.push(name.to_string());
                    }
                }
            }
            break;
        }
    }
    logos
}

#[tauri::command]
pub async fn get_installed_versions() -> Vec<String> {
    let versions_dir = PathManager::get().get_shared_dir().join("versions");
    let mut versions = Vec::new();
    if let Ok(entries) = std::fs::read_dir(versions_dir) {
        for entry in entries.flatten() {
            if entry.path().is_dir() {
                if let Some(name) = entry.file_name().to_str() {
                    versions.push(name.to_string());
                }
            }
        }
    }
    versions.sort_by(|a, b| b.cmp(a));
    versions
}

#[derive(serde::Serialize)]
pub struct ModDto {
    pub name: String,
    pub filename: String,
    pub version: Option<String>,
    pub enabled: bool,
}

#[tauri::command]
pub async fn get_instance_mods(id: String) -> Vec<ModDto> {
    let manager = InstanceManager::get();
    let Some(instance_arc) = manager.get_instance(&id).await else {
        return Vec::new();
    };

    let inst = instance_arc.read().await;
    let mods_dir = inst.get_instance_dir().join("mods");

    let mut mods = Vec::new();
    if let Ok(entries) = std::fs::read_dir(mods_dir) {
        for entry in entries.flatten() {
            let path = entry.path();
            if path.is_file() {
                if let Some(ext) = path.extension() {
                    let ext_str = ext.to_string_lossy().to_lowercase();
                    let file_name_str = path.file_name().unwrap().to_string_lossy().to_lowercase();
                    
                    let (is_mod, enabled) = if ext_str == "jar" || ext_str == "zip" {
                        (true, true)
                    } else if ext_str == "disabled" && (file_name_str.ends_with(".jar.disabled") || file_name_str.ends_with(".zip.disabled")) {
                        (true, false)
                    } else {
                        (false, false)
                    };

                    if is_mod {
                        let filename = path.file_name().unwrap().to_string_lossy().to_string();
                        let display_name = filename.strip_suffix(".disabled").unwrap_or(&filename).to_string();
                        mods.push(ModDto {
                            name: display_name,
                            filename,
                            version: None,
                            enabled,
                        });
                    }
                }
            }
        }
    }
    mods.sort_by(|a, b| a.name.to_lowercase().cmp(&b.name.to_lowercase()));
    mods
}

#[tauri::command]
pub async fn toggle_instance_mod(id: String, filename: String, enable: bool) -> Result<(), String> {
    let manager = InstanceManager::get();
    let Some(instance_arc) = manager.get_instance(&id).await else {
        return Err("Instancia no encontrada".to_string());
    };

    let inst = instance_arc.read().await;
    let mods_dir = inst.get_instance_dir().join("mods");
    let file_path = mods_dir.join(&filename);

    if !file_path.exists() {
        return Err("Mod no encontrado".to_string());
    }

    let is_currently_disabled = filename.ends_with(".disabled");

    if enable && is_currently_disabled {
        let new_filename = filename.strip_suffix(".disabled").unwrap();
        let new_path = mods_dir.join(new_filename);
        std::fs::rename(file_path, new_path).map_err(|e| e.to_string())?;
    } else if !enable && !is_currently_disabled {
        let new_filename = format!("{}.disabled", filename);
        let new_path = mods_dir.join(new_filename);
        std::fs::rename(file_path, new_path).map_err(|e| e.to_string())?;
    }

    Ok(())
}
