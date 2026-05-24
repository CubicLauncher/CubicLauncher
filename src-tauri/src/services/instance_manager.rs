use crate::core::path_manager::PathManager;
use crate::core::{AppEvent, FsError, InstanceError, emit};
use crate::services::SettingsManager;
use serde::{Deserialize, Serialize};
use std::collections::HashMap;
use std::io;
use std::path::PathBuf;
use std::sync::atomic::{AtomicU8, Ordering};
use std::sync::{Arc, LazyLock, Mutex, OnceLock};
use tokio::fs as tokio_fs;
use tokio::sync::RwLock;
use tokio::sync::oneshot;
use tokio::time::{self, Duration};
use tracing::{error, info};

const MAX_LEN: u8 = 16;
const SYNC_INTERVAL_SECS: u64 = 30;

// ── Status ───────────────────────────────────────────────────────────────────

const STATUS_OFF: u8 = 0;
const STATUS_STARTING: u8 = 1;
const STATUS_STARTED: u8 = 2;
const STATUS_ERROR: u8 = 3;

#[derive(Serialize, Clone, PartialEq, Debug)]
#[serde(rename_all = "lowercase")]
pub enum InstanceStatus {
    Off,
    Starting,
    Started,
    Error(String),
}

/// Status sin lock para lecturas frecuentes (polling).
/// Escribe el mensaje de error ANTES de cambiar el AtomicU8
/// para garantizar consistencia con el ordering Release/Acquire.
struct AtomicStatus {
    state: AtomicU8,
    error: Mutex<String>,
}

impl AtomicStatus {
    fn new() -> Self {
        Self {
            state: AtomicU8::new(STATUS_OFF),
            error: Mutex::new(String::new()),
        }
    }

    fn get(&self) -> InstanceStatus {
        match self.state.load(Ordering::Acquire) {
            STATUS_STARTING => InstanceStatus::Starting,
            STATUS_STARTED => InstanceStatus::Started,
            STATUS_ERROR => {
                let msg = self.error.lock().unwrap_or_else(|e| e.into_inner()).clone();
                InstanceStatus::Error(msg)
            }
            _ => InstanceStatus::Off,
        }
    }

    fn set(&self, status: InstanceStatus) {
        match &status {
            InstanceStatus::Off => self.state.store(STATUS_OFF, Ordering::Release),
            InstanceStatus::Starting => self.state.store(STATUS_STARTING, Ordering::Release),
            InstanceStatus::Started => self.state.store(STATUS_STARTED, Ordering::Release),
            InstanceStatus::Error(e) => {
                // Escribe el mensaje antes de cambiar el state
                *self.error.lock().unwrap_or_else(|e| e.into_inner()) = e.clone();
                self.state.store(STATUS_ERROR, Ordering::Release);
            }
        }
    }
}

// ── InstanceData (persistido en disco) ───────────────────────────────────────

#[derive(Debug, Serialize, Deserialize)]
struct InstanceData {
    name: String,
    version: String,
    last_played: u64,
    min_memory: Option<u32>,
    max_memory: Option<u32>,
    cover_image: Option<PathBuf>,
    icon: Option<String>,
    uuid: String,
    #[serde(skip)]
    dirty: bool,
}

impl InstanceData {
    fn new(name: String, version: String, icon: Option<String>) -> Self {
        Self {
            name,
            version,
            last_played: 0,
            min_memory: None,
            max_memory: None,
            cover_image: None,
            icon,
            uuid: uuid::Uuid::new_v4().to_string(),
            dirty: true,
        }
    }

