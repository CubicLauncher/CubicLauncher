# PKGBUILD — Arch Linux / CUBICLAUNCHER

## ⚠️ Aviso

Este PKGBUILD es **inestable** y solo fue probado en mi máquina Arch Linux corriendo **X11 con Cinnamon**. Puede que no funcione en otros entornos (Wayland, otros DE, etc). -- Santiagolxx

Deberia de funcionar igual ya que se buildea localmente con las librerias que tienes localmente.

## Build manual

```bash
cd dist/arch
makepkg -si
```

## Dependencias

Además de las que declara el PKGBUILD, necesitás tener instalado:

- `bun` — build del frontend
- Rust toolchain (`rustc`, `cargo`)
- Dependencias de Tauri: `webkit2gtk`, `gtk3`, etc.

## Notas

- Build desde el repo, no desde el AUR.
- Usá `makepkg -si` para instalar automáticamente.
- Si falta alguna dependencia, makepkg avisa.
