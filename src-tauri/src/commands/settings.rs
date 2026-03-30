use crate::core::SettingsManager;
use tauri::command;

#[command]
pub fn get_settings() -> SettingsManager {
    let settings = SettingsManager::get().lock().unwrap();
    settings.clone()
}

#[command]
pub fn update_settings(new_settings: SettingsManager) -> Result<(), String> {
    let mut settings = SettingsManager::get().lock().unwrap();
    *settings = new_settings;
    settings.dirty = true;
    settings.save();
    Ok(())
}
