# 🚀 Guía de Ejecución de Scripts - YapeHub

Esta guía te explica cómo usar todos los scripts disponibles en el proyecto YapeHub de manera efectiva y segura.

## 📁 Estructura de Scripts

```
scripts/
├── generate-keystore.sh      # Genera keystore para firma
├── build-release.sh          # Compila APK de release
├── build-bundle.sh           # Compila AAB para Play Store
├── verify-apk.sh            # Verifica archivos generados
├── setup-project.sh         # Setup inicial del proyecto
├── troubleshoot.sh          # Diagnóstico de problemas
└── clean-project.sh         # Limpieza profunda
```

## 🔧 Scripts Principales

### 1. **Setup Inicial del Proyecto**
```bash
./scripts/setup-project.sh
```
**¿Qué hace?**
- Verifica dependencias necesarias
- Configura el entorno de desarrollo
- Instala herramientas requeridas
- Prepara archivos de configuración

**¿Cuándo usarlo?**
- Primera vez que clonas el proyecto
- Después de cambios importantes en dependencias
- Cuando hay problemas de configuración

---

### 2. **Generar Keystore (Solo Primera Vez)**
```bash
./scripts/generate-keystore.sh
```
**¿Qué hace?**
- Crea un keystore para firmar APKs
- Genera certificado auto-firmado
- Configura alias y contraseñas

**¿Cuándo usarlo?**
- Solo la primera vez
- Si perdiste el keystore original
- Para crear keystores adicionales

**⚠️ IMPORTANTE:**
- Ejecutar solo UNA vez
- Guardar el keystore de forma segura
- No subir a Git

---

### 3. **Compilar APK de Release**
```bash
./scripts/build-release.sh
```
**¿Qué hace?**
- Limpia el proyecto
- Compila APK firmada de release
- Verifica la generación exitosa
- Muestra información del archivo

**¿Cuándo usarlo?**
- Para testing en dispositivos
- Distribución directa
- Pruebas internas

**Salida:** `composeApp/build/outputs/apk/release/composeApp-release.apk`

---

### 4. **Compilar AAB para Play Store**
```bash
./scripts/build-bundle.sh
```
**¿Qué hace?**
- Limpia el proyecto
- Compila Android App Bundle firmado
- Optimiza para Play Store
- Verifica la generación

**¿Cuándo usarlo?**
- Para subir a Google Play Store
- Distribución oficial
- Builds de producción

**Salida:** `composeApp/build/outputs/bundle/release/composeApp-release.aab`

---

### 5. **Verificar Archivos Generados**
```bash
./scripts/verify-apk.sh
```
**¿Qué hace?**
- Verifica existencia de APK y AAB
- Comprueba tamaños de archivo
- Muestra información de firma
- Genera reporte de verificación

**¿Cuándo usarlo?**
- Después de cada build
- Para confirmar archivos válidos
- Antes de distribuir

---

## 🛠️ Scripts de Utilidad

### 6. **Limpieza del Proyecto**
```bash
./scripts/clean-project.sh
```
**¿Qué hace?**
- Limpia builds anteriores
- Elimina archivos temporales
- Resetea cache de Gradle
- Prepara para build limpio

**¿Cuándo usarlo?**
- Cuando hay errores extraños
- Antes de builds importantes
- Para liberar espacio

---

### 7. **Diagnóstico de Problemas**
```bash
./scripts/troubleshoot.sh
```
**¿Qué hace?**
- Verifica configuración del sistema
- Comprueba dependencias
- Analiza logs de error
- Sugiere soluciones

**¿Cuándo usarlo?**
- Cuando algo no funciona
- Errores de compilación
- Problemas de configuración

---

## 📋 Flujo de Trabajo Recomendado

### Para Desarrollo Diario:
```bash
# 1. Limpiar proyecto
./scripts/clean-project.sh

# 2. Compilar debug para pruebas
./gradlew :composeApp:assembleDebug

# 3. Si hay problemas
./scripts/troubleshoot.sh
```

### Para Release de Testing:
```bash
# 1. Verificar configuración
./scripts/verify-apk.sh

# 2. Compilar APK
./scripts/build-release.sh

# 3. Verificar resultado
./scripts/verify-apk.sh
```

### Para Release de Producción:
```bash
# 1. Limpiar completamente
./scripts/clean-project.sh

# 2. Compilar AAB
./scripts/build-bundle.sh

# 3. Verificar archivos
./scripts/verify-apk.sh

# 4. Subir a Play Store
```

## 🔍 Solución de Problemas Comunes

### Error: "Permission denied"
```bash
chmod +x scripts/*.sh
```

### Error: "keystore.properties not found"
```bash
cp keystore.properties.example keystore.properties
# Editar con tus datos
```

### Error: "keystore file not found"
```bash
./scripts/generate-keystore.sh
```

### Error de compilación
```bash
./scripts/clean-project.sh
./scripts/troubleshoot.sh
```

### APK no firmada
```bash
# Verificar configuración
./gradlew :composeApp:signingReport

# Regenerar keystore si es necesario
./scripts/generate-keystore.sh
```

## 📊 Información de Archivos Generados

| Archivo | Tamaño | Uso | Distribución |
|---------|--------|-----|--------------|
| `composeApp-release.apk` | ~39MB | Testing directo | Email, Drive, ADB |
| `composeApp-release.aab` | ~27MB | Play Store | Google Play Console |

## 🔐 Seguridad y Mejores Prácticas

### ✅ Hacer:
- Ejecutar scripts desde la raíz del proyecto
- Verificar archivos después de cada build
- Guardar keystore de forma segura
- Usar AAB para Play Store

### ❌ No hacer:
- Ejecutar scripts como root
- Subir keystore a Git
- Compartir contraseñas
- Usar APK para Play Store

## 📱 Instalación Manual

```bash
# Instalar APK en dispositivo conectado
adb install composeApp/build/outputs/apk/release/composeApp-release.apk

# Desinstalar versión anterior
adb uninstall com.yapechamo.composeapp

# Ver dispositivos conectados
adb devices
```

## 🎯 Tips y Trucos

### Acelerar Builds:
```bash
# Build paralelo
./gradlew --parallel :composeApp:assembleRelease

# Con más memoria
./gradlew -Xmx4g :composeApp:assembleRelease
```

### Debugging:
```bash
# Build con información detallada
./gradlew --info :composeApp:assembleRelease

# Ver dependencias
./gradlew :composeApp:dependencies
```

### Automatización:
```bash
# Build completo automatizado
./scripts/clean-project.sh && ./scripts/build-bundle.sh && ./scripts/verify-apk.sh
```

---

**🚀 ¡Con estos scripts tendrás un flujo de trabajo eficiente y profesional!**