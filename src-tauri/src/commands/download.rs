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
