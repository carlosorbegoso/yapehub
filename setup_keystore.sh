#!/bin/bash

# Script para configurar el keystore de YapeHub
# Ejecutar: chmod +x setup_keystore.sh && ./setup_keystore.sh

echo "🚀 Configurando keystore para YapeHub..."

# Crear directorio keystore si no existe
mkdir -p keystore

# Verificar si ya existe el keystore
if [ -f "keystore/yapehub-release-key.jks" ]; then
    echo "⚠️  El keystore ya existe. ¿Deseas regenerarlo? (y/N)"
    read -r response
    if [[ "$response" =~ ^[Yy]$ ]]; then
        rm -f keystore/yapehub-release-key.jks
    else
        echo "✅ Usando keystore existente."
        exit 0
    fi
fi

echo "📝 Generando nuevo keystore..."
echo "Por favor, completa la información solicitada:"
echo ""

# Generar el keystore
keytool -genkey -v -keystore keystore/yapehub-release-key.jks -keyalg RSA -keysize 2048 -validity 10000 -alias yapehub-key

if [ $? -eq 0 ]; then
    echo ""
    echo "✅ Keystore generado exitosamente!"
    echo ""
    echo "📋 Próximos pasos:"
    echo "1. Edita el archivo 'keystore.properties' con las contraseñas que acabas de crear"
    echo "2. Ejecuta: ./gradlew bundleRelease"
    echo "3. El archivo AAB estará en: composeApp/build/outputs/bundle/release/"
    echo ""
    echo "⚠️  IMPORTANTE:"
    echo "- Guarda las contraseñas en un lugar seguro"
    echo "- Nunca subas el archivo keystore.properties a Git"
    echo "- Haz backup del keystore en un lugar seguro"
else
    echo "❌ Error al generar el keystore. Intenta nuevamente."
    exit 1
fi
