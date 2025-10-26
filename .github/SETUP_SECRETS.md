# 🔐 Configuración de Secrets para GitHub Actions

Este documento explica cómo configurar los secrets necesarios para que el workflow de CI/CD funcione correctamente.

## 📋 Secrets Requeridos

Ve a **Settings** > **Secrets and variables** > **Actions** en tu repositorio de GitHub y agrega estos secrets:

### 🔑 **Keystore Secrets**

#### `KEYSTORE_BASE64`
```bash
# Convertir tu keystore a base64
base64 -i keystores/yapehub-release.keystore | pbcopy
```
Pega el resultado en el secret.

#### `KEYSTORE_PASSWORD`
```
yapehub2024!
```

#### `KEY_ALIAS`
```
yapehub
```

#### `KEY_PASSWORD`
```
yapehub2024!
```

### 🏪 **Google Play Store Secrets**

#### `GOOGLE_PLAY_SERVICE_ACCOUNT_JSON`
1. Ve a [Google Play Console](https://play.google.com/console)
2. Ir a **Setup** > **API access**
3. Crear una cuenta de servicio
4. Descargar el archivo JSON
5. Copiar todo el contenido del JSON y pegarlo en el secret

## 🚀 **Cómo usar el workflow**

### **Build automático:**
- Cada push a `main` ejecuta tests y build
- Cada PR ejecuta tests

### **Deploy a Play Store:**
- Hacer commit con `[deploy]` en el mensaje
- Ejemplo: `git commit -m "Release v1.1.0 [deploy]"`
- El workflow automáticamente subirá a Google Play Store

### **Artifacts generados:**
- `debug-apk`: APK de debug para testing
- `release-aab`: AAB firmado para Play Store
- `release-apk`: APK firmado para distribución manual

## 🔧 **Comandos útiles**

### Generar keystore en base64:
```bash
base64 -i keystores/yapehub-release.keystore
```

### Verificar secrets (localmente):
```bash
echo $KEYSTORE_BASE64 | base64 -d > test-keystore.keystore
```

### Deploy manual con mensaje:
```bash
git add .
git commit -m "Nueva versión con mejoras [deploy]"
git push origin main
```

## ⚠️ **Notas importantes**

1. **Nunca** commitees el keystore real al repositorio
2. Los secrets son **encriptados** y solo accesibles durante el workflow
3. El deploy solo ocurre con commits que contengan `[deploy]`
4. El workflow cambia automáticamente a entorno PRODUCTION para releases

## 🐛 **Troubleshooting**

### Error: "Keystore not found"
- Verifica que `KEYSTORE_BASE64` esté configurado correctamente
- Asegúrate de que el base64 sea válido

### Error: "Google Play API"
- Verifica que `GOOGLE_PLAY_SERVICE_ACCOUNT_JSON` sea válido
- Asegúrate de que la cuenta de servicio tenga permisos

### Error: "Gradle build failed"
- Revisa los logs del workflow
- Puede ser un problema de dependencias o código