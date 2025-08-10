@echo off
setlocal enabledelayedexpansion

REM Script para instalar dependencias y lanzar Cypress
REM Soporta deno, npm, bun, y pnpm

set CYPRESS_DIR=.\cypress

REM Códigos de color para Windows
set "RED=[91m"
set "GREEN=[92m"
set "YELLOW=[93m"
set "BLUE=[94m"
set "NC=[0m"

REM Funciones para imprimir mensajes con color
goto :main

:print_info
echo %BLUE%[INFO]%NC% %~1
goto :eof

:print_success
echo %GREEN%[SUCCESS]%NC% %~1
goto :eof

:print_warning
echo %YELLOW%[WARNING]%NC% %~1
goto :eof

:print_error
echo %RED%[ERROR]%NC% %~1
goto :eof

:main
REM Verificar si existe el directorio cypress
if not exist "%CYPRESS_DIR%" (
    call :print_error "El directorio %CYPRESS_DIR% no existe"
    exit /b 1
)

REM Cambiar al directorio cypress
call :print_info "Cambiando al directorio %CYPRESS_DIR%"
cd /d "%CYPRESS_DIR%"

REM Función para detectar el gestor de paquetes
set PACKAGE_MANAGER=none

REM Verificar por archivos de configuración existentes
if exist "deno.json" (
    set PACKAGE_MANAGER=deno
    goto :detected
)
if exist "deno.jsonc" (
    set PACKAGE_MANAGER=deno
    goto :detected
)
if exist "bun.lockb" (
    set PACKAGE_MANAGER=bun
    goto :detected
)
if exist "pnpm-lock.yaml" (
    set PACKAGE_MANAGER=pnpm
    goto :detected
)
if exist "package-lock.json" (
    set PACKAGE_MANAGER=npm
    goto :detected
)
if exist "yarn.lock" (
    set PACKAGE_MANAGER=yarn
    goto :detected
)

REM Si no hay archivos de lock, verificar qué gestores están disponibles
where deno >nul 2>&1
if !errorlevel! equ 0 (
    set PACKAGE_MANAGER=deno
    goto :detected
)

where bun >nul 2>&1
if !errorlevel! equ 0 (
    set PACKAGE_MANAGER=bun
    goto :detected
)

where pnpm >nul 2>&1
if !errorlevel! equ 0 (
    set PACKAGE_MANAGER=pnpm
    goto :detected
)

where npm >nul 2>&1
if !errorlevel! equ 0 (
    set PACKAGE_MANAGER=npm
    goto :detected
)

:detected
call :print_info "Gestor de paquetes detectado: !PACKAGE_MANAGER!"

REM Instalar dependencias según el gestor detectado
if "!PACKAGE_MANAGER!"=="deno" (
    call :print_info "Instalando dependencias con Deno..."
    where deno >nul 2>&1
    if !errorlevel! equ 0 (
        deno install
        call :print_success "Dependencias instaladas con Deno"
    ) else (
        call :print_error "Deno no está instalado"
        exit /b 1
    )
) else if "!PACKAGE_MANAGER!"=="bun" (
    call :print_info "Instalando dependencias con Bun..."
    where bun >nul 2>&1
    if !errorlevel! equ 0 (
        bun install
        call :print_success "Dependencias instaladas con Bun"
    ) else (
        call :print_error "Bun no está instalado"
        exit /b 1
    )
) else if "!PACKAGE_MANAGER!"=="pnpm" (
    call :print_info "Instalando dependencias con pnpm..."
    where pnpm >nul 2>&1
    if !errorlevel! equ 0 (
        pnpm install
        call :print_success "Dependencias instaladas con pnpm"
    ) else (
        call :print_error "pnpm no está instalado"
        exit /b 1
    )
) else if "!PACKAGE_MANAGER!"=="npm" (
    call :print_info "Instalando dependencias con npm..."
    where npm >nul 2>&1
    if !errorlevel! equ 0 (
        npm install
        call :print_success "Dependencias instaladas con npm"
    ) else (
        call :print_error "npm no está instalado"
        exit /b 1
    )
) else if "!PACKAGE_MANAGER!"=="yarn" (
    call :print_info "Instalando dependencias con Yarn..."
    where yarn >nul 2>&1
    if !errorlevel! equ 0 (
        yarn install
        call :print_success "Dependencias instaladas con Yarn"
    ) else (
        call :print_error "Yarn no está instalado"
        exit /b 1
    )
) else (
    call :print_error "No se encontró ningún gestor de paquetes compatible"
    call :print_info "Instala uno de los siguientes: deno, bun, pnpm, npm, o yarn"
    exit /b 1
)

REM Verificar si npx está disponible para lanzar Cypress
call :print_info "Lanzando Cypress..."

where npx >nul 2>&1
if !errorlevel! equ 0 (
    call :print_success "Abriendo Cypress Test Runner..."
    start /b npx cypress open >nul 2>&1
    goto :end
)

where yarn >nul 2>&1
if !errorlevel! equ 0 (
    call :print_warning "npx no encontrado, usando yarn dlx..."
    start /b yarn dlx cypress open >nul 2>&1
    goto :end
)

where pnpm >nul 2>&1
if !errorlevel! equ 0 (
    call :print_warning "npx no encontrado, usando pnpm dlx..."
    start /b pnpm dlx cypress open >nul 2>&1
    goto :end
)

where bun >nul 2>&1
if !errorlevel! equ 0 (
    call :print_warning "npx no encontrado, usando bunx..."
    start /b bunx cypress open >nul 2>&1
    goto :end
) else (
    call :print_error "No se pudo encontrar una forma de ejecutar Cypress"
    call :print_info "Asegúrate de que npm, yarn, pnpm, o bun estén instalados"
    exit /b 1
)

:end
call :print_success "Script completado exitosamente"
echo Presiona cualquier tecla para continuar...
pause >nul
