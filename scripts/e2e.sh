#!/bin/bash

# Script para instalar dependencias y lanzar Cypress
# Soporta deno, npm, bun, y pnpm

set -e  # Salir si cualquier comando falla

CYPRESS_DIR="./cypress"

# Colores para output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Función para imprimir mensajes con color
print_info() {
    echo -e "${BLUE}[INFO]${NC} $1"
}

print_success() {
    echo -e "${GREEN}[SUCCESS]${NC} $1"
}

print_warning() {
    echo -e "${YELLOW}[WARNING]${NC} $1"
}

print_error() {
    echo -e "${RED}[ERROR]${NC} $1"
}

# Verificar si existe el directorio cypress
if [ ! -d "$CYPRESS_DIR" ]; then
    print_error "El directorio $CYPRESS_DIR no existe"
    exit 1
fi

# Cambiar al directorio cypress
print_info "Cambiando al directorio $CYPRESS_DIR"
cd "$CYPRESS_DIR"

# Función para detectar el gestor de paquetes preferido
detect_package_manager() {
    # Verificar por archivos de lock existentes
    if [ -f "deno.json" ] || [ -f "deno.jsonc" ]; then
        echo "deno"
    elif [ -f "bun.lockb" ]; then
        echo "bun"
    elif [ -f "pnpm-lock.yaml" ]; then
        echo "pnpm"
    elif [ -f "package-lock.json" ]; then
        echo "npm"
    elif [ -f "yarn.lock" ]; then
        echo "yarn"
    else
        # Si no hay archivos de lock, verificar qué gestores están disponibles
        if command -v deno >/dev/null 2>&1; then
            echo "deno"
        elif command -v bun >/dev/null 2>&1; then
            echo "bun"
        elif command -v pnpm >/dev/null 2>&1; then
            echo "pnpm"
        elif command -v npm >/dev/null 2>&1; then
            echo "npm"
        else
            echo "none"
        fi
    fi
}

# Detectar el gestor de paquetes
PACKAGE_MANAGER=$(detect_package_manager)

print_info "Gestor de paquetes detectado: $PACKAGE_MANAGER"

# Instalar dependencias según el gestor detectado
case $PACKAGE_MANAGER in
    "deno")
        print_info "Instalando dependencias con Deno..."
        if command -v deno >/dev/null 2>&1; then
            deno install
            print_success "Dependencias instaladas con Deno"
        else
            print_error "Deno no está instalado"
            exit 1
        fi
        ;;
    "bun")
        print_info "Instalando dependencias con Bun..."
        if command -v bun >/dev/null 2>&1; then
            bun install
            print_success "Dependencias instaladas con Bun"
        else
            print_error "Bun no está instalado"
            exit 1
        fi
        ;;
    "pnpm")
        print_info "Instalando dependencias con pnpm..."
        if command -v pnpm >/dev/null 2>&1; then
            pnpm install
            print_success "Dependencias instaladas con pnpm"
        else
            print_error "pnpm no está instalado"
            exit 1
        fi
        ;;
    "npm")
        print_info "Instalando dependencias with npm..."
        if command -v npm >/dev/null 2>&1; then
            npm install
            print_success "Dependencias instaladas con npm"
        else
            print_error "npm no está instalado"
            exit 1
        fi
        ;;
    "yarn")
        print_info "Instalando dependencias con Yarn..."
        if command -v yarn >/dev/null 2>&1; then
            yarn install
            print_success "Dependencias instaladas con Yarn"
        else
            print_error "Yarn no está instalado"
            exit 1
        fi
        ;;
    "none")
        print_error "No se encontró ningún gestor de paquetes compatible"
        print_info "Instala uno de los siguientes: deno, bun, pnpm, npm, o yarn"
        exit 1
        ;;
esac

# Verificar si npx está disponible para lanzar Cypress
print_info "Lanzando Cypress..."
if command -v npx >/dev/null 2>&1; then
    print_success "Abriendo Cypress Test Runner..."
    npx cypress open > /dev/null 2>&1
elif command -v yarn >/dev/null 2>&1; then
    print_warning "npx no encontrado, usando yarn dlx..."
    yarn dlx cypress open > /dev/null 2>&1
elif command -v pnpm >/dev/null 2>&1; then
    print_warning "npx no encontrado, usando pnpm dlx..."
    pnpm dlx cypress open > /dev/null 2>&1
elif command -v bun >/dev/null 2>&1; then
    print_warning "npx no encontrado, usando bunx..."
    bunx cypress open > /dev/null 2>&1
else
    print_error "No se pudo encontrar una forma de ejecutar Cypress"
    print_info "Asegúrate de que npm, yarn, pnpm, o bun estén instalados"
    exit 1
fi

print_success "Script completado exitosamente"
