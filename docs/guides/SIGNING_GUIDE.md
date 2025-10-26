# 🔐 Guía de Firma de APK - YapeHub

## 📋 Pasos para Firmar tu APK

### 1. **Primera vez - Generar Keystore**

```bash
# Ejecutar solo una vez
./scripts/generate-keystore.sh
```

**⚠️ IMPORTANTE:**
- Cambia las contraseñas por defecto en el script
- Guarda el keystore de forma segura (nunca lo subas a Git)
- Si pierdes el keystore, no podrás actualizar la app en Play Store

### 2. **Configurar Credenciales**

```bash
# Copiar archivo de ejemplo
cp keystore.properties.example keystore.properties

# Editar con tus datos reales
nano keystore.properties
```

Contenido del archivo:
```properties
storeFile=keystores/yapehub-release.keystore
storePassword=TU_PASSWORD_SEGURO
keyAlias=yapehub
keyPassword=TU_PASSWORD_SEGURO
```

### 3. **Generar APK Firmada**

```bash
# Para APK normal
./scripts/build-release.sh

# Para Android App Bundle (recomendado para Play Store)
./scripts/build-bundle.sh
```

## 📁 Archivos Generados

### APK Release:
```
composeApp/build/outputs/apk/release/composeApp-release.apk
```

### Android App Bundle:
```
composeApp/build/outputs/bundle/release/composeApp-release.aab
```

## 🔍 Verificar Firma

```bash
# Verificar que la APK está firmada
jarsigner -verify -verbose -certs composeApp/build/outputs/apk/release/composeApp-release.apk

# Ver información de la APK
aapt dump badging composeApp/build/outputs/apk/release/composeApp-release.apk
```

## 🚀 Distribución

### Para Testing:
- Usa la **APK** (`composeApp-release.apk`)
- Instala directamente en dispositivos
- Comparte por email/drive

### Para Play Store:
- Usa el **AAB** (`composeApp-release.aab`)
- Sube a Google Play Console
- Google genera APKs optimizadas automáticamente

## 🔒 Seguridad

### ✅ Hacer:
- Usar contraseñas fuertes
- Guardar keystore en lugar seguro
- Hacer backup del keystore
- No compartir credenciales

### ❌ No hacer:
- Subir keystore a Git
- Usar contraseñas simples
- Compartir keystore.properties
- Perder el keystore original

## 🐛 Solución de Problemas

### Error: "keystore.properties not found"
```bash
cp keystore.properties.example keystore.properties
# Editar con tus datos
```

### Error: "keystore file not found"
```bash
./scripts/generate-keystore.sh
```

### Error de compilación:
```bash
./gradlew clean
./gradlew :composeApp:assembleDebug  # Probar debug primero
```

### Verificar configuración:
```bash
./gradlew :composeApp:signingReport
```

## 📱 Instalación Manual

```bash
# Instalar APK en dispositivo conectado
adb install composeApp/build/outputs/apk/release/composeApp-release.apk

# Desinstalar versión anterior si es necesario
adb uninstall com.yapechamo.composeapp
```

## 🔄 Actualización de Versión

Editar `composeApp/build.gradle.kts`:
```kotlin
defaultConfig {
    versionCode = 2        // Incrementar para cada release
    versionName = "1.1.0"  // Versión visible para usuarios
}
```

---

**🎯 Tip:** Para desarrollo usa `assembleDebug`, para producción usa `assembleRelease`