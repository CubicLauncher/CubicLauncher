use crate::core::SettingsManager;
use claunch_rs::auth::microsoft::MicrosoftAuth;
use claunch_rs::MinecraftUser;
use tauri::command;
use serde::Serialize;

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
    let res = tokio::task::spawn_blocking(|| {
        MicrosoftAuth::get_device_code().map_err(|e| e.to_string())
    })
    .await
    .map_err(|e| e.to_string())??;

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
    // 1. Wait for Microsoft Authentication
    let user = tokio::task::spawn_blocking(move || {
        MicrosoftAuth::authenticate_with_device_code(&device_code, interval, expires_in)
            .map_err(|e| e.to_string())
    })
    .await
    .map_err(|e| format!("Task failed: {}", e))??;

    // 2. Securely store the authentication tokens
    user.save_tokens()
        .map_err(|e| format!("Failed to save tokens securely: {}", e))?;

    // 3. Update the global launcher settings
    let mut settings = SettingsManager::get().lock().unwrap();
    settings.set_user(Some(user.clone()));
    settings.save();

    Ok(user)
}

#[command]
pub fn get_current_user() -> Option<MinecraftUser> {
    let settings = SettingsManager::get().lock().unwrap();
    settings.user.as_ref().map(|user| {
        let mut u = user.clone();
        let _ = u.load_tokens();
        u
    })
}

#[command]
pub fn logout() {
    let mut settings = SettingsManager::get().lock().unwrap();
    
    // Securely delete tokens before clearing the user
    if let Some(user) = settings.user.as_ref() {
        let _ = user.delete_tokens();
    }
    
    settings.set_user(None);
    settings.save();
}
