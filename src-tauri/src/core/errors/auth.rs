use thiserror::Error;

#[derive(Debug, Error)]
pub enum AuthError {
    #[error("Error al obtener el device code: {0}")]
    DeviceCodeFailed(String),

    #[error("Error al autenticar con Microsoft: {0}")]
    AuthFailed(String),

    #[error("Error al guardar los tokens: {0}")]
    SaveTokensFailed(String),

    #[error("Error al eliminar los tokens: {0}")]
    DeleteTokensFailed(String),

    #[error("La tarea bloqueante falló: {0}")]
    SpawnBlocking(String),

    #[error(transparent)]
    CoreError(#[from] crate::core::errors::CoreError),
}
