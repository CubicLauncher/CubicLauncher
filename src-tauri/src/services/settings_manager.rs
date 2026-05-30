use crate::core::{AppError, CoreError, FsError, PathManager, emit};
use launchwerk::auth::MinecraftUser;
use parking_lot::{RwLock, RwLockReadGuard};
use serde::{Deserialize, Serialize};
use std::collections::HashMap;
use std::path::{Path, PathBuf};
use std::sync::LazyLock;
use tokio::fs;
use tracing::{error, info, warn};

// ── Static global ─────────────────────────────────────────────────────────────

static SETTINGS: LazyLock<RwLock<SettingsManager>> =
    LazyLock::new(|| RwLock::new(SettingsManager::load()));

// ── Defaults (serde) ──────────────────────────────────────────────────────────

fn default_username() -> String {
    String::from("steve")
}
fn default_min_mem() -> u32 {
    1
}
fn default_max_mem() -> u32 {
    2
}
fn default_lang() -> String {
    String::from("es")
}
fn default_true() -> bool {
    true
}
fn default_theme() -> String {
    String::from("dark")
}

// ── SettingsManager ───────────────────────────────────────────────────────────

/// Configuración persistida del launcher.
/// La memoria se almacena en GB.
#[derive(Debug, Deserialize, Serialize, Clone)]
pub struct SettingsManager {
    #[serde(default = "default_username")]
    pub username: String,
    #[serde(default)]
    pub user: Option<MinecraftUser>,
    #[serde(default = "default_min_mem")]
    pub min_memory: u32,
    #[serde(default = "default_max_mem")]
    pub max_memory: u32,
    #[serde(default)]
    pub jre8_path: PathBuf,
    #[serde(default)]
    pub jre17_path: PathBuf,
    #[serde(default)]
    pub jre21_path: PathBuf,
    #[serde(default)]
    pub jre25_path: PathBuf,
    #[serde(default = "default_lang")]
    pub language: String,
    #[serde(default = "default_true")]
    pub auto_updates: bool,
    #[serde(default)]
    pub show_error_console: bool,
    #[serde(default = "default_true")]
    pub close_launcher_on_play: bool,
    #[serde(default)]
    pub show_snapshots: bool,
    #[serde(default)]
    pub show_alpha: bool,
    #[serde(default)]
    pub jvm_args: String,
    #[serde(default)]
    pub env_vars: HashMap<String, String>,
    #[serde(default = "default_theme")]
    pub theme: String,
    #[serde(default = "default_true")]
    pub discord_presence: bool,
    #[serde(skip)]
    pub dirty: bool,
}

impl Default for SettingsManager {
    fn default() -> Self {
        Self {
            username: String::from("steve"),
            user: None,
            min_memory: 1,
            max_memory: 2,
            jre8_path: PathBuf::new(),
            jre17_path: PathBuf::new(),
            jre21_path: PathBuf::new(),
            jre25_path: PathBuf::new(),
            language: String::from("es"),
            auto_updates: true,
            show_error_console: false,
            close_launcher_on_play: true,
            show_snapshots: false,
            show_alpha: false,
            jvm_args: String::new(),
            env_vars: HashMap::new(),
            theme: String::from("dark"),
            discord_presence: true,
            dirty: true,
        }
    }
}

