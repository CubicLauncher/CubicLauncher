use crate::core::{path_manager::PathManager, settings_manager::SettingsManager};
use serde::{Deserialize, Serialize};
use serde_json;
use std::sync::Arc;
use std::{fs, sync::LazyLock};
use std::{
    path::{Path, PathBuf},
    process::Child,
};
use tokio::sync::Mutex;

static INSTANCE_MANAGER: LazyLock<Mutex<InstanceManager>> =
    LazyLock::new(|| Mutex::new(InstanceManager::load_all()));
const MAX_LEN: u8 = 16;

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
#[derive(Clone)]
pub struct InstanceEntry {
    name: String,
    data: Arc<Mutex<Instance>>,
}
pub struct InstanceManager {
    instances: Vec<InstanceEntry>,
}
#[derive(Serialize, Clone)]
pub struct InstancesPollingPayload {
    running: Vec<InstanceDto>,
    all: Vec<InstanceDto>,
    count: usize,
}
/// Dto para tauri
#[derive(Serialize, Clone)]
pub struct InstanceDto {
    name: String,
    version: String,
    loader: String,
    last_played: u64,
    is_running: bool,
    cover_image: Option<PathBuf>,
}

impl Instance {
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
        self.isdirty = true
    }

    pub fn set_version(&mut self, version: String) {
        self.version = version;
        self.isdirty = true
    }

    pub fn set_min_memory(&mut self, min_memory: Option<u32>) {
        self.min_memory = min_memory;
        self.isdirty = true
    }
    pub fn set_max_memory(&mut self, max_memory: Option<u32>) {
        self.max_memory = max_memory;
        self.isdirty = true
    }

    pub fn set_cover_image(&mut self, cover_image: Option<PathBuf>) {
        self.cover_image = cover_image;
        self.isdirty = true
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
    pub fn kill(&mut self) {
        if let Some(ref mut process) = self.process {
            let _ = process.kill();
        }
        self.process = None;
    }
    pub fn check_and_detach(&mut self) -> bool {
        match self.process {
            None => true, // ya terminó o fue killed
            Some(ref mut process) => {
                match process.try_wait() {
                    Ok(Some(_)) => {
                        self.process = None;
                        true // terminó naturalmente
                    }
                    Ok(None) => false, // sigue corriendo
                    Err(e) => {
                        eprintln!("Error al verificar proceso: {:?}", e);
                        self.process = None;
                        true // asumimos muerto ante error
                    }
                }
            }
        }
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
                        instances.push(InstanceEntry {
                            name,
                            data: Arc::new(Mutex::new(instance)),
                        });
                    }
                }
            }
        }
        InstanceManager { instances }
    }

    pub async fn create_instance(&mut self, name: String, version: String) {
        match validate_instance_name(&name) {
            Ok(_) => (),
            Err(e) => {
                eprintln!("Error al crear instancia {e}")
            }
        }
        let instance = Arc::new(Mutex::new(Instance::new(name.clone(), version)));

        let mut inst = instance.lock().await;
        match inst.save() {
            Ok(_) => println!("Instancia {} guardada", inst.get_name()),
            Err(e) => println!("Error al guardar instancia: {:#?}", e.raw_os_error()),
        }
        drop(inst);
        self.instances.push(InstanceEntry {
            name,
            data: instance,
        });
    }

    pub async fn get_running_dtos(&self) -> Vec<InstanceDto> {
        let mut dtos = Vec::new();
        for arc in &self.instances {
            let inst = arc.data.lock().await;
            if inst.get_is_running() {
                dtos.push(inst.to_dto());
            }
        }
        dtos
    }

    pub fn get_instance(&self, name: &str) -> Option<Arc<Mutex<Instance>>> {
        self.instances
            .iter()
            .find(|i| i.name == name)
            .map(|i| Arc::clone(&i.data))
    }

    pub fn get_all(&self) -> Vec<Arc<Mutex<Instance>>> {
        self.instances.iter().map(|i| Arc::clone(&i.data)).collect()
    }

    pub async fn get_dto(&self, name: &str) -> Option<InstanceDto> {
        let arc = self.get_instance(name)?;
        let inst = arc.lock().await;
        Some(inst.to_dto())
    }

    pub async fn get_all_dtos(&self) -> Vec<InstanceDto> {
        // 1. Pre-reservamos memoria para evitar re-asignaciones en el loop
        let mut dtos = Vec::with_capacity(self.instances.len());

        for entry in &self.instances {
            // 2. Bloqueamos solo el tiempo necesario para generar el DTO
            let inst = entry.data.lock().await;
            dtos.push(inst.to_dto());
        }
        dtos
    }

    pub fn exists(&self, name: &str) -> bool {
        self.instances.iter().any(|i| i.name == name)
    }

    pub fn count(&self) -> usize {
        self.instances.len()
    }
}

impl InstancesPollingPayload {
    pub fn new(
        running: Vec<InstanceDto>,
        all: Vec<InstanceDto>,
        count: usize,
    ) -> InstancesPollingPayload {
        InstancesPollingPayload {
            running,
            all,
            count,
        }
    }
}

fn validate_instance_name(name: &str) -> Result<(), String> {
    if name.is_empty() {
        return Err("El nombre de la instancia no puede estar vacío.".into());
    }

    if !name.is_ascii() {
        return Err("El nombre de la instancia debe contener solo caracteres ASCII.".into());
    }

    if name.len() > MAX_LEN.into() {
        return Err(format!(
            "El nombre de la instancia no puede superar {} caracteres.",
            MAX_LEN
        ));
    }

    Ok(())
}
