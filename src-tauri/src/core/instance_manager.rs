use crate::core::{path_manager::PathManager, settings_manager::SettingsManager};
use serde::{Deserialize, Serialize};
use std::collections::HashMap;
use std::path::{Path, PathBuf};
use std::process::Child;
use std::sync::Arc;
use std::sync::OnceLock;
use std::{fs, io};
use tokio::fs as tokio_fs;
use tokio::sync::RwLock;
use tokio::time::{self, Duration};
use tracing::{error, info};

const MAX_LEN: u8 = 16;
const SYNC_INTERVAL_SECS: u64 = 30;

#[derive(Debug, Serialize, Deserialize)]
struct InstanceData {
    name: String,
    version: String,
    last_played: u64,        // unix timestamp
    min_memory: Option<u32>, // None: usar SettingsManager
    max_memory: Option<u32>,
    cover_image: Option<PathBuf>,
    uuid: String, // uuidv4
}

pub struct Instance {
    data: InstanceData,
    process: Option<Child>,
    dirty: bool,
}

impl Instance {
    pub fn new(name: String, version: String) -> Self {
        Self {
            data: InstanceData {
                name,
                version,
                last_played: 0,
                min_memory: None,
                max_memory: None,
                cover_image: None,
                uuid: uuid::Uuid::new_v4().to_string(),
            },
            process: None,
            dirty: true,
        }
    }

    pub fn get_name(&self) -> &str {
        &self.data.name
    }

    pub fn get_version(&self) -> &str {
        &self.data.version
    }

    pub fn get_last_played(&self) -> u64 {
        self.data.last_played
    }

    pub fn get_cover_image(&self) -> Option<&Path> {
        self.data.cover_image.as_deref()
    }

    pub fn get_loader(&self) -> &str {
        if self.data.version.contains("fabric") {
            return "Fabric";
        }
        if self.data.version.contains("forge") {
            return "Forge";
        }
        if self.data.version.contains("quilt") {
            return "Quilt";
        }
        "Vanilla"
    }

    pub fn get_is_running(&self) -> bool {
        self.process.is_some()
    }

    pub fn get_max_memory(&self) -> u32 {
        self.data
            .max_memory
            .unwrap_or_else(|| SettingsManager::get().lock().unwrap().get_max_memory())
    }
    pub fn get_id(&self) -> &str {
        &self.data.uuid
    }
    pub fn get_min_memory(&self) -> u32 {
        self.data
            .min_memory
            .unwrap_or_else(|| SettingsManager::get().lock().unwrap().get_min_memory())
    }

    pub fn set_name(&mut self, name: String) {
        self.data.name = name;
        self.dirty = true;
    }

    pub fn set_version(&mut self, version: String) {
        self.data.version = version;
        self.dirty = true;
    }

    pub fn set_min_memory(&mut self, min_memory: Option<u32>) {
        self.data.min_memory = min_memory;
        self.dirty = true;
    }

    pub fn set_max_memory(&mut self, max_memory: Option<u32>) {
        self.data.max_memory = max_memory;
        self.dirty = true;
    }

    pub fn set_cover_image(&mut self, cover_image: Option<PathBuf>) {
        self.data.cover_image = cover_image;
        self.dirty = true;
    }

    pub fn update_last_played(&mut self) {
        self.data.last_played = std::time::SystemTime::now()
            .duration_since(std::time::UNIX_EPOCH)
            .unwrap()
            .as_secs();
        self.dirty = true;
    }

    pub fn attach_process(&mut self, process: Child) {
        self.process = Some(process);

        self.update_last_played();
    }

    pub fn detach_process(&mut self) {
        self.process = None;
    }

    pub fn kill(&mut self) {
        if let Some(ref mut process) = self.process {
            let _ = process.kill();
        }
        self.process = None;
    }

    pub fn check_and_detach(&mut self) -> bool {
        match self.process {
            None => true,
            Some(ref mut process) => match process.try_wait() {
                Ok(Some(_)) => {
                    self.process = None;
                    true
                }
                Ok(None) => false,
                Err(e) => {
                    error!("Error al verificar proceso: {:?}", e);
                    self.process = None;
                    true
                }
            },
        }
    }

