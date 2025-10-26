#!/bin/bash

# Script centralizado para ejecutar todo el proceso de build de YapeHub
# Este script ejecuta todos los pasos necesarios para generar APK y AAB listos para distribución

# Colores para output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
PURPLE='\033[0;35m'
CYAN='\033[0;36m'
NC='\033[0m'

print_header() { echo -e "${PURPLE}🚀 $1${NC}"; }
print_step() { echo -e "${CYAN}📋 $1${NC}"; }
print_status() { echo -e "${GREEN}✅ $1${NC}"; }
print_error() { echo -e "${RED}❌ $1${NC}"; }
print_warning() { echo -e "${YELLOW}⚠️ $1${NC}"; }
print_info() { echo -e "${BLUE}ℹ️ $1${NC}"; }

# Variables
BUILD_TYPE="both"  # both, apk, aab
SKIP_CLEAN=false
SKIP_SECURITY=false
SKIP_VERIFICATION=false

# Función de ayuda
show_help() {
    echo "🚀 Script Centralizado de Build - YapeHub"
    echo ""
    echo "Uso: $0 [opciones]"
    echo ""
    echo "Opciones:"
    echo "  -t, --type TYPE        Tipo de build: 'apk', 'aab', 'both' (default: both)"
    echo "  -s, --skip-clean       Saltar limpieza inicial"
    echo "  --skip-security        Saltar verificación de seguridad"
    echo "  --skip-verification    Saltar verificación final"
    echo "  -h, --help             Mostrar esta ayuda"
    echo ""
    echo "Ejemplos:"
    echo "  $0                     # Build completo (APK + AAB)"
    echo "  $0 -t apk              # Solo APK"
    echo "  $0 -t aab              # Solo AAB"
    echo "  $0 -s                  # Sin limpieza inicial"
    echo ""
}

# Procesar argumentos
while [[ $# -gt 0 ]]; do
    case $1 in
        -t|--type)
            BUILD_TYPE="$2"
            shift 2
            ;;
        -s|--skip-clean)
            SKIP_CLEAN=true
            shift
            ;;
        --skip-security)
            SKIP_SECURITY=true
            shift
            ;;
        --skip-verification)
            SKIP_VERIFICATION=true
            shift
            ;;
        -h|--help)
            show_help
            exit 0
            ;;
        *)
            print_error "Opción desconocida: $1"
            show_help
            exit 1
            ;;
    esac
done

# Validar tipo de build
if [[ ! "$BUILD_TYPE" =~ ^(apk|aab|both)$ ]]; then
    print_error "Tipo de build inválido: $BUILD_TYPE"
    print_info "Tipos válidos: apk, aab, both"
    exit 1
fi

# Banner inicial
clear
echo "╔══════════════════════════════════════════════════════════════╗"
echo "║                    🚀 YapeHub Build Script                   ║"
echo "║                   Script Centralizado v1.0                  ║"
echo "╚══════════════════════════════════════════════════════════════╝"
echo ""

print_header "Configuración del Build"
echo "• Tipo de build: $BUILD_TYPE"
echo "• Limpieza inicial: $([ "$SKIP_CLEAN" = true ] && echo "❌ Saltada" || echo "✅ Incluida")"
echo "• Verificación de seguridad: $([ "$SKIP_SECURITY" = true ] && echo "❌ Saltada" || echo "✅ Incluida")"
echo "• Verificación final: $([ "$SKIP_VERIFICATION" = true ] && echo "❌ Saltada" || echo "✅ Incluida")"
echo ""

# Verificar que estamos en la ubicación correcta
if [ ! -f "build.gradle.kts" ]; then
    print_error "Este script debe ejecutarse desde la raíz del proyecto YapeHub"
    exit 1
fi

# Función para ejecutar paso con manejo de errores
execute_step() {
    local step_name="$1"
    local command="$2"
    local required="${3:-true}"
    
    print_step "$step_name"
    
    if eval "$command"; then
        print_status "$step_name completado"
        return 0
    else
        if [ "$required" = "true" ]; then
            print_error "$step_name falló - Abortando build"
            exit 1
        else
            print_warning "$step_name falló - Continuando..."
            return 1
        fi
    fi
}

