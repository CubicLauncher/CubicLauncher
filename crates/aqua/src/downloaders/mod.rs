use std::path::PathBuf;
use std::sync::atomic::{AtomicBool, AtomicUsize, Ordering};
use std::sync::Arc;

use futures::stream::{FuturesUnordered, StreamExt};
use log::info;
use tokio::sync::{Mutex, Semaphore};
use tokio::sync::mpsc::Sender;
use tokio::task::JoinHandle;

use crate::manifest::{resolve_asset_index, resolve_version_data};
use crate::natives::natives_subdir;
use crate::types::MCVersion;
use crate::types::{
    DownloadProgress, DownloadProgressInfo, DownloadProgressType, NormalizedVersion,
    RESOURCES_BASE_URL,
};
use crate::utilities::download_file;
#[cfg(feature = "extract-natives")]
use crate::utilities::extract_native;
use crate::ProtonError;

const DEFAULT_MAX_HANDLES: usize = 2;
const DEFAULT_DOWNLOADS_PER_HANDLE: usize = 128;
#[cfg(feature = "extract-natives")]
const MAX_CONCURRENT_EXTRACTIONS: usize = 8;

// ─── DownloadManager ──────────────────────────────────────────────────────────

pub struct DownloadManager {
    game_path: PathBuf,
    handle_semaphore: Arc<Semaphore>,
    downloads_per_handle: usize,
}

impl DownloadManager {
    pub fn new(game_path: PathBuf) -> Self {
        Self {
            game_path,
            handle_semaphore: Arc::new(Semaphore::new(DEFAULT_MAX_HANDLES)),
            downloads_per_handle: DEFAULT_DOWNLOADS_PER_HANDLE,
        }
    }

    pub fn with_max_handles(mut self, max: usize) -> Self {
        self.handle_semaphore = Arc::new(Semaphore::new(max));
        self
    }

    pub fn with_max_downloads(mut self, max: usize) -> Self {
        self.downloads_per_handle = max;
        self
    }

    pub async fn prepare(&self, version_id: &str) -> Result<DownloadHandle, ProtonError> {
        let version = resolve_version_data(version_id).await?;

        Ok(DownloadHandle {
            inner: Arc::new(DownloadInner {
                game_path: self.game_path.clone(),
                version,
                handle_sem: Arc::clone(&self.handle_semaphore),
                download_sem: Arc::new(Semaphore::new(self.downloads_per_handle)),
                cancel_flag: AtomicBool::new(false),
                join_handle: Mutex::new(None),
                completed_items: Arc::new(AtomicUsize::new(0)),
                total_items: Arc::new(AtomicUsize::new(0)),
            }),
        })
    }
}

// ─── DownloadHandle ───────────────────────────────────────────────────────────

struct DownloadInner {
    game_path: PathBuf,
    version: NormalizedVersion,
    handle_sem: Arc<Semaphore>,
    download_sem: Arc<Semaphore>,
    cancel_flag: AtomicBool,
    join_handle: Mutex<Option<JoinHandle<Result<(), ProtonError>>>>,
    completed_items: Arc<AtomicUsize>,
    total_items: Arc<AtomicUsize>,
}

pub struct DownloadHandle {
    inner: Arc<DownloadInner>,
}

impl DownloadHandle {
    pub fn version(&self) -> &NormalizedVersion {
        &self.inner.version
    }

    pub fn is_cancelled(&self) -> bool {
        self.inner.cancel_flag.load(Ordering::Relaxed)
    }

    pub fn progress(&self) -> (usize, usize) {
        let c = self.inner.completed_items.load(Ordering::Relaxed);
        let t = self.inner.total_items.load(Ordering::Relaxed);
        (c, t)
    }

    pub fn cancel(&self) {
        self.inner.cancel_flag.store(true, Ordering::Relaxed);
    }

    pub async fn download_all(
        &self,
        progress_tx: Option<Sender<DownloadProgress>>,
    ) -> Result<(), ProtonError> {
        self.start(progress_tx).await?;
        self.wait().await
    }

    pub async fn start(
        &self,
        progress_tx: Option<Sender<DownloadProgress>>,
    ) -> Result<(), ProtonError> {
        let mut slot = self.inner.join_handle.lock().await;
        if slot.is_some() {
            return Err(ProtonError::Other("Download already in progress or completed".into()));
        }

        let inner = Arc::clone(&self.inner);
        let handle = tokio::spawn(async move {
            let _handle_permit = Arc::clone(&inner.handle_sem).acquire_owned().await;
            run_download(inner, progress_tx).await
        });

        *slot = Some(handle);
        Ok(())
    }

