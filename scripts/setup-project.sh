#!/bin/bash

# Script de configuración inicial del proyecto YapeHub
echo "🚀 Configurando proyecto YapeHub..."

# Colores para output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Función para imprimir con colores
print_status() {
    echo -e "${GREEN}✅${NC} $1"
}

print_warning() {
    echo -e "${YELLOW}⚠️${NC} $1"
}

print_error() {
    echo -e "${RED}❌${NC} $1"
}

print_info() {
    echo -e "${BLUE}ℹ️${NC} $1"
}

# Verificar que estamos en la raíz del proyecto
if [ ! -f "build.gradle.kts" ]; then
    print_error "Este script debe ejecutarse desde la raíz del proyecto"
    exit 1
fi

print_info "Verificando dependencias del sistema..."

# Verificar Java
if command -v java &> /dev/null; then
    JAVA_VERSION=$(java -version 2>&1 | head -n 1 | cut -d'"' -f2)
    print_status "Java encontrado: $JAVA_VERSION"
else
    print_error "Java no encontrado. Instala Java 21 o superior"
    exit 1
fi

# Verificar Gradle
if [ -f "./gradlew" ]; then
    print_status "Gradle Wrapper encontrado"
    chmod +x ./gradlew
else
    print_error "Gradle Wrapper no encontrado"
    exit 1
fi

# Verificar Android SDK (opcional)
if [ -n "$ANDROID_HOME" ]; then
    print_status "Android SDK configurado: $ANDROID_HOME"
else
    print_warning "ANDROID_HOME no configurado (opcional para desarrollo)"
fi

# Crear directorios necesarios
print_info "Creando directorios necesarios..."
mkdir -p keystores
mkdir -p docs/guides
mkdir -p docs/scripts
print_status "Directorios creados"

# Configurar keystore.properties si no existe
if [ ! -f "keystore.properties" ]; then
    print_info "Configurando keystore.properties..."
    if [ -f "keystore.properties.example" ]; then
        cp keystore.properties.example keystore.properties
        print_status "keystore.properties creado desde ejemplo"
        print_warning "Recuerda editar keystore.properties con tus datos reales"
    else
        print_error "keystore.properties.example no encontrado"
    fi
else
    print_status "keystore.properties ya existe"
fi

# Hacer ejecutables todos los scripts
print_info "Configurando permisos de scripts..."
chmod +x scripts/*.sh
print_status "Scripts configurados como ejecutables"

# Verificar configuración de Git
if [ -d ".git" ]; then
    print_status "Repositorio Git detectado"
    
    # Verificar .gitignore
    if [ -f ".gitignore" ]; then
        if grep -q "keystore.properties" .gitignore; then
            print_status ".gitignore configurado correctamente"
        else
            print_warning ".gitignore podría necesitar actualización"
        fi
    fi
else
    print_warning "No es un repositorio Git"
fi

# Limpiar proyecto inicial
print_info "Realizando limpieza inicial..."
./gradlew clean > /dev/null 2>&1
if [ $? -eq 0 ]; then
    print_status "Limpieza inicial completada"
else
    print_warning "Limpieza inicial falló (normal en primera ejecución)"
fi

# Verificar compilación debug
print_info "Verificando compilación debug..."
./gradlew :composeApp:assembleDebug > /dev/null 2>&1
if [ $? -eq 0 ]; then
    print_status "Compilación debug exitosa"
else
    print_warning "Compilación debug falló - revisar configuración"
fi

# Mostrar resumen
echo ""
echo "📋 Resumen de configuración:"
echo "================================"
print_status "Proyecto YapeHub configurado"
print_status "Scripts disponibles en ./scripts/"
print_status "Documentación en ./docs/"

if [ ! -f "keystores/yapehub-release.keystore" ]; then
    print_warning "Keystore no encontrado - ejecuta: ./scripts/generate-keystore.sh"
fi

echo ""
echo "🎯 Próximos pasos:"
echo "1. Edita keystore.properties con tus datos"
echo "2. Ejecuta: ./scripts/generate-keystore.sh"
echo "3. Compila tu primera APK: ./scripts/build-release.sh"
echo ""
print_status "¡Configuración completada!"