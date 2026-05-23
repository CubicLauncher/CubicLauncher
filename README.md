<div align="center">
  <img src="static/images/cubic.svg" width="120" alt="CubicLauncher" />
  <h1>CubicLauncher</h1>

  ![License](https://img.shields.io/badge/Licence-GPL--3.0-blue)
  [![dependency status](https://deps.rs/repo/github/cubiclauncher/cubiclauncher/status.svg?path=src-tauri)](https://deps.rs/repo/github/cubiclauncher/cubiclauncher?path=src-tauri)
  ![Discord](https://img.shields.io/discord/1366945511273398342)
  ![Website](https://img.shields.io/website/https/www.cubiclauncher.com)
</div>

---

## Descripción

**CubicLauncher** es un launcher de Minecraft multiplataforma construido sobre [Tauri v2](https://tauri.app) + [SvelteKit](https://kit.svelte.dev/) (frontend) y [Rust](https://www.rust-lang.org/) (backend nativo). Gestiona instancias aisladas de Minecraft con soporte para múltiples versiones, loaders de mods (Vanilla, Fabric), autenticación mediante OAuth 2.0 (código de dispositivo de Microsoft) y un sistema modular de comandos Tauri.

El proyecto está estructurado como un **monorepo** no convencional —el frontend SvelteKit y el backend Tauri coexisten en el mismo repositorio, compartiendo configuración y pipeline de build sin un workspace manager formal.

## Stack tecnológico

| Capa       | Tecnología                                          |
|------------|-----------------------------------------------------|
| Shell nativo | [Tauri v2](https://tauri.app) + Rust              |
| Frontend   | [Svelte 5](https://svelte.dev/) + [SvelteKit](https://kit.svelte.dev/) + TypeScript |
| Bundler    | [Vite 6](https://vite.dev/)                         |
| Backend    | Rust (edition 2024)                                 |
| Runtime JS | [Bun](https://bun.sh/)                              |
| Auth       | OAuth 2.0 — flujo de código de dispositivo (Microsoft) |
| Addons     | Modrinth API (consumo desde Rust)                   |
| UI         | Componentes nativos Svelte 5 (sin framework CSS)    |

## Arquitectura

```
┌─────────────────────────────────────────────────┐
│                  Tauri Shell                      │
│  ┌──────────────────┐  ┌──────────────────────┐  │
│  │   Frontend (JS)   │  │   Backend (Rust)      │  │
│  │   SvelteKit + Vite│  │   src-tauri/src/      │  │
│  │                   │  │   ├── commands/       │  │
│  │   state.svelte.ts │◄─┤   ├── services/      │  │
│  │   (reactividad)   │  │   ├── core/           │  │
│  └──────────────────┘  │   └── theme_watcher/  │  │
│         │ Tauri IPC     └──────────────────────┘  │
│         ▼                                          │
│  ┌──────────────────────────────────────────────┐ │
│  │          Sistema de archivos local             │ │
│  │  (instancias, config, capturas, mods, etc.)   │ │
│  └──────────────────────────────────────────────┘ │
└─────────────────────────────────────────────────┘
```

El frontend se comunica con el backend Rust exclusivamente mediante **Tauri Commands** (IPC). El estado global reactivo se maneja del lado del frontend con `state.svelte.ts` (runas de Svelte 5). El backend orquesta el ciclo de vida de las instancias (descarga, lanzamiento, gestión de mods) y expone una API de comandos tipados.

## Estructura del proyecto

```
cubiclauncher/
├── src/                          # Frontend SvelteKit
│   ├── app.html                  # Template HTML raíz
│   ├── routes/
│   │   ├── +layout.ts            # Layout global
│   │   └── +page.svelte          # Página principal
│   └── lib/
│       ├── api/                  # Capa de comunicación con Tauri
│       ├── components/           # Componentes Svelte 5
│       ├── i18n/                 # Internacionalización (es/en)
│       ├── icons/                # Iconos SVG
│       ├── state/
│       │   └── state.svelte.ts   # Estado global reactivo (runas)
│       ├── types/
│       │   └── types.ts          # Tipos TypeScript compartidos
│       └── Logo.svelte
├── src-tauri/                    # Backend Rust
│   ├── Cargo.toml                # Manifiesto Rust
│   ├── tauri.conf.json           # Configuración de Tauri
│   ├── build.rs                  # Script de build de Tauri
│   ├── capabilities/             # Permisos Tauri
│   ├── icons/                    # Íconos de la app
│   └── src/
│       ├── main.rs               # Entry point binario
│       ├── lib.rs                # Biblioteca compartida
│       ├── commands/             # Comandos Tauri IPC
│       │   ├── auth.rs           #   Autenticación Microsoft
│       │   ├── download.rs       #   Descarga de versiones
│       │   ├── instance.rs       #   CRUD de instancias
│       │   ├── modrinth.rs       #   Integración Modrinth
│       │   ├── others.rs         #   Utilidades varias
│       │   ├── settings.rs       #   Configuración global
│       │   └── themes.rs         #   Temas visuales
│       ├── services/             # Lógica de negocio
│       │   ├── instance_manager.rs
│       │   ├── launcher.rs       #   Lanzador del proceso Java
│       │   ├── settings_manager.rs
│       │   └── addon_manager.rs  #   Gestión de mods
│       ├── core/                 # Infraestructura
│       │   ├── errors/           #   Sistema de errores
│       │   ├── event_bus.rs      #   Bus de eventos interno
│       │   ├── http_client.rs    #   Cliente HTTP compartido
│       │   └── path_manager.rs   #   Resolución de rutas
│       └── theme_watcher/        # Watcher de temas del SO
├── dist/                         # Distribuciones auxiliares
│   └── arch/                     # PKGBUILD para Arch Linux
├── static/                       # Archivos estáticos
│   └── images/
├── build/                        # Output de build
├── vite.config.js                # Configuración de Vite
├── svelte.config.js              # Configuración de SvelteKit
├── tsconfig.json                 # TypeScript config
└── package.json                  # Dependencias y scripts JS
```

## Primeros pasos

### Prerrequisitos

- [Bun](https://bun.sh/) ≥ 1.x
- [Rust](https://www.rust-lang.org/tools/install) ≥ 1.85 (edition 2024)
- [Tauri CLI v2](https://v2.tauri.app/start/prerequisites/)

### Instalación

```bash
git clone https://github.com/CubicLauncher/CubicLauncher.git
cd CubicLauncher
bun install
```

### Desarrollo

```bash
# Inicia el servidor de desarrollo Vite + Tauri
bun run tauri dev
```

### Scripts disponibles

| Comando                    | Descripción                                   |
|----------------------------|-----------------------------------------------|
| `bun run dev`              | Servidor de desarrollo Vite (solo frontend)   |
| `bun run build`            | Build de producción del frontend              |
| `bun run preview`          | Preview del build de frontend                 |
| `bun run check`            | Type-check con `svelte-check`                 |
| `bun run tauri dev`        | Entorno de desarrollo Tauri (frontend + Rust) |
| `bun run tauri build`      | Build completo de la aplicación Tauri         |

### Dependencias clave

**Rust (Cargo)**:
- `tauri` v2 — shell nativo multiplataforma
- `launchwerk` — lanzamiento y autenticación de Minecraft [REPO](https://github.com/CubicLauncher/Launchwerk)
- `aqua` — utilidades compartidas internas [REPO](https://github.com/CubicLauncher/Aqua)
- `reqwest` + `tokio` — async HTTP y runtime
- `serde` / `serde_json` — serialización
- `notify` — file system watcher (tema del SO)

**JavaScript**:
- `@sveltejs/kit` + `svelte` v5 — framework reactivo
- `vite` v6 — bundler y HMR
- `@tauri-apps/api` v2 — bridge IPC
- `@tauri-apps/plugin-dialog`, `plugin-process`, `plugin-updater`

## Compilación

```bash
bun run tauri build
```

El binario compilado se genera en `src-tauri/target/release/`. Consulta la [documentación oficial](https://www.cubiclauncher.com/docs/main.html#desarrollo/compilacion) para builds específicos por plataforma.

## Arch Linux

Descargá solo el [PKGBUILD](dist/arch/PKGBUILD) y compilá (el PKGBUILD ya clona el repo automáticamente):

```bash
mkdir cubiclauncher-build && cd cubiclauncher-build
wget https://raw.githubusercontent.com/CubicLauncher/CubicLauncher/main/dist/arch/PKGBUILD
makepkg -si
```

> ⚠️ **Compilar localmente es obligatorio.** Los binarios generados por las CI de GitHub (Ubuntu) pueden no ser compatibles con Arch Linux debido a su modelo rolling release.

> ⚠️ **Inestable** — Revisá `dist/arch/IMPORTANTE.md`.

## Comunidad

| Plataforma  | URL                                                |
|-------------|----------------------------------------------------|
| Discord     | [https://discord.gg/7VaqSrPukm](https://discord.gg/7VaqSrPukm) |

## Licencia

Distribuido bajo [GNU General Public License v3.0](LICENSE).
