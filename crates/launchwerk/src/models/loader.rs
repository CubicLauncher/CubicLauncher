// Copyright (C) 2025 Santiagolxx, CubicLauncher contributors
// SPDX-License-Identifier: AGPL-3.0-or-later

#[derive(Debug, Clone, PartialEq, Eq)]
pub enum Loader {
    Vanilla,
    Forge(String),
    NeoForge(String),
    Fabric(String),
}

impl Loader {
    /// Infer loader from a version ID string (e.g. "1.20.1-forge-47.2.0").
    pub fn from_version_id(id: &str) -> Self {
        let lower = id.to_lowercase();
        if lower.contains("neoforge") {
            Self::NeoForge(id.to_string())
        } else if lower.contains("forge") {
            Self::Forge(id.to_string())
        } else if lower.contains("fabric") {
            Self::Fabric(id.to_string())
        } else {
            Self::Vanilla
        }
    }
}

impl std::fmt::Display for Loader {
    fn fmt(&self, f: &mut std::fmt::Formatter<'_>) -> std::fmt::Result {
        match self {
            Self::Vanilla => write!(f, "vanilla"),
            Self::Forge(v) => write!(f, "forge ({v})"),
            Self::NeoForge(v) => write!(f, "neoforge ({v})"),
            Self::Fabric(v) => write!(f, "fabric ({v})"),
        }
    }
}
