use crate::core::{HTTP, PathManager};
use crate::services::DownloadQueue;
use aqua::{DownloadManager, FabricBatch};
use serde::{Deserialize, Serialize};
use tokio::fs;
use tracing::{info, warn};

const MOJANG_MANIFEST_URL: &str = "https://piston-meta.mojang.com/mc/game/version_manifest_v2.json";

#[derive(Debug, Serialize, Deserialize)]
pub struct MinecraftVersion {
    pub id: String,
    #[serde(rename = "type")]
    pub version_type: String,
    pub url: String,
    pub time: String,
    #[serde(rename = "releaseTime")]
    pub release_time: String,
}

#[derive(Debug, Serialize, Deserialize)]
pub struct MinecraftManifest {
    pub latest: LatestVersions,
    pub versions: Vec<MinecraftVersion>,
}

#[derive(Debug, Serialize, Deserialize)]
pub struct LatestVersions {
    pub release: String,
    pub snapshot: String,
}

#[derive(Debug, Serialize, Deserialize, Clone)]
pub struct FabricGameVersion {
    pub version: String,
    pub stable: bool,
}

#[tauri::command]
pub async fn add_to_queue(version: String) {
    DownloadQueue::get().enqueue(version).await
}

pub async fn download_manifest() -> Result<Vec<MinecraftVersion>, String> {
    let path = PathManager::get()
        .get_settings_dir()
        .join("manifest_cache.cub");

    info!("Descargando manifiesto de versiones desde Mojang");
    let response = HTTP
        .get(MOJANG_MANIFEST_URL)
        .send()
        .await
        .map_err(|e| format!("Error al obtener el manifiesto: {e}"))?;

    let bytes = response
        .bytes()
        .await
        .map_err(|e| format!("Error al leer bytes del manifiesto: {e}"))?;

    info!(
        "Manifiesto descargado ({} bytes), cacheando en disco",
        bytes.len()
    );
    fs::write(&path, &bytes)
        .await
        .map_err(|e| format!("Error al escribir manifiesto al disco: {e}"))?;

    let manifest: MinecraftManifest =
        serde_json::from_slice(&bytes).map_err(|e| format!("Error parseando JSON: {e}"))?;

    info!(
        "Manifiesto parseado: {} versiones disponibles",
        manifest.versions.len()
    );
    Ok(manifest.versions)
}

#[tauri::command]
pub async fn get_available_versions() -> Result<Vec<MinecraftVersion>, String> {
    let path = PathManager::get()
        .get_settings_dir()
        .join("manifest_cache.cub");

    if tokio::fs::try_exists(&path).await.unwrap_or(false) {
        info!("Usando manifiesto cacheado");
        match tokio::fs::read(path).await {
            Ok(d) => {
                let manifest: MinecraftManifest = serde_json::from_slice(&d)
                    .map_err(|e| format!("Error parseando manifest: {}", e))?;
                info!("{} versiones cargadas desde caché", manifest.versions.len());
                Ok(manifest.versions)
            }
            Err(_) => {
                info!("Error leyendo caché, descargando manifiesto nuevo");
                download_manifest().await
            }
        }
    } else {
        info!("No hay caché de manifiesto, descargando");
        download_manifest().await
    }
}

const FABRIC_CACHE_TTL: std::time::Duration = std::time::Duration::from_secs(3600);

