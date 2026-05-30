use crate::core::HTTP;
use crate::services::InstanceManager;
use serde::{Deserialize, Serialize};
use tracing::{error, info};

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
    let instance_dir = handle.get_instance_dir().await;
    let mods_dir = instance_dir.join("mods");

    if !mods_dir.exists() {
        tokio::fs::create_dir_all(&mods_dir)
            .await
            .map_err(|e| e.to_string())?;
    }

    for mod_info in mods {
        let file_url = &mod_info.url;
        let filename = &mod_info.filename;
        let dest_path = mods_dir.join(filename);

        info!(
            "Descargando {} desde {} a {:?}",
            filename, file_url, dest_path
        );

        let mut response = match HTTP.get(file_url).send().await {
            Ok(res) => res,
            Err(e) => {
                error!("Error al iniciar descarga de {}: {}", filename, e);
                continue; // Optionally could fail the whole request, but we continue to download the rest
            }
        };

        if !response.status().is_success() {
            error!(
                "Error HTTP al descargar mod {}: {}",
                filename,
                response.status()
            );
            continue;
        }

        let mut dest_file = match tokio::fs::File::create(&dest_path).await {
            Ok(f) => f,
            Err(e) => {
                error!("Error al crear archivo {}: {}", filename, e);
                continue;
            }
        };

        use tokio::io::AsyncWriteExt;
        let mut failed = false;
        loop {
            match response.chunk().await {
                Ok(Some(chunk)) => {
                    if let Err(e) = dest_file.write_all(&chunk).await {
                        error!("Error al escribir archivo {}: {}", filename, e);
                        failed = true;
                        break;
                    }
                }
                Ok(None) => break,
                Err(e) => {
                    error!("Error de red descargando {}: {}", filename, e);
                    failed = true;
                    break;
                }
            }
        }

        if !failed {
            info!("Mod {} descargado exitosamente", filename);
        }
    }

    Ok(())
}
