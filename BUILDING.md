# Guía de compilación

Esta guía explica cómo compilar CubicLauncher desde el código fuente en Windows, Linux y macOS.

## Requisitos previos

Antes de comenzar, asegúrate de tener instalado lo siguiente:

### 1. Rust

CubicLauncher requiere Rust edición 2024 o superior. Instálalo con `rustup`:

```bash
curl --proto '=https' --tlsv1.2 -sSf https://sh.rustup.rs | sh
```

Verifica la instalación:

```bash
rustc --version
cargo --version
```

### 2. Bun

El frontend utiliza [Bun](https://bun.sh/) como gestor de paquetes y runtime:

```bash
curl -fsSL https://bun.sh/install | bash
```

Verifica la instalación:

```bash
bun --version
```

### 3. Dependencias del sistema (solo Linux)

En distribuciones basadas en Debian/Ubuntu, instala las dependencias nativas que requiere Tauri:

```bash
sudo apt update
sudo apt install -y \
  libwebkit2gtk-4.1-dev \
  libgtk-3-dev \
  libayatana-appindicator3-dev \
  librsvg2-dev \
  patchelf
```

En distribuciones basadas en Arch:

```bash
sudo pacman -S --needed \
  webkit2gtk-4.1 \
  gtk3 \
  libayatana-appindicator \
  librsvg
```

En Fedora / RHEL:

```bash
sudo dnf install \
  webkit2gtk4.1-devel \
  gtk3-devel \
  libappindicator-gtk3-devel \
  librsvg2-devel
```

### 4. Tauri CLI

Instala el CLI de Tauri v2 globalmente como plugin de Cargo:

```bash
cargo install tauri-cli --version "^2"
```

O usando Bun (recomendado si ya tienes el proyecto clonado):

```bash
bun add -d @tauri-apps/cli
```

---

## Clonar el repositorio

```bash
git clone https://github.com/CubicLauncher/CubicLauncher.git
cd CubicLauncher
```

El backend depende de dos bibliotecas internas alojadas en GitHub:

- [`Proton`](https://github.com/CubicLauncher/Proton) — se descarga automáticamente por Cargo.
- [`claunch-rs`](https://github.com/CubicLauncher/claunch-rs) — se descarga automáticamente por Cargo.

Cargo resolverá estas dependencias en el primer `build` o `dev`. Asegúrate de tener acceso a Internet y permisos para clonar repositorios de la organización `CubicLauncher`.

---

## Instalar dependencias del frontend

```bash
bun install
```

---

## Modo desarrollo

Levanta el servidor de desarrollo con recarga en caliente:

```bash
bun run tauri dev
```

Esto ejecuta en paralelo:
- El servidor de desarrollo de Vite en `http://localhost:1420`.
- La ventana nativa de Tauri apuntando a esa URL.

Los cambios en el frontend se reflejan instantáneamente. Los cambios en el código Rust requieren recompilar el backend (Tauri lo hace automáticamente al detectar cambios en `src-tauri/src/`).

---

## Compilación para producción

```bash
bun run tauri build
```

Tauri compilará el frontend, lo empaquetará en el binario Rust y generará los instaladores nativos para el sistema operativo actual.

Los artefactos se ubican en:

```
src-tauri/target/release/bundle/
```

| Sistema operativo | Formatos generados               |
|-------------------|----------------------------------|
| Windows           | `.msi`, `.exe` (NSIS)            |
| Linux             | `.deb`, `.AppImage`, `.rpm`      |
| macOS             | `.dmg`, `.app`                   |

> Para compilar el instalador de un sistema operativo distinto al tuyo es necesario usar compilación cruzada o un entorno de CI. Tauri no soporta cross-compilation nativa entre plataformas.

---

## Verificar el tipo de check del frontend

Si solo quieres verificar que el código TypeScript y Svelte no tiene errores sin compilar la aplicación completa:

```bash
bun run check
```

Para ejecutarlo en modo watch durante el desarrollo:

```bash
bun run check:watch
```

---

## Problemas comunes

**Cargo no encuentra las dependencias Git**

Asegúrate de que Git está instalado y configurado, y de que tienes acceso a la organización `CubicLauncher` en GitHub.

**Error de WebKit en Linux**

Si obtienes un error relacionado con `webkit2gtk`, instala la versión correcta de la librería para tu distribución (ver sección de requisitos del sistema).

**`bun run tauri dev` falla al iniciar**

Verifica que el puerto `1420` no esté ocupado por otro proceso:

```bash
lsof -i :1420
```

**Rust desactualizado**

Algunas dependencias requieren una versión reciente de Rust. Actualiza con:

```bash
rustup update stable
```

**El AppImage no se ejecuta en Linux**

Al intentar ejecutar el `.AppImage` generado, es posible encontrar el siguiente error:

```
dlopen(): error loading libfuse.so.2
AppImages require FUSE to run.
```

Instala `libfuse2` según tu distribución:

```bash
# Debian / Ubuntu
sudo apt install libfuse2

# Arch Linux
sudo pacman -S fuse2

# Fedora
sudo dnf install fuse-libs
```

Si el sistema no permite FUSE (por ejemplo, entornos de contenedores o máquinas virtuales sin acceso a `/dev/fuse`), puedes extraer y ejecutar el AppImage directamente sin montarlo:

```bash
chmod +x CubicLauncher.AppImage
./CubicLauncher.AppImage --appimage-extract
./squashfs-root/AppRun
```

**El binario o AppImage no arranca en distribuciones con glibc antigua**

El binario compilado enlaza contra la versión de `glibc` del sistema donde se compiló. Si al ejecutarlo en otra máquina aparece un error como:

```
version 'GLIBC_2.xx' not found
```

Compila directamente en el sistema destino, o usa una imagen de Docker basada en una distribución con `glibc` más antigua (por ejemplo, Ubuntu 20.04) para generar un binario más compatible:

```bash
docker run --rm -v "$PWD":/app -w /app ubuntu:20.04 bash -c \
  "apt update && apt install -y curl && curl --proto '=https' --tlsv1.2 -sSf https://sh.rustup.rs | sh -s -- -y && ... && cargo tauri build"
```

**Permisos insuficientes para ejecutar el instalador `.deb`**

Si `dpkg` rechaza instalar el paquete `.deb` generado, instálalo con:

```bash
sudo dpkg -i cubiclauncher_0.1.0_amd64.deb
sudo apt install -f   # resuelve dependencias faltantes si las hay
```
