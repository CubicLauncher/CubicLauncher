use crate::core::path_manager::PathManager;
use crate::core::{AppError, AppEvent, AuthError, DownloadError, FsError, InstanceError, emit};
use crate::services::instance_manager::{register_kill_sender, unregister_kill_sender, InstanceHandle, InstanceStatus};
use crate::services::SettingsManager;
use launchwerk::models::VersionManifest;
use launchwerk::{LaunchConfig, Launchwerk};
use launchwerk::{auth::AccountType, auth::microsoft::MicrosoftAuth};
use proton::{DownloadProgress, MinecraftDownloader, resolve_version_data};
use std::collections::HashMap;
use std::sync::atomic::{AtomicU8, AtomicU64, Ordering};
use std::sync::{Arc, OnceLock};
use tokio::fs;
use tokio::sync::{RwLock, mpsc};
use tracing::{error, info, trace, warn};
// ── Statics ───────────────────────────────────────────────────────────────────

static LAUNCHER: OnceLock<Arc<Launcher>> = OnceLock::new();
static DOWNLOAD_QUEUE: OnceLock<Arc<DownloadQueue>> = OnceLock::new();

// ── DownloadStatus ────────────────────────────────────────────────────────────

const DS_PENDING: u8 = 0;
const DS_DOWNLOADING: u8 = 1;
const DS_DONE: u8 = 2;
const DS_ERROR: u8 = 3;

#[derive(Debug, Clone, PartialEq, serde::Serialize)]
#[serde(rename_all = "lowercase")]
pub enum DownloadStatus {
    Pending,
    Downloading,
    Done,
    Error(String),
}

struct AtomicDownloadStatus {
    state: AtomicU8,
    error: std::sync::Mutex<String>,
}

impl AtomicDownloadStatus {
    fn new() -> Self {
        Self {
            state: AtomicU8::new(DS_PENDING),
            error: std::sync::Mutex::new(String::new()),
        }
    }

    fn get(&self) -> DownloadStatus {
        match self.state.load(Ordering::Acquire) {
            DS_DOWNLOADING => DownloadStatus::Downloading,
            DS_DONE => DownloadStatus::Done,
            DS_ERROR => {
                DownloadStatus::Error(self.error.lock().unwrap_or_else(|e| e.into_inner()).clone())
            }
            _ => DownloadStatus::Pending,
        }
    }

    fn set(&self, status: DownloadStatus) {
        match &status {
            DownloadStatus::Pending => self.state.store(DS_PENDING, Ordering::Release),
            DownloadStatus::Downloading => self.state.store(DS_DOWNLOADING, Ordering::Release),
            DownloadStatus::Done => self.state.store(DS_DONE, Ordering::Release),
            DownloadStatus::Error(e) => {
                *self.error.lock().unwrap_or_else(|e| e.into_inner()) = e.clone();
                self.state.store(DS_ERROR, Ordering::Release);
            }
        }
    }
}

// ── DownloadProgress (atomic, sin lock) ──────────────────────────────────────

struct AtomicProgress {
    current: AtomicU64,
    total: AtomicU64,
}

impl AtomicProgress {
    fn new() -> Self {
        Self {
            current: AtomicU64::new(0),
            total: AtomicU64::new(0),
        }
    }

    fn update(&self, current: u64, total: u64) {
        self.current.store(current, Ordering::Relaxed);
        self.total.store(total, Ordering::Relaxed);
    }

    fn get(&self) -> (u64, u64) {
        (
            self.current.load(Ordering::Relaxed),
            self.total.load(Ordering::Relaxed),
        )
    }
}

// ── DownloadHandle ────────────────────────────────────────────────────────────
//
// Clone es O(1) — solo incrementa reference counts.

#[derive(Clone)]
pub struct DownloadHandle {
    pub version: String,
    status: Arc<AtomicDownloadStatus>,
    progress: Arc<AtomicProgress>,
}