# Iniciar cronómetro
START_TIME=$(date +%s)

echo "🔄 INICIANDO PROCESO DE BUILD"
echo "=============================="

# Paso 1: Verificación de seguridad (opcional)
if [ "$SKIP_SECURITY" = false ]; then
    execute_step "Verificación de seguridad" "./scripts/security-check.sh > /dev/null 2>&1" false
    echo ""
fi

# Paso 2: Limpieza inicial (opcional)
if [ "$SKIP_CLEAN" = false ]; then
    execute_step "Limpieza del proyecto" "echo 'N' | ./scripts/clean-project.sh > /dev/null 2>&1"
    echo ""
fi

# Paso 3: Verificar configuración
execute_step "Verificación de configuración" "./scripts/troubleshoot.sh > /dev/null 2>&1" false
echo ""

# Paso 4: Build según tipo seleccionado
case $BUILD_TYPE in
    "apk")
        execute_step "Generando APK de release" "./scripts/build-release.sh"
        ;;
    "aab")
        execute_step "Generando AAB para Play Store" "./scripts/build-bundle.sh"
        ;;
    "both")
        execute_step "Generando APK de release" "./scripts/build-release.sh"
        echo ""
        execute_step "Generando AAB para Play Store" "./scripts/build-bundle.sh"
        ;;
esac

echo ""

# Paso 5: Verificación final (opcional)
if [ "$SKIP_VERIFICATION" = false ]; then
    execute_step "Verificación de archivos generados" "./scripts/verify-apk.sh > /dev/null 2>&1"
    echo ""
fi

# Calcular tiempo transcurrido
END_TIME=$(date +%s)
DURATION=$((END_TIME - START_TIME))
MINUTES=$((DURATION / 60))
SECONDS=$((DURATION % 60))

# Resumen final
echo "🎉 BUILD COMPLETADO EXITOSAMENTE"
echo "================================="
print_status "Tiempo total: ${MINUTES}m ${SECONDS}s"
echo ""

# Mostrar archivos generados
print_header "Archivos Generados"
case $BUILD_TYPE in
    "apk")
        if [ -f "composeApp/build/outputs/apk/release/composeApp-release.apk" ]; then
            APK_SIZE=$(du -h "composeApp/build/outputs/apk/release/composeApp-release.apk" | cut -f1)
            print_status "APK: composeApp-release.apk ($APK_SIZE)"
        fi
        ;;
    "aab")
        if [ -f "composeApp/build/outputs/bundle/release/composeApp-release.aab" ]; then
            AAB_SIZE=$(du -h "composeApp/build/outputs/bundle/release/composeApp-release.aab" | cut -f1)
            print_status "AAB: composeApp-release.aab ($AAB_SIZE)"
        fi
        ;;
    "both")
        if [ -f "composeApp/build/outputs/apk/release/composeApp-release.apk" ]; then
            APK_SIZE=$(du -h "composeApp/build/outputs/apk/release/composeApp-release.apk" | cut -f1)
            print_status "APK: composeApp-release.apk ($APK_SIZE)"
        fi
        if [ -f "composeApp/build/outputs/bundle/release/composeApp-release.aab" ]; then
            AAB_SIZE=$(du -h "composeApp/build/outputs/bundle/release/composeApp-release.aab" | cut -f1)
            print_status "AAB: composeApp-release.aab ($AAB_SIZE)"
        fi
        ;;
esac

echo ""
print_header "Próximos Pasos"
case $BUILD_TYPE in
    "apk"|"both")
        echo "📱 Para testing: Instala la APK en tu dispositivo"
        echo "   adb install composeApp/build/outputs/apk/release/composeApp-release.apk"
        ;;
esac

case $BUILD_TYPE in
    "aab"|"both")
        echo "🏪 Para Play Store: Sube el AAB a Google Play Console"
        echo "   Archivo: composeApp/build/outputs/bundle/release/composeApp-release.aab"
        ;;
esac

echo ""
print_status "¡Build de YapeHub completado exitosamente! 🚀"