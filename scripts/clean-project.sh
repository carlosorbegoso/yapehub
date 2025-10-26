#!/bin/bash

# Script de limpieza profunda del proyecto YapeHub
echo "🧹 Iniciando limpieza profunda del proyecto..."

# Colores para output
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m'

print_status() {
    echo -e "${GREEN}✅${NC} $1"
}

print_info() {
    echo -e "${BLUE}ℹ️${NC} $1"
}

print_warning() {
    echo -e "${YELLOW}⚠️${NC} $1"
}

# Verificar que estamos en la raíz del proyecto
if [ ! -f "build.gradle.kts" ]; then
    echo "❌ Este script debe ejecutarse desde la raíz del proyecto"
    exit 1
fi

# Limpiar builds de Gradle
print_info "Limpiando builds de Gradle..."
./gradlew clean
if [ $? -eq 0 ]; then
    print_status "Builds de Gradle limpiados"
else
    print_warning "Error al limpiar builds de Gradle"
fi

# Limpiar cache de Gradle
print_info "Limpiando cache de Gradle..."
rm -rf ~/.gradle/caches/ 2>/dev/null || true
print_status "Cache de Gradle limpiado"

# Eliminar directorios de build
print_info "Eliminando directorios de build..."
find . -name "build" -type d -exec rm -rf {} + 2>/dev/null || true
print_status "Directorios de build eliminados"

# Limpiar archivos temporales de Kotlin
print_info "Limpiando archivos temporales de Kotlin..."
rm -rf .kotlin/
print_status "Archivos temporales de Kotlin eliminados"

# Limpiar logs de error antiguos
print_info "Limpiando logs de error antiguos..."
if [ -d ".kotlin/errors" ]; then
    find .kotlin/errors -name "*.log" -mtime +7 -delete 2>/dev/null || true
    print_status "Logs antiguos eliminados"
fi

# Limpiar archivos temporales del sistema
print_info "Limpiando archivos temporales del sistema..."
find . -name ".DS_Store" -delete 2>/dev/null || true
find . -name "*.tmp" -delete 2>/dev/null || true
find . -name "*.temp" -delete 2>/dev/null || true
print_status "Archivos temporales del sistema eliminados"

# Limpiar cache de Android (si existe)
if [ -d "$HOME/.android" ]; then
    print_info "Limpiando cache de Android..."
    rm -rf "$HOME/.android/build-cache" 2>/dev/null || true
    print_status "Cache de Android limpiado"
fi

# Limpiar archivos de salida anteriores (opcional)
read -p "¿Eliminar APKs y AABs anteriores? (y/N): " -n 1 -r
echo
if [[ $REPLY =~ ^[Yy]$ ]]; then
    print_info "Eliminando archivos de salida anteriores..."
    find . -name "*.apk" -not -path "./keystores/*" -delete 2>/dev/null || true
    find . -name "*.aab" -not -path "./keystores/*" -delete 2>/dev/null || true
    print_status "Archivos de salida eliminados"
fi

# Mostrar espacio liberado
print_info "Calculando espacio liberado..."
FREED_SPACE=$(du -sh . 2>/dev/null | cut -f1 || echo "N/A")

echo ""
echo "📊 Resumen de limpieza:"
echo "======================="
print_status "Limpieza profunda completada"
print_status "Proyecto listo para build limpio"
print_info "Tamaño actual del proyecto: $FREED_SPACE"

echo ""
echo "🎯 Próximos pasos recomendados:"
echo "1. Ejecutar: ./gradlew :composeApp:assembleDebug"
echo "2. Si hay problemas: ./scripts/troubleshoot.sh"
echo "3. Para release: ./scripts/build-release.sh"

print_status "¡Limpieza completada exitosamente!"