use crate::core::{HTTP, PathManager};
use crate::services::DownloadQueue;
use serde::{Deserialize, Serialize};
use tokio::fs;
use tracing::{info, error};

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

#[derive(Debug, Serialize, Deserialize)]
pub struct FabricGameVersion {
    pub version: String,
    pub stable: bool,
}

#[derive(Debug, Serialize, Deserialize)]
pub struct FabricLoaderVersion {
    pub version: String,
    pub stable: bool,
}

#[derive(Debug, Serialize, Deserialize)]
pub struct FabricLoaderResponse {
    pub loader: FabricLoaderVersion,
}

#[derive(Debug, Serialize, Deserialize)]
pub struct FabricProfile {
    pub id: String,
    pub libraries: Vec<FabricLibrary>,
}

#[derive(Debug, Serialize, Deserialize)]
pub struct FabricLibrary {
    pub name: String,
    pub url: String,
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
    let response = HTTP.get(MOJANG_MANIFEST_URL)
        .send()
        .await
        .map_err(|e| format!("Error al obtener el manifiesto: {e}"))?;

    let bytes = response
        .bytes()
        .await
        .map_err(|e| format!("Error al leer bytes del manifiesto: {e}"))?;

    info!("Manifiesto descargado ({} bytes), cacheando en disco", bytes.len());
    fs::write(&path, &bytes)
        .await
        .map_err(|e| format!("Error al escribir manifiesto al disco: {e}"))?;

    let manifest: MinecraftManifest =
        serde_json::from_slice(&bytes).map_err(|e| format!("Error parseando JSON: {e}"))?;

