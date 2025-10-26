#!/bin/bash

# Script para limpiar completamente el cache de Gradle cuando hay problemas
echo "🔧 Limpieza completa del cache de Gradle"

# Colores para output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m'

print_step() { echo -e "${BLUE}📋 $1${NC}"; }
print_status() { echo -e "${GREEN}✅ $1${NC}"; }
print_warning() { echo -e "${YELLOW}⚠️ $1${NC}"; }

print_step "Deteniendo daemon de Gradle"
./gradlew --stop 2>/dev/null || true
print_status "Daemon detenido"

print_step "Limpiando cache local del proyecto"
rm -rf .gradle/ 2>/dev/null || true
rm -rf build/ 2>/dev/null || true
rm -rf composeApp/build/ 2>/dev/null || true
print_status "Cache local limpiado"

print_step "Limpiando cache global de Gradle"
rm -rf ~/.gradle/caches/ 2>/dev/null || true
rm -rf ~/.gradle/daemon/ 2>/dev/null || true
rm -rf ~/.gradle/wrapper/ 2>/dev/null || true
print_status "Cache global limpiado"

print_step "Limpiando archivos temporales"
rm -f .gradle_cache_corrupted 2>/dev/null || true
rm -f /tmp/build_output.log 2>/dev/null || true
print_status "Archivos temporales limpiados"

print_step "Regenerando wrapper de Gradle"
chmod +x gradlew
./gradlew wrapper --gradle-version=8.14.3 --no-daemon 2>/dev/null || true
print_status "Wrapper regenerado"

echo ""
print_status "🎉 Limpieza completa terminada"
print_warning "Nota: El próximo build será más lento porque regenerará el cache"