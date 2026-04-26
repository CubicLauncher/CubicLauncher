use crate::core::path_manager::PathManager;
use claunch_rs::MinecraftUser;
use serde::{Deserialize, Serialize};
use serde_json;
use std::path::{Path, PathBuf};
use std::sync::{LazyLock, Mutex};
static SETTINGS: LazyLock<Mutex<SettingsManager>> =
    LazyLock::new(|| Mutex::new(SettingsManager::load()));

// puede ser que luego mueva todo a structs mas chicos
// para tener metodos utiles a partir de lo que se guarde
/// Struct CRUD para los settings
/// nota: la memoria se guarda en megabytes
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
#[allow(dead_code)]
impl SettingsManager {
    pub fn get() -> &'static Mutex<SettingsManager> {
        &SETTINGS
    }
    // Getters
    pub fn get_username(&self) -> &str {
        &self.username
    }
    pub fn get_max_memory(&self) -> u32 {
        self.max_memory
    }
    pub fn get_min_memory(&self) -> u32 {
        self.min_memory
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
        if let Some(user) = &self.user {
            let mut u = user.clone();
            let _ = u.load_tokens();
            u
        } else {
            MinecraftUser::cracked(&self.username)
        }
    }
    // setters
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
    pub fn set_max_memory(&mut self, max_memory: u32) {
        self.max_memory = max_memory;
        self.dirty = true;
    }
    pub fn set_min_memory(&mut self, min_memory: u32) {
        if min_memory > self.max_memory {
            eprintln!("Min mem no puede ser mayor que max mem, ignorando cambios");
            return;
        }
        self.min_memory = min_memory;
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
    pub fn save(&mut self) {
        if !self.dirty {
            return; // no hacer nada ya q no hay cambios
        }
        let path = PathManager::get().get_settings_dir().join("settings.cub");
        match serde_json::to_string_pretty(&self) {
            Ok(content) => {
                if std::fs::write(path, content).is_ok() {
                    self.dirty = false
                }
            }
            Err(e) => eprintln!("No se pudo guardar la configuracion: {:?}", e.classify()),
        }
    }
    pub fn load() -> SettingsManager {
        let path = PathManager::get().get_settings_dir().join("settings.cub");
        if !path.exists() {
            return SettingsManager::default();
        }
        let content = match std::fs::read_to_string(&path) {
            Ok(c) => c,
            Err(_) => {
                eprintln!("Error al cargar archivo de configuracion");
                return SettingsManager::default();
            }
        };
        match serde_json::from_str::<SettingsManager>(&content) {
            Ok(mut settings) => {
                // Migrate from MB to GB if it looks like MB (e.g. > 128)
                if settings.min_memory > 128 {
                    settings.min_memory = (settings.min_memory / 1024).max(1);
                    settings.dirty = true;
                }
                if settings.max_memory > 128 {
                    settings.max_memory = (settings.max_memory / 1024).max(1);
                    settings.dirty = true;
                }
                settings
            }
            Err(_) => {
                eprintln!("Configuracion invalida, creando backup");
                let _ = std::fs::copy(&path, path.with_extension("cub.bak"));
                SettingsManager::default()
            }
        }
    }
}
impl Default for SettingsManager {
    fn default() -> Self {
        SettingsManager {
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
