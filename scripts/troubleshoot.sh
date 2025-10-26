#!/bin/bash

# Script de diagnóstico y solución de problemas para YapeHub
echo "🔍 Iniciando diagnóstico del proyecto YapeHub..."

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

ISSUES_FOUND=0

# Función para reportar problemas
report_issue() {
    print_error "$1"
    ISSUES_FOUND=$((ISSUES_FOUND + 1))
}

echo "🔍 DIAGNÓSTICO DEL SISTEMA"
echo "=========================="

# Verificar ubicación
if [ ! -f "build.gradle.kts" ]; then
    report_issue "No estás en la raíz del proyecto YapeHub"
    echo "💡 Solución: cd al directorio correcto"
    exit 1
fi
print_status "Ubicación correcta del proyecto"

# Verificar Java
print_info "Verificando Java..."
if command -v java &> /dev/null; then
    JAVA_VERSION=$(java -version 2>&1 | head -n 1 | cut -d'"' -f2)
    JAVA_MAJOR=$(echo $JAVA_VERSION | cut -d'.' -f1)
    if [ "$JAVA_MAJOR" -ge "17" ]; then
        print_status "Java $JAVA_VERSION (compatible)"
    else
        report_issue "Java $JAVA_VERSION es muy antiguo (necesita 17+)"
        echo "💡 Solución: Actualiza Java a versión 17 o superior"
    fi
else
    report_issue "Java no encontrado"
    echo "💡 Solución: Instala Java 17+ desde https://adoptium.net/"
fi

# Verificar JAVA_HOME
if [ -n "$JAVA_HOME" ]; then
    print_status "JAVA_HOME configurado: $JAVA_HOME"
else
    print_warning "JAVA_HOME no configurado (puede causar problemas)"
    echo "💡 Solución: export JAVA_HOME=/path/to/java"
fi

# Verificar Gradle
print_info "Verificando Gradle..."
if [ -f "./gradlew" ]; then
    if [ -x "./gradlew" ]; then
        print_status "Gradle Wrapper ejecutable"
        GRADLE_VERSION=$(./gradlew --version 2>/dev/null | grep "Gradle" | head -1 || echo "Desconocido")
        print_info "$GRADLE_VERSION"
    else
        print_warning "Gradle Wrapper no es ejecutable"
        chmod +x ./gradlew
        print_status "Permisos corregidos"
    fi
else
    report_issue "Gradle Wrapper no encontrado"
fi

# Verificar Android SDK
print_info "Verificando Android SDK..."
if [ -n "$ANDROID_HOME" ]; then
    if [ -d "$ANDROID_HOME" ]; then
        print_status "Android SDK encontrado: $ANDROID_HOME"
    else
        report_issue "ANDROID_HOME apunta a directorio inexistente"
    fi
else
    print_warning "ANDROID_HOME no configurado"
    echo "💡 Solución: Instala Android Studio y configura ANDROID_HOME"
fi

echo ""
echo "🔍 DIAGNÓSTICO DEL PROYECTO"
echo "==========================="

# Verificar archivos de configuración
print_info "Verificando archivos de configuración..."

if [ -f "keystore.properties" ]; then
    print_status "keystore.properties existe"
    
    # Verificar contenido
    if grep -q "CAMBIAR_PASSWORD" keystore.properties; then
        print_warning "keystore.properties contiene contraseñas por defecto"
        echo "💡 Solución: Edita keystore.properties con contraseñas reales"
    fi
    
    # Verificar keystore
    KEYSTORE_FILE=$(grep "storeFile=" keystore.properties | cut -d'=' -f2)
    if [ -f "$KEYSTORE_FILE" ]; then
        print_status "Keystore encontrado: $KEYSTORE_FILE"
    else
        report_issue "Keystore no encontrado: $KEYSTORE_FILE"
        echo "💡 Solución: ./scripts/generate-keystore.sh"
    fi
else
    report_issue "keystore.properties no encontrado"
    echo "💡 Solución: cp keystore.properties.example keystore.properties"
fi

# Verificar dependencias
print_info "Verificando dependencias..."
if [ -f "gradle/libs.versions.toml" ]; then
    print_status "Catálogo de versiones encontrado"
else
    print_warning "Catálogo de versiones no encontrado"
fi

# Verificar permisos de scripts
print_info "Verificando scripts..."
SCRIPT_COUNT=0
EXECUTABLE_COUNT=0
for script in scripts/*.sh; do
    if [ -f "$script" ]; then
        SCRIPT_COUNT=$((SCRIPT_COUNT + 1))
        if [ -x "$script" ]; then
            EXECUTABLE_COUNT=$((EXECUTABLE_COUNT + 1))
        fi
    fi
done

if [ $SCRIPT_COUNT -eq $EXECUTABLE_COUNT ]; then
    print_status "Todos los scripts ($SCRIPT_COUNT) son ejecutables"
else
    print_warning "$((SCRIPT_COUNT - EXECUTABLE_COUNT)) scripts no son ejecutables"
    echo "💡 Solución: chmod +x scripts/*.sh"
fi

echo ""
echo "🔍 DIAGNÓSTICO DE COMPILACIÓN"
echo "============================="

# Verificar compilación básica
print_info "Probando compilación debug..."
if ./gradlew :composeApp:assembleDebug --quiet > /dev/null 2>&1; then
    print_status "Compilación debug exitosa"
else
    report_issue "Compilación debug falló"
    echo "💡 Solución: Revisar logs con: ./gradlew :composeApp:assembleDebug"
fi

# Verificar logs de error recientes
print_info "Verificando logs de error..."
if [ -d ".kotlin/errors" ]; then
    ERROR_COUNT=$(find .kotlin/errors -name "*.log" -mtime -1 | wc -l)
    if [ $ERROR_COUNT -gt 0 ]; then
        print_warning "$ERROR_COUNT logs de error recientes encontrados"
        echo "💡 Revisar: ls -la .kotlin/errors/"
    else
        print_status "No hay logs de error recientes"
    fi
fi

# Verificar espacio en disco
print_info "Verificando espacio en disco..."
AVAILABLE_SPACE=$(df -h . | tail -1 | awk '{print $4}')
print_info "Espacio disponible: $AVAILABLE_SPACE"

echo ""
echo "📊 RESUMEN DEL DIAGNÓSTICO"
echo "=========================="

if [ $ISSUES_FOUND -eq 0 ]; then
    print_status "¡No se encontraron problemas críticos!"
    echo "🎯 El proyecto está listo para compilar"
else
    print_error "Se encontraron $ISSUES_FOUND problema(s)"
    echo "🔧 Revisa las soluciones sugeridas arriba"
fi

echo ""
echo "🛠️ COMANDOS ÚTILES PARA SOLUCIONAR PROBLEMAS"
echo "============================================="
echo "• Limpiar proyecto: ./scripts/clean-project.sh"
echo "• Regenerar keystore: ./scripts/generate-keystore.sh"
echo "• Setup completo: ./scripts/setup-project.sh"
echo "• Ver logs detallados: ./gradlew --info :composeApp:assembleDebug"
echo "• Ver dependencias: ./gradlew :composeApp:dependencies"
echo "• Verificar firma: ./gradlew :composeApp:signingReport"

echo ""
if [ $ISSUES_FOUND -eq 0 ]; then
    print_status "¡Diagnóstico completado exitosamente!"
else
    print_warning "Diagnóstico completado con $ISSUES_FOUND problema(s) encontrado(s)"
fi