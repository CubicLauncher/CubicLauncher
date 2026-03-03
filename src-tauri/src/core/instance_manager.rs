use crate::core::{path_manager::PathManager, settings_manager::SettingsManager};
use serde::{Deserialize, Serialize};
use serde_json;
use std::{fs, sync::LazyLock};
use std::{
    path::{Path, PathBuf},
    process::Child,
};
use tokio::sync::Mutex;

static INSTANCE_MANAGER: LazyLock<Mutex<InstanceManager>> =
    LazyLock::new(|| Mutex::new(InstanceManager::load_all()));

#[derive(Debug, Serialize, Deserialize)]
pub struct Instance {
    name: String,
    version: String,
    last_played: u64,             // unix timestamp
    min_memory: Option<u32>,      // None: Usar la de settingsmanager
    max_memory: Option<u32>,      // igual q en min_memory
    cover_image: Option<PathBuf>, // puede existir o no
    #[serde(skip)]
    process: Option<Child>,
    #[serde(skip)]
    isdirty: bool,
}

pub struct InstanceManager {
    instances: Vec<Instance>,
}

/// Dto para tauri
#[derive(Serialize)]
pub struct InstanceDto {
    name: String,
    version: String,
    loader: String,
    last_played: u64,
    is_running: bool,
    cover_image: Option<PathBuf>,
}

impl Instance {
    //getters
    pub fn get_is_running(&self) -> bool {
        self.process.is_some()
    }
    pub fn get_loader(&self) -> &str {
        if self.version.contains("fabric") {
            return "Fabric";
        }
        if self.version.contains("forge") {
            return "Forge";
        }
        if self.version.contains("quilt") {
            return "Quilt";
        }
        "Vanilla"
    }

    pub fn get_instance_dir(&self) -> PathBuf {
        PathManager::get().get_instance_dir().join(&self.name)
    }

    pub fn get_screenshots_dir(&self) -> PathBuf {
        self.get_instance_dir().join("screenshots")
    }

    pub fn get_config_path(&self) -> PathBuf {
        self.get_instance_dir().join("instance.cub")
    }

    pub fn get_max_memory(&self) -> u32 {
        self.max_memory
            .unwrap_or_else(|| SettingsManager::get().lock().unwrap().get_max_memory())
    }

    pub fn get_min_memory(&self) -> u32 {
        self.min_memory
            .unwrap_or_else(|| SettingsManager::get().lock().unwrap().get_min_memory())
    }

    pub fn get_name(&self) -> &str {
        &self.name
    }
    pub fn get_version(&self) -> &str {
        &self.version
    }
    pub fn get_last_played(&self) -> u64 {
        self.last_played
    }
    pub fn get_cover_image(&self) -> Option<&Path> {
        self.cover_image.as_deref()
    }
    pub fn to_dto(&self) -> InstanceDto {
        InstanceDto {
            name: self.name.clone(),
            version: self.version.clone(),
            loader: self.get_loader().to_string(),
            last_played: self.last_played,
            is_running: self.get_is_running(),
            cover_image: self.cover_image.clone(),
        }
    }

    pub fn update_last_played(&mut self) {
        self.last_played = std::time::SystemTime::now()
            .duration_since(std::time::UNIX_EPOCH)
            .unwrap()
            .as_secs()
    }
    pub fn new(name: String, version: String) -> Instance {
        Instance {
            name,
            version,
            last_played: 0,
            min_memory: None,
            max_memory: None,
            cover_image: None,
            process: None,
            isdirty: true,
        }
    }
    pub fn attach_process(&mut self, process: Child) {
        self.process = Some(process)
    }

    pub fn set_name(&mut self, name: String) {
        self.name = name;
    }

    pub fn set_version(&mut self, version: String) {
        self.version = version;
    }

    pub fn set_min_memory(&mut self, min_memory: Option<u32>) {
        self.min_memory = min_memory;
    }

    pub fn set_max_memory(&mut self, max_memory: Option<u32>) {
        self.max_memory = max_memory;
    }

    pub fn set_cover_image(&mut self, cover_image: Option<PathBuf>) {
        self.cover_image = cover_image;
    }

    pub fn detach_process(&mut self) {
        self.process = None;
    }

    pub fn save(&mut self) -> Result<(), std::io::Error> {
        if !self.isdirty {
            return Ok(());
        }

        let dir = self.get_instance_dir();
        std::fs::create_dir_all(&dir)?;

        let content = serde_json::to_string_pretty(&self)
            .map_err(|e| std::io::Error::new(std::io::ErrorKind::Other, e))?;

        std::fs::write(self.get_config_path(), content)?;
        self.isdirty = false;
        Ok(())
    }
    pub fn load(name: &str) -> Option<Instance> {
        let path = PathManager::get()
            .get_instance_dir()
            .join(name)
            .join("instance.cub");

        let content = std::fs::read_to_string(path).ok()?;
        serde_json::from_str(&content).ok()
    }
}

impl InstanceManager {
    pub fn get() -> &'static Mutex<InstanceManager> {
        return &INSTANCE_MANAGER;
    }
    fn load_all() -> InstanceManager {
        let mut instances = Vec::new();
        if let Ok(entries) = fs::read_dir(PathManager::get().get_instance_dir()) {
            for entry in entries.flatten() {
                if entry.path().is_dir() {
                    let name = entry.file_name().to_string_lossy().to_string();
                    if let Some(instance) = Instance::load(&name) {
                        instances.push(instance);
                    }
                }
            }
        }
        InstanceManager { instances }
    }

    pub fn get_all<'a, T: FromInstance<'a>>(&'a self) -> Vec<T> {
        self.instances.iter().map(T::from_instance).collect()
    }

    pub fn get_instance<'a, T: FromInstance<'a>>(&'a self, name: &str) -> Option<T> {
        self.instances
            .iter()
            .find(|i| i.get_name() == name)
            .map(T::from_instance)
    }

    pub fn create_instance(&mut self, name: String, version: String) {
        let mut instance = Instance::new(name, version);

        match instance.save() {
            Ok(_) => {
                println!("Instancia {} guardada", instance.get_name())
            }
            Err(e) => {
                println!("Error al guardar instancia: {:#?}", e.raw_os_error())
            }
        }
        self.instances.push(instance);
    }

    pub fn exists(&self, name: String) -> bool {
        self.instances.iter().any(|i| i.get_name() == name)
    }

    pub fn count(&self) -> usize {
        self.instances.len()
    }
}

pub trait FromInstance<'a> {
    fn from_instance(instance: &'a Instance) -> Self;
}

impl<'a> FromInstance<'a> for InstanceDto {
    fn from_instance(instance: &'a Instance) -> Self {
        instance.to_dto()
    }
}

impl<'a> FromInstance<'a> for &'a Instance {
    fn from_instance(instance: &'a Instance) -> Self {
        instance
    }
}
