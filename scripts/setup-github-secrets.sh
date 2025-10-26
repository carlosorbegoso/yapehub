#!/bin/bash

# Script para ayudar a configurar los secrets de GitHub Actions
echo "🔐 Configuración de Secrets para GitHub Actions - YapeChamoApp"
echo "=============================================================="

# Colores para output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m'

print_step() { echo -e "${BLUE}📋 $1${NC}"; }
print_status() { echo -e "${GREEN}✅ $1${NC}"; }
print_warning() { echo -e "${YELLOW}⚠️ $1${NC}"; }
print_info() { echo -e "${BLUE}ℹ️ $1${NC}"; }

echo ""
print_step "Generando valores para los secrets de GitHub..."

# Verificar que existe el keystore
if [ ! -f "keystores/yapehub-release.keystore" ]; then
    print_warning "No se encontró el keystore en keystores/yapehub-release.keystore"
    echo "Ejecuta primero: bash scripts/generate-keystore.sh"
    exit 1
fi

echo ""
print_step "1. KEYSTORE_BASE64"
echo "Convierte tu keystore a base64 para GitHub:"
echo ""
echo "Valor para el secret KEYSTORE_BASE64:"
echo "======================================"
base64 -i keystores/yapehub-release.keystore
echo "======================================"
echo ""

print_step "2. Otros secrets del keystore"
echo "KEYSTORE_PASSWORD: yapehub2024!"
echo "KEY_ALIAS: yapehub"
echo "KEY_PASSWORD: yapehub2024!"
echo ""

print_step "3. Configurar en GitHub"
echo "Ve a tu repositorio en GitHub:"
echo "Settings > Secrets and variables > Actions > New repository secret"
echo ""
echo "Agrega estos secrets:"
echo "- KEYSTORE_BASE64 (el valor de arriba)"
echo "- KEYSTORE_PASSWORD: yapehub2024!"
echo "- KEY_ALIAS: yapehub"
echo "- KEY_PASSWORD: yapehub2024!"
echo ""

print_step "4. Para Google Play Store (opcional)"
echo "Si quieres deploy automático, también agrega:"
echo "- GOOGLE_PLAY_SERVICE_ACCOUNT_JSON"
echo "  (JSON completo de la cuenta de servicio de Google Play)"
echo ""

print_step "5. Cómo usar el workflow"
echo "• Push normal: Solo ejecuta tests y build"
echo "• Push con [deploy]: Sube automáticamente a Play Store"
echo "  Ejemplo: git commit -m 'Nueva versión [deploy]'"
echo ""

print_status "¡Configuración lista!"
print_info "Revisa .github/SETUP_SECRETS.md para más detalles"