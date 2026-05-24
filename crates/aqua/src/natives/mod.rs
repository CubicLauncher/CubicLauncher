use std::path::{Path, PathBuf};

use crate::types::{MCVersion, VersionManifest};
#[cfg(feature = "extract-natives")]
use crate::utilities::extract_native_jar_sync;

#[cfg(feature = "extract-natives")]
use log::warn;
#[cfg(feature = "extract-natives")]
use crate::ProtonError;

pub fn natives_subdir(version: &MCVersion) -> &'static str {
    if version.major > 26 || (version.major == 26 && version.minor >= 2) {
        "java"
    } else {
        ""
    }
}

#[cfg(feature = "extract-natives")]
pub fn extract_natives(
    manifest: &VersionManifest,
    lib_dir: &Path,
    natives_dir: &Path,
) -> Result<(), ProtonError> {
    std::fs::create_dir_all(natives_dir)?;

    let Some(libraries) = &manifest.libraries else {
        return Ok(());
    };

    for lib in libraries {
        if !lib.should_include() {
            continue;
        }

        let jar_path = match lib.native_artifact() {
            Some(art) => lib_dir.join(&art.path),
            None => {
                if lib.is_native() {
                    lib_dir.join(lib.get_path())
                } else {
                    continue;
                }
            }
        };

        if !jar_path.exists() {
            warn!("Native JAR not found, skipping: {}", jar_path.display());
            continue;
        }

        extract_native_jar_sync(&jar_path, natives_dir)?;
    }
    Ok(())
}



#[allow(dead_code)]
pub fn list_native_jars(manifest: &VersionManifest, lib_dir: &Path) -> Vec<PathBuf> {
    let Some(libraries) = &manifest.libraries else {
        return vec![];
    };
    libraries
        .iter()
        .filter(|l| l.should_include() && l.is_native())
        .map(|l| lib_dir.join(l.get_path()))
        .collect()
}
