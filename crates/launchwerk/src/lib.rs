// Copyright (C) 2025 Santiagolxx, CubicLauncher contributors
// SPDX-License-Identifier: AGPL-3.0-or-later

//! CLaunch - Minecraft launcher library for CubicLauncher
//!
//! Supports Vanilla, Forge, NeoForge and Fabric with version inheritance.
//!
//! # Example
//! ```no_run
//! use claunch::{Launchwerk, LaunchConfig};
//! use claunch::models::VersionManifest;
//! use std::path::PathBuf;
//!
//! #[tokio::main]
//! async fn main() {
//!     let lw = Launchwerk::new(PathBuf::from("/home/user/.cubic"));
//!     let manifest = VersionManifest::from_file("versions/1.21/1.21.json").unwrap();
//!     let config = LaunchConfig::default();
//!     let handle = lw.prepare(manifest, config).await.unwrap();
//!     handle.launch().await.unwrap();
//! }
//! ```

pub mod error;
pub mod handle;
pub mod launch_config;
pub mod launchwerk;
pub mod models;
pub mod natives;
pub mod resolvers;
pub mod utils;

pub use error::Error;
pub use handle::InstanceHandle;
pub use launch_config::LaunchConfig;
pub use launchwerk::Launchwerk;
pub use natives::extract_natives;

pub type Result<T> = std::result::Result<T, Error>;

#[cfg(feature = "auth")]
pub mod auth;

#[cfg(feature = "auth")]
pub use auth::MinecraftUser;
