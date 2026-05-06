use thiserror::Error;

#[derive(Debug, Error)]
pub enum SettingsError {
    #[error("Lock de ajustes envenenado: {0}")]
    LockPoisoned(String),

    #[error("No se pudo serializar la configuración: {0}")]
    Serialize(String),

    #[error("No se pudo guardar la configuración: {0}")]
    SaveFailed(String),

    #[error("No se pudo leer la configuración: {0}")]
    LoadFailed(String),
}
