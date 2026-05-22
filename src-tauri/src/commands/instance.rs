use crate::core::{AppEvent, PathManager, emit};
use crate::services::{InstanceDto, InstanceManager, InstanceStatus, Launcher, signal_kill};
use std::path::{Path, PathBuf};
use std::sync::Mutex;
use std::sync::OnceLock;
use std::time::Instant;
use tracing::{error, info, warn};

fn validate_uuid(uuid: &str) -> Result<(), String> {
    uuid::Uuid::parse_str(uuid).map_err(|_| format!("UUID inválido: '{}'", uuid))?;
    Ok(())
}

fn sanitize_sub_path(instance_dir: &Path, sub_path: &Path) -> Result<PathBuf, String> {
    if sub_path.is_absolute() {
        return Err("La ruta no puede ser absoluta".to_string());
    }
    for component in sub_path.components() {
        if matches!(component, std::path::Component::ParentDir) {
            return Err("La ruta no puede contener '..'".to_string());
        }
    }
    Ok(instance_dir.join(sub_path))
}

const INSTALLED_VERSIONS_TTL: std::time::Duration = std::time::Duration::from_secs(30);

type InstalledVersionsCache = (Instant, Vec<String>);

fn installed_versions_cache() -> &'static Mutex<Option<InstalledVersionsCache>> {
    static CACHE: OnceLock<Mutex<Option<InstalledVersionsCache>>> = OnceLock::new();
    CACHE.get_or_init(|| Mutex::new(None))
}

// ═══════════════════════════════════════════════════════════════════════════════
// Lifecycle
// ═══════════════════════════════════════════════════════════════════════════════

#[tauri::command]
pub async fn launch(instance_id: String) -> Result<(), String> {
    validate_uuid(&instance_id)?;
    info!("Lanzando instancia {}", instance_id);
    let manager = InstanceManager::get();
    let Some(handle) = manager.get_handle(&instance_id).await else {
        error!("Instancia {} no encontrada para lanzar", instance_id);
        return Err("Instancia no encontrada".to_string());
    };

    Launcher::get()
        .launch(handle.clone())
        .await
        .map_err(|e| {
            error!("Error lanzando instancia {}: {}", instance_id, e);
            e.to_string()
        })?;

    info!("Instancia {} lanzada exitosamente", instance_id);
    Ok(())
}

// ── Kill ─────────────────────────────────────────────────────────────────────

#[tauri::command]
pub async fn kill_instance(uuid: String) -> Result<(), String> {
    validate_uuid(&uuid)?;
    info!("Matando instancia {}", uuid);
    let manager = InstanceManager::get();
    let handle = manager
        .get_handle(&uuid)
        .await
        .ok_or_else(|| "Instancia no encontrada".to_string())?;

    if !signal_kill(&uuid) {
        warn!("Instancia {} no estaba corriendo, forzando Off", uuid);
        handle.set_status(InstanceStatus::Off);
    }
    Ok(())
}

// ═══════════════════════════════════════════════════════════════════════════════
// Querying
// ═══════════════════════════════════════════════════════════════════════════════

#[tauri::command]
pub async fn get_instances() -> Vec<InstanceDto> {
    InstanceManager::get().get_all_dtos().await
}

// ── CRUD ─────────────────────────────────────────────────────────────────────

#[tauri::command]
pub async fn create_instance(
    name: String,
    version: String,
    icon: Option<String>,
) -> Result<(), String> {
    info!("Creando instancia: name={}, version={}, icon={:?}", name, version, icon);
    match InstanceManager::get()
        .create_instance(name, version, icon)
        .await
    {
        Ok(d) => {
            info!("Instancia creada: uuid={}", d.uuid);
            emit(AppEvent::InstanceCreated {
                id: d.uuid.clone(),
                dto: d.to_dto().await,
            });
            Ok(())
        }
        Err(e) => {
            error!("Error creando instancia: {}", e);
            Err(e.to_string())
        }
    }
}

#[tauri::command]
pub async fn delete_instance(id: String) -> Result<(), String> {
    validate_uuid(&id)?;
    info!("Eliminando instancia {}", id);
    let result = InstanceManager::get().delete_instance(&id).await;
    if let Err(ref e) = result {
        error!("Error eliminando instancia {}: {}", id, e);
    } else {
        info!("Instancia {} eliminada", id);
    }
    result
}

// ═══════════════════════════════════════════════════════════════════════════════
// Screenshots & Cover Image
// ═══════════════════════════════════════════════════════════════════════════════

