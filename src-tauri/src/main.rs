// Prevents additional console window on Windows in release, DO NOT REMOVE!!
#![cfg_attr(not(debug_assertions), windows_subsystem = "windows")]
use cubiclauncher_lib::InstanceManager;
#[tokio::main]
async fn main() {
    InstanceManager::init().await;
    tracing_subscriber::fmt::init();
    cubiclauncher_lib::run()
}
