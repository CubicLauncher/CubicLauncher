use crate::core::LauncherWrapper;
use serde::{Deserialize, Serialize};

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
pub struct VersionSummary {
    pub id: String,
    #[serde(rename = "type")]
    pub version_type: String,
    #[serde(rename = "releaseTime")]
    pub release_time: String,
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
    LauncherWrapper::get()
        .lock()
        .await
        .queue_download(version)
        .await;
}

#[tauri::command]
pub async fn get_available_versions() -> Result<Vec<VersionSummary>, String> {
    let url = "https://piston-meta.mojang.com/mc/game/version_manifest_v2.json";
    let response = reqwest::get(url)
        .await
        .map_err(|e| format!("Error al obtener el manifiesto: {}", e))?;
    
    let manifest = response
        .json::<MinecraftManifest>()
        .await
        .map_err(|e| format!("Error al parsear el manifiesto: {}", e))?;
    
    let summary: Vec<VersionSummary> = manifest.versions.into_iter().map(|v| VersionSummary {
        id: v.id,
        version_type: v.version_type,
        release_time: v.release_time,
    }).collect();
    
    Ok(summary)
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
    let loader_url = format!("https://meta.fabricmc.net/v2/versions/loader/{}", game_version);
    let response = reqwest::get(&loader_url)
        .await
        .map_err(|e| format!("Error al obtener loaders: {}", e))?;
    
    let loaders = response
        .json::<Vec<FabricLoaderResponse>>()
        .await
        .map_err(|e| format!("Error al parsear loaders: {}", e))?;
    
    let latest_loader = loaders.first()
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
    let shared_dir = crate::core::PathManager::get().get_shared_dir();
    let version_dir = shared_dir.join("versions").join(&fabric_version_id);
    std::fs::create_dir_all(&version_dir)
        .map_err(|e| format!("Error al crear el directorio de la versión: {}", e))?;
    
    let json_path = version_dir.join(format!("{}.json", fabric_version_id));
    std::fs::write(json_path, &profile_json)
        .map_err(|e| format!("Error al guardar el JSON: {}", e))?;
    
    // 4. Descargar librerías de Fabric
    let lib_base_dir = shared_dir.join("libraries");
    for lib in profile.libraries {
        let parts: Vec<&str> = lib.name.split(':').collect();
        if parts.len() != 3 { continue; }
        
        let group = parts[0].replace('.', "/");
        let artifact = parts[1];
        let version = parts[2];
        
        let rel_path = format!("{}/{}/{}/{}-{}.jar", group, artifact, version, artifact, version);
        let dest_path = lib_base_dir.join(&rel_path);
        
        if !dest_path.exists() {
            if let Some(parent) = dest_path.parent() {
                std::fs::create_dir_all(parent).ok();
            }
            
            let download_url = format!("{}{}", lib.url, rel_path);
            if let Ok(res) = reqwest::get(&download_url).await {
                if let Ok(bytes) = res.bytes().await {
                    std::fs::write(dest_path, bytes).ok();
                }
            }
        }
    }
    
    // 5. Agregar a la cola (LauncherWrapper se encargará de bajar la base si falta)
    // Primero aseguramos la base
    LauncherWrapper::get()
        .lock()
        .await
        .queue_download(game_version)
        .await;
        
    LauncherWrapper::get()
        .lock()
        .await
        .queue_download(fabric_version_id)
        .await;
    
    Ok(())
}
