use crate::core::PathManager;
use crate::services::DownloadQueue;
use serde::{Deserialize, Serialize};
use tokio::fs;

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

    let response = reqwest::get(MOJANG_MANIFEST_URL)
        .await
        .map_err(|e| format!("Error al obtener el manifiesto: {e}"))?;

    let bytes = response
        .bytes()
        .await
        .map_err(|e| format!("Error al leer bytes del manifiesto: {e}"))?;

    fs::write(&path, &bytes)
        .await
        .map_err(|e| format!("Error al escribir manifiesto al disco: {e}"))?;

    let manifest: MinecraftManifest =
        serde_json::from_slice(&bytes).map_err(|e| format!("Error parseando JSON: {e}"))?;

    Ok(manifest.versions)
}

#[tauri::command]
pub async fn get_available_versions() -> Result<Vec<MinecraftVersion>, String> {
    let path = PathManager::get()
        .get_settings_dir()
        .join("manifest_cache.cub");

    if tokio::fs::try_exists(&path).await.unwrap_or(false) {
        match tokio::fs::read(path).await {
            Ok(d) => {
                let manifest: MinecraftManifest = serde_json::from_slice(&d)
                    .map_err(|e| format!("Error parseando manifest: {}", e))?;
                Ok(manifest.versions)
            }
            Err(_) => download_manifest().await,
        }
    } else {
        download_manifest().await
    }
}

#[tauri::command]
pub async fn get_fabric_versions() -> Result<Vec<FabricGameVersion>, String> {
    let url = "https://meta.fabricmc.net/v2/versions/game";
    let response = reqwest::get(url)
        .await
        .map_err(|e| format!("Error al obtener versiones de Fabric: {}", e))?;

    let versions = response
        .json::<Vec<FabricGameVersion>>()
        .await
        .map_err(|e| format!("Error al parsear versiones de Fabric: {}", e))?;

    Ok(versions)
}

#[tauri::command]
pub async fn download_fabric(game_version: String) -> Result<(), String> {
    // 1. Obtener el último loader estable para esa versión
    let loader_url = format!(
        "https://meta.fabricmc.net/v2/versions/loader/{}",
        game_version
    );
    let response = reqwest::get(&loader_url)
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

    // 2. Descargar el perfil JSON
    let profile_url = format!(
        "https://meta.fabricmc.net/v2/versions/loader/{}/{}/profile/json",
        game_version, loader_version
    );

    let profile_response = reqwest::get(&profile_url)
        .await
        .map_err(|e| format!("Error al descargar el perfil de Fabric: {}", e))?;

    let profile_json: String = profile_response
        .text()
        .await
        .map_err(|e| format!("Error al leer el JSON del perfil: {}", e))?;

    let profile: FabricProfile = serde_json::from_str(&profile_json)
        .map_err(|e| format!("Error al parsear el JSON del perfil: {}", e))?;

    // 3. Guardar el JSON en shared/versions/ID/ID.json
    let shared_dir = PathManager::get().get_shared_dir();
    let version_dir = shared_dir.join("versions").join(&fabric_version_id);
    tokio::fs::create_dir_all(&version_dir)
        .await
        .map_err(|e| format!("Error al crear el directorio de la versión: {}", e))?;

    let json_path = version_dir.join(format!("{}.json", fabric_version_id));
    tokio::fs::write(json_path, &profile_json)
        .await
        .map_err(|e| format!("Error al guardar el JSON: {}", e))?;

    // 4. Descargar librerías de Fabric
    let lib_base_dir = shared_dir.join("libraries");
    for lib in profile.libraries {
        let parts: Vec<&str> = lib.name.split(':').collect();
        if parts.len() != 3 {
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
            if let Ok(res) = reqwest::get(&download_url).await
                && let Ok(bytes) = res.bytes().await
            {
                let _ = tokio::fs::write(dest_path, bytes).await;
            }
        }
    }

    {
        let d_queue = crate::services::DownloadQueue::get();
        d_queue.enqueue(game_version).await;
        d_queue.enqueue(fabric_version_id).await;
    }

    Ok(())
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
