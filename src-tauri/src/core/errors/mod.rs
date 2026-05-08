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