    fn get_loader(&self) -> &str {
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

    fn get_instance_dir(&self) -> PathBuf {
        PathManager::get().get_instance_dir().join(&self.name)
    }

    async fn save(&mut self) -> Result<(), io::Error> {
        if !self.dirty {
            return Ok(());
        }
        let dir = self.get_instance_dir();
        tokio_fs::create_dir_all(&dir).await?;
        let content = serde_json::to_string(self).map_err(io::Error::other)?;
        tokio_fs::write(dir.join("instance.cub"), content).await?;
        self.dirty = false;
        Ok(())
    }

    async fn load(name: &str) -> Option<Self> {
        let path = PathManager::get()
            .get_instance_dir()
            .join(name)
            .join("instance.cub");
        let content = tokio_fs::read_to_string(path).await.ok()?;
        let mut data: InstanceData = serde_json::from_str(&content).ok()?;
        data.dirty = false;
        Some(data)
    }
}

// ── InstanceHandle ────────────────────────────────────────────────────────────
//
// Clone es O(1) — solo incrementa reference counts.
// El uuid está fuera del RwLock para poder leerlo sin await.

#[derive(Clone)]
pub struct InstanceHandle {
    pub uuid: String,
    data: Arc<RwLock<InstanceData>>,
    status: Arc<AtomicStatus>,
}

impl InstanceHandle {
    fn new(data: InstanceData) -> Self {
        Self {
            uuid: data.uuid.clone(),
            data: Arc::new(RwLock::new(data)),
            status: Arc::new(AtomicStatus::new()),
        }
    }

    async fn load(name: &str) -> Option<Self> {
        let data = InstanceData::load(name).await?;
        Some(Self::new(data))
    }

    // ── Status — sin await ────────────────────────────────────────────────

    pub fn get_status(&self) -> InstanceStatus {
        self.status.get()
    }

    pub fn set_status(&self, status: InstanceStatus) {
        self.status.set(status);
        emit(AppEvent::InstanceEdited {
            id: self.uuid.to_string(),
        });
    }

    pub fn is_busy(&self) -> bool {
        matches!(
            self.get_status(),
            InstanceStatus::Starting | InstanceStatus::Started
        )
    }

    // ── Proceso ───────────────────────────────────────────────────────────

    pub async fn kill(&self) -> Result<(), InstanceError> {
        signal_kill(&self.uuid);
        self.set_status(InstanceStatus::Off);
        Ok(())
    }

    // ── Lecturas de data ──────────────────────────────────────────────────

    pub async fn get_name(&self) -> String {
        self.data.read().await.name.clone()
    }

    pub async fn get_version(&self) -> String {
        self.data.read().await.version.clone()
    }

    pub async fn get_min_memory(&self) -> u32 {
        self.data
            .read()
            .await
            .min_memory
            .unwrap_or_else(|| SettingsManager::read().get_min_memory())
    }

    pub async fn get_max_memory(&self) -> u32 {
        self.data
            .read()
            .await
            .max_memory
            .unwrap_or_else(|| SettingsManager::read().get_max_memory())
    }

    pub async fn get_instance_dir(&self) -> PathBuf {
        self.data.read().await.get_instance_dir()
    }

    pub async fn get_cover_image(&self) -> Option<PathBuf> {
        self.data.read().await.cover_image.clone()
    }

    pub async fn get_icon(&self) -> Option<String> {
        self.data.read().await.icon.clone()
    }

    pub async fn to_dto(&self) -> InstanceDto {
        let data = self.data.read().await;
        InstanceDto {
            name: data.name.clone(),
            version: data.version.clone(),
            loader: data.get_loader().to_string(),
            last_played: data.last_played,
            status: self.get_status(), // sin await — AtomicU8
            cover_image: data.cover_image.clone(),
            icon: data.icon.clone(),
            uuid: self.uuid.clone(), // sin await — campo directo
            path: data.get_instance_dir(),
        }
    }

    // ── Escrituras de data ────────────────────────────────────────────────

    pub async fn set_name(&self, name: String) {
        let mut data = self.data.write().await;
        data.name = name;
        data.dirty = true;
    }

    pub async fn set_version(&self, version: String) {
        let mut data = self.data.write().await;
        data.version = version;
        data.dirty = true;
    }

    pub async fn set_icon(&self, icon: Option<String>) {
        let mut data = self.data.write().await;
        data.icon = icon;
        data.dirty = true;
    }

    pub async fn set_cover_image(&self, cover_image: Option<PathBuf>) {
        let mut data = self.data.write().await;
        data.cover_image = cover_image;
        data.dirty = true;
    }