    fn get_instance_dir(&self) -> PathBuf {
        PathManager::get().get_instance_dir().join(&self.data.name)
    }

    fn get_config_path(&self) -> PathBuf {
        self.get_instance_dir().join("instance.cub")
    }

    pub async fn save_to_disk(&mut self) -> Result<(), io::Error> {
        if !self.dirty {
            return Ok(());
        }

        let dir = self.get_instance_dir();
        tokio_fs::create_dir_all(&dir).await?;

        let content = serde_json::to_string_pretty(&self.data)
            .map_err(|e| io::Error::new(io::ErrorKind::Other, e))?;

        tokio_fs::write(self.get_config_path(), content).await?;
        self.dirty = false;
        Ok(())
    }

    async fn load_from_disk(name: &str) -> Option<Self> {
        let path = PathManager::get()
            .get_instance_dir()
            .join(name)
            .join("instance.cub");

        let content = tokio_fs::read_to_string(path).await.ok()?;
        let data: InstanceData = serde_json::from_str(&content).ok()?;
        Some(Self {
            data,
            process: None,
            dirty: false,
        })
    }

    pub fn to_dto(&self) -> InstanceDto {
        InstanceDto {
            name: self.data.name.clone(),
            version: self.data.version.clone(),
            loader: self.get_loader().to_string(),
            last_played: self.data.last_played,
            is_running: self.get_is_running(),
            cover_image: self.data.cover_image.clone(),
            uuid: self.data.uuid.clone(),
        }
    }
}

pub struct InstanceManager {
    instances: RwLock<HashMap<String, Arc<RwLock<Instance>>>>,
    _sync_handle: tokio::task::JoinHandle<()>,
}

impl InstanceManager {
    pub async fn init() -> Arc<Self> {
        let manager = Arc::new(Self {
            instances: RwLock::new(HashMap::new()),
            _sync_handle: tokio::spawn(Self::sync_task()),
        });

        let mut guard = manager.instances.write().await;
        if let Ok(entries) = fs::read_dir(PathManager::get().get_instance_dir()) {
            for entry in entries.flatten() {
                if entry.path().is_dir() {
                    let name = entry.file_name().to_string_lossy().to_string();
                    if let Some(instance) = Instance::load_from_disk(&name).await {
                        guard.insert(
                            instance.get_id().to_string(),
                            Arc::new(RwLock::new(instance)),
                        );
                    }
                }
            }
        }
        drop(guard);

        let _ = INSTANCE_MANAGER.set(manager.clone());
        manager
    }