impl SettingsManager {
    pub fn read() -> RwLockReadGuard<'static, SettingsManager> {
        SETTINGS.read()
    }

    pub fn write(f: impl FnOnce(&mut SettingsManager)) -> Result<(), CoreError> {
        let mut settings = SETTINGS.write();
        f(&mut settings);
        Ok(())
    }

    pub fn snapshot() -> SettingsManager {
        SETTINGS.read().clone()
    }

    // ── Getters ───────────────────────────────────────────────────────────────

    pub fn get_min_memory(&self) -> u32 {
        self.min_memory
    }
    pub fn get_max_memory(&self) -> u32 {
        self.max_memory
    }
    pub fn get_jre8_path(&self) -> &Path {
        &self.jre8_path
    }
    pub fn get_jre17_path(&self) -> &Path {
        &self.jre17_path
    }
    pub fn get_jre21_path(&self) -> &Path {
        &self.jre21_path
    }
    pub fn get_jre25_path(&self) -> &Path {
        &self.jre25_path
    }
    pub fn get_minecraft_user(&self) -> MinecraftUser {
        match &self.user {
            Some(user) => {
                let mut u = user.clone();
                if let Err(e) = u.load_tokens() {
                    warn!("Error cargando tokens: {:?}", e);
                }
                u
            }
            None => MinecraftUser::cracked(&self.username),
        }
    }

    // ── Setters ───────────────────────────────────────────────────────────────

    pub fn set_user(&mut self, user: Option<MinecraftUser>) {
        if let Some(ref u) = user {
            self.username = u.username.clone();
        }
        self.user = user;
        self.dirty = true;
    }

    // ── Persistencia ──────────────────────────────────────────────────────────

    /// Serializa y escribe a disco.
    /// Extrae el contenido con el Mutex tomado, luego escribe fuera del scope
    /// para minimizar el tiempo que el lock bloquea otros hilos.
    pub async fn save() -> Result<(), AppError> {
        let (content, path, was_dirty) = {
            let settings = SETTINGS.read();
            if !settings.dirty {
                (String::new(), PathBuf::new(), false)
            } else {
                let content = serde_json::to_string(&*settings)
                    .map_err(|e| CoreError::Serialize(e.to_string()))?;
                let path = PathManager::get().get_settings_dir().join("settings.cub");
                (content, path, true)
            }
        };

        if !was_dirty {
            return Ok(());
        }

        let parent = path.parent().ok_or_else(|| {
            AppError::CoreError(CoreError::Serialize(format!(
                "Ruta de settings inválida: {}",
                path.display()
            )))
        })?;

        fs::create_dir_all(parent).await.map_err(|e| {
            AppError::Fs(FsError::CreateDir {
                path: parent.to_string_lossy().to_string(),
                source: e,
            })
        })?;

        fs::write(&path, content).await.map_err(|e| {
            AppError::Fs(FsError::WriteFile {
                path: path.to_string_lossy().to_string(),
                source: e,
            })
        })?;

        // Esto solo se ejecuta si la operacion de escritura paso sin errores.
        {
            let mut settings = SETTINGS.write();
            settings.dirty = false;
        }

        info!("Configuración guardada en {:?}", path);
        emit(crate::core::AppEvent::STChanged);
        Ok(())
    }

    pub fn load() -> Self {
        let path = PathManager::get().get_settings_dir().join("settings.cub");

        if !path.exists() {
            info!("No hay archivo de configuración, usando valores por defecto");
            return Self::default();
        }

        let content = match std::fs::read_to_string(&path) {
            Ok(c) => c,
            Err(e) => {
                error!("Error al leer la configuración desde {:?}: {}", path, e);
                return Self::default();
            }
        };

        match serde_json::from_str::<Self>(&content) {
            Ok(mut settings) => {
                settings.migrate();
                info!("Configuración cargada desde {:?}", path);
                settings
            }
            Err(e) => {
                error!(
                    "Configuración inválida en {:?} ({}), creando backup",
                    path, e
                );
                if let Err(e) = std::fs::copy(&path, path.with_extension("cub.bak")) {
                    warn!("Error creando backup de configuración {:?}: {}", path, e);
                }
                Self::default()
            }
        }
    }

    /// Migraciones de versiones anteriores del formato.
    fn migrate(&mut self) {
        // v1 → v2: memoria en MB a GB
        if self.min_memory > 128 {
            self.min_memory = (self.min_memory / 1024).max(1);
            self.dirty = true;
        }
        if self.max_memory > 128 {
            self.max_memory = (self.max_memory / 1024).max(1);
            self.dirty = true;
        }
    }
}

#[cfg(test)]
mod tests {
    use super::*;

