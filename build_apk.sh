#!/bin/bash

# Script para compilar APKs de YapeHub
# Ejecutar: chmod +x build_apk.sh && ./build_apk.sh

echo "🚀 Compilando APKs de YapeHub..."

# Verificar que existe el keystore para release
if [ ! -f "keystore/yapehub-release-key.jks" ]; then
    echo "⚠️  No se encontró el keystore para release. Ejecuta primero: ./setup_keystore.sh"
    echo "📱 Generando solo APK de debug..."
    ./gradlew assembleDebug
else
    echo "📱 Generando APKs de debug y release..."
    ./gradlew assembleDebug assembleRelease
fi

if [ $? -eq 0 ]; then
    echo ""
    echo "✅ ¡Compilación exitosa!"
    echo ""
    echo "📁 Archivos generados:"
    echo ""
    echo "🔧 APKs de Debug:"
    ls -lh composeApp/build/outputs/apk/debug/*.apk 2>/dev/null || echo "No se encontraron APKs de debug"
    echo ""
    echo "🚀 APKs de Release:"
    ls -lh composeApp/build/outputs/apk/release/*.apk 2>/dev/null || echo "No se encontraron APKs de release"
    echo ""
    echo "📋 Información importante:"
    echo "- APK Universal: Funciona en todos los dispositivos Android"
    echo "- APK específico por arquitectura: Más pequeño, solo para ciertos dispositivos"
    echo "- Para testing: Usa el APK universal de debug"
    echo "- Para Play Store: Usa el AAB de release (./gradlew bundleRelease)"
    echo ""
    echo "📱 Para instalar en tu dispositivo:"
    echo "adb install composeApp/build/outputs/apk/debug/composeApp-universal-debug.apk"
else
    echo "❌ Error en la compilación. Revisa los errores arriba."
    exit 1
fi
