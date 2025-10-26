# 🔐 GitHub Secrets Configuration

Guía completa para configurar los secrets necesarios para GitHub Actions.

## 📋 Secrets Requeridos

### 🔑 **Keystore Secrets**

#### `KEYSTORE_BASE64`
Convierte tu keystore a base64:
```bash
base64 -i keystores/yapehub-release.keystore | pbcopy
```

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

### 🏪 **Google Play Store Secrets (Opcional)**

#### `GOOGLE_PLAY_SERVICE_ACCOUNT_JSON`

**Pasos para obtener el JSON:**
1. Ve a [Google Play Console](https://play.google.com/console)
2. **Setup** > **API access**
3. **Create new service account** o usar existente
4. **Download JSON key**
5. Copiar **todo el contenido** del archivo JSON

**Ejemplo del JSON:**
```json
{
  "type": "service_account",
  "project_id": "tu-proyecto",
  "private_key_id": "...",
  "private_key": "-----BEGIN PRIVATE KEY-----\n...\n-----END PRIVATE KEY-----\n",
  "client_email": "...",
  "client_id": "...",
  "auth_uri": "https://accounts.google.com/o/oauth2/auth",
  "token_uri": "https://oauth2.googleapis.com/token"
}
```

## 🛠️ **Configuración en GitHub**

### Paso a paso:
1. Ve a tu repositorio en GitHub
2. **Settings** > **Secrets and variables** > **Actions**
3. Click **New repository secret**
4. Agrega cada secret:

| Secret Name | Value | Description |
|-------------|-------|-------------|
| `KEYSTORE_BASE64` | (output del comando base64) | Keystore en base64 |
| `KEYSTORE_PASSWORD` | `yapehub2024!` | Contraseña del keystore |
| `KEY_ALIAS` | `yapehub` | Alias de la clave |
| `KEY_PASSWORD` | `yapehub2024!` | Contraseña de la clave |
| `GOOGLE_PLAY_SERVICE_ACCOUNT_JSON` | (JSON completo) | Cuenta de servicio |

## 🚀 **Script Automático**

Usa el script para generar los valores:
```bash
bash scripts/setup-github-secrets.sh
```

Este script te mostrará:
- ✅ Valor de `KEYSTORE_BASE64`
- ✅ Confirmación de otros valores
- ✅ Instrucciones paso a paso

## 🔍 **Verificación**

### Verificar keystore localmente:
```bash
# Convertir base64 de vuelta a keystore
echo "TU_KEYSTORE_BASE64" | base64 -d > test-keystore.keystore

# Verificar que funciona
keytool -list -keystore test-keystore.keystore -alias yapehub

# Limpiar archivo de prueba
rm test-keystore.keystore
```

### Verificar JSON de Google Play:
```bash
# Verificar que es JSON válido
echo 'TU_JSON' | jq .

# Verificar campos requeridos
echo 'TU_JSON' | jq '.client_email, .private_key'
```

## ⚠️ **Seguridad**

### ✅ **Buenas prácticas:**
- Nunca commitear secrets al repositorio
- Usar secrets de GitHub (están encriptados)
- Rotar secrets periódicamente
- Limitar permisos de cuentas de servicio

### ❌ **Evitar:**
- Poner secrets en código
- Compartir secrets por chat/email
- Usar secrets en logs
- Dar permisos excesivos

## 🐛 **Troubleshooting**

### Error: "Invalid keystore format"
```bash
# Verificar que el keystore existe
ls -la keystores/yapehub-release.keystore

# Regenerar base64 sin saltos de línea
base64 -i keystores/yapehub-release.keystore | tr -d '\n'
```

### Error: "Google Play API authentication failed"
- Verificar que el JSON sea válido
- Confirmar que la cuenta de servicio tenga permisos
- Revisar que el project_id sea correcto

### Error: "Secret not found in workflow"
- Verificar que el nombre del secret sea exacto
- Confirmar que el secret esté en el repositorio correcto
- Revisar que el workflow tenga acceso a secrets

## 📚 **Referencias**

- [GitHub Secrets Documentation](https://docs.github.com/en/actions/security-guides/encrypted-secrets)
- [Google Play Console API Setup](https://developers.google.com/android-publisher/getting_started)
- [Android App Signing](https://developer.android.com/studio/publish/app-signing)