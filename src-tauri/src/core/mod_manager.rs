use serde::{Deserialize, Serialize};
use std::fs::File;
use std::io::Read;
use std::path::Path;
use zip::ZipArchive;
use base64::{Engine as _, engine::general_purpose};

#[derive(Debug, Serialize, Deserialize, Clone)]
pub struct ModMetadata {
    pub name: String,
    pub version: Option<String>,
    pub description: Option<String>,
    pub authors: Option<Vec<String>>,
    pub icon: Option<String>, // Base64
}

pub struct ModManager;

impl ModManager {
    pub fn get_mod_info(path: &Path) -> Option<ModMetadata> {
        let file = File::open(path).ok()?;
        let mut archive = ZipArchive::new(file).ok()?;

        // Try Fabric
        if let Some(meta) = Self::parse_fabric(&mut archive) {
            return Some(meta);
        }

        // Try Quilt
        if let Some(meta) = Self::parse_quilt(&mut archive) {
            return Some(meta);
        }

        // Try Forge Modern
        if let Some(meta) = Self::parse_forge_modern(&mut archive) {
            return Some(meta);
        }

        // Try Forge Legacy
        if let Some(meta) = Self::parse_forge_legacy(&mut archive) {
            return Some(meta);
        }

        None
    }

    fn extract_icon(archive: &mut ZipArchive<File>, path: &str) -> Option<String> {
        let clean_path = path.trim_start_matches('/');
        let mut file = archive.by_name(clean_path).ok()?;
        let mut buffer = Vec::new();
        file.read_to_end(&mut buffer).ok()?;

        let mime_type = if clean_path.ends_with(".png") {
            "image/png"
        } else if clean_path.ends_with(".jpg") || clean_path.ends_with(".jpeg") {
            "image/jpeg"
        } else if clean_path.ends_with(".svg") {
            "image/svg+xml"
        } else {
            "image/png"
        };

        Some(format!("data:{};base64,{}", mime_type, general_purpose::STANDARD.encode(buffer)))
    }

    fn parse_fabric(archive: &mut ZipArchive<File>) -> Option<ModMetadata> {
        let json: serde_json::Value = {
            let mut file = archive.by_name("fabric.mod.json").ok()?;
            let mut content = String::new();
            file.read_to_string(&mut content).ok()?;
            serde_json::from_str(&content).ok()?
        };

        let name = json["name"].as_str()?.to_string();
        let version = json["version"].as_str().map(|s| s.to_string());
        let description = json["description"].as_str().map(|s| s.to_string());
        
        let authors = if let Some(authors_val) = json.get("authors") {
            if let Some(arr) = authors_val.as_array() {
                Some(arr.iter()
                    .filter_map(|v| {
                        if let Some(s) = v.as_str() {
                            Some(s.to_string())
                        } else if let Some(name) = v["name"].as_str() {
                            Some(name.to_string())
                        } else {
                            None
                        }
                    })
                    .collect())
            } else if let Some(s) = authors_val.as_str() {
                Some(vec![s.to_string()])
            } else {
                None
            }
        } else {
            None
        };

        let icon_path = json["icon"].as_str().map(|s| s.to_string());
        let icon = icon_path.and_then(|path| Self::extract_icon(archive, &path));

        Some(ModMetadata {
            name,
            version,
            description,
            authors,
            icon,
        })
    }

    fn parse_quilt(archive: &mut ZipArchive<File>) -> Option<ModMetadata> {
        let json: serde_json::Value = {
            let mut file = archive.by_name("quilt.mod.json").ok()?;
            let mut content = String::new();
            file.read_to_string(&mut content).ok()?;
            serde_json::from_str(&content).ok()?
        };

        let metadata = json.get("quilt_loader").and_then(|ql| ql.get("metadata")).unwrap_or(&json);

        let name = metadata["name"].as_str()?.to_string();
        let version = json.get("quilt_loader").and_then(|ql| ql["version"].as_str()).map(|s| s.to_string());
        let description = metadata["description"].as_str().map(|s| s.to_string());
        
        let authors = if let Some(contribs) = metadata.get("contributors") {
            if let Some(map) = contribs.as_object() {
                Some(map.keys().cloned().collect())
            } else {
                None
            }
        } else {
            None
        };

        let icon_path = metadata["icon"].as_str().map(|s| s.to_string());
        let icon = icon_path.and_then(|path| Self::extract_icon(archive, &path));

        Some(ModMetadata {
            name,
            version,
            description,
            authors,
            icon,
        })
    }

    fn parse_forge_modern(archive: &mut ZipArchive<File>) -> Option<ModMetadata> {
        let content = {
            let mut file = archive.by_name("META-INF/mods.toml").ok()?;
            let mut content = String::new();
            file.read_to_string(&mut content).ok()?;
            content
        };

        let toml_val: toml::Value = toml::from_str(&content).ok()?;
        let mods = toml_val.get("mods").and_then(|m| m.as_array())?;
        let first_mod = mods.first()?;

        let name = first_mod.get("displayName").and_then(|v| v.as_str())
            .or_else(|| first_mod.get("modId").and_then(|v| v.as_str()))?
            .to_string();
            
        let version = first_mod.get("version").and_then(|v| v.as_str()).map(|s| s.to_string());
        let description = first_mod.get("description").and_then(|v| v.as_str()).map(|s| s.to_string());
        let authors = first_mod.get("authors").and_then(|v| v.as_str()).map(|s| vec![s.to_string()]);
        
        let icon_path = first_mod.get("logoFile").and_then(|v| v.as_str()).map(|s| s.to_string());
        let icon = icon_path.and_then(|path| Self::extract_icon(archive, &path));

        Some(ModMetadata {
            name,
            version,
            description,
            authors,
            icon,
        })
    }

    fn parse_forge_legacy(archive: &mut ZipArchive<File>) -> Option<ModMetadata> {
        let json: serde_json::Value = {
            let mut file = archive.by_name("mcmod.info").ok()?;
            let mut content = String::new();
            file.read_to_string(&mut content).ok()?;
            serde_json::from_str(&content).ok()?
        };
        
        let mod_data = if let Some(arr) = json.as_array() {
            arr.first()?
        } else if let Some(obj) = json.get("modList").and_then(|m| m.as_array()) {
            obj.first()?
        } else {
            &json
        };

        let name = mod_data["name"].as_str()?.to_string();
        let version = mod_data["version"].as_str().map(|s| s.to_string());
        let description = mod_data["description"].as_str().map(|s| s.to_string());
        let authors = mod_data["authorList"].as_array().map(|arr| {
            arr.iter().filter_map(|v| v.as_str().map(|s| s.to_string())).collect()
        });

        let icon_path = mod_data["logoFile"].as_str().map(|s| s.to_string());
        let icon = icon_path.and_then(|path| Self::extract_icon(archive, &path));

        Some(ModMetadata {
            name,
            version,
            description,
            authors,
            icon,
        })
    }
}
