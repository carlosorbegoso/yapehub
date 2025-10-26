#!/bin/bash

# Script de verificación de seguridad para YapeHub
echo "🔒 Iniciando verificación de seguridad..."

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

SECURITY_ISSUES=0

# Función para reportar problemas de seguridad
report_security_issue() {
    print_error "$1"
    SECURITY_ISSUES=$((SECURITY_ISSUES + 1))
}

echo "🔍 VERIFICACIÓN DE ARCHIVOS SENSIBLES"
echo "===================================="

# Verificar archivos sensibles en Git
print_info "Verificando archivos sensibles en Git..."
SENSITIVE_FILES=$(git ls-files 2>/dev/null | grep -E "\.(keystore|jks|p12|pem|apk|aab|properties)$" | grep -v "\.example$" | grep -v "gradle-wrapper.properties")

if [ -n "$SENSITIVE_FILES" ]; then
    report_security_issue "Archivos sensibles encontrados en Git:"
    echo "$SENSITIVE_FILES"
    echo "💡 Solución: git rm --cached <archivo> && git commit"
else
    print_status "No hay archivos sensibles en Git"
fi

# Verificar .gitignore
print_info "Verificando configuración de .gitignore..."
if [ -f ".gitignore" ]; then
    if grep -q "keystore.properties" .gitignore && grep -q "keystores/" .gitignore; then
        print_status ".gitignore configurado correctamente"
    else
        report_security_issue ".gitignore no incluye archivos sensibles"
        echo "💡 Solución: Actualizar .gitignore con archivos sensibles"
    fi
else
    report_security_issue ".gitignore no encontrado"
fi

# Verificar contraseñas hardcodeadas
print_info "Buscando contraseñas hardcodeadas..."
HARDCODED_SECRETS=$(grep -r "password.*=" --include="*.kt" . 2>/dev/null | grep -v "// " | head -5)
if [ -n "$HARDCODED_SECRETS" ]; then
    report_security_issue "Posibles contraseñas hardcodeadas encontradas:"
    echo "$HARDCODED_SECRETS"
    echo "💡 Solución: Usar variables de entorno o archivos de configuración"
else
    print_status "No se encontraron contraseñas hardcodeadas"
fi

# Verificar archivos de configuración
print_info "Verificando archivos de configuración..."
if [ -f "keystore.properties" ]; then
    if grep -q "CAMBIAR_PASSWORD" keystore.properties; then
        print_warning "keystore.properties contiene contraseñas por defecto"
        echo "💡 Solución: Cambiar contraseñas por valores seguros"
    else
        print_status "keystore.properties configurado"
    fi
    
    # Verificar permisos
    PERMS=$(stat -f "%A" keystore.properties 2>/dev/null || stat -c "%a" keystore.properties 2>/dev/null)
    if [ "$PERMS" != "600" ] && [ "$PERMS" != "644" ]; then
        print_warning "Permisos de keystore.properties: $PERMS"
        echo "💡 Solución: chmod 600 keystore.properties"
    fi
else
    print_info "keystore.properties no encontrado (normal si no se ha configurado)"
fi

echo ""
echo "🔍 VERIFICACIÓN DE ARCHIVOS DE BUILD"
echo "===================================="

# Verificar archivos de build en Git
print_info "Verificando archivos de build..."
BUILD_FILES=$(git ls-files 2>/dev/null | grep -E "^build/|\.apk$|\.aab$")
if [ -n "$BUILD_FILES" ]; then
    report_security_issue "Archivos de build encontrados en Git:"
    echo "$BUILD_FILES"
    echo "💡 Solución: git rm -r --cached build/ && git commit"
else
    print_status "No hay archivos de build en Git"
fi

# Verificar tamaño del repositorio
print_info "Verificando tamaño del repositorio..."
if [ -d ".git" ]; then
    REPO_SIZE=$(du -sh .git 2>/dev/null | cut -f1)
    print_info "Tamaño del repositorio: $REPO_SIZE"
    
    # Advertir si es muy grande
    REPO_SIZE_MB=$(du -sm .git 2>/dev/null | cut -f1)
    if [ "$REPO_SIZE_MB" -gt 100 ]; then
        print_warning "Repositorio grande (${REPO_SIZE_MB}MB) - posibles archivos binarios"
        echo "💡 Solución: Revisar archivos grandes con: git ls-files | xargs ls -la | sort -k5 -nr | head -10"
    fi
fi

echo ""
echo "🔍 VERIFICACIÓN DE CONFIGURACIÓN"
echo "================================"

# Verificar variables de entorno sensibles
print_info "Verificando variables de entorno..."
ENV_VARS=$(env | grep -i "password\|secret\|key\|token" | wc -l)
if [ "$ENV_VARS" -gt 0 ]; then
    print_info "$ENV_VARS variables de entorno sensibles encontradas"
    print_warning "Asegúrate de que no estén hardcodeadas en scripts"
fi

# Verificar archivos de logs
print_info "Verificando archivos de logs..."
LOG_FILES=$(find . -name "*.log" -not -path "./.git/*" 2>/dev/null | head -5)
if [ -n "$LOG_FILES" ]; then
    print_warning "Archivos de log encontrados:"
    echo "$LOG_FILES"
    echo "💡 Solución: Agregar *.log a .gitignore"
fi

# Verificar permisos de scripts
print_info "Verificando permisos de scripts..."
NON_EXECUTABLE=$(find scripts/ -name "*.sh" ! -executable 2>/dev/null)
if [ -n "$NON_EXECUTABLE" ]; then
    print_warning "Scripts no ejecutables encontrados:"
    echo "$NON_EXECUTABLE"
    echo "💡 Solución: chmod +x scripts/*.sh"
else
    print_status "Todos los scripts son ejecutables"
fi

echo ""
echo "📊 RESUMEN DE SEGURIDAD"
echo "======================="

if [ $SECURITY_ISSUES -eq 0 ]; then
    print_status "¡No se encontraron problemas de seguridad críticos!"
    echo "🛡️ El proyecto cumple con las mejores prácticas de seguridad"
else
    print_error "Se encontraron $SECURITY_ISSUES problema(s) de seguridad"
    echo "🔧 Revisa las soluciones sugeridas arriba"
fi

echo ""
echo "🛠️ COMANDOS DE LIMPIEZA RECOMENDADOS"
echo "===================================="
echo "• Limpiar archivos sensibles: git rm --cached keystore.properties"
echo "• Limpiar builds: git rm -r --cached build/"
echo "• Actualizar .gitignore: echo 'archivo_sensible' >> .gitignore"
echo "• Verificar historial: git log --oneline --name-only | grep -E '\.(keystore|apk|aab)$'"
echo "• Limpiar historial: git filter-branch --index-filter 'git rm --cached --ignore-unmatch archivo'"

echo ""
if [ $SECURITY_ISSUES -eq 0 ]; then
    print_status "¡Verificación de seguridad completada exitosamente!"
else
    print_warning "Verificación completada con $SECURITY_ISSUES problema(s) de seguridad"
    echo "🔒 Soluciona los problemas antes de hacer push al repositorio"
fi