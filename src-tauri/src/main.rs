// Prevents additional console window on Windows in release, DO NOT REMOVE!!
#![cfg_attr(not(debug_assertions), windows_subsystem = "windows")]

#[tokio::main]
async fn main() {
    tracing_subscriber::fmt().init();
    tracing::info!("Logger iniciado");

    cubic_launcher_lib::run().await;
}
