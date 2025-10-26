# 🚀 CI/CD Setup - YapeChamoApp

Configuración completa de GitHub Actions para automatizar el build, testing y deploy de YapeChamoApp.

## 📋 Workflows Disponibles

### 1. **CI/CD Completo** (`.github/workflows/ci-cd.yml`)

**Triggers:**
- Push a `main`
- Pull requests a `main`

**Jobs:**
1. **create-tag**: Crea tags automáticamente usando template personalizado
2. **build-and-test**: Ejecuta tests y build de debug
3. **build-release**: Build de producción (solo en main)
4. **deploy-to-play-store**: Deploy automático (solo con `[deploy]` en commit)

**Artifacts generados:**
- `debug-apk`: Para testing interno
- `release-aab`: Para Google Play Store
- `release-apk`: Para distribución manual

### 2. **Tag Only** (`.github/workflows/tag-only.yml`)

**Triggers:**
- Push a `main`

**Jobs:**
1. **create-tag**: Solo crea tags usando template

## 🔐 Configuración de Secrets

### Script de configuración:
```bash
bash scripts/setup-github-secrets.sh
```

### Secrets requeridos en GitHub:

#### **Keystore Secrets:**
- `KEYSTORE_BASE64`: Keystore convertido a base64
- `KEYSTORE_PASSWORD`: `yapehub2024!`
- `KEY_ALIAS`: `yapehub`
- `KEY_PASSWORD`: `yapehub2024!`

#### **Google Play Store (opcional):**
- `GOOGLE_PLAY_SERVICE_ACCOUNT_JSON`: JSON completo de cuenta de servicio

### Cómo configurar secrets:
1. Ve a **Settings** > **Secrets and variables** > **Actions**
2. Click **New repository secret**
3. Agrega cada secret con su valor correspondiente

## 🚀 Uso del CI/CD

### **Build automático:**
```bash
git add .
git commit -m "Nuevas funcionalidades"
git push origin main
```

### **Deploy a Play Store:**
```bash
git add .
git commit -m "Release v1.1.0 [deploy]"
git push origin main
```

### **Testing con PR:**
```bash
git checkout -b feature/nueva-funcionalidad
git add .
git commit -m "Implementar nueva funcionalidad"
git push origin feature/nueva-funcionalidad
# Crear PR en GitHub
```

## 📊 Monitoreo y Artifacts

### Ver workflows:
- Pestaña **Actions** en GitHub
- Logs detallados de cada job
- Estado de builds en tiempo real

### Descargar artifacts:
1. Ve al workflow completado
2. Scroll down a **Artifacts**
3. Descarga el que necesites:
   - `debug-apk`: Para testing
   - `release-aab`: Para Play Store
   - `release-apk`: Para distribución

## 🔧 Personalización

### Cambiar branches monitoreados:
```yaml
on:
  push:
    branches: [ main, develop ]
  pull_request:
    branches: [ main, develop ]
```

### Cambiar trigger de deploy:
```yaml
if: contains(github.event.head_commit.message, '[release]')
```

### Agregar más validaciones:
```yaml
lint:
  runs-on: ubuntu-latest
  steps:
    - name: Run lint
      run: ./gradlew lint
```

## 🐛 Troubleshooting

### Error: "Keystore not found"
```bash
# Verificar keystore
ls -la keystores/yapehub-release.keystore

# Regenerar base64
base64 -i keystores/yapehub-release.keystore
```

### Error: "Gradle build failed"
```bash
# Limpiar y probar localmente
./gradlew clean
bash scripts/build-all.sh -t apk
```

### Error: "Google Play API"
- Verificar JSON de cuenta de servicio
- Confirmar permisos de la cuenta
- Revisar package name en workflow

## 📚 Referencias

- [GitHub Actions Docs](https://docs.github.com/en/actions)
- [Android CI/CD Best Practices](https://developer.android.com/studio/publish/app-signing)
- [Google Play Console API](https://developers.google.com/android-publisher)