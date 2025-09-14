package org.sysarp.project.utils

/**
 * Utilidad centralizada para manejo de errores
 * Elimina duplicación de patrones de manejo de errores
 */
object ErrorHandler {
    
    /**
     * Errores comunes de autenticación
     */
    object AuthErrors {
        const val NO_ACCESS_TOKEN = "No hay token de acceso disponible"
        const val NO_REFRESH_TOKEN = "No hay refresh token disponible"
        const val USER_NOT_AUTHENTICATED = "Usuario no autenticado"
        const val INVALID_TOKEN = "Token de acceso inválido"
        const val SELLER_ID_NOT_AVAILABLE = "SellerId no disponible en el perfil del usuario"
        const val ONLY_VENDORS_ALLOWED = "Solo los vendedores pueden realizar esta acción"
        const val ONLY_ADMINS_ALLOWED = "Solo los administradores pueden realizar esta acción"
    }
    
    /**
     * Errores comunes de API
     */
    object ApiErrors {
        const val SERVER_ERROR = "Error del servidor"
        const val NETWORK_ERROR = "Error de red"
        const val VALIDATION_ERROR = "Error de validación"
        const val UNAUTHORIZED = "No autorizado"
        const val FORBIDDEN = "Acceso denegado"
        const val NOT_FOUND = "Recurso no encontrado"
        const val TIMEOUT = "Timeout de conexión"
    }
    
    /**
     * Errores comunes de datos
     */
    object DataErrors {
        const val INVALID_USER_ID = "ID de usuario inválido"
        const val INVALID_SELLER_ID = "ID de vendedor inválido"
        const val INVALID_PAYMENT_ID = "ID de pago inválido"
        const val DATA_NOT_FOUND = "Datos no encontrados"
        const val PARSE_ERROR = "Error parseando datos"
    }
    
    /**
     * Crea un Result.failure con mensaje de error estándar
     */
    fun <T> createFailure(message: String): Result<T> {
        return Result.failure(Exception(message))
    }
    
    /**
     * Crea un Result.failure con mensaje de error de autenticación
     */
    fun <T> createAuthFailure(error: String): Result<T> {
        return Result.failure(Exception(error))
    }
    
    /**
     * Crea un Result.failure con mensaje de error de API
     */
    fun <T> createApiFailure(error: String): Result<T> {
        return Result.failure(Exception(error))
    }
    
    /**
     * Crea un Result.failure con mensaje de error de datos
     */
    fun <T> createDataFailure(error: String): Result<T> {
        return Result.failure(Exception(error))
    }
    
    /**
     * Crea un Result.failure con mensaje de error de red
     */
    fun <T> createNetworkFailure(error: String): Result<T> {
        return Result.failure(Exception("$ApiErrors.NETWORK_ERROR: $error"))
    }
    
    /**
     * Valida que el token de acceso esté disponible
     */
    fun <T> validateAccessToken(accessToken: String?): Result<T>? {
        return if (accessToken == null) {
            createAuthFailure(AuthErrors.NO_ACCESS_TOKEN)
        } else null
    }
    
    /**
     * Valida que el usuario esté autenticado
     */
    fun <T> validateUserAuthenticated(userProfile: Any?): Result<T>? {
        return if (userProfile == null) {
            createAuthFailure(AuthErrors.USER_NOT_AUTHENTICATED)
        } else null
    }
    
    /**
     * Valida que el usuario sea vendedor
     */
    fun <T> validateVendorRole(role: String): Result<T>? {
        return if (role != "VENDOR" && role != "SELLER") {
            createAuthFailure(AuthErrors.ONLY_VENDORS_ALLOWED)
        } else null
    }
    
    /**
     * Valida que el usuario sea administrador
     */
    fun <T> validateAdminRole(role: String): Result<T>? {
        return if (role != "ADMIN") {
            createAuthFailure(AuthErrors.ONLY_ADMINS_ALLOWED)
        } else null
    }
    
    /**
     * Valida que el sellerId esté disponible
     */
    fun <T> validateSellerId(sellerId: Int?): Result<T>? {
        return if (sellerId == null) {
            createAuthFailure(AuthErrors.SELLER_ID_NOT_AVAILABLE)
        } else null
    }
    
    /**
     * Maneja errores de excepción con logging
     */
    fun <T> handleException(service: String, operation: String, exception: Exception): Result<T> {
        Logger.error(service, "Error en $operation: ${exception.message}")
        return createFailure("Error en $operation: ${exception.message}")
    }
    
    /**
     * Maneja errores de red con logging
     */
    fun <T> handleNetworkException(service: String, operation: String, exception: Exception): Result<T> {
        Logger.error(service, "Error de red en $operation: ${exception.message}")
        return createNetworkFailure("Error en $operation: ${exception.message}")
    }

    
    /**
     * Obtiene mensaje de error amigable para el usuario
     * Elimina duplicación de manejo de errores HTTP
     */
    fun getFriendlyErrorMessage(statusCode: Int, errorMessage: String? = null): String {
        return when (statusCode) {
            400 -> "Error de validación: Los datos enviados no son válidos"
            401 -> "No autorizado: Token de acceso inválido o expirado"
            403 -> "Acceso denegado: No tienes permisos para realizar esta acción"
            404 -> "Recurso no encontrado: El elemento solicitado no existe"
            409 -> "Conflicto: El recurso ya existe o está en uso"
            422 -> "Error de validación: Los datos enviados no cumplen los requisitos"
            429 -> "Demasiadas solicitudes: Intenta nuevamente en unos minutos"
            500 -> "Error interno del servidor: Intenta nuevamente más tarde"
            502 -> "Servicio temporalmente no disponible"
            503 -> "Servicio en mantenimiento: Intenta más tarde"
            504 -> "Timeout: El servidor tardó demasiado en responder"
            else -> errorMessage ?: "Error inesperado: Código $statusCode"
        }
    }
}