    pub fn get() -> &'static Arc<InstanceManager> {
        INSTANCE_MANAGER
            .get()
            .expect("InstanceManager no inicializado")
    }

    async fn sync_task() {
        let mut interval = time::interval(Duration::from_secs(SYNC_INTERVAL_SECS));
        loop {
            interval.tick().await;
            info!("Ejecutando tarea de sincronizacion");
            let manager = match Self::get_opt() {
                Some(m) => m.clone(),
                None => continue,
            };

            let instances = manager.instances.read().await;
            for (name, instance_arc) in instances.iter() {
                let mut inst = instance_arc.write().await;
                if inst.dirty {
                    if let Err(e) = inst.save_to_disk().await {
                        error!("Error guardando instancia {}: {:?}", name, e);
                    }
                }
            }
        }
    }

    fn get_opt() -> Option<&'static Arc<InstanceManager>> {
        INSTANCE_MANAGER.get()
    }

    pub async fn create_instance(&self, name: String, version: String) {
        if let Err(e) = validate_instance_name(&name) {
            error!("Error al crear instancia: {}", e);
            return;
        }

        let instance = Arc::new(RwLock::new(Instance::new(name.clone(), version)));

        let uuid = {
            let inst = instance.read().await;
            inst.get_id().to_string()
        };
        {
            let mut inst = instance.write().await;
            if let Err(e) = inst.save_to_disk().await {
                error!("Error al guardar instancia nueva: {:?}", e);
            }
        }

        let mut guard = self.instances.write().await;
        guard.insert(uuid, instance);
    }

    pub async fn get_instance(&self, uuid: &str) -> Option<Arc<RwLock<Instance>>> {
        let guard = self.instances.read().await;
        guard.get(uuid).cloned()
    }

    pub async fn get_all_instances(&self) -> Vec<Arc<RwLock<Instance>>> {
        let guard = self.instances.read().await;
        guard.values().cloned().collect()
    }

    pub async fn exists(&self, uuid: &str) -> bool {
        let guard = self.instances.read().await;
        guard.contains_key(uuid)
    }

    pub async fn count(&self) -> usize {
        let guard = self.instances.read().await;
        guard.len()
    }

    pub async fn get_dto(&self, name: &str) -> Option<InstanceDto> {
        let instance = self.get_instance(name).await?;
        let inst = instance.read().await;
        Some(inst.to_dto())
    }

    pub async fn get_all_dtos(&self) -> Vec<InstanceDto> {
        let instances = self.get_all_instances().await;
        let mut dtos = Vec::with_capacity(instances.len());
        for inst_arc in instances {
            let inst = inst_arc.read().await;
            dtos.push(inst.to_dto());
        }
        dtos
    }

    pub async fn get_running_dtos(&self) -> Vec<String> {
        let instances = self.get_all_instances().await;
        let mut dtos = Vec::new();
        for inst_arc in instances {
            let inst = inst_arc.read().await;
            if inst.get_is_running() {
                dtos.push(inst.to_dto().uuid);
            }
        }
        dtos
    }

    pub async fn delete_instance(&self, uuid: &str) -> Result<(), String> {
        let instance_arc = {
            let mut guard = self.instances.write().await;
            guard.remove(uuid).ok_or_else(|| "Instancia no encontrada".to_string())?
        };

        let inst = instance_arc.read().await;
        let dir = inst.get_instance_dir();
        if dir.exists() {
            if let Err(e) = fs::remove_dir_all(&dir) {
                return Err(format!("Error al eliminar el directorio: {}", e));
            }
        }
        Ok(())
    }

    pub async fn rename_instance(&self, uuid: &str, new_name: String) -> Result<(), String> {
        validate_instance_name(&new_name)?;

        let instance_arc = self
            .get_instance(uuid)
            .await
            .ok_or_else(|| "Instancia no encontrada".to_string())?;

        let mut inst = instance_arc.write().await;
        let old_name = inst.get_name().to_string();

        if old_name == new_name {
            return Ok(());
        }

        let base_dir = PathManager::get().get_instance_dir();
        let old_dir = base_dir.join(&old_name);
        let new_dir = base_dir.join(&new_name);

        if new_dir.exists() {
            return Err("Ya existe una instancia con ese nombre".to_string());
        }

        if old_dir.exists() {
            if let Err(e) = tokio_fs::rename(&old_dir, &new_dir).await {
                return Err(format!("Error al renombrar el directorio: {}", e));
            }
        }

        inst.set_name(new_name);
        if let Err(e) = inst.save_to_disk().await {
            return Err(format!("Error al guardar la instancia: {}", e));
        }

        Ok(())
    }
}

// Global manager (inicializado con init())
static INSTANCE_MANAGER: OnceLock<Arc<InstanceManager>> = OnceLock::new();

// -------------------- DTOs --------------------
#[derive(Serialize, Clone)]
pub struct InstanceDto {
    pub name: String,
    pub version: String,
    pub loader: String,
    pub last_played: u64,
    pub is_running: bool,
    pub cover_image: Option<PathBuf>,
    pub uuid: String,
}

#[derive(Serialize, Clone)]
pub struct InstancesPollingPayload {
    running: Vec<String>, // vec de uuidv4
    all: Vec<InstanceDto>,
    count: usize,
}

impl InstancesPollingPayload {
    /// El vector de Running guardaria los UUIDv4 de las instances
    pub fn new(running: Vec<String>, all: Vec<InstanceDto>, count: usize) -> Self {
        Self {
            running,
            all,
            count,
        }
    }
}

// -------------------- Validación --------------------
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
