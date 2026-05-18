use std::sync::mpsc;
use std::sync::OnceLock;
use std::time::Duration;

use crate::core::{AppEvent, PathManager, emit};

static WATCHER_TX: OnceLock<mpsc::Sender<Option<String>>> = OnceLock::new();

pub struct ThemeWatcher;

impl ThemeWatcher {
    pub fn watch(id: Option<String>) {
        if let Some(tx) = WATCHER_TX.get() {
            let _ = tx.send(id);
        }
    }

    pub async fn start() {
        let (tx, rx) = mpsc::channel();
        let _ = WATCHER_TX.set(tx);

        tokio::task::spawn_blocking(move || {
            let mut current_id: Option<String> = None;
            let mut last_mtime: Option<std::time::SystemTime> = None;

            loop {
                // Drenar cambios de theme pendientes
                loop {
                    match rx.try_recv() {
                        Ok(opt) => {
                            current_id = opt;
                            last_mtime = None;
                        }
                        Err(mpsc::TryRecvError::Disconnected) => return,
                        Err(mpsc::TryRecvError::Empty) => break,
                    }
                }

                // Polling del theme activo
                if let Some(ref id) = current_id {
                    let theme_file =
                        PathManager::get().get_themes_dir().join(id).join("theme.json");
                    match std::fs::metadata(&theme_file) {
                        Ok(meta) => {
                            if let Ok(mtime) = meta.modified()
                                && last_mtime != Some(mtime) {
                                    last_mtime = Some(mtime);
                                    emit(AppEvent::ThemeChanged {
                                        id: format!("user:{}", id),
                                    });
                                }
                        }
                        Err(_) => {
                            // El directorio fue eliminado
                            if last_mtime.is_some() {
                                last_mtime = None;
                                current_id = None;
                            }
                        }
                    }
                }

                std::thread::sleep(Duration::from_secs(1));
            }
        });
    }
}
