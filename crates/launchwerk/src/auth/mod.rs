// Copyright (C) 2026 Santiagolxx, Notstaff and CubicLauncher contributors
// SPDX-License-Identifier: AGPL-3.0-or-later
// src/auth/mod.rs
#![cfg(feature = "auth")]
pub mod microsoft;
pub mod storage;
#[cfg(test)]
mod tests;

use serde::{Deserialize, Serialize};
use storage::SecureStorage;

#[derive(Debug, Clone, Serialize, Deserialize)]
pub struct MinecraftUser {
    pub username: String,
    pub uuid: String,
    #[serde(skip)]
    pub access_token: String,
    #[serde(skip)]
    pub refresh_token: Option<String>,
    pub user_type: AccountType,
}

#[derive(Debug, Clone, Copy, PartialEq, Eq, Serialize, Deserialize)]
pub enum AccountType {
    Cracked,
    Microsoft,
}

impl MinecraftUser {
    /// Create a new cracked (offline) user
    pub fn cracked(username: impl Into<String>) -> Self {
        Self {
            username: username.into(),
            uuid: uuid::Uuid::new_v4().to_string(),
            access_token: "0".to_string(),
            refresh_token: None,
            user_type: AccountType::Cracked,
        }
    }

    /// Create a new premium user (from Microsoft auth)
    pub fn premium(
        username: String,
        uuid: String,
        access_token: String,
        refresh_token: Option<String>,
    ) -> Self {
        Self {
            username,
            uuid,
            access_token,
            refresh_token,
            user_type: AccountType::Microsoft,
        }
    }

    /// Save tokens to secure storage
    pub fn save_tokens(&self) -> crate::Result<()> {
        if self.user_type == AccountType::Microsoft {
            SecureStorage::save(&self.uuid, "access", &self.access_token)?;
            if let Some(refresh) = &self.refresh_token {
                SecureStorage::save(&self.uuid, "refresh", refresh)?;
            }
        }
        Ok(())
    }

    /// Load tokens from secure storage
    pub fn load_tokens(&mut self) -> crate::Result<()> {
        if self.user_type == AccountType::Microsoft {
            self.access_token = SecureStorage::load(&self.uuid, "access")?;
            if let Ok(token) = SecureStorage::load(&self.uuid, "refresh") {
                self.refresh_token = Some(token);
            }
        }
        Ok(())
    }

    /// Delete tokens from secure storage
    pub fn delete_tokens(&self) -> crate::Result<()> {
        if self.user_type == AccountType::Microsoft {
            SecureStorage::delete(&self.uuid, "access")?;
            SecureStorage::delete(&self.uuid, "refresh")?;
        }
        Ok(())
    }
}
