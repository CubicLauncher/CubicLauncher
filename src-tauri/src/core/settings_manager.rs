use crate::core::path_manager::PathManager;
use claunch_rs::MinecraftUser;
use serde::{Deserialize, Serialize};
use std::path::{Path, PathBuf};
use std::sync::{LazyLock, Mutex};
use tracing::{error, warn};

// ── Static global ─────────────────────────────────────────────────────────────

static SETTINGS: LazyLock<Mutex<SettingsManager>> =
    LazyLock::new(|| Mutex::new(SettingsManager::load()));

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
    pub force_gpu: bool,
    #[serde(default)]
    pub jvm_args: String,
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
            language: String::from("es"),
            auto_updates: true,
            show_error_console: false,
            close_launcher_on_play: true,
            show_snapshots: false,
            show_alpha: false,
            force_gpu: false,
            jvm_args: String::new(),
            dirty: true,
        }
    }
}

impl SettingsManager {
    // ── Global ────────────────────────────────────────────────────────────────

    pub fn get() -> &'static Mutex<SettingsManager> {
        &SETTINGS
    }

    // ── Getters ───────────────────────────────────────────────────────────────

    pub fn get_username(&self) -> &str {
        &self.username
    }
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

    pub fn get_minecraft_user(&self) -> MinecraftUser {
        match &self.user {
            Some(user) => {
                let mut u = user.clone();
                let _ = u.load_tokens();
                u
            }
            None => MinecraftUser::cracked(&self.username),
        }
    }

    // ── Setters ───────────────────────────────────────────────────────────────

    pub fn set_username(&mut self, username: String) {
        self.username = username;
        self.dirty = true;
    }

    pub fn set_user(&mut self, user: Option<MinecraftUser>) {
        if let Some(ref u) = user {
            self.username = u.username.clone();
        }
        self.user = user;
        self.dirty = true;
    }

    pub fn set_min_memory(&mut self, min_memory: u32) {
        if min_memory > self.max_memory {
            warn!(
                "min_memory ({}) no puede ser mayor que max_memory ({}), ignorando",
                min_memory, self.max_memory
            );
            return;
        }
        self.min_memory = min_memory;
        self.dirty = true;
    }

    pub fn set_max_memory(&mut self, max_memory: u32) {
        self.max_memory = max_memory;
        self.dirty = true;
    }

    pub fn set_jre8_path(&mut self, path: PathBuf) {
        self.jre8_path = path;
        self.dirty = true;
    }
    pub fn set_jre17_path(&mut self, path: PathBuf) {
        self.jre17_path = path;
        self.dirty = true;
    }
    pub fn set_jre21_path(&mut self, path: PathBuf) {
        self.jre21_path = path;
        self.dirty = true;
    }

    // ── Persistencia ──────────────────────────────────────────────────────────

    /// Serializa y escribe a disco.
    /// Extrae el contenido con el Mutex tomado, luego escribe fuera del scope
    /// para minimizar el tiempo que el lock bloquea otros hilos.
    pub fn save(&mut self) {
        if !self.dirty {
            return;
        }

        let path = PathManager::get().get_settings_dir().join("settings.cub");

        // Serializar mientras tenemos &self — el write al disco ocurre después
        let content = match serde_json::to_string_pretty(&self) {
            Ok(c) => c,
            Err(e) => {
                error!("No se pudo serializar la configuración: {}", e);
                return;
            }
        };

        // Marcar como limpio antes de escribir — si el write falla dirty
        // vuelve a true en el Err de abajo, evitando pérdida silenciosa
        self.dirty = false;

        if let Err(e) = std::fs::write(&path, content) {
            error!("No se pudo guardar la configuración en {:?}: {}", path, e);
            self.dirty = true; // revertir para reintentar en el próximo save()
        }
    }

    pub fn load() -> Self {
        let path = PathManager::get().get_settings_dir().join("settings.cub");

        if !path.exists() {
            return Self::default();
        }

        let content = match std::fs::read_to_string(&path) {
            Ok(c) => c,
            Err(e) => {
                error!("Error al leer la configuración: {}", e);
                return Self::default();
            }
        };

        match serde_json::from_str::<Self>(&content) {
            Ok(mut settings) => {
                settings.migrate();
                settings
            }
            Err(e) => {
                error!("Configuración inválida ({}), creando backup", e);
                let _ = std::fs::copy(&path, path.with_extension("cub.bak"));
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
