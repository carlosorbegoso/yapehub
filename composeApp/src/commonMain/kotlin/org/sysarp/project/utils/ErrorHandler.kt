package org.sysarp.project.utils

import org.sysarp.project.data.ApiError
import org.sysarp.project.data.ValidationError

object ErrorHandler {
    
    /**
     * Extrae un mensaje de error amigable del ApiError
     */
    fun getFriendlyErrorMessage(apiError: ApiError): String {
        return when (apiError.code) {
            "VALIDATION_ERROR" -> {
                val validationErrors = apiError.details?.validationErrors
                if (validationErrors != null && validationErrors.isNotEmpty()) {
                    // Tomar el primer error de validación
                    val firstError = validationErrors.values.first()
                    firstError.message
                } else {
                    apiError.message
                }
            }
            "EMAIL_ALREADY_EXISTS" -> "Este email ya está registrado. Por favor usa otro email."
            "INVALID_CREDENTIALS" -> "Email o contraseña incorrectos."
            "ACCOUNT_DISABLED" -> "Tu cuenta está desactivada. Contacta al soporte."
            "TOKEN_EXPIRED" -> "Tu sesión ha expirado. Por favor inicia sesión nuevamente."
            "INSUFFICIENT_PERMISSIONS" -> "No tienes permisos para realizar esta acción."
            "BUSINESS_NOT_FOUND" -> "No se encontró el negocio especificado."
            "SELLER_NOT_FOUND" -> "No se encontró el vendedor especificado."
            "TRANSACTION_NOT_FOUND" -> "No se encontró la transacción especificada."
            "QR_CODE_EXPIRED" -> "El código QR ha expirado. Genera uno nuevo."
            "AFFILIATION_CODE_INVALID" -> "El código de afiliación no es válido o ha expirado."
            "RATE_LIMIT_EXCEEDED" -> "Has realizado demasiadas solicitudes. Espera un momento antes de intentar de nuevo."
            "SERVER_ERROR" -> "Error interno del servidor. Por favor intenta más tarde."
            else -> apiError.message
        }
    }
    
    /**
     * Extrae todos los errores de validación como una lista de mensajes
     */
    fun getAllValidationErrors(apiError: ApiError): List<String> {
        val validationErrors = apiError.details?.validationErrors
        return if (validationErrors != null) {
            validationErrors.values.map { it.message }
        } else {
            listOf(apiError.message)
        }
    }
    
    /**
     * Obtiene errores específicos por campo
     */
    fun getFieldErrors(apiError: ApiError): Map<String, String> {
        val validationErrors = apiError.details?.validationErrors
        return if (validationErrors != null) {
            validationErrors.mapValues { (_, error) -> error.message }
        } else {
            emptyMap()
        }
    }
    
    /**
     * Verifica si el error es de validación
     */
    fun isValidationError(apiError: ApiError): Boolean {
        return apiError.code == "VALIDATION_ERROR"
    }
    
    /**
     * Obtiene el nombre del campo del error de validación
     */
    fun getFieldNameFromValidationError(fieldPath: String): String {
        return when {
            fieldPath.contains("businessName") -> "Nombre del negocio"
            fieldPath.contains("businessType") -> "Tipo de negocio"
            fieldPath.contains("ruc") -> "RUC"
            fieldPath.contains("email") -> "Email"
            fieldPath.contains("password") -> "Contraseña"
            fieldPath.contains("phone") -> "Teléfono"
            fieldPath.contains("address") -> "Dirección"
            fieldPath.contains("contactName") -> "Nombre de contacto"
            fieldPath.contains("sellerName") -> "Nombre del vendedor"
            fieldPath.contains("branchCode") -> "Código de sucursal"
            fieldPath.contains("branchName") -> "Nombre de sucursal"
            fieldPath.contains("qrCode") -> "Código QR"
            else -> fieldPath.substringAfterLast(".")
        }
    }
}
