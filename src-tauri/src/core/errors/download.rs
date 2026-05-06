use thiserror::Error;

#[derive(Debug, Error)]
pub enum DownloadError {
    #[error("Error de red: {0}")]
    Request(String),

    #[error("Error al leer la respuesta: {0}")]
    ReadResponse(String),

    #[error("Error al parsear JSON: {0}")]
    ParseJson(String),

    #[error(transparent)]
    Fs(#[from] crate::core::errors::fs::FsError),

    #[error("No se encontró ningún loader de Fabric para esta versión")]
    NoFabricLoader,
}
