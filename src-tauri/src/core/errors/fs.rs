use thiserror::Error;

#[derive(Debug, Error)]
pub enum FsError {
    // lectura
    #[error("No se pudo leer el directorio '{path}': {source}")]
    ReadDir {
        path: String,
        #[source]
        source: std::io::Error,
    },

    #[error("No se pudo leer el archivo '{path}': {source}")]
    ReadFile {
        path: String,
        #[source]
        source: std::io::Error,
    },

    // escritura
    #[error("No se pudo crear el directorio '{path}': {source}")]
    CreateDir {
        path: String,
        #[source]
        source: std::io::Error,
    },

    #[error("No se pudo escribir el archivo '{path}': {source}")]
    WriteFile {
        path: String,
        #[source]
        source: std::io::Error,
    },

    // operaciones
    #[error("No se pudo copiar '{from}' a '{to}': {source}")]
    Copy {
        from: String,
        to: String,
        #[source]
        source: std::io::Error,
    },

    #[error("No se pudo renombrar '{from}' a '{to}': {source}")]
    Rename {
        from: String,
        to: String,
        #[source]
        source: std::io::Error,
    },

    #[error("No se pudo eliminar '{path}': {source}")]
    Remove {
        path: String,
        #[source]
        source: std::io::Error,
    },

    // paths xd
    #[error("Archivo no encontrado: '{0}'")]
    NotFound(String),

    #[error("Ruta inválida: '{0}'")]
    InvalidPath(String),
}