    pub async fn update_last_played(&self) {
        let mut data = self.data.write().await;
        data.last_played = std::time::SystemTime::now()
            .duration_since(std::time::UNIX_EPOCH)
            .unwrap_or_default()
            .as_secs();
        data.dirty = true;
    }

    pub async fn save_if_dirty(&self) -> Result<(), io::Error> {
        // Primero chequeamos con read lock — ruta rápida si no hay cambios
        if !self.data.read().await.dirty {
            return Ok(());
        }
        // Re-check tras adquirir write lock
        self.data.write().await.save().await
    }
}

// ── InstanceManager ───────────────────────────────────────────────────────────

pub struct InstanceManager {
    instances: RwLock<HashMap<String, InstanceHandle>>,
    _sync_handle: tokio::task::JoinHandle<()>,
}

impl InstanceManager {
    pub async fn init() -> Arc<Self> {
        let manager = Arc::new(Self {
            instances: RwLock::new(HashMap::new()),
            _sync_handle: tokio::spawn(Self::sync_task()),
        });

        let base_dir = PathManager::get().get_instance_dir().to_path_buf();
        let names = tokio::task::spawn_blocking(move || -> Vec<String> {
            match std::fs::read_dir(&base_dir) {
                Ok(entries) => entries
                    .flatten()
                    .filter(|e| e.path().is_dir())
                    .map(|e| e.file_name().to_string_lossy().to_string())
                    .collect(),
                Err(_) => Vec::new(),
            }
        })
        .await
        .unwrap_or_default();

        let handles: Vec<Option<InstanceHandle>> =
            futures::future::join_all(names.iter().map(|name| InstanceHandle::load(name))).await;

        let mut guard = manager.instances.write().await;
        for handle in handles.into_iter().flatten() {
            guard.insert(handle.uuid.clone(), handle);
        }
        drop(guard);

        let _ = INSTANCE_MANAGER.set(manager.clone());
        manager
    }

    pub fn get() -> &'static Arc<InstanceManager> {
        INSTANCE_MANAGER
            .get()
            .expect("BUG: InstanceManager usado antes de inicializar")
    }

    async fn sync_task() {
        let mut interval = time::interval(Duration::from_secs(SYNC_INTERVAL_SECS));
        loop {
            interval.tick().await;
            info!("Ejecutando tarea de sincronizacion");

            let manager = match INSTANCE_MANAGER.get() {
                Some(m) => m.clone(),
                None => continue,
            };

            let handles: Vec<InstanceHandle> =
                { manager.instances.read().await.values().cloned().collect() };

            // Guardamos fuera del lock del HashMap
            for handle in handles {
                if let Err(e) = handle.save_if_dirty().await {
                    error!("Error guardando instancia {}: {:?}", handle.uuid, e);
                }
            }
        }
    }

    pub async fn create_instance(
        &self,
        name: String,
        version: String,
        icon: Option<String>,
    ) -> Result<InstanceHandle, InstanceError> {
        validate_instance_name(&name).map_err(InstanceError::InstNameParse)?;

        let mut data = InstanceData::new(name, version, icon);
        if data.get_instance_dir().exists() {
            Err(InstanceError::AlreadyExists)?;
        }
        data.save().await.map_err(|e| {
            InstanceError::Fs(FsError::WriteFile {
                path: data
                    .get_instance_dir()
                    .join("instance.cub")
                    .to_string_lossy()
                    .to_string(),
                source: e,
            })
        })?;

        let handle = InstanceHandle::new(data);
        self.instances
            .write()
            .await
            .insert(handle.uuid.clone(), handle.clone());

        Ok(handle)
    }

    pub async fn get_handle(&self, uuid: &str) -> Option<InstanceHandle> {
        self.instances.read().await.get(uuid).cloned()
    }

    pub async fn get_all_handles(&self) -> Vec<InstanceHandle> {
        self.instances.read().await.values().cloned().collect()
    }

    pub async fn count(&self) -> usize {
        self.instances.read().await.len()
    }

    pub async fn get_all_dtos(&self) -> Vec<InstanceDto> {
        let handles = self.get_all_handles().await;
        futures::future::join_all(handles.iter().map(|h| h.to_dto())).await
    }

