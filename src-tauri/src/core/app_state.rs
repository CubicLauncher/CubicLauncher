use crate::core::{
    discord::{default_activity, playing_activity},
    error::CubicErr,
};
use communicator::DiscordRpcClient;
use std::borrow::Cow;
use std::sync::Arc;
use tokio::sync::RwLock;

/// Struct maestro con estructuras y estados de la app.
pub struct ApplicationState {
    app_state: AppState,
    rpc_client: Option<DiscordRpcClient>,
}

/// Enum que trae estados de la aplicacion
#[derive(Debug, Clone, PartialEq)]
pub enum AppState {
    Idle,
    PlayingVersion(Arc<str>), // Cambio: Arc<str> en lugar de String para evitar clones
}

impl ApplicationState {
    /// Constructor seguro - usando RwLock para mejor concurrencia
    pub fn new() -> Arc<RwLock<ApplicationState>> {
        Arc::new(RwLock::new(Self {
            app_state: AppState::Idle,
            rpc_client: None,
        }))
    }

    /// Constructor con cliente RPC
    pub fn with_rpc_client(client: DiscordRpcClient) -> Arc<RwLock<ApplicationState>> {
        Arc::new(RwLock::new(Self {
            app_state: AppState::Idle,
            rpc_client: Some(client),
        }))
    }

    /// Utilidad para configurar estados de la app - optimizada
    pub async fn set_app_state(&mut self, state: AppState) -> Result<(), CubicErr> {
        // Early return si el estado ya es el mismo (evita trabajo innecesario)
        if self.app_state == state {
            return Ok(());
        }

        // Verificar que tenemos un cliente RPC antes de continuar
        let rpc_client = match self.rpc_client.as_mut() {
            Some(client) => client,
            None => {
                // Si no hay cliente RPC, solo actualizamos el estado local
                self.app_state = state;
                return Ok(());
            }
        };

        // Actualizar la actividad según el estado
        let result = match &state {
            AppState::Idle => rpc_client.set_activity(default_activity()).await,
            AppState::PlayingVersion(version) => {
                if version.trim().is_empty() {
                    return Err(CubicErr::EmptyVersionError);
                }
                rpc_client.set_activity(playing_activity(version)).await
            }
        };

        result?;
        self.app_state = state;
        Ok(())
    }

    /// Método optimizado para transiciones de estado
    pub async fn transition_to_playing(
        &mut self,
        version: impl Into<Cow<'_, str>>,
    ) -> Result<(), CubicErr> {
        let version_str = version.into();

        // Validar que la versión no esté vacía
        if version_str.trim().is_empty() {
            return Err(CubicErr::EmptyVersionError);
        }

        // Validar formato de versión de forma más eficiente
        self.validate_version_format(&version_str)?;

        // Convertir a Arc<str> solo una vez
        let version_arc: Arc<str> = version_str.into();

        // Verificar si ya estamos jugando esta versión (evita trabajo innecesario)
        if let AppState::PlayingVersion(current) = &self.app_state {
            if current == &version_arc {
                return Ok(());
            }
        }

        self.set_app_state(AppState::PlayingVersion(version_arc))
            .await
    }

    /// Validación de formato más eficiente
    fn validate_version_format(&self, version: &str) -> Result<(), CubicErr> {
        // Validaciones más eficientes usando iteradores
        if version.len() > 50 {
            return Err(CubicErr::InvalidVersionFormat(
                "Version too long (max 50 characters)".to_string(),
            ));
        }

        // Usar any() es más eficiente que contains() múltiples
        if version
            .chars()
            .any(|c| c == '\n' || c == '\r' || c.is_control())
        {
            return Err(CubicErr::InvalidVersionFormat(
                "Version contains invalid characters".to_string(),
            ));
        }

        Ok(())
    }

    /// Transición segura a idle
    pub async fn transition_to_idle(&mut self) -> Result<(), CubicErr> {
        self.set_app_state(AppState::Idle).await
    }

    /// Getter de estado - usando referencia inmutable
    pub fn get_app_state(&self) -> &AppState {
        &self.app_state
    }

    /// Métodos de verificación más eficientes
    pub fn is_playing(&self) -> bool {
        matches!(self.app_state, AppState::PlayingVersion(_))
    }

    pub fn is_idle(&self) -> bool {
        matches!(self.app_state, AppState::Idle)
    }

    /// Obtener versión si está jugando - devuelve &str en lugar de &String
    pub fn get_current_version(&self) -> Option<&str> {
        match &self.app_state {
            AppState::PlayingVersion(version) => Some(version.as_ref()),
            _ => None,
        }
    }

    /// Configura el cliente rpc de la aplicacion
    pub fn set_rpc_client(&mut self, client: DiscordRpcClient) {
        self.rpc_client = Some(client);
    }

    /// Verificar si hay cliente RPC disponible
    pub fn has_rpc_client(&self) -> bool {
        self.rpc_client.is_some()
    }

    /// Getter de rpc más seguro
    pub fn get_rpc_client(&mut self) -> Option<&mut DiscordRpcClient> {
        self.rpc_client.as_mut()
    }

    /// Getter de rpc de solo lectura
    pub fn get_rpc_client_ref(&self) -> Option<&DiscordRpcClient> {
        self.rpc_client.as_ref()
    }

    /// Remover cliente RPC de forma más eficiente
    pub async fn disconnect_rpc(&mut self) -> Result<(), CubicErr> {
        if let Some(client) = self.rpc_client.take() {
            client.disconnect().await?;
        }
        Ok(())
    }

    /// Validar que el estado actual sea válido - optimizado
    pub fn validate_state(&self) -> Result<(), CubicErr> {
        match &self.app_state {
            AppState::Idle => Ok(()),
            AppState::PlayingVersion(version) => {
                if version.is_empty() {
                    Err(CubicErr::InvalidState(
                        "Playing version cannot be empty".to_string(),
                    ))
                } else {
                    Ok(())
                }
            }
        }
    }
}

// Implementación de Default para mayor ergonomía
impl Default for ApplicationState {
    fn default() -> Self {
        Self {
            app_state: AppState::Idle,
            rpc_client: None,
        }
    }
}

// Helper functions para uso más eficiente
impl ApplicationState {
    /// Método para leer el estado sin lock de escritura (solo para RwLock)
    pub async fn read_state<T>(
        state: &Arc<RwLock<ApplicationState>>,
        f: impl FnOnce(&ApplicationState) -> T,
    ) -> T {
        let guard = state.read().await;
        f(&guard)
    }

    /// Método para operaciones de escritura más seguras
    pub async fn write_state<T>(
        state: &Arc<RwLock<ApplicationState>>,
        f: impl FnOnce(&mut ApplicationState) -> T,
    ) -> T {
        let mut guard = state.write().await;
        f(&mut guard)
    }
}
