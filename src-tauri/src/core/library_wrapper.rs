use crate::core::instance_manager::Instance;
use crate::core::path_manager::PathManager;
use claunch_rs::{LaunchOptions, Launcher};
use proton::{resolve_version_data, MinecraftDownloader};
use std::collections::HashMap;
use std::collections::VecDeque;
use std::sync::Arc;
use std::sync::LazyLock;
use tokio::sync::Mutex;

static LAUNCHER: LazyLock<Mutex<LauncherWrapper>> =
    LazyLock::new(|| Mutex::new(LauncherWrapper::new()));

pub struct LauncherWrapper {
    queue: VecDeque<String>,
    is_downloading: bool,
    running_instances: Vec<Arc<Mutex<Instance>>>,
}

impl LauncherWrapper {
    pub fn get() -> &'static Mutex<LauncherWrapper> {
        &LAUNCHER
    }

    fn new() -> LauncherWrapper {
        LauncherWrapper {
            queue: VecDeque::new(),
            running_instances: Vec::new(),
            is_downloading: false,
        }
    }

    pub async fn queue_download(&mut self, version: String) {
        if self.queue.contains(&version) {
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

            let version_data = match resolve_version_data(version).await {
                Ok(v) => v,
                Err(_) => {
                    eprintln!("La version solicitada no existe");
                    self.is_downloading = false;
                    continue; // siguiente en la cola
                }
            };

            let mut downloader = MinecraftDownloader::new(
                PathManager::get().get_shared_dir().to_path_buf(),
                version_data,
            );

            match downloader.download_all(None).await {
                Ok(_) => println!("Version descargada correctamente"),
                Err(e) => eprintln!("No se pudo descargar: {:?}", e),
            }
        }
        self.is_downloading = false;
    }

    pub async fn launch(&mut self, instance: Arc<Mutex<Instance>>) {
        println!("=== CLaunch ===\n");

        // Estructura esperada:
        // game_dir/
        // ├── shared/
        // │   ├── libraries/    ← Aquí están las libs
        // │   ├── versions/     ← Aquí están los JARs
        // │   └── assets/       ← Aquí están los assets
        // └── instances/
        //     └── 1.20.1/       ← Directorio de la instancia
        let (version, min_mem, max_mem, is_running) = {
            let inst = instance.lock().await;
            (
                inst.get_version().to_string(),
                inst.get_min_memory(),
                inst.get_max_memory(),
                inst.get_is_running(),
            )
        };
        if is_running {
            println!("No se puede lanzar una instancia que ya esta lanzada");
            return;
        }
        let shared_dir = PathManager::get().get_shared_dir();
        let instance_dir = PathManager::get().get_instance_dir();
        let java_path = "/usr/lib/jvm/java-17-openjdk/bin/java";
        let version_json = shared_dir.join(format!("versions/{}/{}.json", version, version));
        if !version_json.exists() {
            println!("La instancia no se encuentra descargada. Descargando...");
            self.queue_download(version).await;
        }
        println!("Configuration:");
        println!("  Shared dir:      {}", shared_dir.display());
        println!("  Version JSON:  {}", version_json.display());
        println!("  Instance dir:  {}", instance_dir.display());
        println!("  Java:          {}", java_path);
        println!();

        // Launch options
        let options = LaunchOptions::new().with_demo(false);
        let mut custom_env = HashMap::new();
        custom_env.insert("DRI_PRIME".to_string(), "1".to_string());
        let instance_clone = Arc::clone(&instance);
        let child_result = tokio::task::spawn_blocking(move || {
            Launcher::launch_with_options_and_child(
                &version_json,
                &shared_dir,
                &instance_dir,
                "Player",
                &java_path,
                "2G",
                "4G",
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
                instance.lock().await.attach_process(child);

                tokio::spawn(async move {
                    loop {
                        tokio::time::sleep(tokio::time::Duration::from_millis(500)).await;
                        let finished = instance_clone.lock().await.check_and_detach();
                        if finished {
                            break;
                        }
                    }
                });
            }
            Ok(Err(e)) => eprintln!("\n❌ Launch failed: {}", e),
            Err(e) => eprintln!("\n❌ spawn_blocking panicked: {}", e),
        }
    }
    // este es el verify del ejemplo de claunch, hay q rehacelo
    // acorde a este proyecto
    // fn verify_structure(game_dir: &str, version_json: &str) {
    //     let mut errors = Vec::new();

    //     // Check version JSON
    //     if !Path::new(version_json).exists() {
    //         errors.push(format!("❌ Version JSON not found: {}", version_json));
    //     } else {
    //         println!("  ✓ Version JSON exists");
    //     }

    //     // Check libraries
    //     let lib_dir = Path::new(game_dir).join("shared/libraries");
    //     if !lib_dir.exists() {
    //         errors.push(format!(
    //             "❌ Libraries directory not found: {}",
    //             lib_dir.display()
    //         ));
    //     } else {
    //         println!("  ✓ Libraries directory exists");

    //         // Count libraries
    //         if let Ok(entries) = std::fs::read_dir(&lib_dir) {
    //             let count = entries.count();
    //             if count == 0 {
    //                 errors.push(format!("⚠️  Libraries directory is empty!"));
    //             } else {
    //                 println!("  ✓ Found {} items in libraries/", count);
    //             }
    //         }
    //     }

    //     // Check versions
    //     let versions_dir = Path::new(game_dir).join("shared/versions");
    //     if !versions_dir.exists() {
    //         errors.push(format!(
    //             "❌ Versions directory not found: {}",
    //             versions_dir.display()
    //         ));
    //     } else {
    //         println!("  ✓ Versions directory exists");
    //     }

    //     // Check assets
    //     let assets_dir = Path::new(game_dir).join("shared/assets");
    //     if !assets_dir.exists() {
    //         errors.push(format!(
    //             "⚠️  Assets directory not found: {}",
    //             assets_dir.display()
    //         ));
    //     } else {
    //         println!("  ✓ Assets directory exists");
    //     }

    //     if !errors.is_empty() {
    //         eprintln!("\nErrors found:");
    //         for error in errors {
    //             eprintln!("  {}", error);
    //         }
    //         eprintln!("\nPlease fix these issues before launching.");
    //         std::process::exit(1);
    //     }
    // }
}
