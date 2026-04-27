use crate::core::SettingsManager;
use serde::Serialize;
use tauri::command;

#[command]
pub fn get_settings() -> SettingsManager {
    let settings = SettingsManager::get().lock().unwrap();
    settings.clone()
}

#[command]
pub fn update_settings(new_settings: SettingsManager) -> Result<(), String> {
    let mut settings = SettingsManager::get().lock().unwrap();
    *settings = new_settings;
    settings.dirty = true;
    settings.save();
    Ok(())
}

#[derive(Serialize)]
pub struct JavaPaths {
    jre8: String,
    jre17: String,
    jre21: String,
}

#[command]
pub fn detect_java_paths() -> Result<JavaPaths, String> {
    let mut paths = JavaPaths {
        jre8: String::new(),
        jre17: String::new(),
        jre21: String::new(),
    };

    #[cfg(target_os = "windows")]
    {
        // Simple heuristic for Windows
        let base_dirs = [
            "C:\\Program Files\\Java",
            "C:\\Program Files\\Eclipse Adoptium",
            "C:\\Program Files\\AdoptOpenJDK",
        ];

        for base in base_dirs {
            if let Ok(entries) = std::fs::read_dir(base) {
                for entry in entries.flatten() {
                    let path = entry.path();
                    if path.is_dir() {
                        let name = path
                            .file_name()
                            .unwrap_or_default()
                            .to_string_lossy()
                            .to_lowercase();
                        let exact_java = path.join("bin").join("javaw.exe");
                        if exact_java.exists() {
                            if name.contains("1.8") || name.contains("-8") {
                                if paths.jre8.is_empty() {
                                    paths.jre8 = exact_java.to_string_lossy().into_owned();
                                }
                            } else if name.contains("-17") || name.contains("17.") {
                                if paths.jre17.is_empty() {
                                    paths.jre17 = exact_java.to_string_lossy().into_owned();
                                }
                            } else if name.contains("-21") || name.contains("21.") {
                                if paths.jre21.is_empty() {
                                    paths.jre21 = exact_java.to_string_lossy().into_owned();
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    #[cfg(any(target_os = "linux", target_os = "macos"))]
    {
        // Simple heuristic for Linux
        let base_dir = "/usr/lib/jvm";
        if let Ok(entries) = std::fs::read_dir(base_dir) {
            for entry in entries.flatten() {
                let path = entry.path();
                if path.is_dir() {
                    let name = path
                        .file_name()
                        .unwrap_or_default()
                        .to_string_lossy()
                        .to_lowercase();
                    let exact_java = path.join("bin").join("java");
                    if exact_java.exists() {
                        if name.contains("-8-") || name.contains("1.8.0") {
                            if paths.jre8.is_empty() {
                                paths.jre8 = exact_java.to_string_lossy().into_owned();
                            }
                        } else if name.contains("-17-")
                            || name.ends_with("-17")
                            || name.contains("17-")
                        {
                            if paths.jre17.is_empty() {
                                paths.jre17 = exact_java.to_string_lossy().into_owned();
                            }
                        } else if name.contains("-21-")
                            || name.ends_with("-21")
                            || name.contains("21-")
                        {
                            if paths.jre21.is_empty() {
                                paths.jre21 = exact_java.to_string_lossy().into_owned();
                            }
                        }
                    }
                }
            }
        }

        // Fallbacks if not found
        if paths.jre8.is_empty() {
            if Path::new("/usr/bin/java").exists() {
                paths.jre8 = "/usr/bin/java".to_string();
            }
        }
        if paths.jre17.is_empty() {
            if Path::new("/usr/bin/java").exists() {
                paths.jre17 = "/usr/bin/java".to_string();
            }
        }
        if paths.jre21.is_empty() {
            if Path::new("/usr/bin/java").exists() {
                paths.jre21 = "/usr/bin/java".to_string();
            }
        }
    }

    Ok(paths)
}