#[tauri::command]
pub async fn get_instance_screenshot(instance_name: String) -> Option<String> {
    let screenshots_dir = PathManager::get()
        .get_instance_dir()
        .join(&instance_name)
        .join("screenshots");

    if let Ok(entries) = std::fs::read_dir(screenshots_dir) {
        let mut screenshots: Vec<_> = entries
            .flatten()
            .filter(|e| {
                e.path()
                    .extension()
                    .is_some_and(|ext| ext.eq_ignore_ascii_case("png"))
            })
            .collect();

        screenshots.sort_by_key(|e| match e.metadata().and_then(|m| m.modified()) {
            Ok(t) => t,
            Err(_) => std::time::SystemTime::UNIX_EPOCH,
        });

        if let Some(latest) = screenshots.last() {
            return Some(latest.path().to_string_lossy().to_string());
        }
    }
    None
}

#[tauri::command]
pub async fn get_all_instance_screenshots(instance_name: String) -> Vec<String> {
    let screenshots_dir = PathManager::get()
        .get_instance_dir()
        .join(&instance_name)
        .join("screenshots");

    let mut result = Vec::new();
    if let Ok(entries) = std::fs::read_dir(screenshots_dir) {
        let mut screenshots: Vec<_> = entries
            .flatten()
            .filter(|e| {
                e.path()
                    .extension()
                    .is_some_and(|ext| ext.eq_ignore_ascii_case("png"))
            })
            .collect();

        screenshots.sort_by_key(|e| {
            let time = match e.metadata().and_then(|m| m.modified()) {
                Ok(t) => t,
                Err(_) => std::time::SystemTime::UNIX_EPOCH,
            };
            std::cmp::Reverse(time)
        });

        for e in screenshots {
            result.push(e.path().to_string_lossy().to_string());
        }
    }
    result
}

#[tauri::command]
pub async fn set_instance_cover_image(instance_id: String, path: String) {
    if let Err(e) = validate_uuid(&instance_id) {
        warn!("{}", e);
        return;
    }
    info!("Estableciendo cover image para instancia {}: {}", instance_id, path);
    let manager = InstanceManager::get();
    if let Some(handle) = manager.get_handle(&instance_id).await {
        handle.set_cover_image(Some(PathBuf::from(path))).await;
        if let Err(e) = handle.save_if_dirty().await {
            warn!("Error guardando cover image de instancia {}: {:?}", instance_id, e);
        }
    } else {
        warn!("Instancia {} no encontrada para establecer cover image", instance_id);
    }
}

#[tauri::command]
pub async fn reset_instance_cover_image(instance_id: String) {
    if let Err(e) = validate_uuid(&instance_id) {
        warn!("{}", e);
        return;
    }
    info!("Reseteando cover image para instancia {}", instance_id);
    let manager = InstanceManager::get();
    if let Some(handle) = manager.get_handle(&instance_id).await {
        handle.set_cover_image(None).await;
        if let Err(e) = handle.save_if_dirty().await {
            warn!("Error guardando reset cover image de instancia {}: {:?}", instance_id, e);
        }
    } else {
        warn!("Instancia {} no encontrada para resetear cover image", instance_id);
    }
}

#[tauri::command]
pub async fn get_instance_banner(instance_id: String) -> Option<String> {
    if validate_uuid(&instance_id).is_err() {
        return None;
    }
    let manager = InstanceManager::get();
    let handle = manager.get_handle(&instance_id).await?;

    if let Some(path) = handle.get_cover_image().await {
        return Some(path.to_string_lossy().to_string());
    }

    get_instance_screenshot(handle.get_name().await).await
}

// ═══════════════════════════════════════════════════════════════════════════════
// Edición
// ═══════════════════════════════════════════════════════════════════════════════

#[tauri::command]
pub async fn open_instance_dir(id: String, sub_dir: Option<String>) -> Result<(), String> {
    validate_uuid(&id)?;
    let manager = InstanceManager::get();
    let Some(handle) = manager.get_handle(&id).await else {
        warn!("Intento de abrir directorio de instancia {} no encontrada", id);
        return Err("Instancia no encontrada".to_string());
    };

    let instance_dir = handle.get_instance_dir().await;

    let path = match sub_dir {
        Some(ref sub) => sanitize_sub_path(&instance_dir, Path::new(sub))?,
        None => instance_dir,
    };

    info!("Abriendo directorio: {:?}", path);

    if !path.exists()
        && let Err(e) = tokio::fs::create_dir_all(&path).await
    {
        error!("No se pudo crear el directorio {:?}: {}", path, e);
        return Err(format!("No se pudo crear el directorio: {}", e));
    }

    #[cfg(target_os = "windows")]
    {
        use std::process::Command;
        Command::new("explorer")
            .arg(path)
            .spawn()
            .map_err(|e| e.to_string())?;
    }

    #[cfg(target_os = "linux")]
    {
        use std::process::Command;
        Command::new("xdg-open")
            .arg(path)
            .spawn()
            .map_err(|e| e.to_string())?;
    }

    #[cfg(target_os = "macos")]
    {
        use std::process::Command;
        Command::new("open")
            .arg(path)
            .spawn()
            .map_err(|e| e.to_string())?;
    }

    Ok(())
}

