# 🔒 Lista de Seguridad - Archivos Sensibles YapeHub

## ❌ Archivos que NUNCA deben subirse a Git

### 🔑 Archivos de Firma y Credenciales
```
keystore.properties          # Contraseñas del keystore
keystores/                   # Directorio completo de keystores
*.keystore                   # Archivos de keystore
*.jks                        # Java KeyStore files
*.p12                        # PKCS#12 certificates
*.pem                        # PEM certificates
local.properties             # Configuración local de Android
signing.properties           # Propiedades de firma adicionales
```

### 🏗️ Archivos de Build y Cache
```
build/                       # Directorios de compilación
.gradle/                     # Cache de Gradle
.kotlin/                     # Cache de Kotlin
captures/                    # Screenshots de testing
output.json                  # Archivos de salida
lint-results*.xml           # Reportes de lint
```

### 📱 Archivos de Distribución
```
*.apk                        # APKs compiladas
*.aab                        # Android App Bundles
*.ipa                        # iOS App files
```

### 🛠️ Archivos de IDE y Sistema
```
.idea/                       # Configuración de IntelliJ/Android Studio
.vscode/                     # Configuración de VS Code
*.iml                        # Archivos de módulo IntelliJ
*.iws                        # Workspace IntelliJ
*.ipr                        # Project IntelliJ
.DS_Store                    # Archivos de macOS
Thumbs.db                    # Archivos de Windows
*.swp                        # Archivos temporales de Vim
*.swo                        # Archivos temporales de Vim
```

### 🔧 Archivos de Configuración Sensible
```
.env                         # Variables de entorno
.env.local                   # Variables locales
.env.production              # Variables de producción
secrets.properties           # Archivos de secretos
gradle.properties.local      # Configuración local de Gradle
```

### 🗂️ Archivos Temporales
```
*.tmp                        # Archivos temporales
*.temp                       # Archivos temporales
*.log                        # Logs
*.bak                        # Backups
*.backup                     # Backups
*.orig                       # Archivos originales de merge
```

## ✅ Verificación de Seguridad

### Comando para verificar archivos sensibles:
```bash
# Verificar que no hay archivos sensibles en Git
git ls-files | grep -E "\.(keystore|jks|p12|pem|apk|aab)$" || echo "✅ No hay archivos sensibles"

# Verificar .gitignore
grep -E "(keystore|\.apk|\.aab)" .gitignore && echo "✅ .gitignore configurado"

# Buscar contraseñas hardcodeadas
grep -r "password\|secret\|key.*=" --include="*.kt" . || echo "✅ No hay credenciales hardcodeadas"
```

### Script de verificación automática:
```bash
./scripts/security-check.sh
```

## 🚨 Qué hacer si subiste archivos sensibles

### 1. Eliminar del repositorio:
```bash
# Eliminar archivo del historial completo
git filter-branch --force --index-filter \
  'git rm --cached --ignore-unmatch keystore.properties' \
  --prune-empty --tag-name-filter cat -- --all

# Forzar push
git push origin --force --all
```

### 2. Regenerar credenciales:
```bash
# Regenerar keystore
rm keystores/yapehub-release.keystore
./scripts/generate-keystore.sh

# Actualizar contraseñas
nano keystore.properties
```

### 3. Verificar .gitignore:
```bash
# Asegurar que está en .gitignore
echo "keystore.properties" >> .gitignore
echo "keystores/" >> .gitignore
```

## 🛡️ Mejores Prácticas de Seguridad

### ✅ Hacer:
- Usar archivos .example para plantillas
- Configurar .gitignore antes del primer commit
- Usar variables de entorno para secretos
- Hacer backup seguro de keystores
- Verificar regularmente con scripts de seguridad

### ❌ No hacer:
- Hardcodear contraseñas en código
- Compartir keystores por email/chat
- Usar contraseñas simples
- Subir archivos de configuración local
- Ignorar warnings de seguridad

## 🔍 Auditoría Regular

### Comandos de verificación:
```bash
# Verificar archivos trackeados
git ls-files | grep -E "\.(properties|keystore|apk|aab)$"

# Buscar secretos en código
grep -r "password\|secret\|api.*key" --include="*.kt" .

# Verificar tamaño del repositorio
du -sh .git/

# Verificar archivos grandes
git ls-files | xargs ls -la | sort -k5 -nr | head -10
```

### Frecuencia recomendada:
- **Antes de cada release**: Verificación completa
- **Semanalmente**: Verificación básica
- **Después de cambios de configuración**: Verificación inmediata

---

**🔒 La seguridad es responsabilidad de todos los desarrolladores**