    pub async fn wait(&self) -> Result<(), ProtonError> {
        let handle = self.inner.join_handle.lock().await.take();
        match handle {
            Some(h) => h.await?,
            None => Err(ProtonError::Other("Download not started".into())),
        }
    }
}

// ─── Internal ─────────────────────────────────────────────────────────────────

#[derive(Clone)]
struct DirPaths {
    natives_dir: PathBuf,
    objects_dir: PathBuf,
    libraries_dir: PathBuf,
    versions_dir: PathBuf,
    assets_indexes_dir: PathBuf,
}

fn compute_dirs(game_path: &PathBuf, version_id: &str, version: &MCVersion) -> DirPaths {
    let sub = natives_subdir(version);
    DirPaths {
        natives_dir: game_path.join("natives").join(version_id).join(sub),
        objects_dir: game_path.join("assets").join("objects"),
        libraries_dir: game_path.join("libraries"),
        versions_dir: game_path.join("versions").join(version_id),
        assets_indexes_dir: game_path.join("assets").join("indexes"),
    }
}

async fn run_download(
    inner: Arc<DownloadInner>,
    progress_tx: Option<Sender<DownloadProgress>>,
) -> Result<(), ProtonError> {
    let dirs = compute_dirs(&inner.game_path, &inner.version.id, &inner.version.parsed_version);

    tokio::fs::create_dir_all(&dirs.natives_dir).await?;
    tokio::fs::create_dir_all(&dirs.objects_dir).await?;
    tokio::fs::create_dir_all(&dirs.libraries_dir).await?;
    tokio::fs::create_dir_all(&dirs.versions_dir).await?;
    tokio::fs::create_dir_all(&dirs.assets_indexes_dir).await?;

    download_manifest_json(&inner, &dirs).await?;
    download_asset_index_json(&inner, &dirs).await?;

    let grand_total = {
        let asset_index = resolve_asset_index(&inner.version).await?;
        inner.version.libraries.len()
            + inner.version.natives.len()
            + 1
            + asset_index.len()
    };
    inner.total_items.store(grand_total, Ordering::Relaxed);

    let h1 = {
        let i = Arc::clone(&inner);
        let d = dirs.clone();
        let tx = progress_tx.clone();
        tokio::spawn(async move { download_natives(i, d, tx).await })
    };
    let h2 = {
        let i = Arc::clone(&inner);
        let d = dirs.clone();
        let tx = progress_tx.clone();
        tokio::spawn(async move { download_libraries(i, d, tx).await })
    };
    let h3 = {
        let i = Arc::clone(&inner);
        let d = dirs.clone();
        let tx = progress_tx.clone();
        tokio::spawn(async move { download_assets(i, d, tx).await })
    };
    let h4 = {
        let i = Arc::clone(&inner);
        let d = dirs;
        let tx = progress_tx;
        tokio::spawn(async move { download_client(i, d, tx).await })
    };

    let r = tokio::join!(h1, h2, h3, h4);
    r.0??;
    r.1??;
    r.2??;
    r.3??;

    info!("Download complete: {}", inner.version.id);
    Ok(())
}

// ─── Manifest JSON ────────────────────────────────────────────────────────────

async fn download_manifest_json(
    inner: &DownloadInner,
    dirs: &DirPaths,
) -> Result<(), ProtonError> {
    use crate::utilities::HTTP_CLIENT;

    let path = dirs.versions_dir.join(format!("{}.json", inner.version.id));
    if path.exists() {
        return Ok(());
    }

    let v2_url = crate::types::MOJANG_MANIFEST_URL;
    let v2: serde_json::Value = HTTP_CLIENT.get(v2_url).send().await?.json().await?;
    let entry = v2["versions"]
        .as_array()
        .and_then(|arr| arr.iter().find(|v| v["id"] == inner.version.id))
        .ok_or_else(|| ProtonError::VersionNotFound(inner.version.id.clone()))?;
    let detail_url = entry["url"]
        .as_str()
        .ok_or_else(|| ProtonError::Other("No URL in manifest".into()))?;

    let detail = HTTP_CLIENT.get(detail_url).send().await?.text().await?;
    tokio::fs::write(&path, detail).await?;
    Ok(())
}

