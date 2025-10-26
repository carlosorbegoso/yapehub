# 📱 Tipos de Build Explicados - YapeHub

## 🔐 APK Firmada vs No Firmada

### ✅ **APK FIRMADA (Release)**
```bash
./scripts/build-all.sh        # APK + AAB firmados
./scripts/build-release.sh    # Solo APK firmada
./scripts/build-bundle.sh     # Solo AAB firmado
```

**Características:**
- ✅ **Firmada** con tu keystore personal
- ✅ **Lista para distribución** (Play Store, instalación directa)
- ✅ **Optimizada** (ProGuard, minificación)
- ✅ **Segura** (no se puede modificar sin romper la firma)
- ✅ **Válida hasta 2053**

**Archivos generados:**
- `composeApp-release.apk` (~39MB)
- `composeApp-release.aab` (~27MB)

---

### ⚠️ **APK DEBUG (No firmada para release)**
```bash
./scripts/dev-build.sh        # Solo APK debug
./gradlew assembleDebug       # APK debug manual
```

**Características:**
- ❌ **Firmada con keystore debug** (temporal)
- ❌ **Solo para desarrollo** (no para distribución)
- ❌ **No optimizada** (más grande, más lenta)
- ❌ **No válida para Play Store**
- ⚡ **Más rápida de compilar**

**Archivos generados:**
- `composeApp-debug.apk` (~47MB)

---

## 🔍 Cómo Verificar que tu APK está Firmada

### 1. **Verificar configuración de firma:**
```bash
./gradlew :composeApp:signingReport
```

### 2. **Verificar archivo específico:**
```bash
# Para APK release
ls -la composeApp/build/outputs/apk/release/composeApp-release.apk

# Para AAB release  
ls -la composeApp/build/outputs/bundle/release/composeApp-release.aab
```

### 3. **Verificar con script:**
```bash
./scripts/verify-apk.sh
```

---

## 📊 Comparación de Builds

| Aspecto | Debug | Release |
|---------|-------|---------|
| **Firma** | Debug keystore | Tu keystore personal |
| **Tamaño** | ~47MB | ~39MB |
| **Velocidad compilación** | ⚡ Rápida | 🐌 Más lenta |
| **Optimización** | ❌ No | ✅ Sí |
| **Play Store** | ❌ No válida | ✅ Válida |
| **Distribución** | ❌ Solo desarrollo | ✅ Producción |
| **Debugging** | ✅ Completo | ❌ Limitado |

---

## 🎯 Cuándo Usar Cada Tipo

### 🔧 **Usa APK DEBUG cuando:**
- Desarrollas y pruebas cambios
- Necesitas debugging completo
- Quieres compilación rápida
- Testing interno en tu dispositivo

```bash
./scripts/dev-build.sh
```

### 🚀 **Usa APK RELEASE cuando:**
- Vas a distribuir la app
- Necesitas testing final
- Quieres enviar a testers externos
- Preparas para Play Store

```bash
./scripts/build-all.sh -t apk
```

### 🏪 **Usa AAB RELEASE cuando:**
- Vas a subir a Google Play Store
- Quieres optimización automática de Google
- Distribución oficial

```bash
./scripts/build-all.sh -t aab
```

---

## 🔐 Información de tu Keystore

**Tu keystore actual:**
- **Archivo**: `keystores/yapehub-release.keystore`
- **Alias**: `yapehub`
- **Algoritmo**: RSA 2048 bits
- **Válido hasta**: Marzo 12, 2053
- **SHA-256**: `61:ED:C5:C6:83:3E:06:5D:32:5F:0F:28:C9:E6:EB:CB:D0:CE:4C:FB:9F:5F:77:01:2F:49:60:4B:57:2F:BE:39`

---

## ✅ Confirmación Final

**SÍ, tus scripts generan APK firmada para release:**

1. ✅ `./scripts/build-all.sh` → APK + AAB **FIRMADOS**
2. ✅ `./scripts/build-release.sh` → APK **FIRMADA**  
3. ✅ `./scripts/build-bundle.sh` → AAB **FIRMADO**

**Listos para:**
- 📱 Instalación en cualquier dispositivo Android
- 🏪 Subir a Google Play Store
- 📤 Distribución a testers
- 🔒 Distribución segura

---

**💡 Tip: Siempre usa los scripts de release para distribución, nunca la APK debug**