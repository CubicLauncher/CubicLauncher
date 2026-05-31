use aqua::{DownloadItemSpec, DownloadManager, GenericBatch};
use serde::{Deserialize, Serialize};
use tracing::info;

use crate::core::PathManager;
use crate::services::InstanceManager;

#[derive(Debug, Serialize, Deserialize)]
pub struct ModDownloadInfo {
    pub url: String,
    pub filename: String,
}

#[tauri::command]
pub async fn download_mods(instance_id: String, mods: Vec<ModDownloadInfo>) -> Result<(), String> {
    let manager = InstanceManager::get();
    let handle = manager
        .get_handle(&instance_id)
        .await
        .ok_or("Instancia no encontrada")?;
    let mods_dir = handle.get_instance_dir().await.join("mods");

    tokio::fs::create_dir_all(&mods_dir)
        .await
        .map_err(|e| format!("Error creando directorio mods: {}", e))?;

    let count = mods.len();
    let items: Vec<DownloadItemSpec> = mods
        .into_iter()
        .map(|m| {
            info!(
                "Encolando mod: {} -> {:?}",
                m.filename,
                mods_dir.join(&m.filename)
            );
            DownloadItemSpec::new(m.url, mods_dir.join(m.filename), "mod")
        })
        .collect();

    let batch = GenericBatch::new(format!("mods-{}", instance_id), items);

    let shared_dir = PathManager::get().get_shared_dir().to_path_buf();
    let dm = DownloadManager::new(shared_dir);
    let handle = dm
        .prepare_batch(Box::new(batch))
        .await
        .map_err(|e| format!("Error al preparar descarga de mods: {}", e))?;

    handle
        .download_all(None)
        .await
        .map_err(|e| format!("Error al descargar mods: {}", e))?;

    info!("{} mods descargados correctamente en {:?}", count, mods_dir);
    Ok(())
}
