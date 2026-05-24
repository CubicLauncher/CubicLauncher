// Copyright (C) 2025 Santiagolxx, CubicLauncher contributors
// SPDX-License-Identifier: AGPL-3.0-or-later

#[derive(Debug, thiserror::Error)]
pub enum Error {
    #[error("IO error: {0}")]
    Io(#[from] std::io::Error),

    #[error("JSON parse error: {0}")]
    Json(#[from] serde_json::Error),

    #[error("Main class not found in version manifest")]
    MainClassNotFound,

    #[error("Classpath is empty – no libraries were resolved")]
    EmptyClasspath,

    #[error("Java binary not found at path: {0}")]
    JavaNotFound(String),

    #[error("Version file could not be loaded: {0}")]
    VersionLoad(String),

    #[error("Base (parent) version file could not be loaded: {0}")]
    BaseVersionLoad(String),

    #[error("Instance not found: {0}")]
    InstanceNotFound(String),

    // En error.rs, dentro del enum Error:
    #[error("Missing required file: {0}")]
    MissingFile(String),

    // Opcional: para múltiples archivos
    #[error("Missing required files:\n{0}")]
    MissingFiles(String),

    #[cfg(feature = "auth")]
    #[error("Auth error: {0}")]
    AuthError(String),
    #[cfg(feature = "auth")]
    #[error("HTTP request error: {0}")]
    Reqwest(#[from] reqwest::Error),
}
