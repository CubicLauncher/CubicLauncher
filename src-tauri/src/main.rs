// Prevents additional console window on Windows in release, DO NOT REMOVE!!
#![cfg_attr(not(debug_assertions), windows_subsystem = "windows")]

#[tokio::main]
async fn main() {
    tracing_subscriber::fmt().init();
    tracing::info!("Logger iniciado");

    let app_state = cubic_launcher_lib::core::ApplicationState::with_rpc_client(
        cubic_launcher_lib::core::setup_rpc().await.unwrap(),
    );
    {
        let mut state_guard = app_state.write().await;
        match state_guard.transition_to_playing("1.21.3").await {
            Ok(_) => println!("Transición exitosa a jugando"),
            Err(e) => eprintln!("Error en transición: {}", e),
        };
        tokio::time::sleep(tokio::time::Duration::from_secs(10)).await;
        match state_guard.transition_to_playing("1.12.2").await {
            Ok(_) => println!("Transición exitosa a jugando"),
            Err(e) => eprintln!("Error en transición: {}", e),
        };
    }
    tokio::spawn(async {
        cubic_launcher_lib::run().await;
    });
}
