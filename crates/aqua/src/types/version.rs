use serde::{Deserialize, Deserializer};

#[derive(Debug, Clone, Copy, PartialEq, Eq, Default)]
pub struct MCVersion {
    pub major: u8,
    pub minor: u8,
    pub patch: Option<u8>,
}

impl MCVersion {
    pub fn new(major: u8, minor: u8, patch: Option<u8>) -> Self {
        Self { major, minor, patch }
    }
}

impl std::fmt::Display for MCVersion {
    fn fmt(&self, f: &mut std::fmt::Formatter<'_>) -> std::fmt::Result {
        match self.patch {
            Some(p) => write!(f, "{}.{}.{}", self.major, self.minor, p),
            None => write!(f, "{}.{}", self.major, self.minor),
        }
    }
}

impl<'de> Deserialize<'de> for MCVersion {
    fn deserialize<D: Deserializer<'de>>(deserializer: D) -> Result<Self, D::Error> {
        let s = String::deserialize(deserializer)?;
        Ok(parse_version(&s).unwrap_or_default())
    }
}

pub fn parse_version(s: &str) -> Option<MCVersion> {
    let s = s.trim();

    let digits: Vec<&str> = s.split(|c: char| !c.is_ascii_digit() && c != '.')
        .next()
        .map(|part| part.split('.').collect())
        .unwrap_or_default();

    if digits.len() < 2 {
        return None;
    }
    let major = digits[0].parse::<u8>().ok()?;
    let minor = digits[1].parse::<u8>().ok()?;
    let patch = digits.get(2).and_then(|p| p.parse().ok());
    Some(MCVersion { major, minor, patch })
}

#[cfg(test)]
mod tests {
    use super::*;

    #[test]
    fn parses_standard() {
        assert_eq!(parse_version("1.21").unwrap(), MCVersion::new(1, 21, None));
        assert_eq!(parse_version("1.20.6").unwrap(), MCVersion::new(1, 20, Some(6)));
    }

    #[test]
    fn parses_snapshot() {
        let v = parse_version("26.2-snapshot-5").unwrap();
        assert_eq!(v, MCVersion::new(26, 2, None));
    }

    #[test]
    fn parses_26_1() {
        let v = parse_version("26.1").unwrap();
        assert_eq!(v, MCVersion::new(26, 1, None));
    }
}
