package org.sysarp.project.utils

import org.sysarp.project.data.ApiError

object ErrorHandler {
    
    fun getFriendlyErrorMessage(error: ApiError): String {
        return when (error.code) {
            "INVALID_FIELD" -> {
                val details = error.details
                val validationErrors = details?.validationErrors
                when {
                    validationErrors?.containsKey("affiliationCode") == true -> {
                        val affiliationError = validationErrors["affiliationCode"]
                        when (affiliationError?.message) {
                            "Código de afiliación agotado" -> "❌ Este código de afiliación ya fue usado o expiró. Solicita uno nuevo a tu administrador."
                            "Código de afiliación inválido" -> "❌ El código de afiliación no es válido. Verifica que lo hayas ingresado correctamente."
                            else -> "❌ Error con el código de afiliación: ${affiliationError?.message ?: error.message}"
                        }
                    }
                    validationErrors?.containsKey("phone") == true -> {
                        val phoneError = validationErrors["phone"]
                        when (phoneError?.message) {
                            "El número de teléfono ya está registrado" -> "📱 Este número de teléfono ya está registrado. ¿Quieres hacer login?"
                            else -> "❌ Error con el teléfono: ${phoneError?.message ?: error.message}"
                        }
                    }
                    validationErrors?.containsKey("sellerName") == true -> {
                        val nameError = validationErrors["sellerName"]
                        "❌ Error con el nombre: ${nameError?.message ?: error.message}"
                    }
                    validationErrors?.containsKey("credentials") == true -> {
                        val credentialsError = validationErrors["credentials"]
                        when (credentialsError?.message) {
                            "Invalid email or password" -> "🔐 Email o contraseña incorrectos. Verifica tus credenciales."
                            else -> "❌ Error de credenciales: ${credentialsError?.message ?: error.message}"
                        }
                    }
                    // Manejar el nuevo formato de detalles directos
                    details?.field == "credentials" -> {
                        when (details.reason) {
                            "Invalid email or password" -> "🔐 Email o contraseña incorrectos. Verifica tus credenciales."
                            else -> "❌ Error de credenciales: ${details.reason ?: error.message}"
                        }
                    }
                    else -> "❌ Error de validación: ${error.message}"
                }
            }
            "VALIDATION_ERROR" -> "❌ Error de validación: ${error.message}"
            "UNAUTHORIZED" -> "🔒 No tienes permisos para realizar esta acción"
            "FORBIDDEN" -> "🚫 Acceso denegado"
            "NOT_FOUND" -> "❓ Recurso no encontrado"
            "CONFLICT" -> "⚠️ Conflicto: ${error.message}"
            "INTERNAL_ERROR" -> "💥 Error interno del servidor. Intenta nuevamente."
            else -> "❌ Error: ${error.message}"
        }
    }
    
    fun getFriendlyErrorMessage(errorMessage: String): String {
        return when {
            errorMessage.contains("Código de afiliación agotado") -> 
                "❌ Este código de afiliación ya fue usado o expiró. Solicita uno nuevo a tu administrador."
            errorMessage.contains("ya está registrado") -> 
                "📱 Este número de teléfono ya está registrado. ¿Quieres hacer login?"
            errorMessage.contains("Invalid affiliationCode") -> 
                "❌ El código de afiliación no es válido. Verifica que lo hayas ingresado correctamente."
            errorMessage.contains("Invalid credentials") -> 
                "🔐 Email o contraseña incorrectos. Verifica tus credenciales."
            errorMessage.contains("Invalid email or password") -> 
                "🔐 Email o contraseña incorrectos. Verifica tus credenciales."
            errorMessage.contains("EPREM") -> 
                "🌐 Error de conectividad: No se puede conectar al servidor. Verifica la IP y que el servidor esté corriendo."
            errorMessage.contains("Connection refused") -> 
                "🔌 Conexión rechazada: El servidor no está corriendo o no es accesible."
            errorMessage.contains("timeout") -> 
                "⏱️ Timeout: El servidor tardó demasiado en responder."
            errorMessage.contains("Network is unreachable") -> 
                "📡 Red inalcanzable: Verifica tu conexión a internet."
            errorMessage.contains("Socket") -> 
                "🔌 Error de socket: Problema de conectividad de red."
            errorMessage.contains("UnknownHostException") -> 
                "🌐 Host desconocido: No se puede resolver la dirección del servidor."
            else -> "❌ Error: $errorMessage"
        }
    }
}