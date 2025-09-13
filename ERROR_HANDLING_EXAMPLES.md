# Ejemplos de Manejo de Errores - YapeChamo

## Errores de Validación del Backend

Tu API maneja errores de validación de manera muy específica. Aquí están los ejemplos de cómo la aplicación ahora maneja estos errores:

### 1. Error de Tipo de Negocio Inválido

**Respuesta del Backend:**
```json
{
    "message": "Validation failed",
    "code": "VALIDATION_ERROR",
    "details": {
        "validationErrors": {
            "registerAdmin.request.businessType": {
                "invalidValue": "Tienda",
                "message": "Business type must be one of: RESTAURANT, RETAIL, SERVICES, OTHER"
            }
        }
    },
    "timestamp": "2025-09-13T00:39:43.499663Z"
}
```

**Manejo en la App:**
- ✅ **Campo específico**: El error aparece directamente debajo del campo "Tipo de negocio"
- ✅ **Mensaje amigable**: "Tipo de negocio debe ser: RESTAURANT, RETAIL, SERVICES, OTHER"
- ✅ **Indicador visual**: El campo se marca en rojo (`isError = true`)
- ✅ **Texto de ayuda**: Muestra las opciones válidas

### 2. Error de Email Inválido

**Respuesta del Backend:**
```json
{
    "message": "Validation failed",
    "code": "VALIDATION_ERROR",
    "details": {
        "validationErrors": {
            "registerAdmin.request.email": {
                "invalidValue": "cm",
                "message": "Invalid email format"
            }
        }
    },
    "timestamp": "2025-09-13T00:42:37.450663Z"
}
```

**Manejo en la App:**
- ✅ **Campo específico**: El error aparece debajo del campo "Email"
- ✅ **Mensaje amigable**: "Formato de email inválido"
- ✅ **Indicador visual**: El campo se marca en rojo
- ✅ **Validación en tiempo real**: Al escribir, el error se limpia automáticamente

### 3. Error de Email Ya Existente

**Respuesta del Backend:**
```json
{
    "message": "Validation failed",
    "code": "VALIDATION_ERROR",
    "details": {
        "validationErrors": {
            "registerAdmin.request.email": {
                "invalidValue": "admin@hotmail.com",
                "message": "Email already exists"
            }
        }
    },
    "timestamp": "2025-09-13T00:43:15.123456Z"
}
```

**Manejo en la App:**
- ✅ **Campo específico**: El error aparece debajo del campo "Email"
- ✅ **Mensaje amigable**: "Este email ya está registrado"
- ✅ **Indicador visual**: El campo se marca en rojo

## Funcionalidades Implementadas

### 1. **ErrorHandler Utilitario**
```kotlin
object ErrorHandler {
    fun getFriendlyErrorMessage(apiError: ApiError): String
    fun getAllValidationErrors(apiError: ApiError): List<String>
    fun getFieldErrors(apiError: ApiError): Map<String, String>
    fun isValidationError(apiError: ApiError): Boolean
    fun getFieldNameFromValidationError(fieldPath: String): String
}
```

### 2. **Modelos de Error Actualizados**
```kotlin
@Serializable
data class ApiError(
    val message: String,
    val code: String,
    val details: ErrorDetails? = null,
    val timestamp: String
)

@Serializable
data class ErrorDetails(
    val validationErrors: Map<String, ValidationError>? = null
)

@Serializable
data class ValidationError(
    val invalidValue: String? = null,
    val message: String
)
```

### 3. **Validación en Tiempo Real**
- ✅ **Limpieza automática**: Los errores se limpian cuando el usuario empieza a escribir
- ✅ **Indicadores visuales**: Campos con error se marcan en rojo
- ✅ **Mensajes de ayuda**: Texto de apoyo para guiar al usuario

### 4. **Validación del Frontend**
```kotlin
private fun validateForm(...): Boolean {
    val validBusinessTypes = listOf("RESTAURANT", "RETAIL", "SERVICES", "OTHER")
    
    return businessName.isNotBlank() && 
           businessType.isNotBlank() &&
           businessType.uppercase() in validBusinessTypes &&
           ruc.isNotBlank() &&
           email.isNotBlank() &&
           password.isNotBlank() &&
           phone.isNotBlank() &&
           address.isNotBlank() &&
           contactName.isNotBlank() &&
           password.length >= 8 &&
           email.contains("@") &&
           ruc.length >= 8
}
```