// ─── Asset index ──────────────────────────────────────────────────────────────

async fn download_asset_index_json(
    inner: &DownloadInner,
    dirs: &DirPaths,
) -> Result<(), ProtonError> {
    let path = dirs
        .assets_indexes_dir
        .join(format!("{}.json", inner.version.asset_index.id));
    if path.exists() {
        let ok = crate::utilities::verify_file_hash(&path, &inner.version.asset_index.sha1)
            .await
            .unwrap_or(false);
        if ok {
            return Ok(());
        }
    }

    download_file(
        &inner.version.asset_index.url,
        &path,
        &inner.version.asset_index.sha1,
    )
    .await?;
    Ok(())
}

// ─── Libraries ────────────────────────────────────────────────────────────────

async fn download_libraries(
    inner: Arc<DownloadInner>,
    dirs: DirPaths,
    progress_tx: Option<Sender<DownloadProgress>>,
) -> Result<(), ProtonError> {
    let libs = &inner.version.libraries;
    let total = libs.len();
    if total == 0 {
        return Ok(());
    }

    let mut tasks = FuturesUnordered::new();
    let sem = Arc::clone(&inner.download_sem);
    let completed = Arc::clone(&inner.completed_items);
    let version_id = inner.version.id.clone();

    for lib in libs {
        if inner.cancel_flag.load(Ordering::Relaxed) {
            return Err(ProtonError::Cancelled);
        }

        let lib_path = dirs.libraries_dir.join(&lib.path);
        let url = lib.url.clone();
        let sha1 = lib.sha1.clone();
        let name = lib.name.clone();
        let tx = progress_tx.clone();
        let s = Arc::clone(&sem);
        let c = Arc::clone(&completed);
        let ti = Arc::clone(&inner.total_items);
        let vid = version_id.clone();

        tasks.push(tokio::spawn(async move {
            let _p = s.acquire_owned().await;
            if let Some(parent) = lib_path.parent() {
                tokio::fs::create_dir_all(parent).await?;
            }
            download_file(&url, &lib_path, &sha1).await?;
            let count = c.fetch_add(1, Ordering::Relaxed) + 1;
            report_progress(&tx, count, ti.load(Ordering::Relaxed), DownloadProgressType::Library, &vid, name).await;
            Ok::<_, ProtonError>(())
        }));
    }

    while let Some(res) = tasks.next().await {
        res??;
    }
    Ok(())
}

// ─── Natives ──────────────────────────────────────────────────────────────────

async fn download_natives(
    inner: Arc<DownloadInner>,
    dirs: DirPaths,
    progress_tx: Option<Sender<DownloadProgress>>,
) -> Result<(), ProtonError> {
    let natives = &inner.version.natives;
    let total = natives.len();
    if total == 0 {
        return Ok(());
    }

    let temp_dir = dirs
        .natives_dir
        .parent()
        .unwrap_or(&inner.game_path)
        .join("temp")
        .join(uuid::Uuid::new_v4().to_string());

    tokio::fs::create_dir_all(&temp_dir).await?;

    let mut tasks = FuturesUnordered::new();
    let mut jar_paths = Vec::with_capacity(total);
    let sem = Arc::clone(&inner.download_sem);
    let completed = Arc::clone(&inner.completed_items);
    let version_id = inner.version.id.clone();

    for native in natives {
        if inner.cancel_flag.load(Ordering::Relaxed) {
            return Err(ProtonError::Cancelled);
        }

        let filename = native
            .path
            .split('/')
            .next_back()
            .unwrap_or(&native.path)
            .to_string();
        let temp_path = temp_dir.join(&filename);
        jar_paths.push(temp_path.clone());

        let url = native.url.clone();
        let sha1 = native.sha1.clone();
        let name = native.name.clone();
        let tx = progress_tx.clone();
        let s = Arc::clone(&sem);
        let c = Arc::clone(&completed);
        let ti = Arc::clone(&inner.total_items);
        let vid = version_id.clone();

        tasks.push(tokio::spawn(async move {
            let _p = s.acquire_owned().await;
            download_file(&url, &temp_path, &sha1).await?;
            let count = c.fetch_add(1, Ordering::Relaxed) + 1;
            report_progress(&tx, count, ti.load(Ordering::Relaxed), DownloadProgressType::Native, &vid, name).await;
            Ok::<_, ProtonError>(())
        }));
    }

    while let Some(res) = tasks.next().await {
        res??;
    }

    #[cfg(feature = "extract-natives")]
    {
        let extract_sem = Arc::new(Semaphore::new(MAX_CONCURRENT_EXTRACTIONS));
        let mut ext_tasks = FuturesUnordered::new();
        let natives_dir = dirs.natives_dir.clone();

        for jar_path in jar_paths {
            let sem = Arc::clone(&extract_sem);
            let dest = natives_dir.clone();
            ext_tasks.push(tokio::spawn(async move {
                let _p = sem.acquire_owned().await;
                extract_native(&jar_path, &dest).await
            }));
        }

        while let Some(res) = ext_tasks.next().await {
            res??;
        }

        tokio::fs::remove_dir_all(&temp_dir).await?;
    }

    Ok(())
}

