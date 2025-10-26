#!/bin/bash

# Script para generar Android App Bundle (AAB) firmado
echo "📦 Iniciando build de Android App Bundle para YapeHub..."

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
./gradlew clean

echo "🔨 Compilando Android App Bundle..."
./gradlew :composeApp:bundleRelease

if [ $? -eq 0 ]; then
    echo "✅ AAB generado exitosamente!"
    echo "📱 Ubicación: composeApp/build/outputs/bundle/release/composeApp-release.aab"
    
    # Mostrar información del AAB
    AAB_PATH="composeApp/build/outputs/bundle/release/composeApp-release.aab"
    if [ -f "$AAB_PATH" ]; then
        echo "📊 Información del AAB:"
        ls -lh "$AAB_PATH"
        echo "📝 Este archivo está listo para subir a Google Play Store"
    fi
else
    echo "❌ Error al generar AAB"
    exit 1
fi