# 🌍 Configuración de Entornos

Este documento explica cómo configurar y cambiar entre diferentes entornos (desarrollo, staging, producción) en YapeChamoApp.

## 🏗️ Entornos Disponibles

### 1. **DEVELOPMENT** (Desarrollo)
- **Base URL**: `https://ks9ql0l7-8080.brs.devtunnels.ms`
- **WebSocket URL**: `wss://ks9ql0l7-8080.brs.devtunnels.ms`
- **Debug**: ✅ Habilitado
- **Logging**: ✅ Habilitado
- **Timeout**: 60 segundos
- **Reintentos**: 5
- **Uso**: Desarrollo local y testing

### 2. **STAGING** (Pruebas)
- **Base URL**: `http://167.172.117.133:8080`
- **WebSocket URL**: `ws://167.172.117.133:8080`
- **Debug**: ✅ Habilitado
- **Logging**: ✅ Habilitado
- **Timeout**: 45 segundos
- **Reintentos**: 4
- **Uso**: Testing pre-producción

### 3. **PRODUCTION** (Producción)
- **Base URL**: `http://167.172.117.133:8080`
- **WebSocket URL**: `ws://167.172.117.133:8080`
- **Debug**: ❌ Deshabilitado
- **Logging**: ❌ Deshabilitado
- **Timeout**: 30 segundos
- **Reintentos**: 3
- **Uso**: App publicada en Play Store

## 🔄 Cómo Cambiar de Entorno

### **Método 1: Cambiar en BuildConfig.kt** (Recomendado)

1. Abre `composeApp/src/commonMain/kotlin/org/sysarp/project/utils/BuildConfig.kt`
2. Busca la línea:
   ```kotlin
   val CURRENT_ENVIRONMENT = Environment.DEVELOPMENT
   ```
3. Cambia el valor según el entorno deseado:
   ```kotlin
   // Para desarrollo
   val CURRENT_ENVIRONMENT = Environment.DEVELOPMENT
   
   // Para staging/pruebas
   val CURRENT_ENVIRONMENT = Environment.STAGING
   
   // Para producción
   val CURRENT_ENVIRONMENT = Environment.PRODUCTION
   ```
4. Recompila la aplicación

### **Método 2: Scripts Automatizados**

```bash
# Cambiar a desarrollo
bash scripts/set-environment.sh development

# Cambiar a staging
bash scripts/set-environment.sh staging

# Cambiar a producción
bash scripts/set-environment.sh production
```

## 🏗️ Arquitectura de Configuración

### **Archivos principales:**
- `BuildConfig.kt`: Configuración central de entornos
- `Constants.kt`: Constantes que usan BuildConfig
- `EnvironmentConfig.kt`: Definición de configuraciones por entorno

### **Flujo de configuración:**
```
BuildConfig.kt → Constants.kt → Toda la app
```

## 🔧 Configuraciones por Entorno

### **URLs y Endpoints:**
```kotlin
// Development
baseUrl = "https://ks9ql0l7-8080.brs.devtunnels.ms"
websocketUrl = "wss://ks9ql0l7-8080.brs.devtunnels.ms"

// Production/Staging
baseUrl = "http://167.172.117.133:8080"
websocketUrl = "ws://167.172.117.133:8080"
```

### **Configuraciones de Red:**
```kotlin
// Development: Más tolerante para debugging
timeoutSeconds = 60L
retryAttempts = 5

// Production: Más estricto para performance
timeoutSeconds = 30L
retryAttempts = 3
```

### **Logging y Debug:**
```kotlin
// Development/Staging
isDebug = true
enableLogging = true

// Production
isDebug = false
enableLogging = false
```

## 🚀 Builds por Entorno

### **Para Development:**
```bash
# Cambiar entorno
sed -i 's/Environment\.[A-Z]*/Environment.DEVELOPMENT/' composeApp/src/commonMain/kotlin/org/sysarp/project/utils/BuildConfig.kt

# Build APK para testing
bash scripts/build-all.sh -t apk
```

### **Para Production:**
```bash
# Cambiar entorno
sed -i 's/Environment\.[A-Z]*/Environment.PRODUCTION/' composeApp/src/commonMain/kotlin/org/sysarp/project/utils/BuildConfig.kt

# Build AAB para Play Store
bash scripts/build-all.sh -t aab
```

## 🔍 Verificación de Entorno

### **En la app:**
La app muestra el entorno actual en:
- Pantalla de debug (si está habilitada)
- Logs de inicio
- Configuración de desarrollador

### **En código:**
```kotlin
// Verificar entorno actual
println("Entorno actual: ${BuildConfig.ENVIRONMENT_NAME}")
println("Base URL: ${BuildConfig.BASE_URL}")
println("Debug habilitado: ${BuildConfig.IS_DEBUG}")
```

### **Script de verificación:**
```bash
bash scripts/check-environment.sh
```

## ⚠️ Consideraciones Importantes

### **Antes de cambiar entorno:**
1. ✅ Hacer commit de cambios actuales
2. ✅ Verificar que el servidor del entorno esté disponible
3. ✅ Limpiar cache si es necesario: `./gradlew clean`

### **Para builds de producción:**
1. ✅ **SIEMPRE** cambiar a `Environment.PRODUCTION`
2. ✅ Verificar que el servidor de producción esté funcionando
3. ✅ Probar la conectividad antes del release
4. ✅ Hacer backup del keystore

### **Para desarrollo:**
1. ✅ Usar `Environment.DEVELOPMENT` para testing local
2. ✅ El servidor de desarrollo debe estar corriendo
3. ✅ Los logs están habilitados para debugging

## 🐛 Troubleshooting

### **Error: "No se puede conectar al servidor"**
```bash
# Verificar entorno actual
grep "CURRENT_ENVIRONMENT" composeApp/src/commonMain/kotlin/org/sysarp/project/utils/BuildConfig.kt

# Verificar URL configurada
grep "BASE_URL" composeApp/src/commonMain/kotlin/org/sysarp/project/utils/Constants.kt
```

### **Error: "Configuración inválida"**
```bash
# Limpiar y recompilar
./gradlew clean
bash scripts/build-all.sh -t apk
```

### **URLs incorrectas después del cambio:**
```bash
# Limpiar cache de Gradle
rm -rf .gradle/
./gradlew clean
```

## 📚 Referencias

- [Kotlin Multiplatform Configuration](https://kotlinlang.org/docs/multiplatform.html)
- [Android Build Variants](https://developer.android.com/studio/build/build-variants)
- [Ktor Client Configuration](https://ktor.io/docs/client.html)