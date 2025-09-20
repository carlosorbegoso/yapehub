#!/bin/bash

# Script para compilar YapeHub para producción
# Ejecutar: chmod +x build_release.sh && ./build_release.sh

echo "🚀 Compilando YapeHub para Google Play Store..."

# Verificar que existe el keystore
if [ ! -f "keystore/yapehub-release-key.jks" ]; then
    echo "❌ No se encontró el keystore. Ejecuta primero: ./setup_keystore.sh"
    exit 1
fi

# Verificar que existe el archivo de configuración
if [ ! -f "keystore.properties" ]; then
    echo "❌ No se encontró keystore.properties. Configúralo primero."
    exit 1
fi

echo "🧹 Limpiando proyecto..."
./gradlew clean

echo "📦 Generando AAB para Play Store..."
./gradlew bundleRelease

if [ $? -eq 0 ]; then
    echo ""
    echo "✅ ¡Compilación exitosa!"
    echo ""
    echo "📁 Archivos generados:"
    echo "- AAB: composeApp/build/outputs/bundle/release/composeApp-release.aab"
    echo ""
    echo "🚀 Próximos pasos:"
    echo "1. Ve a Google Play Console"
    echo "2. Sube el archivo .aab"
    echo "3. Completa la información de la tienda"
    echo "4. Sube las capturas de pantalla"
    echo "5. Configura la política de privacidad"
    echo ""
    echo "📋 Revisa PLAY_STORE_GUIDE.md para más detalles"
else
    echo "❌ Error en la compilación. Revisa los errores arriba."
    exit 1
fi