#[tauri::command]
pub async fn rename_instance(id: String, new_name: String) -> Result<(), String> {
    validate_uuid(&id)?;
    info!("Renombrando instancia {} a '{}'", id, new_name);
    let result = InstanceManager::get()
        .update_instance(&id, Some(new_name), None, None)
        .await;
    if let Err(ref e) = result {
        error!("Error renombrando instancia {}: {}", id, e);
    }
    result
}

#[tauri::command]
pub async fn update_instance(
    id: String,
    new_name: Option<String>,
    new_version: Option<String>,
    new_icon: Option<Option<String>>,
) -> Result<(), String> {
    validate_uuid(&id)?;
    info!("Actualizando instancia {}: name={:?}, version={:?}, icon={:?}", id, new_name, new_version, new_icon);
    let result = InstanceManager::get()
        .update_instance(&id, new_name, new_version, new_icon)
        .await;
    if let Err(ref e) = result {
        error!("Error actualizando instancia {}: {}", id, e);
    }
    result
}

// ── Versiones ────────────────────────────────────────────────────────────────

#[tauri::command]
pub async fn get_installed_versions() -> Vec<String> {
    {
        let cache = installed_versions_cache().lock().unwrap_or_else(|e| e.into_inner());
        if let Some((timestamp, cached)) = cache.as_ref()
            && timestamp.elapsed() < INSTALLED_VERSIONS_TTL {
                return cached.clone();
            }
    }

    let versions_dir = PathManager::get().get_shared_dir().join("versions");
    let mut versions = Vec::new();
    if let Ok(entries) = std::fs::read_dir(versions_dir) {
        for entry in entries.flatten() {
            if entry.path().is_dir()
                && let Some(name) = entry.file_name().to_str()
            {
                versions.push(name.to_string());
            }
        }
    }
    versions.sort_by(|a, b| b.cmp(a));

    let mut cache = installed_versions_cache().lock().unwrap_or_else(|e| e.into_inner());
    *cache = Some((Instant::now(), versions.clone()));
    info!("Versiones instaladas actualizadas ({} versiones)", versions.len());
    versions
}

// ═══════════════════════════════════════════════════════════════════════════════
// Mods
// ═══════════════════════════════════════════════════════════════════════════════

#[derive(serde::Serialize)]
pub struct ModDto {
    pub name: String,
    pub filename: String,
    pub version: Option<String>,
    pub description: Option<String>,
    pub authors: Option<Vec<String>>,
    pub icon: Option<String>,
    pub enabled: bool,
}

#[tauri::command]
pub async fn get_instance_mods(id: String) -> Vec<ModDto> {
    if let Err(e) = validate_uuid(&id) {
        warn!("{}", e);
        return Vec::new();
    }
    let manager = InstanceManager::get();
    let Some(handle) = manager.get_handle(&id).await else {
        warn!("Instancia {} no encontrada para listar mods", id);
        return Vec::new();
    };

    let mods_dir = handle.get_instance_dir().await.join("mods");
    info!("Listando mods de instancia {} en {:?}", id, mods_dir);

    let mut mods = Vec::new();
    if let Ok(entries) = std::fs::read_dir(mods_dir) {
        for entry in entries.flatten() {
            let path = entry.path();
            if path.is_file()
                && let Some(ext) = path.extension()
            {
                let ext_str = ext.to_string_lossy().to_lowercase();
                let Some(file_name) = path.file_name() else {
                    continue;
                };
                let file_name_str = file_name.to_string_lossy().to_lowercase();

                let (is_mod, enabled) = if ext_str == "jar" || ext_str == "zip" {
                    (true, true)
                } else if ext_str == "disabled"
                    && (file_name_str.ends_with(".jar.disabled")
                        || file_name_str.ends_with(".zip.disabled"))
                {
                    (true, false)
                } else {
                    (false, false)
                };

                if is_mod {
                    let filename = file_name.to_string_lossy().to_string();
                    let display_name = match filename.strip_suffix(".disabled") {
                        Some(stripped) => stripped.to_string(),
                        None => filename.clone(),
                    };

                    let path_clone = path.clone();
                    let metadata = tokio::task::spawn_blocking(move || {
                        crate::services::AddonManager::get_mod_info(&path_clone)
                    })
                    .await
                    .unwrap_or(None);

                    let (md_name, md_version, md_desc, md_authors, md_icon) = match metadata {
                        Some(m) => (m.name, m.version, m.description, m.authors, m.icon),
                        None => (display_name, None, None, None, None),
                    };

                    mods.push(ModDto {
                        name: md_name,
                        filename,
                        version: md_version,
                        description: md_desc,
                        authors: md_authors,
                        icon: md_icon,
                        enabled,
                    });
                }
            }
        }
    }
    mods.sort_by_key(|a| a.name.to_lowercase());
    info!("{} mods encontrados en instancia {}", mods.len(), id);
    mods
}