#[tauri::command]
pub async fn get_fabric_versions() -> Result<Vec<FabricGameVersion>, String> {
    let cache_path = PathManager::get()
        .get_settings_dir()
        .join("fabric_versions_cache.cub");

    let cached = tokio::task::spawn_blocking({
        let path = cache_path.clone();
        move || -> Option<Vec<FabricGameVersion>> {
            let metadata = std::fs::metadata(&path).ok()?;
            let modified = metadata.modified().ok()?;
            if let Ok(age) = modified.elapsed()
                && age > FABRIC_CACHE_TTL
            {
                return None;
            }
            let data = std::fs::read(&path).ok()?;
            serde_json::from_slice(&data).ok()
        }
    })
    .await
    .unwrap_or(None);

    if let Some(versions) = cached {
        info!(
            "Usando caché de versiones de Fabric ({} versiones)",
            versions.len()
        );
        return Ok(versions);
    }

    info!("Cache de Fabric expirado o ausente, descargando desde meta.fabricmc.net");
    let url = "https://meta.fabricmc.net/v2/versions/game";
    let response = HTTP
        .get(url)
        .send()
        .await
        .map_err(|e| format!("Error al obtener versiones de Fabric: {}", e))?;

    let versions = response
        .json::<Vec<FabricGameVersion>>()
        .await
        .map_err(|e| format!("Error al parsear versiones de Fabric: {}", e))?;

    info!(
        "{} versiones de Fabric obtenidas, cacheando en disco",
        versions.len()
    );

    let write_path = cache_path;
    let write_versions = versions.clone();
    tokio::task::spawn_blocking(move || {
        if let Ok(data) = serde_json::to_vec(&write_versions)
            && let Err(e) = std::fs::write(&write_path, &data)
        {
            warn!("Error escribiendo caché en {:?}: {}", write_path, e);
        }
    })
    .await
    .ok();

    Ok(versions)
}

#[tauri::command]
pub async fn download_fabric(
    game_version: String,
    loader_version: Option<String>,
) -> Result<(), String> {
    info!(
        "Iniciando descarga de Fabric para Minecraft {}",
        game_version
    );

    let loader_version = if let Some(specific) = loader_version {
        specific
    } else {
        FabricBatch::resolve_latest_loader(&game_version)
            .await
            .map_err(|e| format!("Error al obtener loaders: {}", e))?
    };

    let fabric_version_id = format!("fabric-loader-{}-{}", loader_version, game_version);
    info!("Loader: {}, ID: {}", loader_version, fabric_version_id);

    let shared_dir = PathManager::get().get_shared_dir();
    let json_path = shared_dir
        .join("versions")
        .join(&fabric_version_id)
        .join(format!("{}.json", fabric_version_id));

    if tokio::fs::try_exists(&json_path).await.unwrap_or(false) {
        info!(
            "Fabric {} ya instalado, encolando assets",
            fabric_version_id
        );
        DownloadQueue::get().enqueue(fabric_version_id).await;
        return Ok(());
    }

    let batch = FabricBatch::new(shared_dir, &game_version, &loader_version)
        .await
        .map_err(|e| format!("Error al crear perfil Fabric: {}", e))?;

    let manager = DownloadManager::new(shared_dir.to_path_buf());
    let handle = manager
        .prepare_batch(Box::new(batch))
        .await
        .map_err(|e| format!("Error al preparar descarga Fabric: {}", e))?;

    handle
        .download_all(None)
        .await
        .map_err(|e| format!("Error al descargar Fabric: {}", e))?;

    info!("Fabric {} descargado correctamente", fabric_version_id);
    DownloadQueue::get().enqueue(fabric_version_id).await;

    Ok(())
}
#[tauri::command]
pub async fn refresh_versions() -> Result<Vec<MinecraftVersion>, String> {
    info!("Forzando actualización del manifiesto de versiones");
    let path = PathManager::get()
        .get_settings_dir()
        .join("manifest_cache.cub");

    if path.exists() {
        tokio::fs::remove_file(&path)
            .await
            .map_err(|e| format!("Error al eliminar la caché del manifiesto: {}", e))?;
        info!("Caché de manifiesto eliminado: {:?}", path);
    }

    download_manifest().await
}

#[derive(serde::Serialize)]
pub struct DownloadQueueItem {
    pub version: String,
    pub status: String,
    pub current: u64,
    pub total: u64,
}

#[tauri::command]
pub async fn get_download_queue() -> Vec<DownloadQueueItem> {
    let queue = crate::services::DownloadQueue::get();
    let handles = queue.get_active_downloads().await;

    handles
        .iter()
        .map(|h| {
            let (current, total) = h.get_progress();
            DownloadQueueItem {
                version: h.version.clone(),
                status: format!("{:?}", h.get_status()).to_lowercase(),
                current,
                total,
            }
        })
        .collect()
}
