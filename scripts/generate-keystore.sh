#!/bin/bash

# Script para generar keystore para firmar APK
# Ejecutar solo una vez

echo "🔑 Generando keystore para YapeHub..."

# Crear directorio para keystores si no existe
mkdir -p keystores

# Generar keystore
keytool -genkey -v -keystore keystores/yapehub-release.keystore \
    -alias yapehub \
    -keyalg RSA \
    -keysize 2048 \
    -validity 10000 \
    -storepass yapehub2024! \
    -keypass yapehub2024! \
    -dname "CN=YapeHub, OU=Development, O=SysArp, L=Lima, ST=Lima, C=PE"

echo "✅ Keystore generado en: keystores/yapehub-release.keystore"
echo "⚠️  IMPORTANTE: Cambia las contraseñas por defecto"
echo "⚠️  IMPORTANTE: Guarda este archivo de forma segura"