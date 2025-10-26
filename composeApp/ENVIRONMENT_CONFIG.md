# Configuración de Entornos

Este documento explica cómo configurar y cambiar entre diferentes entornos (desarrollo, staging, producción) en la aplicación.

## Entornos Disponibles

### 1. DEVELOPMENT (Desarrollo)
- **Base URL**: `https://ks9ql0l7-8080.brs.devtunnels.ms`
- **WebSocket URL**: `wss://ks9ql0l7-8080.brs.devtunnels.ms`
- **Debug**: Habilitado
- **Logging**: Habilitado
- **Timeout**: 60 segundos
- **Reintentos**: 5

### 2. STAGING (Pruebas)
- **Base URL**: `http://167.172.117.133:8080`
- **WebSocket URL**: `ws://167.172.117.133:8080`
- **Debug**: Habilitado
- **Logging**: Habilitado
- **Timeout**: 45 segundos
- **Reintentos**: 4

### 3. PRODUCTION (Producción)
- **Base URL**: `http://167.172.117.133:8080`
- **WebSocket URL**: `ws://167.172.117.133:8080`
- **Debug**: Deshabilitado
- **Logging**: Deshabilitado
- **Timeout**: 30 segundos
- **Reintentos**: 3

## Cómo Cambiar de Entorno

### Método 1: Cambiar en BuildConfig.kt (Recomendado)

1. Abre el archivo `composeApp/src/commonMain/kotlin/org/sysarp/project/utils/BuildConfig.kt`
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

### Método 2: Usando Build Variants (Avanzado)

Para automatizar el cambio de entornos según el build type, puedes configurar build variants en `build.gradle.kts`:

```kotlin
android {
    buildTypes {
        debug {
            buildConfigField("String", "ENVIRONMENT", "\"DEVELOPMENT\"")
        }
        release {
            buildConfigField("String", "ENVIRONMENT", "\"PRODUCTION\"")
        }
        create("staging") {
            buildConfigField("String", "ENVIRONMENT", "\"STAGING\"")
        }
    }
}
```

## Verificación de Configuración

La aplicación automáticamente:
1. Valida la configuración al iniciar
2. Imprime la configuración actual en modo debug
3. Lanza una excepción si la configuración es inválida

## Archivos Importantes

- `BuildConfig.kt`: Configuración principal de entornos
- `Constants.kt`: Constantes que usan la configuración de BuildConfig
- `ENVIRONMENT_CONFIG.md`: Este archivo de documentación

## Notas Importantes

⚠️ **IMPORTANTE**: Antes de hacer un release a producción, asegúrate de:
1. Cambiar `CURRENT_ENVIRONMENT` a `Environment.PRODUCTION`
2. Verificar que las URLs de producción sean correctas
3. Probar la conectividad con el servidor de producción

## Troubleshooting

### Error: "Configuración inválida detectada"
- Verifica que todas las URLs estén correctamente configuradas
- Asegúrate de que los valores de timeout y reintentos sean mayores a 0

### No se conecta al servidor
- Verifica que la URL del entorno seleccionado sea correcta
- Comprueba la conectividad de red
- Revisa los logs para más detalles del error