    /// Sin await — lee el AtomicU8 directamente por cada handle
    pub async fn get_running_ids(&self) -> Vec<String> {
        self.instances
            .read()
            .await
            .values()
            .filter(|h| h.is_busy())
            .map(|h| h.uuid.clone())
            .collect()
    }

    pub async fn delete_instance(&self, uuid: &str) -> Result<(), String> {
        let handle = {
            self.instances
                .write()
                .await
                .remove(uuid)
                .ok_or_else(|| "Instancia no encontrada".to_string())?
        };

        let dir = handle.get_instance_dir().await;
        if dir.exists() {
            tokio_fs::remove_dir_all(&dir)
                .await
                .map_err(|e| format!("Error al eliminar el directorio: {}", e))?;
        }
        Ok(())
    }

    pub async fn update_instance(
        &self,
        uuid: &str,
        new_name: Option<String>,
        new_version: Option<String>,
        new_icon: Option<Option<String>>,
    ) -> Result<(), String> {
        let handle = self
            .get_handle(uuid)
            .await
            .ok_or_else(|| "Instancia no encontrada".to_string())?;

        if let Some(name) = new_name {
            validate_instance_name(&name)?;

            let old_name = handle.get_name().await;
            if old_name != name {
                let base_dir = PathManager::get().get_instance_dir();
                let old_dir = base_dir.join(&old_name);
                let new_dir = base_dir.join(&name);

                if new_dir.exists() {
                    return Err("Ya existe una instancia con ese nombre".to_string());
                }
                if old_dir.exists() {
                    tokio_fs::rename(&old_dir, &new_dir)
                        .await
                        .map_err(|e| format!("Error al renombrar el directorio: {}", e))?;
                }
                handle.set_name(name).await;
            }
        }

        if let Some(version) = new_version {
            handle.set_version(version).await;
        }

        if let Some(icon) = new_icon {
            handle.set_icon(icon).await;
        }

        handle
            .save_if_dirty()
            .await
            .map_err(|e| format!("Error al guardar la instancia: {}", e))?;

        Ok(())
    }
}

static INSTANCE_MANAGER: OnceLock<Arc<InstanceManager>> = OnceLock::new();

// ── Process kill coordination ─────────────────────────────────────────────────
//
// Se usa un canal oneshot para señalarle al background task que debe matar el
// proceso. El launchwerk::InstanceHandle nunca se comparte — vive en el closure.

static KILL_SENDERS: LazyLock<Mutex<HashMap<String, oneshot::Sender<()>>>> =
    LazyLock::new(|| Mutex::new(HashMap::new()));

pub fn register_kill_sender(uuid: &str, tx: oneshot::Sender<()>) {
    KILL_SENDERS
        .lock()
        .unwrap_or_else(|e| e.into_inner())
        .insert(uuid.to_string(), tx);
}

pub fn unregister_kill_sender(uuid: &str) {
    KILL_SENDERS
        .lock()
        .unwrap_or_else(|e| e.into_inner())
        .remove(uuid);
}

/// Envía la señal de kill. Retorna `true` si el proceso estaba corriendo.
pub fn signal_kill(uuid: &str) -> bool {
    let tx = KILL_SENDERS
        .lock()
        .unwrap_or_else(|e| e.into_inner())
        .remove(uuid);
    tx.is_some_and(|tx| tx.send(()).is_ok())
}

// ── DTOs ──────────────────────────────────────────────────────────────────────

#[derive(Serialize, Clone)]
pub struct InstanceDto {
    pub name: String,
    pub version: String,
    pub loader: String,
    pub last_played: u64,
    pub status: InstanceStatus,
    pub cover_image: Option<PathBuf>,
    pub icon: Option<String>,
    pub uuid: String,
    pub path: PathBuf,
}

// ── Validación ────────────────────────────────────────────────────────────────

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
    if name.contains('/') || name.contains('\\') || name.contains('\0') || name.contains("..") {
        return Err("El nombre contiene caracteres no permitidos (/, \\, .., \\0).".into());
    }
    Ok(())
}
