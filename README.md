<div align="center">

<img src="src/main/resources/com.cubiclauncher.launcher/assets/logos/cubic.png" alt="CubicLauncher Logo" width="150"/>

# CubicLauncher

**Un launcher de Minecraft moderno y de código abierto basado en Java y JavaFX**

[![Build Status](https://img.shields.io/github/actions/workflow/status/CubicLauncher/CubicLauncher/buildjar.yml?style=for-the-badge&logo=github-actions&logoColor=white)](https://github.com/CubicLauncher/CubicLauncher/actions)
[![Java](https://img.shields.io/badge/Java-21-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white)](https://adoptium.net/)
[![JavaFX](https://img.shields.io/badge/JavaFX-21.0.4-4B7BE5?style=for-the-badge)](https://openjfx.io/)
[![License](https://img.shields.io/badge/License-GPL%20v3-blue?style=for-the-badge)](LICENSE)
[![Version](https://img.shields.io/badge/Version-2603a-brightgreen?style=for-the-badge)](https://github.com/CubicLauncher/CubicLauncher/releases)

<br/>

*Gestione, configure y lance sus instancias de Minecraft con eficiencia en Windows, Linux y macOS.*

</div>

---

## Tabla de Contenidos

- [Resumen](#resumen)
- [Características](#características)
- [Capturas de Pantalla](#capturas-de-pantalla)
- [Requisitos](#requisitos)
- [Primeros Pasos](#primeros-pasos)
  - [Compilación desde el Código Fuente](#compilación-desde-el-código-fuente)
  - [Ejecución del Launcher](#ejecución-del-launcher)
  - [Creación de un Fat JAR](#creación-de-un-fat-jar)
- [Configuración](#configuración)
- [Internacionalización](#internacionalización)
- [Contribuciones](#contribuciones)
- [Licencia](#licencia)
- [Autores](#autores)

---

## Resumen

CubicLauncher es un lanzador de Minecraft gratuito y de código abierto diseñado para otorgar a los jugadores un control total sobre su experiencia de juego. Basado en la librería personalizada claunch y una interfaz desarrollada en JavaFX, CubicLauncher ofrece un entorno limpio y moderno para gestionar múltiples instancias de Minecraft, configurar parámetros de Java y lanzar el juego sin publicidad ni cuentas innecesarias.

> Nota: Este proyecto se encuentra en desarrollo activo. Las funcionalidades pueden variar entre versiones.

---

## Características

| Función | Descripción |
|---|---|
| Gestión de Instancias | Creación, renombramiento, eliminación y lanzamiento de múltiples instancias de forma independiente. |
| Descarga Automática de Versiones | Descarga automáticamente las versiones de Minecraft faltantes antes del inicio. |
| Configuración por Instancia | Permite definir límites de memoria (mín/máx), argumentos JVM personalizados e imágenes de portada. |
| Navegador de Versiones | Exploración y filtrado de versiones Release, Beta y Alpha de Minecraft. |
| Interfaz Multilingüe | Soporte integrado para inglés (en_us) y español (es_es). |
| Configuración de Rutas Java | Definición de rutas personalizadas para JRE 8, 17 y 21. |
| Sistema de Eventos de Juego | Bus de eventos interno que reacciona al ciclo de vida del juego (inicio, cierre, errores). |
| Consola de Errores | Consola integrada opcional para visualizar registros de fallos en tiempo real. |
| Multiplataforma | Compatibilidad con Windows, Linux y macOS. |
| Estilos Nativos y Personalizados | Alternancia entre decoraciones nativas del sistema operativo y temas basados en BootstrapFX. |
| Cierre al Iniciar | Opción para cerrar el lanzador automáticamente al detectar el inicio del juego. |
| Modo Sin Conexión | Diseñado para operar de manera offline con las versiones instaladas localmente. |

---

## Capturas de Pantalla

> Las capturas de pantalla serán añadidas próximamente.

---

## Requisitos

Antes de compilar o ejecutar CubicLauncher, asegúrese de contar con los siguientes componentes:

| Requisito | Versión |
|---|---|
| JDK | 21 (Se recomienda Temurin) |
| Gradle | 8.3+ (Wrapper incluido en el proyecto) |
| Sistema Operativo | Windows 10+, Ubuntu 20.04+, macOS 12+ |

> No es necesaria la instalación manual de Gradle, ya que se incluye el Gradle Wrapper (./gradlew).

---

## Primeros Pasos

### Clonación del Repositorio

```bash
git clone https://github.com/CubicLauncher/CubicLauncher.git
cd CubicLauncher
```

### Compilación desde el Código Fuente

Utilice el wrapper de Gradle incluido para compilar el proyecto:

```bash
# Linux / macOS
./gradlew build

# Windows
gradlew.bat build
```

### Ejecución del Launcher

```bash
# Linux / macOS
./gradlew run

# Windows
gradlew.bat run
```

### Creación de un Fat JAR

Para empaquetar el lanzador en un único archivo JAR autónomo:

```bash
# Linux / macOS
./gradlew fatJar

# Windows
gradlew.bat fatJar
```

El archivo generado se ubicará en:
```
build/libs/CubicLauncher-all.jar
```

---

## Configuración

En la primera ejecución, un Asistente de Configuración guiará al usuario. Los ajustes se guardan en un archivo settings.cub (formato JSON) dentro del directorio de datos del lanzador.

### Ajustes Disponibles

| Ajuste | Por Defecto | Descripción |
|---|---|---|
| language | es_es | Idioma de la interfaz (en_us o es_es). |
| username | steve | Nombre de usuario en el juego. |
| minMemory / maxMemory | 512 MB / 2 GB | Asignación de memoria para la JVM. |
| jvmArguments | (vacío) | Parámetros JVM adicionales para Minecraft. |
| jre8_path / jre17_path / jre21_path | (vacío) | Rutas personalizadas para los entornos de ejecución Java. |
| autoUpdate | true | Comprobación automática de actualizaciones del lanzador. |
| closeLauncherOnGameStart | false | Cerrar el lanzador al iniciar Minecraft. |
| errorConsole | false | Activar la visualización de la consola de errores. |
| showAlphaVersions | false | Mostrar versiones Alpha en el listado. |
| showBetaVersions | false | Mostrar versiones Beta en el listado. |
| forceDiscreteGpu | false | Forzar el uso de GPU dedicada (si aplica). |
| native_styles | true | Utilizar las decoraciones de ventana nativas del SO. |

---

## Internacionalización

CubicLauncher soporta múltiples idiomas mediante archivos JSON ubicados en:

```
src/main/resources/com.cubiclauncher.launcher/lang/
├── en_us.json    # Inglés (Estados Unidos)
└── es_es.json    # Español (España)
```

---

## Contribuciones

Las contribuciones son fundamentales para la mejora de los proyectos de código abierto.

### Cómo Contribuir

1. Realice un Fork del repositorio.
2. Cree una rama para su funcionalidad: `git checkout -b feature/nueva-funcionalidad`.
3. Realice sus cambios y confírmelos: `git commit -m "feat: descripción del cambio"`.
4. Envíe los cambios a su rama: `git push origin feature/nueva-funcionalidad`.
5. Abra una Pull Request detallando los cambios realizados.

---

## Licencia

Este proyecto se distribuye bajo la licencia GNU General Public License v3.0.

Consulte el archivo LICENSE para obtener el texto completo de la licencia o visite [https://www.gnu.org/licenses/gpl-3.0.html](https://www.gnu.org/licenses/gpl-3.0.html).

---

## Autores

CubicLauncher es desarrollado y mantenido por:

- Santiagolxx — Desarrollador Principal
- Notstaff — Co-Desarrollador
- Contribuidores de CubicLauncher — Comunidad

---

<div align="center">

Desarrollado con compromiso por el equipo de CubicLauncher

*© 2025–2026 Santiagolxx, Notstaff y contribuidores de CubicLauncher*

</div>
