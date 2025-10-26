# 🔧 Solución: Errores de Parsing JSON en YapeHub

## 🐛 Errores Identificados

### Error 1: JsonNull no es JsonArray
```
[PARSING] ❌ Error parseando Hourly Sales: Element class kotlinx.serialization.json.JsonNull (Kotlin reflection is not available) is not a JsonArray
```

### Error 2: Datos nulos inesperados
```json
{
  "success": true,
  "message": "Datos de ventas por hora obtenidos exitosamente",
  "data": {
    "hourlySales": null,  // ❌ Debería ser un array
    "userType": "ADMIN",
    "userId": 1
  }
}
```

## 🔧 Soluciones de Código

### 1. **Manejo Seguro de Valores Nulos**

```kotlin
// ❌ Código problemático
val hourlySales = jsonElement.jsonArray

// ✅ Código corregido
val hourlySales = when {
    jsonElement is JsonNull -> emptyList()
    jsonElement is JsonArray -> jsonElement.map { /* parsing */ }
    else -> emptyList()
}
```

### 2. **Parsing Defensivo con Try-Catch**

```kotlin
// ✅ Parsing seguro
fun parseHourlySales(jsonString: String): List<HourlySale> {
    return try {
        val json = Json.parseToJsonElement(jsonString)
        val dataObject = json.jsonObject["data"]?.jsonObject
        val hourlySalesElement = dataObject?.get("hourlySales")
        
        when (hourlySalesElement) {
            is JsonNull, null -> {
                println("[PARSING] ⚠️ hourlySales es null, retornando lista vacía")
                emptyList()
            }
            is JsonArray -> {
                hourlySalesElement.map { element ->
                    Json.decodeFromJsonElement<HourlySale>(element)
                }
            }
            else -> {
                println("[PARSING] ❌ hourlySales no es un array válido")
                emptyList()
            }
        }
    } catch (e: Exception) {
        println("[PARSING] ❌ Error parseando hourlySales: ${e.message}")
        emptyList()
    }
}
```

### 3. **Modelo de Datos con Valores por Defecto**

```kotlin
@Serializable
data class ApiResponse<T>(
    val success: Boolean,
    val message: String,
    val data: T? = null,
    val error: Boolean = false
)

@Serializable
data class HourlySalesData(
    val hourlySales: List<HourlySale>? = null, // Nullable con default
    val userType: String,
    val userId: Int
)

@Serializable
data class HourlySale(
    val hour: Int,
    val sales: Double,
    val transactions: Int = 0
)
```

### 4. **Extensión para Parsing Seguro**

```kotlin
// Extensión útil para parsing seguro
fun JsonElement?.asArrayOrEmpty(): JsonArray {
    return when (this) {
        is JsonArray -> this
        is JsonNull, null -> JsonArray(emptyList())
        else -> JsonArray(emptyList())
    }
}

fun JsonElement?.asStringOrEmpty(): String {
    return when (this) {
        is JsonPrimitive -> this.content
        is JsonNull, null -> ""
        else -> ""
    }
}

// Uso
val hourlySales = dataObject?.get("hourlySales")
    .asArrayOrEmpty()
    .map { Json.decodeFromJsonElement<HourlySale>(it) }
```

## 🔍 Debugging y Logging Mejorado

### 1. **Logging Estructurado**

```kotlin
object ParseLogger {
    fun logSuccess(operation: String, data: Any?) {
        println("[PARSING] ✅ $operation exitoso: ${data?.toString()?.take(100)}...")
    }
    
    fun logError(operation: String, error: String, json: String? = null) {
        println("[PARSING] ❌ Error en $operation: $error")
        json?.let { println("[PARSING] 📄 JSON: ${it.take(200)}...") }
    }
    
    fun logWarning(operation: String, message: String) {
        println("[PARSING] ⚠️ $operation: $message")
    }
}
```

### 2. **Validación de JSON**

```kotlin
fun validateJsonStructure(jsonString: String): Boolean {
    return try {
        val json = Json.parseToJsonElement(jsonString)
        val hasSuccess = json.jsonObject.containsKey("success")
        val hasData = json.jsonObject.containsKey("data")
        
        if (!hasSuccess || !hasData) {
            ParseLogger.logWarning("Validación", "JSON no tiene estructura esperada")
            return false
        }
        true
    } catch (e: Exception) {
        ParseLogger.logError("Validación", "JSON inválido: ${e.message}", jsonString)
        false
    }
}
```

## 🚀 Implementación Recomendada

### 1. **Crear Utility Class**

```kotlin
object JsonParsingUtils {
    private val json = Json {
        ignoreUnknownKeys = true
        coerceInputValues = true
        isLenient = true
    }
    
    inline fun <reified T> safeParseArray(
        jsonElement: JsonElement?,
        operation: String
    ): List<T> {
        return try {
            when (jsonElement) {
                is JsonNull, null -> {
                    ParseLogger.logWarning(operation, "Datos nulos, retornando lista vacía")
                    emptyList()
                }
                is JsonArray -> {
                    jsonElement.map { json.decodeFromJsonElement<T>(it) }
                }
                else -> {
                    ParseLogger.logError(operation, "Elemento no es un array: ${jsonElement::class}")
                    emptyList()
                }
            }
        } catch (e: Exception) {
            ParseLogger.logError(operation, "Error parseando array: ${e.message}")
            emptyList()
        }
    }
}
```

### 2. **Uso en el Código**

```kotlin
// En lugar de parsing directo
val hourlySales = JsonParsingUtils.safeParseArray<HourlySale>(
    dataObject?.get("hourlySales"),
    "Hourly Sales"
)
```

## 🔧 Configuración de Kotlinx Serialization

### En build.gradle.kts:

```kotlin
kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.9.0")
        }
    }
}
```

### Configuración JSON:

```kotlin
val json = Json {
    ignoreUnknownKeys = true      // Ignora campos desconocidos
    coerceInputValues = true      // Convierte tipos automáticamente
    isLenient = true              // Permite JSON menos estricto
    allowStructuredMapKeys = true // Permite claves complejas
    encodeDefaults = true         // Incluye valores por defecto
}
```

## 🧪 Testing del Parsing

```kotlin
@Test
fun testHourlySalesParsing() {
    // Caso 1: Datos válidos
    val validJson = """
        {
            "success": true,
            "data": {
                "hourlySales": [
                    {"hour": 9, "sales": 100.0, "transactions": 5}
                ]
            }
        }
    """.trimIndent()
    
    // Caso 2: Datos nulos
    val nullJson = """
        {
            "success": true,
            "data": {
                "hourlySales": null
            }
        }
    """.trimIndent()
    
    // Caso 3: JSON malformado
    val invalidJson = "{ invalid json }"
    
    // Verificar que todos los casos se manejen correctamente
    assert(parseHourlySales(validJson).isNotEmpty())
    assert(parseHourlySales(nullJson).isEmpty())
    assert(parseHourlySales(invalidJson).isEmpty())
}
```

## 📋 Checklist de Solución

- [ ] Implementar parsing defensivo con try-catch
- [ ] Manejar valores nulos explícitamente
- [ ] Agregar logging estructurado
- [ ] Configurar kotlinx-serialization correctamente
- [ ] Crear utility classes para parsing común
- [ ] Agregar tests para casos edge
- [ ] Validar estructura JSON antes de parsing

---

**💡 Tip: Siempre asume que los datos del servidor pueden ser nulos o tener formato inesperado**