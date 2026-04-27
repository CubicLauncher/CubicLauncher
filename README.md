<div align="center">
  <img src="static/images/cubic.svg" width="120" alt="CubicLauncher" />
  <h1>CubicLauncher</h1>
  <p>Lanzador de Minecraft rápido, ligero y de código abierto.</p>

  ![License](https://img.shields.io/badge/Licence-GPL--3.0-blue)
  [![dependency status](https://deps.rs/repo/github/cubiclauncher/cubiclauncher/status.svg?path=src-tauri)](https://deps.rs/repo/github/cubiclauncher/cubiclauncher?path=src-tauri)
  ![Discord](https://img.shields.io/discord/1366945511273398342)
  ![Website](https://img.shields.io/website/https/www.cubiclauncher.com)
</div>

---

CubicLauncher es un lanzador de Minecraft de escritorio multiplataforma pensado para ser rápido y sencillo de usar. Permite administrar varias instancias de juego de forma independiente, cada una con su propia versión, loader de mods y configuración.

## Funcionalidades

### Gestión de instancias

- Crea instancias de Minecraft con Vanilla, Fabric u otros loaders.
- Renombra, actualiza o elimina instancias cuando quieras.
- Cada instancia tiene su propio directorio aislado de juego.
- Personaliza el ícono y la imagen de portada de cada instancia usando tus propias capturas de pantalla.
- Consulta la fecha y hora de la última vez que jugaste en cada instancia.

### Mods

- Activa o desactiva mods individualmente por instancia sin tener que borrarlos.
- El soporte de mods está disponible en instancias con Fabric u otros loaders compatibles.

### Capturas de pantalla

- Visualiza las capturas de pantalla de cada instancia directamente desde el lanzador.
- Selecciona cualquier captura para usarla como imagen de portada de la instancia.

### Autenticación

- Inicia sesión con tu cuenta de Microsoft mediante el flujo de código de dispositivo.
- El lanzador guarda la sesión de forma segura para que no tengas que autenticarte en cada inicio.

### Configuración

**Lanzador**
- Selección de idioma (Español / English).
- Opción para cerrar el lanzador automáticamente al iniciar el juego.
- Actualizaciones automáticas.

**Minecraft**
- Ajuste de memoria RAM mínima y máxima asignada al juego.
- Opción para mostrar versiones snapshot y alpha en el selector de versiones.
- Forzado de GPU discreta.

**Java**
- Rutas configurables para Java 8, 17 y 21.
- Detección automática de instalaciones de Java en el sistema.
- Argumentos personalizados de la JVM.

### Descarga de versiones

- Descarga versiones de Minecraft desde los repositorios oficiales de Mojang.
- Soporte para instalar Fabric en instancias existentes.

## Instalación

Descarga el instalador correspondiente a tu sistema operativo desde la sección de [Releases](https://github.com/CubicLauncher/CubicLauncher/releases).

| Sistema operativo | Formato        |
|-------------------|----------------|
| Windows           | `.msi` / `.exe`|
| Linux             | `.deb` / `.AppImage` |
| macOS             | `.dmg`         |

## Compilar desde el código fuente

Si prefieres compilar el lanzador manualmente, consulta la [guía de compilación](https://www.cubiclauncher.com/docs/main.html#desarrollo/compilacion).

Requisitos mínimos: [Bun](https://bun.sh/), [Rust](https://www.rust-lang.org/tools/install) 2021+ y [Tauri CLI v2](https://tauri.app/start/prerequisites/).

```bash
git clone https://github.com/CubicLauncher/CubicLauncher.git
cd CubicLauncher
bun install
bun run tauri build
```

## Licencia

Este proyecto se distribuye bajo los términos de la [Licencia Pública General GNU v3.0](LICENSE).
