use directories::UserDirs;
use std::env::{current_dir, current_exe, temp_dir};
use std::path::{Path, PathBuf};
use std::sync::LazyLock;

static PATH_MANAGER: LazyLock<PathManager> = LazyLock::new(PathManager::initialize);

pub struct PathManager {
    instances_dir: PathBuf,
    shared_dir: PathBuf,
    settings_dir: PathBuf,
}

impl PathManager {
    // Getters
    pub fn get() -> &'static PathManager {
        &PATH_MANAGER
    }
    // getters
    pub fn get_instance_dir(&self) -> &Path {
        &self.instances_dir
    }
    pub fn get_shared_dir(&self) -> &Path {
        &self.shared_dir
    }
    pub fn get_settings_dir(&self) -> &Path {
        &self.settings_dir
    }
    // Inicializador
    fn initialize() -> PathManager {
        let base_dir = resolve_base_dir();

        let instances_dir = base_dir.join(".cubic/instances");
        let shared_dir = base_dir.join(".cubic/shared");
        let settings_dir = base_dir.join(".cubic/settings");

        // Crear directorios si no existen
        std::fs::create_dir_all(&instances_dir)
            .unwrap_or_else(|e| panic!("No se pudo crear instances dir: {}", e));
        std::fs::create_dir_all(&shared_dir)
            .unwrap_or_else(|e| panic!("No se pudo crear shared dir: {}", e));
        std::fs::create_dir_all(&settings_dir)
            .unwrap_or_else(|e| panic!("No se pudo crear settings dir: {}", e));

        PathManager {
            instances_dir,
            shared_dir,
            settings_dir,
        }
    }
}

// utilidades
fn resolve_base_dir() -> PathBuf {
    // con la lib normal
    if let Some(d) = UserDirs::new() {
        return d.home_dir().to_path_buf();
    }
    // en el caso de que no obtenga path que use el path actual de donde se ejecuta el binario
    if let Ok(exe) = std::env::current_exe() {
        if let Some(parent) = exe.parent() {
            return parent.to_path_buf();
        }
    }
    // si eso no funciona
    // entonces dir de trabajo actual lpm
    if let Ok(cwd) = std::env::current_dir() {
        return cwd;
    }
    // si tampoco da entonces temp
    return temp_dir();
}
