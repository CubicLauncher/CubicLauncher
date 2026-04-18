use crate::core::path_manager::PathManager;
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
    pub username: String,
    pub min_memory: u32, // No se quien tenga tanta ram amigo xD
    pub max_memory: u32,
    pub jre8_path: PathBuf,
    pub jre17_path: PathBuf,
    pub jre21_path: PathBuf,
    pub language: String,
    pub auto_updates: bool,
    pub show_error_console: bool,
    pub close_launcher_on_play: bool,
    pub show_beta: bool,
    pub show_alpha: bool,
    pub force_gpu: bool,
    pub jvm_args: String,
    #[serde(skip)]
    pub dirty: bool, // sistema de dirtness para evitar escribir mucho
}
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
    // setters
    pub fn set_username(&mut self, username: String) {
        self.username = username;
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
        match serde_json::from_str(&content) {
            Ok(settings) => settings,
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
            min_memory: 512,
            max_memory: 2048,
            jre8_path: PathBuf::new(),
            jre17_path: PathBuf::new(),
            jre21_path: PathBuf::new(),
            language: String::from("es"),
            auto_updates: true,
            show_error_console: false,
            close_launcher_on_play: true,
            show_beta: false,
            show_alpha: false,
            force_gpu: false,
            jvm_args: String::new(),
            dirty: true,
        }
    }
}
