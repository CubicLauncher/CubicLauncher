use std::collections::HashMap;
use std::path::{Path, PathBuf};

use log::info;

use crate::ProtonError;
use crate::natives::natives_subdir;
use crate::resolvers::ClasspathResolver;
use crate::types::{Argument, QuickPlay, VersionManifest};
use uuid::Uuid;

pub struct CommandBuilder<'a> {
    manifest: &'a VersionManifest,
    shared_dir: &'a Path,
    instance_dir: &'a Path,
    config: &'a LaunchConfig,
}

pub struct LaunchConfig {
    pub java_path: PathBuf,
    pub username: String,
    pub min_ram: String,
    pub max_ram: String,
    pub width: u32,
    pub height: u32,
    pub cracked: bool,
    pub demo_mode: bool,
    pub env: HashMap<String, String>,
    pub quick_play: Option<QuickPlay>,
    pub access_token: Option<String>,
    pub auth_uuid: Option<String>,
    pub user_type: Option<String>,
}

impl Default for LaunchConfig {
    fn default() -> Self {
        Self {
            java_path: PathBuf::from("java"),
            username: "Player".into(),
            min_ram: "512M".into(),
            max_ram: "2G".into(),
            width: 854,
            height: 480,
            cracked: false,
            demo_mode: false,
            env: HashMap::new(),
            quick_play: None,
            access_token: None,
            auth_uuid: None,
            user_type: None,
        }
    }
}

impl<'a> CommandBuilder<'a> {
    pub fn new(
        manifest: &'a VersionManifest,
        shared_dir: &'a Path,
        instance_dir: &'a Path,
        config: &'a LaunchConfig,
    ) -> Self {
        Self {
            manifest,
            shared_dir,
            instance_dir,
            config,
        }
    }

    fn resolve_manifest(&self) -> Result<(VersionManifest, String), ProtonError> {
        let mut current = self.manifest.clone();
        let mut seen = std::collections::HashSet::new();
        seen.insert(current.id_raw.clone());
        let mut base_id = current.id_raw.clone();

        while let Some(parent_id) = current.inherits_from.clone() {
            if !seen.insert(parent_id.clone()) {
                return Err(ProtonError::Other(format!(
                    "Circular inheritance: {}",
                    parent_id
                )));
            }
            let parent_path = self
                .shared_dir
                .join("versions")
                .join(&parent_id)
                .join(format!("{}.json", parent_id));
            let parent_manifest = VersionManifest::from_file(&parent_path)
                .map_err(|e| ProtonError::Other(format!("Failed to load parent: {e}")))?;
            current = current.resolve(&parent_manifest);
            base_id = parent_id;
        }
        Ok((current, base_id))
    }

    pub fn verify_requirements(
        &self,
        final_manifest: &VersionManifest,
        base_id: &str,
    ) -> Result<(), ProtonError> {
        let lib_dir = self.shared_dir.join("libraries");
        let classpath = final_manifest.get_classpath(&lib_dir);

        if !self.config.java_path.exists() {
            return Err(ProtonError::MissingFile(format!(
                "Java not found: {}",
                self.config.java_path.display()
            )));
        }

        #[cfg(unix)]
        {
            use std::os::unix::fs::PermissionsExt;
            let executable = self
                .config
                .java_path
                .metadata()
                .map(|m| m.permissions().mode() & 0o111 != 0)
                .unwrap_or(false);
            if !executable {
                return Err(ProtonError::MissingFile(format!(
                    "Java not executable: {}",
                    self.config.java_path.display()
                )));
            }
        }

        let separator = if cfg!(windows) { ';' } else { ':' };
        let mut missing = Vec::new();
        for entry in classpath.split(separator) {
            let p = Path::new(entry);
            if !p.exists() {
                missing.push(entry.to_string());
            }
        }
        if !missing.is_empty() {
            return Err(ProtonError::MissingFile(format!(
                "Missing classpath entries:\n  {}",
                missing.join("\n  ")
            )));
        }

        let version_jar = self
            .shared_dir
            .join("versions")
            .join(base_id)
            .join(format!("{}.jar", base_id));
        if !version_jar.exists() {
            return Err(ProtonError::MissingFile(format!(
                "Version JAR not found: {}",
                version_jar.display()
            )));
        }

        Ok(())
    }

