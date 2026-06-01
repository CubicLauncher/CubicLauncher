use serde::{Deserialize, Serialize};

#[derive(Debug, Clone, Deserialize)]
pub struct ZuluPackage {
    pub availability_type: String,
    pub distro_version: Vec<u32>,
    pub download_url: String,
    pub java_version: Vec<u32>,
    pub latest: bool,
    pub name: String,
    pub openjdk_build_number: u32,
    pub package_uuid: String,
    pub product: String,
}

#[derive(Debug, Clone)]
pub struct JrePackage {
    pub major_version: u8,
    pub java_version: String,
    pub download_url: String,
    pub filename: String,
    pub distro_version: Vec<u32>,
}

impl JrePackage {
    pub fn is_tar_gz(&self) -> bool {
        self.filename.ends_with(".tar.gz")
    }

    pub fn is_zip(&self) -> bool {
        self.filename.ends_with(".zip")
    }
}

#[derive(Debug, Clone, Serialize, Deserialize)]
pub struct JreStatus {
    pub version: u8,
    pub installed: bool,
    pub java_version: Option<String>,
}
