#!/bin/bash

# Script rápido para desarrollo - Solo APK debug
echo "🔧 Build rápido para desarrollo..."

# Colores
GREEN='\033[0;32m'
BLUE='\033[0;34m'
NC='\033[0m'

print_status() { echo -e "${GREEN}✅${NC} $1"; }
print_info() { echo -e "${BLUE}ℹ️${NC} $1"; }

# Verificar ubicación
if [ ! -f "build.gradle.kts" ]; then
    echo "❌ Este script debe ejecutarse desde la raíz del proyecto"
    exit 1
fi

START_TIME=$(date +%s)

print_info "Compilando APK debug..."
./gradlew :composeApp:assembleDebug

if [ $? -eq 0 ]; then
    END_TIME=$(date +%s)
    DURATION=$((END_TIME - START_TIME))
    
    print_status "APK debug generada en ${DURATION}s"
    
    if [ -f "composeApp/build/outputs/apk/debug/composeApp-debug.apk" ]; then
        APK_SIZE=$(du -h "composeApp/build/outputs/apk/debug/composeApp-debug.apk" | cut -f1)
        print_info "Archivo: composeApp-debug.apk ($APK_SIZE)"
        
        # Instalar automáticamente si hay dispositivo conectado
        if command -v adb &> /dev/null; then
            DEVICES=$(adb devices | grep -v "List of devices" | grep "device$" | wc -l)
            if [ $DEVICES -gt 0 ]; then
                echo ""
                read -p "¿Instalar en dispositivo conectado? (y/N): " -n 1 -r
                echo
                if [[ $REPLY =~ ^[Yy]$ ]]; then
                    print_info "Instalando en dispositivo..."
                    adb install -r composeApp/build/outputs/apk/debug/composeApp-debug.apk
                    print_status "Instalación completada"
                fi
            fi
        fi
    fi
else
    echo "❌ Error en la compilación"
    exit 1
fi