// Copyright (C) 2025 Santiagolxx, CubicLauncher contributors
// SPDX-License-Identifier: AGPL-3.0-or-later

mod loader;
mod manifest;
mod version;

pub use loader::Loader;
pub use manifest::{
    Argument, ArgumentValue, AssetIndex, JavaVersion, Library, LibraryArtifact, LibraryDownloads,
    Rule, VersionArgType, VersionManifest,
};
pub use version::MCVersion;
