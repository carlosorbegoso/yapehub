#!/bin/bash

# Script para generar APK de release firmada
echo "🚀 Iniciando build de release para YapeHub..."

# Verificar que existe keystore.properties
if [ ! -f "keystore.properties" ]; then
    echo "❌ Error: No se encontró keystore.properties"
    echo "📝 Copia keystore.properties.example como keystore.properties y configúralo"
    exit 1
fi

# Verificar que existe el keystore
KEYSTORE_FILE=$(grep "storeFile=" keystore.properties | cut -d'=' -f2)
if [ ! -f "$KEYSTORE_FILE" ]; then
    echo "❌ Error: No se encontró el archivo keystore: $KEYSTORE_FILE"
    echo "🔑 Ejecuta scripts/generate-keystore.sh primero"
    exit 1
fi

echo "🧹 Limpiando proyecto..."
# Limpiar cache si hay problemas conocidos
if [ -f ".gradle_cache_corrupted" ]; then
    echo "🔧 Detectado cache corrupto, limpiando..."
    rm -rf ~/.gradle/caches/*/kotlin-dsl/accessors/ 2>/dev/null || true
    rm -rf .gradle/configuration-cache/ 2>/dev/null || true
fi

./gradlew clean --no-daemon

echo "🔨 Compilando APK de release..."
./gradlew :composeApp:assembleRelease --no-daemon

if [ $? -eq 0 ]; then
    echo "✅ APK generada exitosamente!"
    echo "📱 Ubicación: composeApp/build/outputs/apk/release/composeApp-release.apk"
    
    # Mostrar información del APK
    APK_PATH="composeApp/build/outputs/apk/release/composeApp-release.apk"
    if [ -f "$APK_PATH" ]; then
        echo "📊 Información del APK:"
        ls -lh "$APK_PATH"
        
        # Verificar firma (requiere Android SDK)
        if command -v aapt &> /dev/null; then
            echo "🔍 Verificando APK..."
            aapt dump badging "$APK_PATH" | grep "package:"
        fi
    fi
else
    echo "❌ Error al generar APK"
    exit 1
fi