use crate::core::instance_manager::{InstanceHandle, InstanceStatus};
use crate::core::path_manager::PathManager;
use crate::core::SettingsManager;
use claunch_rs::auth::microsoft::MicrosoftAuth;
use claunch_rs::resolvers::{CommandBuilder, DependencyResolver};
use claunch_rs::{AccountType, LaunchOptions, VersionInfo};
use proton::{resolve_version_data, MinecraftDownloader};
use std::collections::HashMap;
use std::collections::VecDeque;
use std::sync::LazyLock;
use tokio::sync::Mutex;
use tracing::{error, info, trace, warn};

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

    fn new() -> Self {
        Self {
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
            self.start_new_download().await;
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
                                    error!(
                                        "No se pudo resolver la versión base {} para Fabric",
                                        game_version
                                    );
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

    pub async fn launch(&mut self, handle: InstanceHandle) -> Result<(), String> {
        trace!("=== CLaunch ===\n");

        // ── Guardia: no lanzar si ya está corriendo o iniciando ──────────
        if handle.is_busy() {
            let msg = "La instancia ya está corriendo o iniciando".to_string();
            warn!("{}", msg);
            return Err(msg);
        }
        handle.set_status(InstanceStatus::Starting);

        // ── Leer lo que necesitamos antes del spawn_blocking ─────────────
        let version = handle.get_version().await;
        let name = handle.get_name().await;

        let shared_dir = PathManager::get().get_shared_dir().to_path_buf();
        let instance_dir = PathManager::get().get_instance_dir().join(&name);

        let settings_raw = SettingsManager::get().lock().unwrap().clone();
        let java_path = settings_raw.get_jre21_path().to_string_lossy().into_owned();

        let version_json = shared_dir.join(format!("versions/{}/{}.json", version, version));
        if !version_json.exists() {
            info!("La instancia no se encuentra descargada. Descargando...");
            self.queue_download(version.clone()).await;
        }

        let mut user = settings_raw.get_minecraft_user();

        // ── Auto-refresh del token Microsoft ─────────────────────────────
        if user.user_type == AccountType::Microsoft {
            if let Some(refresh_token) = &user.refresh_token {
                info!("Refrescando token de Microsoft...");
                let rt = refresh_token.clone();
                let refresh_result = tokio::task::spawn_blocking(move || {
                    MicrosoftAuth::refresh_token(&rt).map_err(|e| e.to_string())
                })
                .await
                .map_err(|e| e.to_string())?;

                match refresh_result {
                    Ok(new_user) => {
                        info!("Token refrescado para {}", new_user.username);
                        user = new_user;
                        let _ = user.save_tokens();
                        let mut settings = SettingsManager::get().lock().unwrap();
                        settings.set_user(Some(user.clone()));
                        settings.save();
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

        let min_mem = format!("{}G", settings_raw.min_memory);
        let max_mem = format!("{}G", settings_raw.max_memory);
        let options = LaunchOptions::new().with_demo(false);
        let mut custom_env = HashMap::new();
        if settings_raw.force_gpu {
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

            let mut vars = HashMap::new();
            let user_type_str = match user.user_type {
                AccountType::Cracked => "mojang",
                AccountType::Microsoft => "msa",
            };
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

                // ── Monitor: detecta cuando el proceso termina ────────────
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
                error!("\n❌ {}", msg);
                handle.set_status(InstanceStatus::Error(msg.clone()));
                Err(msg)
            }
            Err(e) => {
                let msg = format!("spawn_blocking panicked: {}", e);
                error!("\n❌ {}", msg);
                handle.set_status(InstanceStatus::Error(msg.clone()));
                Err(msg)
            }
        }
    }
}