// ─── Assets ───────────────────────────────────────────────────────────────────

async fn download_assets(
    inner: Arc<DownloadInner>,
    dirs: DirPaths,
    progress_tx: Option<Sender<DownloadProgress>>,
) -> Result<(), ProtonError> {
    let asset_index = resolve_asset_index(&inner.version).await?;
    let total = asset_index.len();
    if total == 0 {
        return Ok(());
    }

    let mut tasks = FuturesUnordered::new();
    let sem = Arc::clone(&inner.download_sem);
    let completed = Arc::clone(&inner.completed_items);
    let version_id = inner.version.id.clone();

    for (name, asset) in asset_index.into_vec() {
        if inner.cancel_flag.load(Ordering::Relaxed) {
            return Err(ProtonError::Cancelled);
        }

        let hash = asset.hash;
        let subhash: String = hash.chars().take(2).collect();
        let url = format!("{}/{}/{}", RESOURCES_BASE_URL, subhash, hash);
        let path = dirs.objects_dir.join(&subhash).join(&hash);
        let hash_for_verify = hash;
        let tx = progress_tx.clone();
        let s = Arc::clone(&sem);
        let c = Arc::clone(&completed);
        let ti = Arc::clone(&inner.total_items);
        let vid = version_id.clone();

        tasks.push(tokio::spawn(async move {
            let _p = s.acquire_owned().await;
            if let Some(parent) = path.parent() {
                tokio::fs::create_dir_all(parent).await?;
            }
            download_file(&url, &path, &hash_for_verify).await?;
            let count = c.fetch_add(1, Ordering::Relaxed) + 1;
            report_progress(&tx, count, ti.load(Ordering::Relaxed), DownloadProgressType::Asset, &vid, name).await;
            Ok::<_, ProtonError>(())
        }));
    }

    while let Some(res) = tasks.next().await {
        res??;
    }
    Ok(())
}

// ─── Client JAR ───────────────────────────────────────────────────────────────

async fn download_client(
    inner: Arc<DownloadInner>,
    dirs: DirPaths,
    progress_tx: Option<Sender<DownloadProgress>>,
) -> Result<(), ProtonError> {
    let client = &inner.version.client_jar;
    if client.url.is_empty() {
        return Ok(());
    }

    if inner.cancel_flag.load(Ordering::Relaxed) {
        return Err(ProtonError::Cancelled);
    }

    let client_path = dirs.versions_dir.join(format!("{}.jar", inner.version.id));
    let _p = Arc::clone(&inner.download_sem).acquire_owned().await;
    download_file(&client.url, &client_path, &client.sha1).await?;
    let count = inner.completed_items.fetch_add(1, Ordering::Relaxed) + 1;

    report_progress(
        &progress_tx,
        count,
        inner.total_items.load(Ordering::Relaxed),
        DownloadProgressType::Client,
        &inner.version.id,
        format!("minecraft-{}", inner.version.id),
    )
    .await;

    Ok(())
}

// ─── Helpers ──────────────────────────────────────────────────────────────────

async fn report_progress(
    tx: &Option<Sender<DownloadProgress>>,
    current: usize,
    total: usize,
    dtype: DownloadProgressType,
    version_id: &str,
    name: impl Into<String>,
) {
    if let Some(tx) = tx {
        let _ = tx
            .send(DownloadProgress {
                current,
                total,
                info: DownloadProgressInfo {
                    name: name.into(),
                    version: Arc::new(version_id.to_string()),
                },
                download_type: dtype,
            })
            .await;
    }
}
