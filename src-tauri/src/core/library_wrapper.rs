use crate::core::instance_manager::Instance;
use crate::core::path_manager::PathManager;
use claunch_rs::{LaunchOptions, Launcher};
use proton::{resolve_version_data, MinecraftDownloader};
use std::collections::HashMap;
use std::collections::VecDeque;
use std::sync::Arc;
use std::sync::LazyLock;
use tokio::sync::Mutex;
use tokio::sync::RwLock;
use tracing::error;
use tracing::info;
use tracing::trace;
use tracing::warn;

static LAUNCHER: LazyLock<Mutex<LauncherWrapper>> =
    LazyLock::new(|| Mutex::new(LauncherWrapper::new()));

pub struct LauncherWrapper {
    queue: VecDeque<String>,
    is_downloading: bool,
}

impl LauncherWrapper {
    pub fn get() -> &'static Mutex<LauncherWrapper> {
        &LAUNCHER
    }

    fn new() -> LauncherWrapper {
        LauncherWrapper {
            queue: VecDeque::new(),
            is_downloading: false,
        }
    }

    pub async fn queue_download(&mut self, version: String) {
        trace!("Agregando {} a la cola de descargas", &version);
        if self.queue.contains(&version) {
            warn!("La version {} ya se encuentra en la cola", &version);
            return;
        }
        self.queue.push_back(version);
        if !self.is_downloading {
            self.start_new_download().await
        }
    }

    pub async fn start_new_download(&mut self) {
        while let Some(version) = self.queue.pop_front() {
            self.is_downloading = true;

            let version_data = match resolve_version_data(&version).await {
                Ok(v) => v,
                Err(_) => {
                    if version.starts_with("fabric-loader-") {
                        let parts: Vec<&str> = version.split('-').collect();
                        if let Some(game_version) = parts.last() {
                            match resolve_version_data(game_version).await {
                                Ok(v) => v,
                                Err(_) => {
                                    error!("No se pudo resolver la versión base {} para Fabric", game_version);
                                    self.is_downloading = false;
                                    continue;
                                }
                            }
                        } else {
                            error!("Formato de versión de Fabric inválido: {}", version);
                            self.is_downloading = false;
                            continue;
                        }
                    } else {
                        error!("La version solicitada no existe: {}", version);
                        self.is_downloading = false;
                        continue;
                    }
                }
            };

            let mut downloader = MinecraftDownloader::new(
                PathManager::get().get_shared_dir().to_path_buf(),
                version_data,
            );

            match downloader.download_all(None).await {
                Ok(_) => info!("Version {} descargada correctamente", &version),
                Err(e) => error!("No se pudo descargar: {:?}", e),
            }
        }
        self.is_downloading = false;
    }

    pub async fn launch(&mut self, instance: Arc<RwLock<Instance>>) -> Result<(), String> {
        trace!("=== CLaunch ===\n");

        // Estructura esperada:
        // game_dir/
        // ├── shared/
        // │   ├── libraries/    ← Aquí están las libs
        // │   ├── versions/     ← Aquí están los JARs
        // │   └── assets/       ← Aquí están los assets
        // └── instances/
        //     └── 1.20.1/       ← Directorio de la instancia
        let (version, _min_mem, _max_mem, is_running, name) = {
            let inst = instance.read().await;
            (
                inst.get_version().to_string(),
                inst.get_min_memory(),
                inst.get_max_memory(),
                inst.get_is_running(),
                inst.get_name().to_string(),
            )
        };
        if is_running {
            let msg = "No se puede lanzar una instancia que ya esta lanzada".to_string();
            warn!("{}", msg);
            return Err(msg);
        }
        let shared_dir = PathManager::get().get_shared_dir();
        let instance_dir = PathManager::get().get_instance_dir().join(name);
        
        let settings = crate::core::SettingsManager::get().lock().unwrap().clone();
        
        // Parse version to select java
        let parts: Vec<&str> = version.split('.').collect();
        let minor = parts.get(1).unwrap_or(&"0").parse::<u32>().unwrap_or(0);
        let patch = parts.get(2).unwrap_or(&"0").parse::<u32>().unwrap_or(0);
        
        let mut java_path = settings.get_jre17_path().to_string_lossy().into_owned(); // Default to 17
        
        if minor <= 16 {
            let p = settings.get_jre8_path().to_string_lossy().into_owned();
            if !p.is_empty() { java_path = p; }
        } else if minor >= 21 || (minor == 20 && patch >= 5) {
            let p = settings.get_jre21_path().to_string_lossy().into_owned();
            if !p.is_empty() { java_path = p; }
        } else {
            let p = settings.get_jre17_path().to_string_lossy().into_owned();
            if !p.is_empty() { java_path = p; }
        }
        
        if java_path.is_empty() {
            java_path = "java".to_string(); // fallback to generic `java` if literally nothing
        }

        let version_json = shared_dir.join(format!("versions/{}/{}.json", version, version));
        if !version_json.exists() {
            info!("La instancia no se encuentra descargada. Descargando...");
            self.queue_download(version).await;
        }
        trace!("Configuration:");
        trace!("  Shared dir:      {}", shared_dir.display());
        trace!("  Version JSON:  {}", version_json.display());
        trace!("  Instance dir:  {}", instance_dir.display());
        trace!("  Java:          {}", java_path);

        // Launch options
        let options = LaunchOptions::new().with_demo(false);
        let mut custom_env = HashMap::new();
        if settings.force_gpu {
            custom_env.insert("DRI_PRIME".to_string(), "1".to_string());
        }
        let instance_clone = Arc::clone(&instance);
        
        let username = settings.username.clone();
        let min_mem = format!("{}G", settings.min_memory);
        let max_mem = format!("{}G", settings.max_memory);
        
        let child_result = tokio::task::spawn_blocking(move || {
            Launcher::launch_with_options_and_child(
                &version_json,
                &shared_dir,
                &instance_dir,
                &username,
                &java_path,
                &min_mem,
                &max_mem,
                854,
                480,
                true,
                options,
                custom_env,
            )
            .map_err(|e| e.to_string())
        })
        .await;
        match child_result {
            Ok(Ok(child)) => {
                let mut instance = instance.write().await;
                instance.attach_process(child);
                tokio::spawn(async move {
                    loop {
                        tokio::time::sleep(tokio::time::Duration::from_millis(500)).await;
                        let finished = instance_clone.write().await.check_and_detach();
                        if finished {
                            break;
                        }
                    }
                });
                Ok(())
            }
            Ok(Err(e)) => {
                let msg = format!("Launch failed: {}", e);
                error!("\n❌ {}", msg);
                Err(msg)
            }
            Err(e) => {
                let msg = format!("spawn_blocking panicked: {}", e);
                error!("\n❌ {}", msg);
                Err(msg)
            }
        }
    }
}
