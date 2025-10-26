#!/bin/bash

# Script para diagnosticar errores de parsing en YapeHub
echo "🔍 Diagnosticando errores de parsing..."

# Colores
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m'

print_status() { echo -e "${GREEN}✅${NC} $1"; }
print_error() { echo -e "${RED}❌${NC} $1"; }
print_warning() { echo -e "${YELLOW}⚠️${NC} $1"; }
print_info() { echo -e "${BLUE}ℹ️${NC} $1"; }

echo "🔍 DIAGNÓSTICO DE ERRORES DE PARSING"
echo "===================================="

# Buscar archivos con parsing JSON
print_info "Buscando archivos con parsing JSON..."
JSON_FILES=$(find composeApp/src -name "*.kt" -exec grep -l "Json\|serialization\|parseToJsonElement" {} \; 2>/dev/null)

if [ -n "$JSON_FILES" ]; then
    print_status "Archivos con parsing JSON encontrados:"
    echo "$JSON_FILES"
else
    print_warning "No se encontraron archivos con parsing JSON explícito"
fi

# Buscar logs de error
print_info "Buscando logs de error recientes..."
if [ -d ".kotlin/errors" ]; then
    RECENT_ERRORS=$(find .kotlin/errors -name "*.log" -mtime -1 2>/dev/null)
    if [ -n "$RECENT_ERRORS" ]; then
        print_warning "Logs de error recientes encontrados:"
        echo "$RECENT_ERRORS"
    fi
fi

# Verificar dependencias de serialización
print_info "Verificando dependencias de kotlinx-serialization..."
if grep -q "kotlinx-serialization" gradle/libs.versions.toml; then
    print_status "Dependencia kotlinx-serialization encontrada"
    SERIALIZATION_VERSION=$(grep "kotlinx-serialization" gradle/libs.versions.toml | head -1)
    print_info "$SERIALIZATION_VERSION"
else
    print_error "Dependencia kotlinx-serialization no encontrada"
fi

# Verificar configuración del plugin
print_info "Verificando plugin de serialización..."
if grep -q "kotlinx.serialization" composeApp/build.gradle.kts; then
    print_status "Plugin de serialización configurado"
else
    print_error "Plugin de serialización no configurado"
fi

# Sugerencias
echo ""
echo "💡 SUGERENCIAS PARA SOLUCIONAR PARSING"
echo "======================================"
echo "1. Verificar que el backend devuelva arrays en lugar de null"
echo "2. Implementar parsing defensivo con try-catch"
echo "3. Usar valores por defecto en modelos de datos"
echo "4. Agregar logging detallado para debugging"
echo "5. Configurar Json { ignoreUnknownKeys = true }"

# Comandos útiles
echo ""
echo "🛠️ COMANDOS ÚTILES"
echo "=================="
echo "• Ver logs en tiempo real: adb logcat | grep PARSING"
echo "• Limpiar y recompilar: ./scripts/clean-project.sh && ./scripts/dev-build.sh"
echo "• Verificar dependencias: ./gradlew :composeApp:dependencies | grep serialization"

print_status "Diagnóstico completado"