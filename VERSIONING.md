# Versionado de CubicLauncher

CubicLauncher usa un sistema de versiones basado en fecha con el formato `AAMMP`:

- `AA` — Año (ej. `26` para 2026)
- `MM` — Mes (ej. `04` para Abril)
- `P` — Parche, letra del alfabeto (`a`, `b`, `c`...)

## Ejemplos

| Versión | Significado |
|---------|-------------|
| `2604a` | Primera release de Abril 2026 |
| `2604b` | Segunda release de Abril 2026 |
| `2605a` | Primera release de Mayo 2026 |

## Nota técnica

Internamente el proyecto usa semver (`MAJOR.MINOR.PATCH`) requerido por Tauri 
para el sistema de actualizaciones automáticas. El mapeo es el siguiente:

- `MAJOR` → Año (`26`)
- `MINOR` → Mes (`04`)
- `PATCH` → Parche (`a=0`, `b=1`, `c=2`...)

Entonces `2604a` corresponde a `26.4.0` internamente.
