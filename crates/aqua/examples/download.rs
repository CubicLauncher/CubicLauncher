use aqua::{DownloadManager, DownloadProgress, DownloadProgressType};
use std::env;
use std::path::PathBuf;
use tokio::sync::mpsc;

#[tokio::main]
async fn main() -> Result<(), Box<dyn std::error::Error>> {
    let base_dir =
        env::var("BASE_DIR").unwrap_or_else(|_| "/home/santiagolxx/.cubic/shared".to_string());
    let version = env::var("VERSION").unwrap_or_else(|_| "26.2-snapshot-5".to_string());
    let max_handles: usize = env::var("MAX_HANDLES")
        .unwrap_or_else(|_| "1".to_string())
        .parse()
        .unwrap_or(1);

    let (tx, mut rx) = mpsc::channel::<DownloadProgress>(100);
    let progress_handle = tokio::spawn(async move {
        while let Some(prog) = rx.recv().await {
            let label = match prog.download_type {
                DownloadProgressType::Client => "CLIENT",
                DownloadProgressType::Library => "LIB",
                DownloadProgressType::Asset => "ASSET",
                DownloadProgressType::Native => "NATIVE",
                DownloadProgressType::Verifying => "VERIFY",
            };
            println!(
                "[{}/{}] [{label:6}] {}",
                prog.current, prog.total, prog.info.name
            );
        }
    });

    let manager = DownloadManager::new(PathBuf::from(&base_dir))
        .with_max_handles(max_handles)
        .with_max_downloads(128);

    let handle = manager.prepare(&version).await?;
    println!("=== Proton ===");
    println!("  version:   {}", handle.version().id);
    println!("  java:      {}", handle.version().java_version);
    println!("  libraries: {}", handle.version().libraries.len());
    println!("  natives:   {}", handle.version().natives.len());
    println!("  base_dir:  {base_dir}");
    println!();

    handle.start(Some(tx)).await?;
    handle.wait().await?;
    progress_handle.await?;

    println!("\n✓ Descarga completada: {}", handle.version().id);
    Ok(())
}
