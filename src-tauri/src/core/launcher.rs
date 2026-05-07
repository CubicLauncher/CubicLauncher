use crate::core::SettingsManager;
use crate::core::instance_manager::{InstanceHandle, InstanceStatus};
use crate::core::path_manager::PathManager;
use claunch_rs::auth::microsoft::MicrosoftAuth;
use claunch_rs::resolvers::{CommandBuilder, DependencyResolver};
use claunch_rs::{AccountType, LaunchOptions, VersionInfo};
use proton::{DownloadProgress, MinecraftDownloader, resolve_version_data};
use std::collections::HashMap;
use std::sync::atomic::{AtomicU8, AtomicU64, Ordering};
use std::sync::{Arc, OnceLock};
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
            DS_ERROR => DownloadStatus::Error(self.error.lock().unwrap().clone()),
            _ => DownloadStatus::Pending,
        }
    }

    fn set(&self, status: DownloadStatus) {
        match &status {
            DownloadStatus::Pending => self.state.store(DS_PENDING, Ordering::Release),
            DownloadStatus::Downloading => self.state.store(DS_DOWNLOADING, Ordering::Release),
            DownloadStatus::Done => self.state.store(DS_DONE, Ordering::Release),
            DownloadStatus::Error(e) => {
                *self.error.lock().unwrap() = e.clone();
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
    app_handle: std::sync::Mutex<Option<tauri::AppHandle>>,
}

impl DownloadQueue {
    pub fn get() -> &'static Arc<DownloadQueue> {
        DOWNLOAD_QUEUE.get().expect("DownloadQueue no inicializada")
    }

    pub async fn init(app_handle: Option<tauri::AppHandle>) -> Arc<Self> {
        // Canal ilimitado en la práctica — las descargas no son tan frecuentes
        let (tx, rx) = mpsc::channel::<String>(64);

        let queue = Arc::new(Self {
            sender: tx,
            active: RwLock::new(HashMap::new()),
            app_handle: std::sync::Mutex::new(app_handle),
        });

        // Worker independiente — spawnado una sola vez, vive toda la app
        let queue_clone = queue.clone();
        tokio::spawn(async move {
            Self::worker(rx, queue_clone).await;
        });

        let _ = DOWNLOAD_QUEUE.set(queue.clone());
        queue
    }

    /// Encola una versión. O(1), sin await de lock.
    /// Si ya está activa (pending o downloading), la ignora silenciosamente.
    pub async fn enqueue(&self, version: String) {
        {
            let active = self.active.read().await;
            if let Some(handle) = active.get(&version) {
                if handle.is_active() {
                    warn!("La version {} ya está en cola o descargándose", version);
                    return;
                }
            }
        }

        trace!("Encolando versión {}", version);

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

    async fn worker(mut rx: mpsc::Receiver<String>, queue: Arc<DownloadQueue>) {
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
                    // Fallback para Fabric: intentar con la versión base
                    if version.starts_with("fabric-loader-") {
                        let game_version = version.split('-').last().unwrap_or("");
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

            // Canal de progreso — el monitor actualiza el handle atómicamente
            let (tx, mut progress_rx) = mpsc::channel::<DownloadProgress>(100);
            let app_handle = queue.app_handle.lock().unwrap().clone();
            let handle_for_monitor = handle.clone();
            let version_for_monitor = version.clone();

            let monitor_task = tokio::spawn(async move {
                while let Some(progress) = progress_rx.recv().await {
                    // Actualizar atómicamente — sin lock
                    handle_for_monitor
                        .update_progress(progress.current as u64, progress.total as u64);

                    if let Some(ref app) = app_handle {
                        use tauri::Emitter;
                        let _ = app.emit(
                            "download-progress",
                            serde_json::json!({
                                "version": version_for_monitor,
                                "current": progress.current,
                                "total":   progress.total,
                                "type":    format!("{:?}", progress.download_type),
                            }),
                        );
                    }
                }
            });

            match downloader.download_all(Some(tx)).await {
                Ok(_) => {
                    info!("Versión {} descargada correctamente", version);
                    handle.set_status(DownloadStatus::Done);

                    if let Some(ref app) = queue.app_handle.lock().unwrap().clone() {
                        use tauri::Emitter;
                        let _ = app.emit("download-finished", &version);
                    }
                }
                Err(e) => {
                    let msg = format!("No se pudo descargar {}: {:?}", version, e);
                    error!("{}", msg);
                    handle.set_status(DownloadStatus::Error(msg));
                }
            }

            let _ = monitor_task.await;

            // evitar q acomule descargas fallidas
            queue.active.write().await.retain(|_, h| h.is_active());
        }

        error!("Worker de descargas terminó inesperadamente — el channel fue cerrado");
    }
}

// ── Launcher ──────────────────────────────────────────────────────────────────
//
// Solo responsabilidad: lanzar instancias.
// Ya no mezcla la lógica de descargas.

pub struct Launcher {
    app_handle: std::sync::Mutex<Option<tauri::AppHandle>>,
}

impl Launcher {
    pub fn get() -> &'static Arc<Launcher> {
        LAUNCHER.get().expect("Launcher no inicializado")
    }

    pub fn init() -> Arc<Self> {
        let launcher = Arc::new(Self {
            app_handle: std::sync::Mutex::new(None),
        });
        let _ = LAUNCHER.set(launcher.clone());
        launcher
    }

    pub fn set_handle(&self, handle: tauri::AppHandle) {
        *self.app_handle.lock().unwrap() = Some(handle);
    }

    pub async fn launch(&self, handle: InstanceHandle) -> Result<(), String> {
        trace!("=== CLaunch ===");

        if handle.is_busy() {
            let msg = "La instancia ya está corriendo o iniciando".to_string();
            warn!("{}", msg);
            return Err(msg);
        }
        handle.set_status(InstanceStatus::Starting);

        // Leer todo lo necesario antes del spawn_blocking
        let version = handle.get_version().await;
        let name = handle.get_name().await;
        let shared_dir = PathManager::get().get_shared_dir().to_path_buf();
        let instance_dir = PathManager::get().get_instance_dir().join(&name);

        let java_path = SettingsManager::read().get_jre17_path().to_path_buf();

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
            return Err(format!(
                "La versión {} no está descargada. Iniciando descarga automática.",
                version
            ));
        }

        let mut user = SettingsManager::read().get_minecraft_user();

        // Auto-refresh del token Microsoft — el lock de settings se toma y suelta rápido
        if user.user_type == AccountType::Microsoft {
            if let Some(refresh_token) = &user.refresh_token {
                info!("Refrescando token de Microsoft...");
                let rt = refresh_token.clone();
                let refresh_result = tokio::task::spawn_blocking(move || {
                    MicrosoftAuth::default()
                        .refresh_token(&rt)
                        .map_err(|e| e.to_string())
                })
                .await
                .map_err(|e| e.to_string())?;

                match refresh_result {
                    Ok(new_user) => {
                        info!("Token refrescado para {}", new_user.username);
                        user = new_user;
                        let _ = user.save_tokens();
                        {
                            SettingsManager::write(|settings| {
                                settings.set_user(Some(user.clone()));
                                settings.save();
                            })?;
                        }
                    }
                    Err(e) => {
                        warn!(
                            "No se pudo refrescar el token: {}. Continuando con el actual...",
                            e
                        );
                    }
                }
            }
        }

        let min_mem = format!("{}G", SettingsManager::read().get_min_memory());
        let max_mem = format!("{}G", SettingsManager::read().get_max_memory());
        let options = LaunchOptions::new().with_demo(false);

        let mut custom_env: HashMap<String, String> = HashMap::new();
        if SettingsManager::read().force_gpu {
            custom_env.insert("DRI_PRIME".to_string(), "1".to_string());
        }

        let handle_for_monitor = handle.clone();

        let child_result = tokio::task::spawn_blocking(move || {
            let info = VersionInfo::new(&version_json, &shared_dir, &instance_dir)
                .map_err(|e| e.to_string())?;

            let mut resolver =
                DependencyResolver::new(info.lib_dir.clone(), info.natives_dir.clone());
            if info.has_inheritance() {
                if let Some(base_data) = &info.base_version_data {
                    resolver.process_version(base_data, false);
                }
            }
            resolver.process_version(&info.version_data, true);
            let classpath = resolver.build_classpath(&info);

            let user_type_str = match user.user_type {
                AccountType::Cracked => "mojang",
                AccountType::Microsoft => "msa",
            };

            let mut vars = HashMap::new();
            vars.insert("auth_player_name".to_string(), user.username.clone());
            vars.insert("version_name".to_string(), info.version_id.clone());
            vars.insert(
                "game_directory".to_string(),
                instance_dir.display().to_string(),
            );
            vars.insert(
                "assets_root".to_string(),
                info.assets_dir.display().to_string(),
            );
            vars.insert(
                "assets_index_name".to_string(),
                info.get_assets_index_name(),
            );
            vars.insert("auth_uuid".to_string(), user.uuid.clone());
            vars.insert("auth_access_token".to_string(), user.access_token.clone());
            vars.insert("user_type".to_string(), user_type_str.to_string());
            vars.insert("user_properties".to_string(), "{}".to_string());
            vars.insert(
                "version_type".to_string(),
                info.get_property("type").unwrap_or("release").to_string(),
            );
            #[cfg(windows)]
            vars.insert("classpath_separator".to_string(), ";".to_string());
            #[cfg(not(windows))]
            vars.insert("classpath_separator".to_string(), ":".to_string());
            vars.insert(
                "library_directory".to_string(),
                info.lib_dir.display().to_string(),
            );
            vars.insert(
                "natives_directory".to_string(),
                info.natives_dir.display().to_string(),
            );

            let main_class = info
                .get_property("mainClass")
                .ok_or("Main class not found")?;

            let mut builder = CommandBuilder::new(&info, vars, options);
            builder
                .add_java(&java_path)
                .add_jvm_args(&min_mem, &max_mem, user.user_type == AccountType::Cracked)
                .add_classpath(&classpath)
                .add_main_class(main_class)
                .add_game_args(854, 480);

            let args = builder.build();
            let mut cmd = std::process::Command::new(&args[0]);
            cmd.args(&args[1..])
                .current_dir(&instance_dir)
                .stdin(std::process::Stdio::inherit())
                .stdout(std::process::Stdio::inherit())
                .stderr(std::process::Stdio::inherit());

            for (key, value) in custom_env {
                cmd.env(key, value);
            }

            cmd.spawn().map_err(|e| e.to_string())
        })
        .await;

        match child_result {
            Ok(Ok(child)) => {
                handle.attach_process(child);
                handle.set_status(InstanceStatus::Started);
                handle.update_last_played().await;

                // Monitor de proceso — detecta cuando termina
                tokio::spawn(async move {
                    loop {
                        tokio::time::sleep(tokio::time::Duration::from_millis(500)).await;
                        if handle_for_monitor.check_and_detach() {
                            break;
                        }
                    }
                });

                Ok(())
            }
            Ok(Err(e)) => {
                let msg = format!("Launch failed: {}", e);
                error!("❌ {}", msg);
                handle.set_status(InstanceStatus::Error(msg.clone()));
                Err(msg)
            }
            Err(e) => {
                let msg = format!("spawn_blocking panicked: {}", e);
                error!("❌ {}", msg);
                handle.set_status(InstanceStatus::Error(msg.clone()));
                Err(msg)
            }
        }
    }
}
