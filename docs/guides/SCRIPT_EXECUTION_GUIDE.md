# 🚀 Guía de Ejecución de Scripts - YapeHub

Esta guía te explica cómo usar todos los scripts disponibles en el proyecto YapeHub de manera efectiva y segura.

## 🎯 Scripts Principales (Uso Diario)

### ⭐ **Script Centralizado - TODO EN UNO**
```bash
./scripts/build-all.sh
```
**🔥 RECOMENDADO** - Ejecuta todo el proceso automáticamente:
- Verificación de seguridad
- Limpieza del proyecto
- Generación de APK y AAB
- Verificación final

**Opciones disponibles:**
```bash
./scripts/build-all.sh              # Build completo (APK + AAB)
./scripts/build-all.sh -t apk        # Solo APK
./scripts/build-all.sh -t aab        # Solo AAB para Play Store
./scripts/build-all.sh -s            # Sin limpieza inicial (más rápido)
./scripts/build-all.sh --help        # Ver todas las opciones
```

### ⚡ **Build Rápido para Desarrollo**
```bash
./scripts/dev-build.sh
```
**Para desarrollo diario** - Solo APK debug, instalación automática

---

## 📁 Scripts Individuales

```
scripts/
├── build-all.sh             # 🌟 Script centralizado (PRINCIPAL)
├── dev-build.sh             # ⚡ Build rápido para desarrollo
├── setup-project.sh         # 🔧 Setup inicial del proyecto
├── generate-keystore.sh     # 🔑 Genera keystore para firma
├── build-release.sh         # 📱 Compila APK de release
├── build-bundle.sh          # 📦 Compila AAB para Play Store
├── verify-apk.sh           # ✅ Verifica archivos generados
├── clean-project.sh        # 🧹 Limpieza profunda
├── troubleshoot.sh         # 🔍 Diagnóstico de problemas
└── security-check.sh       # 🔒 Verificación de seguridad
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

## 🎯 Flujos de Trabajo Recomendados

### 🚀 **Para Release Completo (RECOMENDADO)**
```bash
# Un solo comando hace todo
./scripts/build-all.sh

# O con opciones específicas
./scripts/build-all.sh -t both --skip-clean
```

### ⚡ **Para Desarrollo Diario**
```bash
# Build rápido para testing
./scripts/dev-build.sh

# O build debug manual
./gradlew :composeApp:assembleDebug
```

### 📱 **Solo APK para Testing**
```bash
# Opción 1: Script centralizado
./scripts/build-all.sh -t apk

# Opción 2: Script individual
./scripts/build-release.sh
```

### 🏪 **Solo AAB para Play Store**
```bash
# Opción 1: Script centralizado
./scripts/build-all.sh -t aab

# Opción 2: Script individual
./scripts/build-bundle.sh
```

### 🔧 **Solución de Problemas**
```bash
# Diagnóstico completo
./scripts/troubleshoot.sh

# Limpieza profunda
./scripts/clean-project.sh

# Verificación de seguridad
./scripts/security-check.sh
```

### 🆕 **Primera Vez / Setup**
```bash
# 1. Configuración inicial
./scripts/setup-project.sh

# 2. Generar keystore
./scripts/generate-keystore.sh

# 3. Primer build
./scripts/build-all.sh
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

## 💡 Ejemplos Prácticos

### 🎬 **Escenarios Comunes**

#### "Quiero generar APK para testing rápido"
```bash
./scripts/build-all.sh -t apk -s
# -s = sin limpieza (más rápido)
```

#### "Necesito AAB para subir a Play Store"
```bash
./scripts/build-all.sh -t aab
# Incluye verificación de seguridad automática
```

#### "Desarrollo diario - solo quiero probar cambios"
```bash
./scripts/dev-build.sh
# Build debug + instalación automática
```

#### "Tengo problemas, necesito diagnóstico"
```bash
./scripts/troubleshoot.sh
# Diagnóstico completo del proyecto
```

#### "Primera vez usando el proyecto"
```bash
./scripts/setup-project.sh
./scripts/generate-keystore.sh
./scripts/build-all.sh
```

#### "Build completo para release final"
```bash
./scripts/build-all.sh
# Hace todo: limpieza + seguridad + APK + AAB + verificación
```

### ⚡ **Comandos de Una Línea**

```bash
# Build completo silencioso
./scripts/build-all.sh > build.log 2>&1 && echo "✅ Build completado"

# Solo errores
./scripts/build-all.sh 2>&1 | grep -E "(❌|ERROR|FAILED)"

# Build y verificar tamaño
./scripts/build-all.sh && ls -lh composeApp/build/outputs/**/release/*

# Build con tiempo
time ./scripts/build-all.sh

# Build condicional (solo si hay cambios)
git diff --quiet || ./scripts/build-all.sh -t apk
```

## 🎯 Tips y Trucos

### 🚀 **Acelerar Builds:**
```bash
# Build sin limpieza (más rápido)
./scripts/build-all.sh -s

# Solo lo que necesitas
./scripts/build-all.sh -t apk  # Solo APK
./scripts/build-all.sh -t aab  # Solo AAB

# Build paralelo manual
./gradlew --parallel :composeApp:assembleRelease
```

### 🔍 **Debugging:**
```bash
# Ver logs detallados
./scripts/build-all.sh 2>&1 | tee build-detailed.log

# Solo verificar sin build
./scripts/verify-apk.sh

# Diagnóstico sin build
./scripts/troubleshoot.sh
```

### 🤖 **Automatización:**
```bash
# Script personalizado
echo '#!/bin/bash
./scripts/build-all.sh -t aab
if [ $? -eq 0 ]; then
    echo "✅ Listo para Play Store"
    open composeApp/build/outputs/bundle/release/
fi' > my-release.sh && chmod +x my-release.sh
```

### 📊 **Monitoreo:**
```bash
# Ver progreso en tiempo real
./scripts/build-all.sh | while read line; do
    echo "[$(date '+%H:%M:%S')] $line"
done

# Notificación al completar (macOS)
./scripts/build-all.sh && osascript -e 'display notification "Build completado" with title "YapeHub"'
```

---

**🚀 ¡Con el script centralizado tendrás un flujo de trabajo súper eficiente!**