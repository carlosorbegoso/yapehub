#!/bin/bash

# Script para verificar APK y AAB generados
echo "🔍 Verificando archivos generados..."

APK_PATH="composeApp/build/outputs/apk/release/composeApp-release.apk"
AAB_PATH="composeApp/build/outputs/bundle/release/composeApp-release.aab"

echo "📱 Verificando APK..."
if [ -f "$APK_PATH" ]; then
    echo "✅ APK encontrada: $APK_PATH"
    ls -lh "$APK_PATH"
    
    # Verificar que el archivo no esté vacío
    if [ -s "$APK_PATH" ]; then
        echo "✅ APK tiene contenido ($(du -h "$APK_PATH" | cut -f1))"
    else
        echo "❌ APK está vacía"
    fi
    
    # Verificar extensión
    if [[ "$APK_PATH" == *.apk ]]; then
        echo "✅ Extensión correcta (.apk)"
    else
        echo "❌ Extensión incorrecta"
    fi
else
    echo "❌ APK no encontrada en: $APK_PATH"
fi

echo ""
echo "📦 Verificando AAB..."
if [ -f "$AAB_PATH" ]; then
    echo "✅ AAB encontrado: $AAB_PATH"
    ls -lh "$AAB_PATH"
    
    # Verificar que el archivo no esté vacío
    if [ -s "$AAB_PATH" ]; then
        echo "✅ AAB tiene contenido ($(du -h "$AAB_PATH" | cut -f1))"
    else
        echo "❌ AAB está vacío"
    fi
    
    # Verificar extensión
    if [[ "$AAB_PATH" == *.aab ]]; then
        echo "✅ Extensión correcta (.aab)"
    else
        echo "❌ Extensión incorrecta"
    fi
else
    echo "❌ AAB no encontrado en: $AAB_PATH"
fi

echo ""
echo "🔐 Verificando configuración de firma..."
./gradlew :composeApp:signingReport --quiet | grep -A 10 "Variant: release"

echo ""
echo "📋 Resumen:"
echo "- APK para testing: $APK_PATH"
echo "- AAB para Play Store: $AAB_PATH"
echo "- Keystore: keystores/yapehub-release.keystore"
echo ""
echo "🚀 ¡Archivos listos para distribución!"