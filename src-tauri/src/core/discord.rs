use crate::core::error::CubicErr;
use communicator::{Activity, Assets, DiscordRpcClient};
use std::sync::LazyLock;

// Constantes para evitar allocations repetidas
const APP_ID: &str = "1305247641252397059";
const LOGO_IMAGE: &str = "logo";
const RUST_IMAGE: &str = "rust";
const MINECRAFT_IMAGE: &str = "minecraft";
const CUBIC_MC_TEXT: &str = "CubicMC";
const IDLE_DETAILS: &str = "Idle";

// Assets reutilizables usando LazyLock (más eficiente que crear cada vez)
static DEFAULT_ASSETS: LazyLock<Assets> = LazyLock::new(|| Assets {
    large_image: Some(LOGO_IMAGE.to_string()),
    large_text: Some(CUBIC_MC_TEXT.to_string()),
    small_image: Some(RUST_IMAGE.to_string()),
    small_text: None,
});

// Cache para activities comunes
static IDLE_ACTIVITY: LazyLock<Activity> = LazyLock::new(|| Activity {
    details: Some(IDLE_DETAILS.to_string()),
    state: None,
    timestamps: None,
    assets: Some(DEFAULT_ASSETS.clone()),
    party: None,
    secrets: None,
    instance: None,
});

pub async fn setup_rpc() -> Result<DiscordRpcClient, CubicErr> {
    let client = DiscordRpcClient::new(APP_ID);
    Ok(client)
}

// Función optimizada que reutiliza el activity idle cacheado
pub fn default_activity() -> Activity {
    IDLE_ACTIVITY.clone()
}

// Función optimizada para crear playing activity
pub fn playing_activity(version: &str) -> Activity {
    // Reutilizar la estructura base y solo cambiar lo necesario
    Activity {
        details: Some(format!("Playing {version}")),
        state: None,
        timestamps: None,
        assets: Some(Assets {
            large_image: Some(LOGO_IMAGE.to_string()),
            large_text: Some(CUBIC_MC_TEXT.to_string()),
            small_image: Some(MINECRAFT_IMAGE.to_string()),
            small_text: Some(version.to_string()),
        }),
        party: None,
        secrets: None,
        instance: None,
    }
}
