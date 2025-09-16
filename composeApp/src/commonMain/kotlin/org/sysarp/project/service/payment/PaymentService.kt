package org.sysarp.project.service.payment

import org.sysarp.project.service.http.PaymentApiClient
import org.sysarp.project.utils.Logger

class PaymentService(
    private val paymentApiClient: PaymentApiClient
) {
    
    suspend fun getPendingPayments(
        sellerId: Int, 
        page: Int = 0, 
        limit: Int = 20, 
        token: String
    ): Result<org.sysarp.project.data.PendingPaymentsResponse> {
        return try {
            Logger.auth("PAYMENT_SERVICE", "Obteniendo pagos pendientes del vendedor: $sellerId, página: $page")

            val result = paymentApiClient.getPendingPayments(sellerId, page, limit, token)

            result.fold(
                onSuccess = { response ->
                    Logger.auth("PAYMENT_SERVICE", "Pagos pendientes obtenidos: ${response.data.payments.size} pagos en página ${response.data.pagination.currentPage}")
                    Result.success(response)
                },
                onFailure = { error ->
                    Logger.auth("PAYMENT_SERVICE", "Error obteniendo pagos pendientes: ${error.message}")
                    Result.failure(error)
                }
            )
        } catch (e: Exception) {
            Logger.auth("PAYMENT_SERVICE", "Error obteniendo pagos pendientes: ${e.message}")
            Result.failure(e)
        }
    }

    suspend fun claimPayment(
        sellerId: Int,
        paymentId: Int,
        token: String
    ): Result<org.sysarp.project.data.ClaimPaymentResponse> {
        return try {
            Logger.auth("PAYMENT_SERVICE", "Confirmando pago: $paymentId para vendedor: $sellerId")

            val result = paymentApiClient.claimPayment(sellerId, paymentId, token)

            result.fold(
                onSuccess = { response ->
                    Logger.auth("PAYMENT_SERVICE", "Pago confirmado exitosamente: $paymentId")
                    Result.success(response)
                },
                onFailure = { error ->
                    Logger.auth("PAYMENT_SERVICE", "Error confirmando pago: ${error.message}")
                    Result.failure(error)
                }
            )
        } catch (e: Exception) {
            Logger.auth("PAYMENT_SERVICE", "Error confirmando pago: ${e.message}")
            Result.failure(e)
        }
    }

    suspend fun rejectPayment(
        sellerId: Int,
        paymentId: Int,
        reason: String,
        token: String
    ): Result<org.sysarp.project.data.RejectPaymentResponse> {
        return try {
            Logger.auth("PAYMENT_SERVICE", "Rechazando pago: $paymentId para vendedor: $sellerId")

            val result = paymentApiClient.rejectPayment(sellerId, paymentId, reason, token)

            result.fold(
                onSuccess = { response ->
                    Logger.auth("PAYMENT_SERVICE", "Pago rechazado exitosamente: $paymentId")
                    Result.success(response)
                },
                onFailure = { error ->
                    Logger.auth("PAYMENT_SERVICE", "Error rechazando pago: ${error.message}")
                    Result.failure(error)
                }
            )
        } catch (e: Exception) {
            Logger.auth("PAYMENT_SERVICE", "Error rechazando pago: ${e.message}")
            Result.failure(e)
        }
    }
}