    pub fn build(&self) -> Result<Vec<String>, ProtonError> {
        let (final_manifest, base_id) = self.resolve_manifest()?;

        let lib_dir = self.shared_dir.join("libraries");
        let assets_dir = self.shared_dir.join("assets");
        let sub = natives_subdir(&self.manifest.id);
        let natives_dir = self.shared_dir.join("natives").join(&base_id).join(sub);

        let classpath = ClasspathResolver::new(&final_manifest, &base_id, &lib_dir).build();
        if classpath.is_empty() {
            return Err(ProtonError::EmptyClasspath);
        }
        self.verify_requirements(&final_manifest, &base_id)?;

        let uuid = self
            .config
            .auth_uuid
            .clone()
            .unwrap_or_else(|| Uuid::new_v4().to_string());

        let vars = self.build_vars(
            &assets_dir,
            &natives_dir,
            &uuid,
            &classpath,
            &final_manifest,
        );

        let mut cmd: Vec<String> = Vec::new();
        cmd.push(self.config.java_path.to_string_lossy().to_string());

        self.add_jvm_flags(&mut cmd, &natives_dir, &vars, &final_manifest);
        cmd.push("-cp".to_string());
        cmd.push(classpath);

        let main_class = final_manifest
            .main_class
            .as_ref()
            .ok_or(ProtonError::MainClassNotFound)?
            .clone();
        cmd.push(main_class);

        self.add_game_args(&mut cmd, &vars, &final_manifest);
        self.add_default_game_args(&mut cmd, &assets_dir, &final_manifest);
        self.add_optional_args(&mut cmd);
        self.cleanup_unresolved(&mut cmd);

        Ok(cmd)
    }

    fn add_jvm_flags(
        &self,
        cmd: &mut Vec<String>,
        natives_dir: &Path,
        vars: &HashMap<String, String>,
        manifest: &VersionManifest,
    ) {
        cmd.push(format!("-Djava.library.path={}", natives_dir.display()));
        cmd.push("-Dminecraft.launcher.brand=CubicLauncher".to_string());
        cmd.push("-Dminecraft.launcher.version=2.0".to_string());

        if self.config.cracked {
            info!("Cracked mode enabled");
            cmd.push("-Dminecraft.api.env=custom".to_string());
            for host in &["auth.host", "account.host", "session.host", "services.host"] {
                cmd.push(format!("-Dminecraft.api.{}=https://invalid.invalid", host));
            }
        }

        cmd.push(format!("-Xms{}", self.config.min_ram));
        cmd.push(format!("-Xmx{}", self.config.max_ram));

        if let Some(args) = manifest.arguments.as_ref().and_then(|a| a.jvm.as_ref()) {
            for arg in args {
                let tokens = arg.get_if_applies();
                if tokens.len() == 2 && tokens[0] == "-cp" {
                    continue;
                }
                if tokens.iter().any(|t| t.contains("${classpath}")) {
                    continue;
                }
                for token in tokens {
                    let resolved = replace_vars(&token, vars);
                    if resolved == "-cp" || resolved.contains("${classpath}") {
                        continue;
                    }
                    cmd.push(resolved);
                }
            }
        }
    }

    fn add_game_args(
        &self,
        cmd: &mut Vec<String>,
        vars: &HashMap<String, String>,
        manifest: &VersionManifest,
    ) {
        if let Some(args) = manifest.arguments.as_ref().and_then(|a| a.game.as_ref()) {
            for arg in args {
                if let Argument::Plain(s) = arg {
                    if self.should_skip_arg(s) {
                        continue;
                    }
                }
                for s in arg.get_if_applies() {
                    if !self.should_skip_arg(&s) {
                        cmd.push(replace_vars(&s, vars));
                    }
                }
            }
            return;
        }
        if let Some(legacy) = &manifest.minecraft_arguments {
            for token in legacy.split_whitespace() {
                cmd.push(replace_vars(token, vars));
            }
        }
    }

    fn should_skip_arg(&self, arg: &str) -> bool {
        const DEMO_ARGS: &[&str] = &["--demo"];
        const QP_ARGS: &[&str] = &[
            "--quickPlaySingleplayer",
            "--quickPlayMultiplayer",
            "--quickPlayRealms",
            "--quickPlayPath",
        ];
        if DEMO_ARGS.contains(&arg) && !self.config.demo_mode {
            return true;
        }
        if QP_ARGS.contains(&arg) && self.config.quick_play.is_none() {
            return true;
        }
        false
    }