// ── Toggle mod ────────────────────────────────────────────────────────────────

#[tauri::command]
pub async fn toggle_instance_mod(id: String, filename: String, enable: bool) -> Result<(), String> {
    validate_uuid(&id)?;
    info!("Cambiando estado del mod '{}' en instancia {}: enable={}", filename, id, enable);
    let manager = InstanceManager::get();
    let Some(handle) = manager.get_handle(&id).await else {
        error!("Instancia {} no encontrada para toggle mod", id);
        return Err("Instancia no encontrada".to_string());
    };

    let mods_dir = handle.get_instance_dir().await.join("mods");
    let file_path = mods_dir.join(&filename);

    if !file_path.exists() {
        error!("Mod '{}' no encontrado en instancia {}", filename, id);
        return Err("Mod no encontrado".to_string());
    }

    let is_currently_disabled = filename.ends_with(".disabled");

    if enable && is_currently_disabled {
        let new_filename = filename
            .strip_suffix(".disabled")
            .ok_or("Error al procesar el nombre del archivo")?;
        let new_path = mods_dir.join(new_filename);
        tokio::fs::rename(file_path, new_path)
            .await
            .map_err(|e| e.to_string())?;
        info!("Mod '{}' habilitado en instancia {}", new_filename, id);
    } else if !enable && !is_currently_disabled {
        let new_filename = format!("{}.disabled", filename);
        let new_path = mods_dir.join(new_filename);
        tokio::fs::rename(file_path, new_path)
            .await
            .map_err(|e| e.to_string())?;
        info!("Mod '{}' deshabilitado en instancia {}", filename, id);
    }

    Ok(())
}

// ═══════════════════════════════════════════════════════════════════════════════
// Resource Packs
// ═══════════════════════════════════════════════════════════════════════════════

#[tauri::command]
pub async fn get_instance_resourcepacks(id: String) -> Vec<ModDto> {
    if let Err(e) = validate_uuid(&id) {
        warn!("{}", e);
        return Vec::new();
    }
    let manager = InstanceManager::get();
    let Some(handle) = manager.get_handle(&id).await else {
        warn!("Instancia {} no encontrada para listar resourcepacks", id);
        return Vec::new();
    };

    let resourcepacks_dir = handle.get_instance_dir().await.join("resourcepacks");

    let mut resourcepacks = Vec::new();
    if let Ok(entries) = std::fs::read_dir(resourcepacks_dir) {
        for entry in entries.flatten() {
            let path = entry.path();
            if path.is_file() {
                let Some(file_name) = path.file_name() else {
                    continue;
                };
                let filename = file_name.to_string_lossy().to_string();
                let path_clone = path.clone();
                let metadata = tokio::task::spawn_blocking(move || {
                    crate::services::AddonManager::get_resourcepack_info(&path_clone)
                })
                .await
                .unwrap_or(None);

                let (md_name, md_desc, md_icon) = match metadata {
                    Some(m) => (m.name, m.description, m.icon),
                    None => (filename.clone(), None, None),
                };

                resourcepacks.push(ModDto {
                    name: md_name,
                    filename,
                    version: None,
                    description: md_desc,
                    authors: None,
                    icon: md_icon,
                    enabled: true,
                });
            }
        }
    }
    resourcepacks.sort_by_key(|a| a.name.to_lowercase());
    info!("{} resourcepacks encontrados en instancia {}", resourcepacks.len(), id);
    resourcepacks
}

// ═══════════════════════════════════════════════════════════════════════════════
// Logs
// ═══════════════════════════════════════════════════════════════════════════════

