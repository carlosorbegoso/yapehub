package org.sysarp.project.ui.components.dashboard

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import org.sysarp.project.data.SellerPendingPayment
import org.sysarp.project.service.auth.AuthService
import org.sysarp.project.service.payment.PaymentService

/**
 * Manager para manejar la lógica de pagos del vendedor
 */
class SellerPaymentManager(
    private val paymentService: PaymentService,
    private val authService: AuthService
) {
    
    /**
     * Cargar pagos pendientes (primera carga)
     */
    suspend fun loadPendingPayments(
        accessToken: String,
        sellerId: Long,
        onSuccess: (List<SellerPendingPayment>) -> Unit,
        onError: (String) -> Unit
    ) {
        try {
            val response = paymentService.getPendingPayments(sellerId.toInt(), 0, 20, null, null, accessToken)
            
            response.fold(
                onSuccess = { result ->
                    onSuccess(result.data.payments)
                },
                onFailure = { error ->
                    onError(error.message ?: "Error desconocido")
                }
            )
        } catch (e: Exception) {
            onError("Error de conexión: ${e.message}")
        }
    }
    
    /**
     * Cargar más pagos (paginación)
     */
    suspend fun loadMorePendingPayments(
        accessToken: String,
        sellerId: Long,
        currentPage: Int,
        onSuccess: (List<SellerPendingPayment>) -> Unit,
        onError: (String) -> Unit
    ) {
        try {
            val response = paymentService.getPendingPayments(sellerId.toInt(), currentPage, 20, null, null, accessToken)
            
            response.fold(
                onSuccess = { result ->
                    onSuccess(result.data.payments)
                },
                onFailure = { error ->
                    onError(error.message ?: "Error desconocido")
                }
            )
        } catch (e: Exception) {
            onError("Error de conexión: ${e.message}")
        }
    }
    
    /**
     * Confirmar un pago
     */
    suspend fun claimPayment(
        accessToken: String,
        paymentId: Int,
        sellerId: Long,
        onSuccess: (String) -> Unit,
        onError: (String) -> Unit
    ) {
        try {
            val response = paymentService.claimPayment(sellerId.toInt(), paymentId, accessToken)
            
            response.fold(
                onSuccess = { result ->
                    onSuccess("Pago confirmado exitosamente")
                },
                onFailure = { error ->
                    onError(error.message ?: "Error al confirmar el pago")
                }
            )
        } catch (e: Exception) {
            onError("Error de conexión: ${e.message}")
        }
    }
    
    /**
     * Rechazar un pago
     */
    suspend fun rejectPayment(
        accessToken: String,
        paymentId: Int,
        sellerId: Long,
        reason: String,
        onSuccess: (String) -> Unit,
        onError: (String) -> Unit
    ) {
        try {
            val response = paymentService.rejectPayment(sellerId.toInt(), paymentId, reason, accessToken)
            
            response.fold(
                onSuccess = { result ->
                    onSuccess("Pago rechazado exitosamente")
                },
                onFailure = { error ->
                    onError(error.message ?: "Error al rechazar el pago")
                }
            )
        } catch (e: Exception) {
            onError("Error de conexión: ${e.message}")
        }
    }
}

/**
 * Composable para crear el manager de pagos
 */
@Composable
fun rememberSellerPaymentManager(
    paymentService: PaymentService,
    authService: AuthService
): SellerPaymentManager {
    return remember(paymentService, authService) {
        SellerPaymentManager(paymentService, authService)
    }
}