impl DownloadHandle {
    fn new(version: String) -> Self {
        Self {
            version,
            status: Arc::new(AtomicDownloadStatus::new()),
            progress: Arc::new(AtomicProgress::new()),
        }
    }

    pub fn get_status(&self) -> DownloadStatus {
        self.status.get()
    }

    pub fn get_progress(&self) -> (u64, u64) {
        self.progress.get()
    }

    pub fn is_active(&self) -> bool {
        matches!(
            self.get_status(),
            DownloadStatus::Pending | DownloadStatus::Downloading
        )
    }

    fn set_status(&self, status: DownloadStatus) {
        self.status.set(status);
    }

    fn update_progress(&self, current: u64, total: u64) {
        self.progress.update(current, total);
    }
}

// ── DownloadQueue ─────────────────────────────────────────────────────────────
//
// El sender es la única forma de encolar versiones.
// El worker task corre independiente — nunca bloquea al caller.

pub struct DownloadQueue {
    sender: mpsc::Sender<String>,
    active: RwLock<HashMap<String, DownloadHandle>>,
}

impl DownloadQueue {
    pub fn get() -> &'static Arc<DownloadQueue> {
        DOWNLOAD_QUEUE.get().expect("DownloadQueue no inicializada")
    }

    pub async fn init(_app_handle: Option<tauri::AppHandle>) -> Arc<Self> {
        // Canal ilimitado en la práctica — las descargas no son tan frecuentes
        let (tx, rx) = mpsc::channel::<String>(64);

        let queue = Arc::new(Self {
            sender: tx,
            active: RwLock::new(HashMap::new()),
        });

        let queue_clone = queue.clone();
        tokio::spawn(async move {
            if let Err(e) = Self::worker(rx, queue_clone).await {
                error!("Worker de descargas terminó con error: {}", e);
            }
        });

        let _ = DOWNLOAD_QUEUE.set(queue.clone());
        queue
    }

    /// Encola una versión. O(1), sin await de lock.
    /// Si ya está activa (pending o downloading), la ignora silenciosamente.
    pub async fn enqueue(&self, version: String) {
        {
            let active = self.active.read().await;
            if let Some(handle) = active.get(&version)
                && handle.is_active()
            {
                warn!("La version {} ya está en cola o descargándose", version);
                return;
            }
        }

        info!("{} Ha sido encolada en la cola de descargas", &version);

        // Registrar el handle antes de enviar al worker
        // para que get_active_downloads() refleje el estado inmediatamente
        let handle = DownloadHandle::new(version.clone());
        self.active.write().await.insert(version.clone(), handle);

        if let Err(e) = self.sender.send(version).await {
            error!("Error al encolar descarga: {}", e);
        }
    }

    /// Devuelve todos los handles activos (pending o downloading).
    pub async fn get_active_downloads(&self) -> Vec<DownloadHandle> {
        self.active
            .read()
            .await
            .values()
            .filter(|h| h.is_active())
            .cloned()
            .collect()
    }

    /// Devuelve el handle de una versión específica si existe.
    pub async fn get_handle(&self, version: &str) -> Option<DownloadHandle> {
        self.active.read().await.get(version).cloned()
    }

    // ── Worker ────────────────────────────────────────────────────────────────
    //
    // Loop independiente — nadie lo lockea desde afuera.
    // Lee del channel, descarga, actualiza el handle.

    async fn worker(
        mut rx: mpsc::Receiver<String>,
        queue: Arc<DownloadQueue>,
    ) -> Result<(), AppError> {
        while let Some(version) = rx.recv().await {
            let handle = {
                let active = queue.active.read().await;
                match active.get(&version) {
                    Some(h) => h.clone(),
                    None => {
                        error!("Handle no encontrado para versión {}, saltando", version);
                        continue;
                    }
                }
            };

            handle.set_status(DownloadStatus::Downloading);

            let version_data = match resolve_version_data(&version).await {
                Ok(v) => v,
                Err(_) => {
                    if version.starts_with("fabric-loader-") {
                        let game_version = version.split('-').next_back().unwrap_or("");
                        match resolve_version_data(game_version).await {
                            Ok(v) => v,
                            Err(_) => {
                                let msg = format!(
                                    "No se pudo resolver la versión base {} para Fabric",
                                    game_version
                                );
                                error!("{}", msg);
                                handle.set_status(DownloadStatus::Error(msg));
                                continue;
                            }
                        }
                    } else {
                        let msg = format!("La versión solicitada no existe: {}", version);
                        error!("{}", msg);
                        handle.set_status(DownloadStatus::Error(msg));
                        continue;
                    }
                }
            };

            let shared_dir = PathManager::get().get_shared_dir().to_path_buf();
            let mut downloader = MinecraftDownloader::new(shared_dir, version_data);

            let (tx, mut progress_rx) = mpsc::channel::<DownloadProgress>(100);
            let handle_for_monitor = handle.clone();
            let version_for_monitor = version.clone();

            let monitor_task = tokio::spawn(async move {
                while let Some(progress) = progress_rx.recv().await {
                    handle_for_monitor
                        .update_progress(progress.current as u64, progress.total as u64);

                    emit(AppEvent::DProgress {
                        version: version_for_monitor.to_string(),
                        current: progress.current as u32,
                        total: progress.total as u32,
                        d_type: format!("{:?}", progress.download_type),
                    });
                }
            });

            match downloader.download_all(Some(tx)).await {
                Ok(_) => {
                    info!("Versión {} descargada correctamente", version);
                    handle.set_status(DownloadStatus::Done);

                    emit(AppEvent::DFinish { version });
                }
                Err(e) => {
                    let msg = format!("No se pudo descargar {}: {:?}", version, e);
                    error!("{}", msg);
                    handle.set_status(DownloadStatus::Error(msg));
                }
            }

            let _ = monitor_task.await;

            queue.active.write().await.retain(|_, h| h.is_active());
        }

        error!("Worker de descargas terminó inesperadamente — el channel fue cerrado");
        Ok(())
    }
}

