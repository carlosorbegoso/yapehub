package org.sysarp.project.ui.common.components

import org.sysarp.project.service.auth.AuthService

/**
 * Ejemplo de cómo crear servicios que manejan automáticamente la expiración de sesiones
 */
abstract class SessionAwareService(
    protected val authService: AuthService,
    private val onSessionExpired: () -> Unit
) {
    
    protected val sessionValidator = SessionValidator(authService, onSessionExpired)
    
    /**
     * Ejecuta una operación validando primero la sesión
     */
    protected suspend fun <T> executeWithSession(
        operation: suspend () -> T
    ): Result<T> {
        return sessionValidator.withValidSession(operation)
            .handleSessionExpiration(authService, onSessionExpired)
    }
    
    /**
     * Valida la sesión antes de continuar
     */
    protected suspend fun validateSession(): Boolean {
        return sessionValidator.validateSession()
    }
}

/**
 * Ejemplo de implementación en un servicio real
 */
class SessionAwarePaymentService(
    authService: AuthService,
    onSessionExpired: () -> Unit
) : SessionAwareService(authService, onSessionExpired) {
    
    /**
     * Obtener pagos con validación de sesión
     */
    suspend fun getPayments(): Result<List<String>> {
        return executeWithSession {
            // Aquí iría la lógica real del servicio
            // Si el token expira durante esta operación, se manejará automáticamente
            
            // Simular llamada a API
            listOf("Pago 1", "Pago 2", "Pago 3")
        }
    }
    
    /**
     * Crear pago con validación de sesión
     */
    suspend fun createPayment(amount: Double): Result<String> {
        return executeWithSession {
            // Validar sesión antes de la operación crítica
            if (!validateSession()) {
                throw SessionExpiredException("Sesión expirada")
            }
            
            // Lógica de creación de pago
            "Pago creado: $amount"
        }
    }
}

/**
 * Ejemplo de uso en un ViewModel o Composable
 */
class PaymentViewModel(
    private val authService: AuthService,
    private val onNavigateToLogin: () -> Unit
) {
    
    private val paymentService = SessionAwarePaymentService(
        authService = authService,
        onSessionExpired = onNavigateToLogin
    )
    
    suspend fun loadPayments(): Result<List<String>> {
        return paymentService.getPayments()
    }
    
    suspend fun createPayment(amount: Double): Result<String> {
        return paymentService.createPayment(amount)
    }
}

/**
 * Extensiones útiles para manejar respuestas de API
 */
suspend fun <T> Result<T>.onSessionExpired(
    authService: AuthService,
    onNavigateToLogin: () -> Unit
): Result<T> {
    return this.onFailure { exception ->
        if (SessionUtils.isSessionExpiredError(errorMessage = exception.message)) {
            authService.logout()
            onNavigateToLogin()
        }
    }
}

/**
 * Wrapper para operaciones que requieren autenticación
 */
class AuthenticatedOperation<T>(
    private val authService: AuthService,
    private val onSessionExpired: () -> Unit,
    private val operation: suspend () -> T
) {
    
    suspend fun execute(): Result<T> {
        return try {
            // Verificar sesión antes de ejecutar
            if (!authService.isSessionValid()) {
                onSessionExpired()
                return Result.failure(SessionExpiredException("Sesión expirada"))
            }
            
            // Ejecutar operación
            val result = operation()
            Result.success(result)
            
        } catch (e: Exception) {
            // Verificar si el error indica sesión expirada
            if (SessionUtils.isSessionExpiredError(errorMessage = e.message)) {
                authService.logout()
                onSessionExpired()
                Result.failure(SessionExpiredException("Sesión expirada"))
            } else {
                Result.failure(e)
            }
        }
    }
}

/**
 * Función de utilidad para crear operaciones autenticadas
 */
fun <T> authenticatedOperation(
    authService: AuthService,
    onSessionExpired: () -> Unit,
    operation: suspend () -> T
): AuthenticatedOperation<T> {
    return AuthenticatedOperation(authService, onSessionExpired, operation)
}