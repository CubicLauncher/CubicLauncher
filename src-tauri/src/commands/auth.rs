use crate::services::SettingsManager;
use launchwerk::auth::{MinecraftUser, microsoft::MicrosoftAuth};
use serde::Serialize;
use tauri::command;
use tracing::{info, warn};

#[derive(Serialize)]
pub struct DeviceCode {
    pub user_code: String,
    pub device_code: String,
    pub verification_uri: String,
    pub expires_in: u64,
    pub interval: u64,
}

#[command]
pub async fn get_device_code() -> Result<DeviceCode, String> {
    info!("Obteniendo código de dispositivo de Microsoft");
    let res = tokio::task::spawn_blocking(|| {
        MicrosoftAuth::default()
            .get_device_code()
            .map_err(|e| e.to_string())
    })
    .await
    .map_err(|e| e.to_string())??;

    info!("Código de dispositivo obtenido: user_code={}", res.user_code);
    Ok(DeviceCode {
        user_code: res.user_code,
        device_code: res.device_code,
        verification_uri: res.verification_uri,
        expires_in: res.expires_in,
        interval: res.interval,
    })
}

#[command]
pub async fn authenticate_with_device_code(
    device_code: String,
    interval: u64,
    expires_in: u64,
) -> Result<MinecraftUser, String> {
    info!("Autenticando con código de dispositivo...");
    let user = tokio::task::spawn_blocking(move || {
        MicrosoftAuth::default()
            .authenticate_with_device_code(&device_code, interval, expires_in)
            .map_err(|e| e.to_string())
    })
    .await
    .map_err(|e| format!("Task failed: {}", e))??;

    info!("Autenticación exitosa para {}", user.username);

    user.save_tokens()
        .map_err(|e| format!("Failed to save tokens securely: {}", e))?;

    SettingsManager::write(|settings| {
        settings.set_user(Some(user.clone()));
    })?;
    SettingsManager::save().await?;
    Ok(user)
}

#[command]
pub fn get_current_user() -> Result<Option<MinecraftUser>, String> {
    let settings = SettingsManager::read();
    let has_user = settings.user.is_some();
    if has_user {
        info!("Devolviendo usuario autenticado");
    } else {
        info!("No hay usuario autenticado");
    }
    Ok(settings.user.as_ref().map(|user| {
        let mut u = user.clone();
        if let Err(e) = u.load_tokens() {
            warn!("Error cargando tokens: {:?}", e);
        }
        u
    }))
}

#[command]
pub async fn logout() -> Result<(), String> {
    info!("Cerrando sesión de usuario");
    SettingsManager::write(|settings| {
        if let Some(user) = settings.user.as_ref() {
            info!("Eliminando tokens para {}", user.username);
            if let Err(e) = user.delete_tokens() {
                warn!("Error eliminando tokens: {:?}", e);
            }
        }

        settings.set_user(None);
    })?;
    SettingsManager::save().await?;
    info!("Sesión cerrada exitosamente");
    Ok(())
}