// ── Launcher ──────────────────────────────────────────────────────────────────
//
// Solo responsabilidad: lanzar instancias.
// Ya no mezcla la lógica de descargas.

pub struct Launcher {
    app_handle: std::sync::Mutex<Option<tauri::AppHandle>>,
    lw: Launchwerk,
}

impl Launcher {
    pub fn get() -> &'static Arc<Launcher> {
        LAUNCHER.get().expect("Launcher no inicializado")
    }

    pub fn init() -> Arc<Self> {
        let launcher = Arc::new(Self {
            app_handle: std::sync::Mutex::new(None),
            lw: Launchwerk::new(PathManager::get().get_shared_dir().to_path_buf()),
        });
        let _ = LAUNCHER.set(launcher.clone());
        launcher
    }

    pub fn set_handle(&self, handle: tauri::AppHandle) {
        *self.app_handle.lock().unwrap_or_else(|e| e.into_inner()) = Some(handle);
    }

    pub async fn launch(&self, handle: InstanceHandle) -> Result<(), AppError> {
        trace!("=== CubicLaunchwerk ===");

        if handle.is_busy() {
            let msg = "La instancia ya está corriendo o iniciando".to_string();
            warn!("{}", msg);
            return Err(AppError::Instance(InstanceError::AlreadyStarted));
        }
        handle.set_status(InstanceStatus::Starting);

        let settings_m = SettingsManager::snapshot();

        let version = handle.get_version().await;
        let name = handle.get_name().await;
        let shared_dir = PathManager::get().get_shared_dir().to_path_buf();
        let instance_dir = PathManager::get().get_instance_dir().join(&name);

        if !instance_dir.exists() {
            fs::create_dir(&instance_dir)
                .await
                .map_err(|e| FsError::CreateDir {
                    path: instance_dir.to_string_lossy().to_string(),
                    source: e,
                })?;
        }

        // Si la versión no está descargada, encolarla y salir con error descriptivo
        // El frontend puede escuchar "download-finished" y reintentar el launch
        let version_json = shared_dir.join(format!("versions/{}/{}.json", version, version));
        if !version_json.exists() {
            info!(
                "Versión {} no descargada, encolando descarga automática...",
                version
            );
            DownloadQueue::get().enqueue(version.clone()).await;
            handle.set_status(InstanceStatus::Off);
            return Err(AppError::Instance(InstanceError::NotFound));
        }

        let manifest = VersionManifest::from_file(version_json)
            .map_err(|e| DownloadError::ParseJson(e.to_string()))?;
        let mut user = SettingsManager::read().get_minecraft_user();

        let java_path = match manifest.java_version {
            Some(ref v) => match v.major_version {
                25 => settings_m.get_jre25_path(),
                21 => settings_m.get_jre21_path(),
                17 => settings_m.get_jre17_path(),
                8 => settings_m.get_jre8_path(),
                _ => settings_m.get_jre21_path(),
            },
            None => settings_m.get_jre25_path(),
        };

        // Auto-refresh del token Microsoft — el lock de settings se toma y suelta rápido
        if user.user_type == AccountType::Microsoft
            && let Some(refresh_token) = &user.refresh_token
        {
            info!("Refrescando token de Microsoft...");
            let rt = refresh_token.clone();
            let refresh_result = tokio::task::spawn_blocking(move || {
                MicrosoftAuth::default()
                    .refresh_token(&rt)
                    .map_err(|e| e.to_string())
            })
            .await
            .map_err(|e| AuthError::AuthFailed(e.to_string()))?;

            match refresh_result {
                Ok(new_user) => {
                    info!("Token refrescado para {}", new_user.username);
                    user = new_user;
                    let _ = user.save_tokens();
                    SettingsManager::write(|settings| {
                        settings.set_user(Some(user.clone()));
                    })?;
                    SettingsManager::save().await?;
                }
                Err(e) => {
                    warn!(
                        "No se pudo refrescar el token: {}. Continuando con el actual...",
                        e
                    );
                }
            }
        }

        let min_mem = format!("{}G", settings_m.get_min_memory());
        let max_mem = format!("{}G", settings_m.get_max_memory());

        let mut builder = LaunchConfig::builder()
            .java_path(java_path)
            .username(user.username)
            .ram(min_mem, max_mem)
            .cracked(user.user_type != AccountType::Microsoft);

        if user.user_type == AccountType::Microsoft {
            builder = builder
                .access_token(user.access_token)
                .auth_uuid(user.uuid)
                .user_type("msa");
        }

        for (k, v) in &settings_m.env_vars {
            if !k.is_empty() {
                builder = builder.env(k, v);
            }
        }

        let options = builder.build();

        let lw_handle = self.lw.prepare(manifest, options, instance_dir);
        handle.update_last_played().await;
        match lw_handle.launch().await {
            Ok(_) => {
                info!("Handle {} lanzado", lw_handle.id().to_string());
                handle.set_status(InstanceStatus::Started);

                let (kill_tx, kill_rx) = tokio::sync::oneshot::channel::<()>();
                register_kill_sender(&handle.uuid, kill_tx);

                let uuid = handle.uuid.clone();
                let h = handle.clone();
                tokio::spawn(async move {
                    tokio::select! {
                        _ = kill_rx => {
                            info!("Kill signal received for {}", uuid);
                            let _ = lw_handle.kill().await;
                            lw_handle.wait().await;
                        }
                        result = lw_handle.wait() => {
                            info!("Instance {} exited: {:?}", uuid, result);
                            unregister_kill_sender(&uuid);
                        }
                    }
                    h.set_status(InstanceStatus::Off);
                });
            }
            Err(e) => {
                error!("{}", e.to_string());
                handle.set_status(InstanceStatus::Error(e.to_string()));
            }
        }
        Ok(())
    }
}
