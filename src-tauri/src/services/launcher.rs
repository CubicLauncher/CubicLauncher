use crate::core::path_manager::PathManager;
use crate::core::{AppError, AppEvent, AuthError, DownloadError, FsError, InstanceError, emit};
use crate::services::SettingsManager;
use crate::services::instance_manager::{
    InstanceHandle, InstanceStatus, register_kill_sender, unregister_kill_sender,
};
use aqua::{DownloadManager, DownloadProgress};
use dashmap::DashMap;
use launchwerk::models::VersionManifest;
use launchwerk::{LaunchConfig, Launchwerk};
use launchwerk::{auth::AccountType, auth::microsoft::MicrosoftAuth};
use std::sync::atomic::{AtomicU8, AtomicU64, Ordering};
use std::sync::{Arc, OnceLock};
use tauri::Emitter;
use tokio::fs;
use tokio::sync::mpsc;
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
    active: DashMap<String, DownloadHandle>,
}

impl DownloadQueue {
    pub fn get() -> &'static Arc<DownloadQueue> {
        DOWNLOAD_QUEUE
            .get()
            .expect("BUG: DownloadQueue usado antes de inicializar")
    }

    pub async fn init(_app_handle: Option<tauri::AppHandle>) -> Arc<Self> {
        // Canal ilimitado en la práctica — las descargas no son tan frecuentes
        let (tx, rx) = mpsc::channel::<String>(64);

        let queue = Arc::new(Self {
            sender: tx,
            active: DashMap::new(),
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
            if let Some(handle) = self.active.get(&version)
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
        self.active.insert(version.clone(), handle);

        if let Err(e) = self.sender.send(version).await {
            error!("Error al encolar descarga: {}", e);
        }
    }

    pub async fn get_active_downloads(&self) -> Vec<DownloadHandle> {
        self.active
            .iter()
            .filter(|r| r.value().is_active())
            .map(|r| r.value().clone())
            .collect()
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
                match queue.active.get(&version) {
                    Some(h) => h.clone(),
                    None => {
                        error!("Handle no encontrado para versión {}, saltando", version);
                        continue;
                    }
                }
            };

            handle.set_status(DownloadStatus::Downloading);

            let shared_dir = PathManager::get().get_shared_dir().to_path_buf();
            let manager = DownloadManager::new(shared_dir);

            let download_handle = match manager.prepare(&version).await {
                Ok(h) => h,
                Err(_) => {
                    if version.starts_with("fabric-loader-") {
                        let game_version = version.split('-').next_back().unwrap_or("");
                        match manager.prepare(game_version).await {
                            Ok(h) => h,
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

            let (tx, mut progress_rx) = mpsc::channel::<DownloadProgress>(100);
            let handle_for_monitor = handle.clone();
            let version_for_monitor = version.clone();

            let monitor_task = tokio::spawn(async move {
                let mut interval = tokio::time::interval(std::time::Duration::from_millis(150));
                interval.tick().await;
                let mut latest: Option<DownloadProgress> = None;

                loop {
                    tokio::select! {
                        biased;
                        _ = interval.tick() => {
                            if let Some(ref p) = latest {
                                emit(AppEvent::DProgress {
                                    version: version_for_monitor.to_string(),
                                    current: p.current as u32,
                                    total: p.total as u32,
                                    d_type: format!("{:?}", p.download_type),
                                });
                            }
                        }
                        maybe = progress_rx.recv() => {
                            match maybe {
                                Some(progress) => {
                                    handle_for_monitor
                                        .update_progress(progress.current as u64, progress.total as u64);
                                    latest = Some(progress);
                                }
                                None => break,
                            }
                        }
                    }
                }

                if let Some(p) = latest {
                    emit(AppEvent::DProgress {
                        version: version_for_monitor.to_string(),
                        current: p.current as u32,
                        total: p.total as u32,
                        d_type: format!("{:?}", p.download_type),
                    });
                }
            });

            match download_handle.download_all(Some(tx)).await {
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

            queue.active.retain(|_, h| h.is_active());
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
        LAUNCHER
            .get()
            .expect("BUG: Launcher usado antes de inicializar")
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
                    if let Err(e) = user.save_tokens() {
                        warn!("Error guardando tokens: {:?}", e);
                    }
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

                {
                    let guard = self.app_handle.lock().unwrap_or_else(|e| e.into_inner());
                    if let Some(ref app) = *guard {
                        let stdout_rx = lw_handle.subscribe_stdout();
                        let stderr_rx = lw_handle.subscribe_stderr();
                        let id = handle.uuid.clone();

                        let app_stdout = app.clone();
                        let id_stdout = id.clone();
                        tokio::spawn(async move {
                            let mut rx = stdout_rx;
                            while let Ok(line) = rx.recv().await {
                                if app_stdout
                                    .emit(
                                        "instance-console-output",
                                        serde_json::json!({
                                            "id": id_stdout,
                                            "line": line,
                                            "stream": "stdout"
                                        }),
                                    )
                                    .is_err()
                                {
                                    break;
                                }
                            }
                        });

                        let app_stderr = app.clone();
                        tokio::spawn(async move {
                            let mut rx = stderr_rx;
                            while let Ok(line) = rx.recv().await {
                                if app_stderr
                                    .emit(
                                        "instance-console-output",
                                        serde_json::json!({
                                            "id": id,
                                            "line": line,
                                            "stream": "stderr"
                                        }),
                                    )
                                    .is_err()
                                {
                                    break;
                                }
                            }
                        });
                    } else {
                        warn!("AppHandle no disponible, no se reenviará stdout/stderr");
                    }
                }

                let (kill_tx, kill_rx) = tokio::sync::oneshot::channel::<()>();
                register_kill_sender(&handle.uuid, kill_tx);

                let uuid = handle.uuid.clone();
                let h = handle.clone();
                tokio::spawn(async move {
                    tokio::select! {
                        _ = kill_rx => {
                            info!("Kill signal received for {}", uuid);
                            if let Err(e) = lw_handle.kill().await {
                                warn!("Error al matar proceso {}: {:?}", uuid, e);
                            }
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

#[cfg(test)]
mod tests {
    use super::*;

    // ── AtomicDownloadStatus ──────────────────────────────────────────────

    /// Un `AtomicDownloadStatus` recién creado debe estar en `Pending`.
    #[test]
    fn test_download_status_pending() {
        let s = AtomicDownloadStatus::new();
        assert_eq!(s.get(), DownloadStatus::Pending);
    }

    /// Después de `set(Downloading)`, `get()` debe devolver `Downloading`.
    #[test]
    fn test_download_status_downloading() {
        let s = AtomicDownloadStatus::new();
        s.set(DownloadStatus::Downloading);
        assert_eq!(s.get(), DownloadStatus::Downloading);
    }

    /// Después de `set(Done)`, `get()` debe devolver `Done`.
    #[test]
    fn test_download_status_done() {
        let s = AtomicDownloadStatus::new();
        s.set(DownloadStatus::Done);
        assert_eq!(s.get(), DownloadStatus::Done);
    }

    /// Después de `set(Error("network failure"))`, `get()` debe devolver
    /// el mismo mensaje de error, verificando que el Mutex interno
    /// almacene correctamente el texto del error.
    #[test]
    fn test_download_status_error() {
        let s = AtomicDownloadStatus::new();
        s.set(DownloadStatus::Error("network failure".into()));
        assert_eq!(s.get(), DownloadStatus::Error("network failure".into()));
    }

    /// Verifica que el estado pueda transicionar en ciclo completo
    /// Pending → Downloading → Done → Pending sin estados inconsistentes.
    #[test]
    fn test_download_status_cycle() {
        let s = AtomicDownloadStatus::new();
        assert_eq!(s.get(), DownloadStatus::Pending);
        s.set(DownloadStatus::Downloading);
        assert_eq!(s.get(), DownloadStatus::Downloading);
        s.set(DownloadStatus::Done);
        assert_eq!(s.get(), DownloadStatus::Done);
        s.set(DownloadStatus::Pending);
        assert_eq!(s.get(), DownloadStatus::Pending);
    }

    // ── AtomicProgress ────────────────────────────────────────────────────

    /// Un `AtomicProgress` recién creado debe tener current=0 y total=0.
    #[test]
    fn test_progress_default() {
        let p = AtomicProgress::new();
        assert_eq!(p.get(), (0, 0));
    }

    /// Después de `update(50, 100)`, `get()` debe devolver `(50, 100)`.
    #[test]
    fn test_progress_update() {
        let p = AtomicProgress::new();
        p.update(50, 100);
        assert_eq!(p.get(), (50, 100));
    }

    /// Verifica que `update()` sobrescriba valores anteriores correctamente.
    #[test]
    fn test_progress_overwrite() {
        let p = AtomicProgress::new();
        p.update(10, 20);
        p.update(100, 200);
        assert_eq!(p.get(), (100, 200));
    }

    // ── DownloadHandle ────────────────────────────────────────────────────

    /// Un `DownloadHandle` en estado `Pending` debe considerarse activo.
    #[test]
    fn test_download_handle_is_active_pending() {
        let h = DownloadHandle::new("1.21".into());
        assert!(h.is_active());
    }

    /// Un `DownloadHandle` en estado `Downloading` debe considerarse activo.
    #[test]
    fn test_download_handle_is_active_downloading() {
        let h = DownloadHandle::new("1.21".into());
        h.set_status(DownloadStatus::Downloading);
        assert!(h.is_active());
    }

    /// Un `DownloadHandle` en estado `Done` NO debe considerarse activo.
    /// La descarga terminó, el frontend ya no debe mostrarlo como pendiente.
    #[test]
    fn test_download_handle_not_active_done() {
        let h = DownloadHandle::new("1.21".into());
        h.set_status(DownloadStatus::Done);
        assert!(!h.is_active());
    }

    /// Un `DownloadHandle` en estado `Error` NO debe considerarse activo.
    /// La descarga falló, no debe reintentarse automáticamente desde acá.
    #[test]
    fn test_download_handle_not_active_error() {
        let h = DownloadHandle::new("1.21".into());
        h.set_status(DownloadStatus::Error("err".into()));
        assert!(!h.is_active());
    }
}
