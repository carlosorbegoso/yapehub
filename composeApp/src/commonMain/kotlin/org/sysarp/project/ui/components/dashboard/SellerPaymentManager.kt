package org.sysarp.project.ui.components.dashboard

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import kotlinx.coroutines.launch
import org.sysarp.project.data.SellerPendingPayment
import org.sysarp.project.service.auth.AuthService
import org.sysarp.project.service.payment.PaymentService
import org.sysarp.project.utils.Logger

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
            Logger.auth("PAYMENT_MANAGER", "Cargando pagos pendientes para vendedor $sellerId")
            val response = paymentService.getPendingPayments(sellerId.toInt(), 0, 20, accessToken)
            
            response.fold(
                onSuccess = { result ->
                    Logger.auth("PAYMENT_MANAGER", "Pagos cargados exitosamente: ${result.data.payments.size} pagos")
                    onSuccess(result.data.payments)
                },
                onFailure = { error ->
                    Logger.auth("PAYMENT_MANAGER", "Error al cargar pagos: ${error.message}")
                    onError(error.message ?: "Error desconocido")
                }
            )
        } catch (e: Exception) {
            Logger.auth("PAYMENT_MANAGER", "Excepción al cargar pagos: ${e.message}")
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
            Logger.auth("PAYMENT_MANAGER", "Cargando más pagos - página $currentPage")
            val response = paymentService.getPendingPayments(sellerId.toInt(), currentPage, 20, accessToken)
            
            response.fold(
                onSuccess = { result ->
                    Logger.auth("PAYMENT_MANAGER", "Más pagos cargados: ${result.data.payments.size} pagos")
                    onSuccess(result.data.payments)
                },
                onFailure = { error ->
                    Logger.auth("PAYMENT_MANAGER", "Error al cargar más pagos: ${error.message}")
                    onError(error.message ?: "Error desconocido")
                }
            )
        } catch (e: Exception) {
            Logger.auth("PAYMENT_MANAGER", "Excepción al cargar más pagos: ${e.message}")
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
            Logger.auth("PAYMENT_MANAGER", "Confirmando pago $paymentId para vendedor $sellerId")
            val response = paymentService.claimPayment(sellerId.toInt(), paymentId, accessToken)
            
            response.fold(
                onSuccess = { result ->
                    Logger.auth("PAYMENT_MANAGER", "Pago confirmado exitosamente")
                    onSuccess("Pago confirmado exitosamente")
                },
                onFailure = { error ->
                    Logger.auth("PAYMENT_MANAGER", "Error al confirmar pago: ${error.message}")
                    onError(error.message ?: "Error al confirmar el pago")
                }
            )
        } catch (e: Exception) {
            Logger.auth("PAYMENT_MANAGER", "Excepción al confirmar pago: ${e.message}")
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
            Logger.auth("PAYMENT_MANAGER", "Rechazando pago $paymentId para vendedor $sellerId")
            val response = paymentService.rejectPayment(sellerId.toInt(), paymentId, reason, accessToken)
            
            response.fold(
                onSuccess = { result ->
                    Logger.auth("PAYMENT_MANAGER", "Pago rechazado exitosamente")
                    onSuccess("Pago rechazado exitosamente")
                },
                onFailure = { error ->
                    Logger.auth("PAYMENT_MANAGER", "Error al rechazar pago: ${error.message}")
                    onError(error.message ?: "Error al rechazar el pago")
                }
            )
        } catch (e: Exception) {
            Logger.auth("PAYMENT_MANAGER", "Excepción al rechazar pago: ${e.message}")
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
