mod commands;
mod core;
mod services;
pub(crate) mod theme_watcher;

pub use services::InstanceManager;
#[cfg_attr(mobile, tauri::mobile_entry_point)]
pub fn run() {
    tauri::Builder::default()
        .plugin(tauri_plugin_dialog::init())
        .plugin(tauri_plugin_updater::Builder::new().build())
        .invoke_handler(tauri::generate_handler![
            commands::instance::get_instances,
            commands::instance::create_instance,
            commands::instance::launch,
            commands::instance::kill_instance,
            commands::instance::delete_instance,
            commands::instance::open_instance_dir,
            commands::instance::rename_instance,
            commands::instance::update_instance,
            commands::instance::get_installed_versions,
            commands::instance::get_instance_mods,
            commands::instance::toggle_instance_mod,
            commands::instance::get_instance_screenshot,
            commands::instance::get_instance_banner,
            commands::instance::get_all_instance_screenshots,
            commands::instance::set_instance_cover_image,
            commands::instance::reset_instance_cover_image,
            commands::instance::get_instance_resourcepacks,
            commands::instance::get_instance_logs,
            commands::instance::read_instance_log,
            commands::instance::delete_instance_file,
            commands::instance::add_instance_file,
            commands::download::add_to_queue,
            commands::download::get_available_versions,
            commands::download::get_fabric_versions,
            commands::download::download_fabric,
            commands::download::get_download_queue,
            commands::download::refresh_versions,
            commands::others::open_url,
            commands::settings::get_settings,
            commands::settings::update_settings,
            commands::settings::detect_java_paths,
            commands::auth::get_device_code,
            commands::auth::authenticate_with_device_code,
            commands::auth::get_current_user,
            commands::auth::logout,
            commands::themes::list_themes,
            commands::themes::get_user_theme,
            commands::themes::set_theme,
            commands::themes::get_current_theme,
            commands::themes::import_theme,
            commands::themes::get_themes_dir_path,
        ])
        .plugin(tauri_plugin_process::init())
        .setup(|app| {
            if let Err(errors) = core::PathManager::ensure_dirs() {
                use tauri_plugin_dialog::DialogExt;
                let handle = app.handle().clone();
                tauri::async_runtime::spawn(async move {
                    handle
                        .dialog()
                        .message(format!(
                            "No se pudieron crear los directorios necesarios:\n{}",
                            errors.join("\n")
                        ))
                        .title("Error de inicialización")
                        .kind(tauri_plugin_dialog::MessageDialogKind::Error)
                        .show(|_| std::process::exit(1));
                });
                return Err("Error de inicialización: no se pudieron crear los directorios".into());
            }

            let handle = app.handle().clone();
            tauri::async_runtime::spawn(async move {
                tokio::join!(
                    services::DownloadQueue::init(Some(handle.clone())),
                    async {
                        services::Launcher::init().set_handle(handle.clone());
                    },
                    InstanceManager::init(),
                    async {
                        core::init(handle.clone());
                    },
                    theme_watcher::ThemeWatcher::start(),
                );
                let theme = services::SettingsManager::read().theme.clone();
                if let Some(dir) = theme.strip_prefix("user:") {
                    theme_watcher::ThemeWatcher::watch(Some(dir.to_string()));
                }
            });
            Ok(())
        })
        .run(tauri::generate_context!())
        .expect("error while running tauri application");
}
