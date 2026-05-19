use std::sync::LazyLock;
use std::time::Duration;

pub static HTTP: LazyLock<reqwest::Client> = LazyLock::new(|| {
    reqwest::Client::builder()
        .user_agent("CubicLauncher/26.4.3")
        .timeout(Duration::from_secs(30))
        .pool_max_idle_per_host(8)
        .build()
        .expect("Failed to create HTTP client")
});
