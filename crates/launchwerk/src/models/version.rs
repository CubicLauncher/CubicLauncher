// Copyright (C) 2025 Santiagolxx, CubicLauncher contributors
// SPDX-License-Identifier: AGPL-3.0-or-later

use serde::{Deserialize, Deserializer};

/// A parsed Minecraft version number (e.g. "1.21.4" → major=1, minor=21, patch=Some(4)).
/// Si no se puede parsear (snapshots, custom), se guarda como (0, 0, None).
#[derive(Debug, Clone, Copy, PartialEq, Eq, Default)]
pub struct MCVersion {
    pub major: u8,
    pub minor: u8,
    pub patch: Option<u8>,
}

impl MCVersion {
    pub fn new(major: u8, minor: u8, patch: Option<u8>) -> Self {
        Self {
            major,
            minor,
            patch,
        }
    }
}

impl std::fmt::Display for MCVersion {
    fn fmt(&self, f: &mut std::fmt::Formatter<'_>) -> std::fmt::Result {
        match self.patch {
            Some(p) => write!(f, "{}.{}.{}", self.major, self.minor, p),
            None => write!(f, "{}.{}", self.major, self.minor),
        }
    }
}

impl<'de> Deserialize<'de> for MCVersion {
    fn deserialize<D: Deserializer<'de>>(deserializer: D) -> Result<Self, D::Error> {
        let s = String::deserialize(deserializer)?;
        Ok(parse_version(&s).unwrap_or_default()) // si falla, devuelve default (0.0)
    }
}

pub fn parse_version(s: &str) -> Option<MCVersion> {
    let parts: Vec<&str> = s.split('.').collect();
    if parts.len() < 2 {
        return None;
    }
    let major = parts[0].parse::<u8>().ok()?;
    let minor = parts[1].parse::<u8>().ok()?;
    let patch = parts.get(2).and_then(|p| p.parse().ok());
    Some(MCVersion {
        major,
        minor,
        patch,
    })
}
