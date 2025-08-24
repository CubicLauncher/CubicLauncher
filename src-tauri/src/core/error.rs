use communicator::error::DiscordRpcError;
use thiserror::Error;

#[derive(Error, Debug)]
pub enum CubicErr {
    // Discord RPC - usando directamente los errores de communicator
    #[error("Discord RPC error: {0}")]
    DiscordRpcError(#[from] DiscordRpcError),

    // Validation errors específicos de tu aplicación
    #[error("Version string cannot be empty")]
    EmptyVersionError,

    #[error("Invalid version format: {0}")]
    InvalidVersionFormat(String),

    // State errors
    #[error("Invalid state transition from {from} to {to}")]
    InvalidStateTransition { from: String, to: String },

    #[error("Application is in an invalid state: {0}")]
    InvalidState(String),
}