## Códigos de Error Soportados

| Código | Descripción | Mensaje Amigable |
|--------|-------------|------------------|
| `VALIDATION_ERROR` | Error de validación | Mensaje específico del campo |
| `EMAIL_ALREADY_EXISTS` | Email duplicado | "Este email ya está registrado. Por favor usa otro email." |
| `INVALID_CREDENTIALS` | Credenciales inválidas | "Email o contraseña incorrectos." |
| `ACCOUNT_DISABLED` | Cuenta desactivada | "Tu cuenta está desactivada. Contacta al soporte." |
| `TOKEN_EXPIRED` | Token expirado | "Tu sesión ha expirado. Por favor inicia sesión nuevamente." |
| `INSUFFICIENT_PERMISSIONS` | Sin permisos | "No tienes permisos para realizar esta acción." |
| `RATE_LIMIT_EXCEEDED` | Límite de solicitudes | "Has realizado demasiadas solicitudes. Espera un momento antes de intentar de nuevo." |
| `SERVER_ERROR` | Error del servidor | "Error interno del servidor. Por favor intenta más tarde." |

## Ejemplo de Uso en la UI

### Campo con Error
```kotlin
OutlinedTextField(
    value = businessType,
    onValueChange = { 
        businessType = it
        businessTypeError = "" // Limpiar error al escribir
    },
    label = { Text("Tipo de negocio") },
    isError = businessTypeError.isNotEmpty(), // Marcar como error
    supportingText = if (businessTypeError.isNotEmpty()) {
        { Text(businessTypeError, color = MaterialTheme.colorScheme.error) }
    } else {
        { Text("Opciones: RESTAURANT, RETAIL, SERVICES, OTHER") }
    }
)
```

### Manejo de Respuesta del Servidor
```kotlin
onFailure = { error ->
    isLoading = false
    val errorMsg = error.message ?: "Error desconocido"
    
    // Manejar errores específicos por campo
    when {
        errorMsg.contains("Business type must be one of") -> {
            businessTypeError = "Tipo de negocio debe ser: RESTAURANT, RETAIL, SERVICES, OTHER"
        }
        errorMsg.contains("Invalid email format") -> {
            emailError = "Formato de email inválido"
        }
        errorMsg.contains("Email already exists") -> {
            emailError = "Este email ya está registrado"
        }
        else -> {
            errorMessage = errorMsg
        }
    }
}
```

## Beneficios de esta Implementación

### 1. **Experiencia de Usuario Mejorada**
- ✅ **Errores específicos**: El usuario sabe exactamente qué corregir
- ✅ **Feedback inmediato**: Los errores aparecen en tiempo real
- ✅ **Mensajes claros**: Texto en español, fácil de entender

### 2. **Desarrollo Eficiente**
- ✅ **Reutilizable**: `ErrorHandler` se puede usar en toda la app
- ✅ **Mantenible**: Fácil agregar nuevos tipos de error
- ✅ **Consistente**: Manejo uniforme de errores en toda la aplicación

### 3. **Robustez**
- ✅ **Manejo de casos edge**: Errores inesperados se manejan gracefully
- ✅ **Validación doble**: Frontend y backend validan
- ✅ **Fallback**: Si falla el parsing, muestra el mensaje original

## Próximos Pasos

1. **Implementar en otras pantallas**: Login, afiliación de vendedores, etc.
2. **Agregar más validaciones**: RUC válido, formato de teléfono, etc.
3. **Mejorar UX**: Animaciones de error, sonidos de feedback, etc.
4. **Internacionalización**: Soporte para múltiples idiomas
5. **Analytics**: Tracking de errores para mejorar la experiencia

Esta implementación proporciona una base sólida para el manejo de errores en toda la aplicación, mejorando significativamente la experiencia del usuario.
