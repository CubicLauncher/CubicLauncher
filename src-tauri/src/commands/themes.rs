use crate::core::{AppEvent, PathManager, emit};
use crate::services::SettingsManager;
use crate::theme_watcher::ThemeWatcher;
use serde::{Deserialize, Serialize};
use tauri::command;

#[derive(Debug, Serialize, Deserialize, Clone)]
pub struct ThemeFile {
    pub name: String,
    #[serde(default)]
    pub author: String,
    #[serde(default)]
    pub r#type: String,
    pub variables: std::collections::HashMap<String, String>,
    #[serde(default)]
    pub bg_image: Option<String>,
    #[serde(default)]
    pub bg_image_blur: Option<String>,
    #[serde(default)]
    pub bg_image_opacity: Option<f64>,
}

#[derive(Debug, Serialize, Clone)]
pub struct ThemeEntry {
    pub id: String,
    pub name: String,
    pub author: String,
    pub r#type: String,
}

#[command]
pub fn list_themes() -> Result<Vec<ThemeEntry>, String> {
    let themes_dir = PathManager::get().get_themes_dir().to_path_buf();
    let mut themes = Vec::new();

    let entries = match std::fs::read_dir(&themes_dir) {
        Ok(e) => e,
        Err(_) => return Ok(themes),
    };

    for entry in entries.flatten() {
        let path = entry.path();
        if !path.is_dir() {
            continue;
        }
        let theme_file = path.join("theme.json");
        if !theme_file.exists() {
            continue;
        }
        let id = match path.file_name() {
            Some(name) => name.to_string_lossy().to_string(),
            None => continue,
        };
        let content = match std::fs::read_to_string(&theme_file) {
            Ok(c) => c,
            Err(_) => continue,
        };
        let theme: ThemeFile = match serde_json::from_str(&content) {
            Ok(t) => t,
            Err(_) => continue,
        };
        themes.push(ThemeEntry {
            id,
            name: theme.name,
            author: theme.author,
            r#type: theme.r#type,
        });
    }

    Ok(themes)
}

#[command]
pub fn get_user_theme(id: String) -> Result<ThemeFile, String> {
    let theme_path = PathManager::get()
        .get_themes_dir()
        .join(&id)
        .join("theme.json");

    let content = std::fs::read_to_string(&theme_path)
        .map_err(|e| format!("No se pudo leer el theme '{}': {}", id, e))?;

    let mut theme: ThemeFile =
        serde_json::from_str(&content).map_err(|e| format!("Theme '{}' inválido: {}", id, e))?;

    // Resolver bg_image relativa al directorio del theme si no es absoluta
    if let Some(ref bg) = theme.bg_image {
        if !bg.starts_with('/') && !bg.starts_with("file:") {
            let abs_path = PathManager::get()
                .get_themes_dir()
                .join(&id)
                .join(bg);
            theme.bg_image = Some(abs_path.to_string_lossy().to_string());
        }
    }

    Ok(theme)
}

#[command]
pub async fn set_theme(id: String) -> Result<(), String> {
    SettingsManager::write(|s| {
        s.theme = id.clone();
        s.dirty = true;
    })?;

    SettingsManager::save().await?;

    if let Some(dir) = id.strip_prefix("user:") {
        ThemeWatcher::watch(Some(dir.to_string()));
    } else {
        ThemeWatcher::watch(None);
    }

    emit(AppEvent::ThemeChanged { id });

    Ok(())
}

#[command]
pub fn get_current_theme() -> Result<String, String> {
    Ok(SettingsManager::read().theme.clone())
}

#[command]
pub fn get_themes_dir_path() -> Result<String, String> {
    Ok(PathManager::get()
        .get_themes_dir()
        .to_string_lossy()
        .to_string())
}

#[command]
pub fn import_theme(source_path: String) -> Result<ThemeEntry, String> {
    let source = std::path::Path::new(&source_path);
    if !source.exists() {
        return Err("El archivo no existe".to_string());
    }

    let content =
        std::fs::read_to_string(source).map_err(|e| format!("Error al leer el archivo: {}", e))?;

    let theme_file: ThemeFile = serde_json::from_str(&content)
        .map_err(|e| format!("El archivo no es un theme válido: {}", e))?;

    let theme_id = theme_file.name.to_lowercase().replace(' ', "_");
    let theme_dir = PathManager::get().get_themes_dir().join(&theme_id);

    if theme_dir.exists() {
        return Err(format!("Ya existe un theme con el nombre '{}'", theme_file.name));
    }

    std::fs::create_dir_all(&theme_dir)
        .map_err(|e| format!("No se pudo crear el directorio del theme: {}", e))?;

    let dest_path = theme_dir.join("theme.json");
    std::fs::write(&dest_path, &content)
        .map_err(|e| format!("No se pudo copiar el theme: {}", e))?;

    // Si el bg_image es una ruta relativa, intentar copiar el archivo
    if let Some(ref bg) = theme_file.bg_image {
        if !bg.starts_with('/') && !bg.starts_with("file:") {
            let bg_source = source.parent().map(|p| p.join(bg));
            if let Some(bg_src) = bg_source {
                if bg_src.exists() {
                    let bg_dest = theme_dir.join(bg);
                    let _ = std::fs::copy(&bg_src, &bg_dest);
                }
            }
        }
    }

    Ok(ThemeEntry {
        id: theme_id,
        name: theme_file.name,
        author: theme_file.author,
        r#type: "user".to_string(),
    })
}