#[tauri::command]
pub async fn get_instance_logs(id: String) -> Vec<String> {
    if let Err(e) = validate_uuid(&id) {
        warn!("{}", e);
        return Vec::new();
    }
    let manager = InstanceManager::get();
    let Some(handle) = manager.get_handle(&id).await else {
        warn!("Instancia {} no encontrada para listar logs", id);
        return Vec::new();
    };
    let logs_dir = handle.get_instance_dir().await.join("logs");
    let mut logs = Vec::new();
    if let Ok(entries) = std::fs::read_dir(logs_dir) {
        for entry in entries.flatten() {
            let path = entry.path();
            if path.is_file()
                && let Some(file_name) = path.file_name()
                && let Some(ext) = path.extension()
                // Mojang comprime en gzip los logs anteriores
                // asi q nomas nos tendremos que quedar con el latest :/
                //
                // TODO: Podriamos hacer que enves de enviar el filename entero enviar un stream
                // asi podriamos enviar un stream de datos descomprimidos
                && ext == "log"
            {
                logs.push(file_name.to_string_lossy().to_string());
            }
        }
    }
    logs.sort_by(|a, b| b.cmp(a));
    info!("{} archivos de log encontrados en instancia {}", logs.len(), id);
    logs
}
#[tauri::command]
pub async fn read_instance_log(id: String, filename: String) -> Result<String, String> {
    validate_uuid(&id)?;
    let manager = InstanceManager::get();
    let Some(handle) = manager.get_handle(&id).await else {
        warn!("Instancia {} no encontrada para leer log", id);
        return Err("Instancia no encontrada".to_string());
    };

    let log_path = handle.get_instance_dir().await.join("logs").join(&filename);
    if !log_path.exists() {
        warn!("Archivo de log '{}' no encontrado en instancia {}", filename, id);
        return Err("Archivo de registro no encontrado".to_string());
    }

    info!("Leyendo log '{}' de instancia {}", filename, id);
    tokio::fs::read_to_string(log_path)
        .await
        .map_err(|e| {
            error!("Error leyendo log '{}': {}", filename, e);
            e.to_string()
        })
}

// ═══════════════════════════════════════════════════════════════════════════════
// File Operations
// ═══════════════════════════════════════════════════════════════════════════════

#[tauri::command]
pub async fn delete_instance_file(
    id: String,
    sub_dir: String,
    filename: String,
) -> Result<(), String> {
    validate_uuid(&id)?;
    let manager = InstanceManager::get();
    let Some(handle) = manager.get_handle(&id).await else {
        warn!("Instancia {} no encontrada para eliminar archivo", id);
        return Err("Instancia no encontrada".to_string());
    };

    let instance_dir = handle.get_instance_dir().await;
    let sub_path = sanitize_sub_path(&instance_dir, Path::new(&sub_dir))?;
    let file_path = sub_path.join(&filename);
    info!("Eliminando archivo {:?} de instancia {}", file_path, id);
    if file_path.exists() {
        tokio::fs::remove_file(&file_path)
            .await
            .map_err(|e| {
                error!("Error eliminando archivo {:?}: {}", file_path, e);
                e.to_string()
            })?;
    } else {
        warn!("Archivo {:?} no existe, nada que eliminar", file_path);
    }
    Ok(())
}

#[tauri::command]
pub async fn add_instance_file(
    id: String,
    sub_dir: String,
    source_path: String,
) -> Result<(), String> {
    validate_uuid(&id)?;
    let manager = InstanceManager::get();
    let Some(handle) = manager.get_handle(&id).await else {
        warn!("Instancia {} no encontrada para agregar archivo", id);
        return Err("Instancia no encontrada".to_string());
    };

    let instance_dir = handle.get_instance_dir().await;
    let dest_dir = sanitize_sub_path(&instance_dir, Path::new(&sub_dir))?;
    info!("Agregando archivo '{}' a instancia {} en sub_dir '{}'", source_path, id, sub_dir);
    if !dest_dir.exists() {
        tokio::fs::create_dir_all(&dest_dir)
            .await
            .map_err(|e| e.to_string())?;
    }

    let src = PathBuf::from(&source_path);
    let filename = src.file_name().ok_or("Ruta de origen inválida")?;
    let dest_path = dest_dir.join(filename);

    tokio::fs::copy(&src, &dest_path)
        .await
        .map_err(|e| {
            error!("Error copiando archivo a {:?}: {}", dest_path, e);
            e.to_string()
        })?;
    info!("Archivo copiado a {:?}", dest_path);
    Ok(())
}