    fn add_default_game_args(
        &self,
        cmd: &mut Vec<String>,
        assets_dir: &Path,
        manifest: &VersionManifest,
    ) {
        let asset_index_id = manifest
            .asset_index
            .as_ref()
            .map(|a| a.id.clone())
            .unwrap_or_default();

        let defaults: Vec<(&str, String)> = vec![
            ("--width", self.config.width.to_string()),
            ("--height", self.config.height.to_string()),
            ("--username", self.config.username.clone()),
            ("--version", manifest.id_raw.clone()),
            ("--assetsDir", assets_dir.display().to_string()),
            ("--assetIndex", asset_index_id),
            ("--gameDir", self.instance_dir.display().to_string()),
            (
                "--accessToken",
                self.config
                    .access_token
                    .clone()
                    .unwrap_or_else(|| "0".to_string()),
            ),
            (
                "--userType",
                self.config
                    .user_type
                    .clone()
                    .unwrap_or_else(|| "legacy".to_string()),
            ),
        ];

        for (flag, val) in defaults {
            if !cmd.contains(&flag.to_string()) && !val.is_empty() {
                cmd.push(flag.to_string());
                cmd.push(val);
            }
        }
    }

    fn add_optional_args(&self, cmd: &mut Vec<String>) {
        if self.config.demo_mode && !cmd.contains(&"--demo".to_string()) {
            cmd.push("--demo".to_string());
        }
        if let Some(qp) = &self.config.quick_play {
            let (flag, value) = match qp {
                QuickPlay::Singleplayer(v) => ("--quickPlaySingleplayer", v),
                QuickPlay::Multiplayer(v) => ("--quickPlayMultiplayer", v),
                QuickPlay::Realms(v) => ("--quickPlayRealms", v),
            };
            if !cmd.contains(&flag.to_string()) {
                cmd.push(flag.to_string());
                cmd.push(value.clone());
            }
        }
    }

    fn build_vars(
        &self,
        assets_dir: &Path,
        natives_dir: &Path,
        uuid: &str,
        classpath: &str,
        manifest: &VersionManifest,
    ) -> HashMap<String, String> {
        let mut vars = HashMap::new();
        vars.insert("auth_player_name".into(), self.config.username.clone());
        vars.insert("version_name".into(), manifest.id_raw.clone());
        vars.insert(
            "game_directory".into(),
            self.instance_dir.display().to_string(),
        );
        vars.insert("assets_root".into(), assets_dir.display().to_string());
        vars.insert(
            "assets_index_name".into(),
            manifest
                .asset_index
                .as_ref()
                .map(|a| a.id.clone())
                .unwrap_or_default(),
        );
        vars.insert("auth_uuid".into(), uuid.to_string());
        vars.insert(
            "auth_access_token".into(),
            self.config
                .access_token
                .clone()
                .unwrap_or_else(|| "0".into()),
        );
        vars.insert(
            "user_type".into(),
            self.config
                .user_type
                .clone()
                .unwrap_or_else(|| "legacy".into()),
        );
        vars.insert("user_properties".into(), "{}".into());
        vars.insert("version_type".into(), "release".into());
        vars.insert(
            "natives_directory".into(),
            natives_dir.display().to_string(),
        );
        vars.insert(
            "library_directory".into(),
            self.shared_dir.join("libraries").display().to_string(),
        );
        vars.insert("classpath".into(), classpath.to_string());

        #[cfg(windows)]
        vars.insert("classpath_separator".into(), ";".into());
        #[cfg(not(windows))]
        vars.insert("classpath_separator".into(), ":".into());

        vars
    }

    fn cleanup_unresolved(&self, cmd: &mut Vec<String>) {
        let mut remove: Vec<usize> = Vec::new();
        for (i, arg) in cmd.iter().enumerate() {
            if arg.contains("${") {
                remove.push(i);
                if i > 0 && cmd[i - 1].starts_with("--") && !cmd[i - 1].contains("${") {
                    remove.push(i - 1);
                }
            }
        }
        remove.sort_unstable();
        remove.dedup();
        remove.reverse();
        for idx in remove {
            info!("Removing unresolved placeholder: {}", cmd[idx]);
            cmd.remove(idx);
        }
    }
}

fn replace_vars(s: &str, vars: &HashMap<String, String>) -> String {
    let mut out = s.to_string();
    for (k, v) in vars {
        out = out.replace(&format!("${{{k}}}"), v);
    }
    out.replace("${launcher_name}", "CubicLauncher")
        .replace("${launcher_version}", "2.0")
}
