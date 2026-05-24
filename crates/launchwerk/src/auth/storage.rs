// Copyright (C) 2026 Santiagolxx, Notstaff and CubicLauncher contributors
// SPDX-License-Identifier: AGPL-3.0-or-later
#![cfg(feature = "auth")]
use aes_gcm::{
    Aes256Gcm, Nonce,
    aead::{Aead, KeyInit},
};
use rand::{RngCore, rng};
use sha2::{Digest, Sha256};
use std::fs;
use std::path::PathBuf;

pub struct SecureStorage;

impl SecureStorage {
    fn get_key(uuid: &str) -> [u8; 32] {
        let mut hasher = Sha256::new();
        // Usamos el UUID y una sal interna para derivar la clave
        hasher.update(uuid.as_bytes());
        hasher.update(b"claunch-rs-secure-salt-v1");
        let result = hasher.finalize();
        let mut key = [0u8; 32];
        key.copy_from_slice(&result);
        key
    }

    pub fn save(uuid: &str, key_name: &str, value: &str) -> crate::Result<()> {
        let storage_dir = Self::ensure_storage_dir()?;
        let file_path = storage_dir.join(format!("{}_{}.enc", uuid, key_name));

        let key = Self::get_key(uuid);
        let cipher = Aes256Gcm::new_from_slice(&key)
            .map_err(|e| crate::Error::AuthError(format!("Cipher error: {}", e)))?;

        let mut nonce_bytes = [0u8; 12];
        rng().fill_bytes(&mut nonce_bytes);
        let nonce = Nonce::from_slice(&nonce_bytes);

        let ciphertext = cipher
            .encrypt(nonce, value.as_bytes())
            .map_err(|e| crate::Error::AuthError(format!("Encryption error: {}", e)))?;

        let mut data = nonce_bytes.to_vec();
        data.extend_from_slice(&ciphertext);

        fs::write(&file_path, data)?;

        // En Unix, restringir permisos al archivo
        #[cfg(unix)]
        {
            use std::os::unix::fs::PermissionsExt;
            let mut perms = fs::metadata(&file_path)?.permissions();
            perms.set_mode(0o600);
            fs::set_permissions(&file_path, perms)?;
        }

        Ok(())
    }

    pub fn load(uuid: &str, key_name: &str) -> crate::Result<String> {
        let storage_dir = Self::get_storage_dir()?;
        let file_path = storage_dir.join(format!("{}_{}.enc", uuid, key_name));

        if !file_path.exists() {
            return Err(crate::Error::AuthError(format!(
                "Secret not found for {}:{}",
                uuid, key_name
            )));
        }

        let data = fs::read(file_path)?;
        if data.len() < 12 {
            return Err(crate::Error::AuthError("Invalid secret file format".into()));
        }

        let (nonce_bytes, ciphertext) = data.split_at(12);
        let nonce = Nonce::from_slice(nonce_bytes);

        let key = Self::get_key(uuid);
        let cipher = Aes256Gcm::new_from_slice(&key)
            .map_err(|e| crate::Error::AuthError(format!("Cipher error: {}", e)))?;

        let plaintext = cipher
            .decrypt(nonce, ciphertext)
            .map_err(|e| crate::Error::AuthError(format!("Decryption failed: {}", e)))?;

        String::from_utf8(plaintext)
            .map_err(|e| crate::Error::AuthError(format!("UTF-8 error: {}", e)))
    }

    pub fn delete(uuid: &str, key_name: &str) -> crate::Result<()> {
        let storage_dir = Self::get_storage_dir()?;
        let file_path = storage_dir.join(format!("{}_{}.enc", uuid, key_name));
        if file_path.exists() {
            fs::remove_file(file_path)?;
        }
        Ok(())
    }

    fn get_storage_dir() -> crate::Result<PathBuf> {
        let mut path = dirs::data_dir().or_else(dirs::home_dir).ok_or_else(|| {
            crate::Error::AuthError("Could not determine storage directory".into())
        })?;
        path.push("claunch-rs");
        path.push("secrets");
        Ok(path)
    }

    fn ensure_storage_dir() -> crate::Result<PathBuf> {
        let path = Self::get_storage_dir()?;
        if !path.exists() {
            fs::create_dir_all(&path)?;
            #[cfg(unix)]
            {
                use std::os::unix::fs::PermissionsExt;
                let mut perms = fs::metadata(&path)?.permissions();
                perms.set_mode(0o700);
                fs::set_permissions(&path, perms)?;
            }
        }
        Ok(path)
    }
}