    info!("Manifiesto parseado: {} versiones disponibles", manifest.versions.len());
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

fn read_cache_with_ttl<T: serde::de::DeserializeOwned>(
    path: &std::path::Path,
    ttl: std::time::Duration,
) -> Option<T> {
    let metadata = std::fs::metadata(path).ok()?;
    let modified = metadata.modified().ok()?;
    if let Ok(age) = modified.elapsed() {
        if age > ttl {
            return None;
        }
    }
    let data = std::fs::read(path).ok()?;
    serde_json::from_slice(&data).ok()
}

fn write_cache<T: serde::Serialize>(path: &std::path::Path, value: &T) {
    if let Ok(data) = serde_json::to_vec(value) {
        let _ = std::fs::write(path, data);
    }
}

#[tauri::command]
pub async fn get_fabric_versions() -> Result<Vec<FabricGameVersion>, String> {
    let cache_path = PathManager::get()
        .get_settings_dir()
        .join("fabric_versions_cache.cub");

    if let Some(cached) = read_cache_with_ttl::<Vec<FabricGameVersion>>(&cache_path, FABRIC_CACHE_TTL) {
        info!("Usando caché de versiones de Fabric ({} versiones)", cached.len());
        return Ok(cached);
    }

    info!("Cache de Fabric expirado o ausente, descargando desde meta.fabricmc.net");
    let url = "https://meta.fabricmc.net/v2/versions/game";
    let response = HTTP.get(url)
        .send()
        .await
        .map_err(|e| format!("Error al obtener versiones de Fabric: {}", e))?;

    let versions = response
        .json::<Vec<FabricGameVersion>>()
        .await
        .map_err(|e| format!("Error al parsear versiones de Fabric: {}", e))?;

    info!("{} versiones de Fabric obtenidas, cacheando en disco", versions.len());
    write_cache(&cache_path, &versions);

    Ok(versions)
}

#[tauri::command]
pub async fn download_fabric(game_version: String) -> Result<(), String> {
    info!("Iniciando descarga de Fabric para Minecraft {}", game_version);

    // 1. Obtener el último loader estable para esa versión
    info!("Obteniendo último loader de Fabric para versión {}", game_version);
    let loader_url = format!(
        "https://meta.fabricmc.net/v2/versions/loader/{}",
        game_version
    );
    let response = HTTP.get(&loader_url)
        .send()
        .await
        .map_err(|e| format!("Error al obtener loaders: {}", e))?;

    let loaders = response
        .json::<Vec<FabricLoaderResponse>>()
        .await
        .map_err(|e| format!("Error al parsear loaders: {}", e))?;

    let latest_loader = loaders
        .first()
        .ok_or_else(|| "No se encontró ningún loader para esta versión".to_string())?;

    let loader_version = &latest_loader.loader.version;
    let fabric_version_id = format!("fabric-loader-{}-{}", loader_version, game_version);
    info!("Loader seleccionado: {}, ID: {}", loader_version, fabric_version_id);

    // 2. Descargar el perfil JSON
    info!("Descargando perfil JSON de Fabric");
    let profile_url = format!(
        "https://meta.fabricmc.net/v2/versions/loader/{}/{}/profile/json",
        game_version, loader_version
    );

    let profile_response = HTTP.get(&profile_url)
        .send()
        .await
        .map_err(|e| format!("Error al descargar el perfil de Fabric: {}", e))?;

    let profile_json: String = profile_response
        .text()
        .await
        .map_err(|e| format!("Error al leer el JSON del perfil: {}", e))?;

    let profile: FabricProfile = serde_json::from_str(&profile_json)
        .map_err(|e| format!("Error al parsear el JSON del perfil: {}", e))?;

    info!("Perfil JSON parseado: {} librerías declaradas", profile.libraries.len());

    // 3. Guardar el JSON en shared/versions/ID/ID.json
    let shared_dir = PathManager::get().get_shared_dir();
    let version_dir = shared_dir.join("versions").join(&fabric_version_id);
    tokio::fs::create_dir_all(&version_dir)
        .await
        .map_err(|e| format!("Error al crear el directorio de la versión: {}", e))?;

    let json_path = version_dir.join(format!("{}.json", fabric_version_id));
    tokio::fs::write(&json_path, &profile_json)
        .await
        .map_err(|e| format!("Error al guardar el JSON: {}", e))?;
    info!("Perfil JSON guardado en {:?}", json_path);

    // 4. Descargar librerías de Fabric
    info!("Descargando librerías de Fabric");
    let lib_base_dir = shared_dir.join("libraries");
    let mut downloaded = 0u32;
    let mut skipped = 0u32;
    for lib in profile.libraries {
        let parts: Vec<&str> = lib.name.split(':').collect();
        if parts.len() != 3 {
            skipped += 1;
            continue;
        }

        let group = parts[0].replace('.', "/");
        let artifact = parts[1];
        let version = parts[2];

        let rel_path = format!(
            "{}/{}/{}/{}-{}.jar",
            group, artifact, version, artifact, version
        );
        let dest_path = lib_base_dir.join(&rel_path);

        let exists = tokio::fs::try_exists(&dest_path).await.unwrap_or_default();

        if !exists {
            if let Some(parent) = dest_path.parent() {
                let _ = tokio::fs::create_dir_all(parent).await;
            }

            let download_url = format!("{}{}", lib.url, rel_path);
            if let Ok(res) = HTTP.get(&download_url).send().await
                && let Ok(bytes) = res.bytes().await
            {
                let _ = tokio::fs::write(dest_path, bytes).await;
                downloaded += 1;
            } else {
                error!("Error descargando librería: {}", lib.name);
            }
        } else {
            skipped += 1;
        }
    }
    info!("Librerías de Fabric: {} descargadas, {} ya existentes/saltadas", downloaded, skipped);

    {
        let d_queue = crate::services::DownloadQueue::get();
        info!("Encolando {} para descarga completa", fabric_version_id);
        d_queue.enqueue(fabric_version_id).await;
    }

    Ok(())
}
#[tauri::command]
pub async fn refresh_versions() -> Result<Vec<MinecraftVersion>, String> {
    info!("Forzando actualización del manifiesto de versiones");
    let path = PathManager::get()
        .get_settings_dir()
        .join("manifest_cache.cub");

    if path.exists() {
        tokio::fs::remove_file(&path).await.map_err(|e| {
            format!("Error al eliminar la caché del manifiesto: {}", e)
        })?;
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