    /// La función `default_username()` debe devolver el string `"steve"`.
    #[test]
    fn test_default_username() {
        assert_eq!(default_username(), "steve");
    }

    /// `SettingsManager::default()` debe inicializar todos los campos con
    /// los valores por defecto definidos en las funciones `default_*`.
    /// Verifica: username, min_memory, max_memory, language, auto_updates,
    /// close_launcher_on_play, show_snapshots, show_alpha, theme, dirty,
    /// env_vars y jvm_args.
    #[test]
    fn test_default_values() {
        let s = SettingsManager::default();
        assert_eq!(s.username, "steve");
        assert_eq!(s.min_memory, 1);
        assert_eq!(s.max_memory, 2);
        assert_eq!(s.language, "es");
        assert!(s.auto_updates);
        assert!(s.close_launcher_on_play);
        assert!(!s.show_snapshots);
        assert!(!s.show_alpha);
        assert_eq!(s.theme, "dark");
        assert!(s.dirty);
        assert!(s.env_vars.is_empty());
        assert!(s.jvm_args.is_empty());
    }

    /// Cuando los valores ya están en GB (min=2, max=4), `migrate()` no debe
    /// modificarlos y debe dejar `dirty` como `false`.
    #[test]
    fn test_migrate_noop_when_already_gb() {
        let mut s = SettingsManager::default();
        s.min_memory = 2;
        s.max_memory = 4;
        s.dirty = false;
        s.migrate();
        assert_eq!(s.min_memory, 2);
        assert_eq!(s.max_memory, 4);
        assert!(!s.dirty);
    }

    /// Valores en MB (min=2048, max=4096) deben convertirse a GB (min=2,
    /// max=4) y marcar `dirty` como `true` para forzar el guardado.
    /// Esta migración existe porque en versiones anteriores del launcher
    /// la memoria se almacenaba en MB y ahora se almacena en GB.
    #[test]
    fn test_migrate_converts_mb_to_gb() {
        let mut s = SettingsManager::default();
        s.min_memory = 2048;
        s.max_memory = 4096;
        s.dirty = false;
        s.migrate();
        assert_eq!(s.min_memory, 2);
        assert_eq!(s.max_memory, 4);
        assert!(s.dirty);
    }

    /// Un valor impar en MB como 1500 debe convertirse a 1 GB
    /// (1500 / 1024 = 1.46, `max(1)` asegura que no quede en 0).
    #[test]
    fn test_migrate_converts_odd_mb() {
        let mut s = SettingsManager::default();
        s.min_memory = 1500;
        s.dirty = false;
        s.migrate();
        assert_eq!(s.min_memory, 1);
    }

    /// Un valor de max_memory = 128 NO debe convertirse porque la condición
    /// del `if` es `> 128`. Esto evita falsos positivos con valores
    /// pequeños que casualmente están en el rango MB pero son válidos en GB.
    #[test]
    fn test_migrate_does_not_touch_normal_values() {
        let mut s = SettingsManager::default();
        s.min_memory = 2;
        s.max_memory = 128;
        s.dirty = false;
        s.migrate();
        assert_eq!(s.min_memory, 2);
        assert_eq!(s.max_memory, 128);
    }

    /// Serializar y deserializar un `SettingsManager` con serde_json debe
    /// preservar todos los campos públicos. El campo `dirty` tiene
    /// `#[serde(skip)]` por lo que siempre se deserializa como `false`.
    #[test]
    fn test_serde_roundtrip() {
        let s = SettingsManager::default();
        let json = serde_json::to_string(&s).unwrap();
        let deserialized: SettingsManager = serde_json::from_str(&json).unwrap();
        assert_eq!(deserialized.username, s.username);
        assert_eq!(deserialized.min_memory, s.min_memory);
        assert_eq!(deserialized.max_memory, s.max_memory);
        assert_eq!(deserialized.language, s.language);
        assert_eq!(deserialized.theme, s.theme);
        assert!(!deserialized.dirty);
    }
}
