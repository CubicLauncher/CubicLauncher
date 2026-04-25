mod commands;
mod core;
pub use core::InstanceManager;
#[cfg_attr(mobile, tauri::mobile_entry_point)]
pub fn run() {
    tauri::Builder::default()
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
            commands::instance::get_available_logos,
            commands::instance::get_instance_mods,
            commands::instance::toggle_instance_mod,
            commands::instance::get_instance_screenshot,
            commands::instance::get_instance_banner,
            commands::instance::get_all_instance_screenshots,
            commands::instance::set_instance_cover_image,
            commands::instance::reset_instance_cover_image,
            commands::download::add_to_queue,
            commands::download::get_available_versions,
            commands::download::get_fabric_versions,
            commands::download::download_fabric,
            commands::others::start_polling,
            commands::others::open_url,
            commands::settings::get_settings,
            commands::settings::update_settings,
            commands::settings::detect_java_paths,
            commands::auth::get_device_code,
            commands::auth::authenticate_with_device_code,
            commands::auth::get_current_user,
            commands::auth::logout,
        ])
        .run(tauri::generate_context!())
        .expect("error while running tauri application");
}
