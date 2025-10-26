# Fix para URLs Opcionales en la API

## Problema Identificado
La aplicación estaba fallando con el error:
```
Field 'topSellers' is required for type with serial name 'org.sysarp.project.data.UnifiedAnalyticsUrls', but it was missing at path: $.data.urls
```

## Causa
Los campos en `UnifiedAnalyticsUrls` estaban marcados como requeridos, pero la API no siempre devuelve todas las URLs, especialmente para diferentes tipos de usuarios (vendedor vs administrador).

## Solución Implementada

### 1. Campos Opcionales en UnifiedAnalyticsUrls
```kotlin
@Serializable
data class UnifiedAnalyticsUrls(
    val monthlySales: String? = null,
    val hourlySales: String? = null,
    val topSellers: String? = null,        // Ahora opcional
    val completeAnalytics: String? = null,
    val weeklySales: String? = null,
    val dailySales: String? = null,
    val performanceDetails: String? = null
)
```

### 2. Manejo Seguro de URLs Opcionales
```kotlin
// Antes (podía fallar si la URL no existía)
statsService.getAnalyticsFromUrl(response.data.urls.dailySales, token)

// Después (manejo seguro con let)
response.data.urls.dailySales?.let { url ->
    statsService.getAnalyticsFromUrl(url, token).fold(...)
}
```

## Beneficios

1. **Robustez**: La aplicación ya no falla si faltan URLs específicas
2. **Flexibilidad**: Diferentes tipos de usuarios pueden recibir diferentes conjuntos de URLs
3. **Compatibilidad**: Funciona con respuestas de API parciales o evolutivas
4. **Mantenibilidad**: Fácil agregar nuevas URLs sin romper funcionalidad existente

## URLs Disponibles según Tipo de Usuario

### Para Administradores:
- `monthlySales` ✅
- `hourlySales` ✅  
- `topSellers` ✅ (solo para admin)
- `completeAnalytics` ✅
- `weeklySales` ✅
- `dailySales` ✅
- `performanceDetails` ✅

### Para Vendedores:
- `monthlySales` ✅
- `hourlySales` ✅
- `topSellers` ❌ (no aplicable)
- `completeAnalytics` ✅
- `weeklySales` ✅
- `dailySales` ✅
- `performanceDetails` ✅

## Resultado
La aplicación ahora maneja graciosamente las respuestas de la API sin importar qué URLs estén presentes, eliminando los errores de deserialización y mejorando la experiencia del usuario.