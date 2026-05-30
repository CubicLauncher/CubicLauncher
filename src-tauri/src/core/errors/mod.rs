pub mod auth;
pub mod core;
pub mod download;
pub mod fs;
pub mod instance;

pub use auth::AuthError;
pub use core::CoreError;
pub use download::DownloadError;
pub use fs::FsError;
pub use instance::InstanceError;

use thiserror::Error;

#[derive(Debug, Error)]
pub enum AppError {
    #[error(transparent)]
    Instance(#[from] InstanceError),

    #[error(transparent)]
    CoreError(#[from] CoreError),

    #[error(transparent)]
    Auth(#[from] AuthError),

    #[error(transparent)]
    Download(#[from] DownloadError),
    #[error(transparent)]
    Fs(#[from] FsError),
}

impl From<AppError> for String {
    fn from(e: AppError) -> String {
        e.to_string()
    }
}
impl From<InstanceError> for String {
    fn from(e: InstanceError) -> String {
        e.to_string()
    }
}
impl From<CoreError> for String {
    fn from(e: CoreError) -> String {
        e.to_string()
    }
}
impl From<AuthError> for String {
    fn from(e: AuthError) -> String {
        e.to_string()
    }
}
impl From<DownloadError> for String {
    fn from(e: DownloadError) -> String {
        e.to_string()
    }
}
impl From<FsError> for String {
    fn from(e: FsError) -> String {
        e.to_string()
    }
}

#[cfg(test)]
mod tests {
    use super::*;

    /// `InstanceError::NotFound` debe convertirse a `AppError::Instance(InstanceError::NotFound)`.
    #[test]
    fn test_instance_error_into_app_error() {
        let err = AppError::from(InstanceError::NotFound);
        assert!(matches!(err, AppError::Instance(InstanceError::NotFound)));
    }

    /// `FsError` debe convertirse a `AppError::Fs(_)`.
    #[test]
    fn test_fs_error_into_app_error() {
        let fs_err = FsError::NotFound("/tmp/foo".into());
        let err = AppError::from(fs_err);
        assert!(matches!(err, AppError::Fs(_)));
    }

    /// `CoreError` debe convertirse a `AppError::CoreError(_)`.
    #[test]
    fn test_core_error_into_app_error() {
        let err = AppError::from(CoreError::LockPoisoned("test".into()));
        assert!(matches!(err, AppError::CoreError(_)));
    }

    /// `AuthError` debe convertirse a `AppError::Auth(_)`.
    #[test]
    fn test_auth_error_into_app_error() {
        let err = AppError::from(AuthError::AuthFailed("fail".into()));
        assert!(matches!(err, AppError::Auth(_)));
    }

    /// `DownloadError` debe convertirse a `AppError::Download(_)`.
    #[test]
    fn test_download_error_into_app_error() {
        let err = AppError::from(DownloadError::NoFabricLoader);
        assert!(matches!(err, AppError::Download(_)));
    }

    /// `InstanceError::NotFound` debe convertirse al string
    /// `"Instancia no encontrada"` para mostrar al usuario.
    #[test]
    fn test_instance_error_into_string() {
        let s: String = InstanceError::NotFound.into();
        assert_eq!(s, "Instancia no encontrada");
    }

    /// `FsError::NotFound("/x")` debe convertirse al string
    /// `"Archivo no encontrado: '/x'"` incluyendo el path.
    #[test]
    fn test_fs_error_into_string() {
        let s: String = FsError::NotFound("/x".into()).into();
        assert_eq!(s, "Archivo no encontrado: '/x'");
    }

    /// `CoreError::LockPoisoned("oh no")` debe convertirse al string
    /// `"Lock envenenado: oh no"`.
    #[test]
    fn test_core_error_into_string() {
        let s: String = CoreError::LockPoisoned("oh no".into()).into();
        assert_eq!(s, "Lock envenenado: oh no");
    }

    /// `AuthError::SaveTokensFailed("oops")` debe convertirse al string
    /// `"Error al guardar los tokens: oops"`.
    #[test]
    fn test_auth_error_into_string() {
        let s: String = AuthError::SaveTokensFailed("oops".into()).into();
        assert_eq!(s, "Error al guardar los tokens: oops");
    }

    /// `DownloadError::NoFabricLoader` debe convertirse al string
    /// `"No se encontró ningún loader de Fabric para esta versión"`.
    #[test]
    fn test_download_error_into_string() {
        let s: String = DownloadError::NoFabricLoader.into();
        assert_eq!(
            s,
            "No se encontró ningún loader de Fabric para esta versión"
        );
    }
}
