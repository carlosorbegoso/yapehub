# ⚡ Referencia Rápida - YapeHub Scripts

## 🎯 Comandos Más Usados

### 🚀 **Build Completo (RECOMENDADO)**
```bash
./scripts/build-all.sh
```
Genera APK + AAB con verificaciones completas

### ⚡ **Build Rápido para Testing**
```bash
./scripts/dev-build.sh
```
Solo APK debug para desarrollo

### 📱 **Solo APK para Testing**
```bash
./scripts/build-all.sh -t apk -s
```
APK release sin limpieza (más rápido)

### 🏪 **Solo AAB para Play Store**
```bash
./scripts/build-all.sh -t aab
```
AAB listo para Google Play Console

---

## 🔧 Comandos de Mantenimiento

### 🆕 **Primera Vez**
```bash
./scripts/setup-project.sh
./scripts/generate-keystore.sh
```

### 🧹 **Limpiar Proyecto**
```bash
./scripts/clean-project.sh
```

### 🔍 **Diagnosticar Problemas**
```bash
./scripts/troubleshoot.sh
```

### 🔒 **Verificar Seguridad**
```bash
./scripts/security-check.sh
```

### ✅ **Verificar Archivos**
```bash
./scripts/verify-apk.sh
```

---

## 📊 Opciones del Script Principal

| Opción | Descripción | Ejemplo |
|--------|-------------|---------|
| `-t apk` | Solo APK | `./scripts/build-all.sh -t apk` |
| `-t aab` | Solo AAB | `./scripts/build-all.sh -t aab` |
| `-t both` | APK + AAB (default) | `./scripts/build-all.sh` |
| `-s` | Sin limpieza | `./scripts/build-all.sh -s` |
| `--skip-security` | Sin verificación de seguridad | `./scripts/build-all.sh --skip-security` |
| `--help` | Mostrar ayuda | `./scripts/build-all.sh --help` |

---

## 🎬 Escenarios Comunes

### "Quiero probar mi app rápido"
```bash
./scripts/dev-build.sh
```

### "Necesito APK para enviar a testers"
```bash
./scripts/build-all.sh -t apk
```

### "Voy a subir a Play Store"
```bash
./scripts/build-all.sh -t aab
```

### "Build completo para release"
```bash
./scripts/build-all.sh
```

### "Tengo problemas de compilación"
```bash
./scripts/troubleshoot.sh
./scripts/clean-project.sh
./scripts/build-all.sh
```

---

## 📁 Archivos Generados

| Archivo | Ubicación | Uso |
|---------|-----------|-----|
| `composeApp-debug.apk` | `build/outputs/apk/debug/` | Testing desarrollo |
| `composeApp-release.apk` | `build/outputs/apk/release/` | Testing final |
| `composeApp-release.aab` | `build/outputs/bundle/release/` | Google Play Store |

---

## 🚨 Solución Rápida de Problemas

### Error de compilación
```bash
./scripts/clean-project.sh
./scripts/build-all.sh
```

### Error de keystore
```bash
./scripts/generate-keystore.sh
```

### Error de permisos
```bash
chmod +x scripts/*.sh
```

### Verificar configuración
```bash
./scripts/troubleshoot.sh
```

---

**💡 Tip: Usa `./scripts/build-all.sh --help` para ver todas las opciones**