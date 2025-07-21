/*
    CubicLauncher project
    Modulo encargado de manejar los paths del launcher
    Utilizando Once_cell para inicializar las variables una vez.
    [AUTHOR]
    Santiagolxx
*/
use directories::ProjectDirs;
use once_cell::sync::Lazy;
use std::io::{Error, ErrorKind};
use std::path::PathBuf;
use tokio::fs;
use tokio::fs::OpenOptions;
use tokio::sync::OnceCell;
use tracing::{error, info};

pub struct Paths {
    settings_path: PathBuf,
    data_path: PathBuf,
    runtime_path: PathBuf,
    instances_path: PathBuf,
    initialized: OnceCell<bool>,
}

// Static global PATHS, inicializado al primer acceso
pub static PATHS: Lazy<Paths> = Lazy::new(|| {
    let proj = ProjectDirs::from("com", "cubiclauncher", "kepler")
        .expect("No se pudo determinar el directorio de la aplicación");

    let data_dir = proj.data_dir().to_path_buf();

    Paths {
        settings_path: data_dir.join("settings.json"),
        data_path: data_dir.clone(),
        runtime_path: data_dir.join("runtime"),
        instances_path: data_dir.join("instances"),
        initialized: OnceCell::new(),
    }
});

impl Paths {
    pub async fn initialize(&self) -> Result<(), Error> {
        // Verificar si ya se inicializó
        if self.initialized.get().is_some() {
            info!("Inicialización de paths ya realizada");
            return Ok(());
        }

        info!("Iniciando inicialización de paths...");

        // Crear el directorio de datos principal
        info!(
            "Creando directorio de datos en: {}",
            self.data_path.display()
        );
        if let Err(e) = fs::create_dir_all(&self.data_path).await {
            if e.kind() != ErrorKind::AlreadyExists {
                error!("Error al crear el directorio de datos: {}", e);
                return Err(e);
            }
        } else {
            info!("Directorio de datos creado exitosamente");
        }

        // Crear el directorio de runtime
        info!(
            "Creando directorio de runtime en: {}",
            self.runtime_path.display()
        );
        if let Err(e) = fs::create_dir_all(&self.runtime_path).await {
            if e.kind() != ErrorKind::AlreadyExists {
                error!("Error al crear el directorio de runtime: {}", e);
                return Err(e);
            }
        } else {
            info!("Directorio de runtime creado exitosamente");
        }

        // Crear el directorio de instancias
        info!(
            "Creando directorio de instancias en: {}",
            self.instances_path.display()
        );
        if let Err(e) = fs::create_dir_all(&self.instances_path).await {
            if e.kind() != ErrorKind::AlreadyExists {
                error!("Error al crear el directorio de instancias: {}", e);
                return Err(e);
            }
        } else {
            info!("Directorio de instancias creado exitosamente");
        }

        // Crear el archivo de configuración
        info!(
            "Creando archivo de configuración en: {}",
            self.settings_path.display()
        );
        if let Err(e) = OpenOptions::new()
            .write(true)
            .create_new(true)
            .open(&self.settings_path)
            .await
        {
            if e.kind() != ErrorKind::AlreadyExists {
                error!("Error al crear el archivo de configuración: {}", e);
                return Err(e);
            }
        } else {
            info!("Archivo de configuración creado exitosamente");
        }

        if let Err(e) = self.initialized.set(true) {
            error!("Error al marcar como inicializado: {}", e);
            return Err(Error::other("No se pudo inicializar (estado ya seteado)"));
        }

        info!("Inicialización de paths completada");
        Ok(())
    }
}
