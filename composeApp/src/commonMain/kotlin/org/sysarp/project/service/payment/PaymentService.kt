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

    suspend fun getAdminPaymentManagement(
        adminId: Int,
        page: Int = 0,
        size: Int = 20,
        status: String? = null,
        token: String
    ): Result<org.sysarp.project.data.AdminPaymentManagementResponse> {
        return try {
            Logger.auth("PAYMENT_SERVICE", "Obteniendo gestión de pagos para admin: $adminId, página: $page")

            val result = paymentApiClient.getAdminPaymentManagement(adminId, page, size, status, token)

            result.fold(
                onSuccess = { response ->
                    Logger.auth("PAYMENT_SERVICE", "Gestión de pagos obtenida: ${response.data.payments.size} pagos en página ${response.data.pagination.currentPage}")
                    Result.success(response)
                },
                onFailure = { error ->
                    Logger.auth("PAYMENT_SERVICE", "Error obteniendo gestión de pagos: ${error.message}")
                    Result.failure(error)
                }
            )
        } catch (e: Exception) {
            Logger.auth("PAYMENT_SERVICE", "Error obteniendo gestión de pagos: ${e.message}")
            Result.failure(e)
        }
    }

    /**
     * Obtener estado de conexión de un vendedor
     */
    suspend fun getSellerConnectionStatus(
        sellerId: Int,
        token: String
    ): Result<org.sysarp.project.data.SellerConnectionStatusResponse> {
        return try {
            Logger.auth("PAYMENT_SERVICE", "Obteniendo estado de conexión del vendedor: $sellerId")

            val result = paymentApiClient.getSellerConnectionStatus(sellerId, token)

            result.fold(
                onSuccess = { response ->
                    Logger.auth("PAYMENT_SERVICE", "Estado de conexión obtenido: ${response.data?.isConnected}")
                    Result.success(response)
                },
                onFailure = { error ->
                    Logger.auth("PAYMENT_SERVICE", "Error obteniendo estado de conexión: ${error.message}")
                    Result.failure(error)
                }
            )
        } catch (e: Exception) {
            Logger.auth("PAYMENT_SERVICE", "Error obteniendo estado de conexión: ${e.message}")
            Result.failure(e)
        }
    }

    /**
     * Obtener pagos pendientes de un vendedor específico (para administradores)
     */
    suspend fun getSellerPendingPaymentsForAdmin(
        sellerId: Int,
        adminId: Int,
        page: Int = 0,
        size: Int = 20,
        token: String
    ): Result<org.sysarp.project.data.PendingPaymentsResponse> {
        return try {
            Logger.auth("PAYMENT_SERVICE", "Obteniendo pagos pendientes del vendedor $sellerId para admin $adminId, página: $page")

            val result = paymentApiClient.getSellerPendingPaymentsForAdmin(sellerId, adminId, page, size, token)

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

    /**
     * Obtener pagos confirmados de un vendedor
     */
    suspend fun getConfirmedPayments(
        sellerId: Int,
        page: Int = 0,
        size: Int = 20,
        token: String
    ): Result<org.sysarp.project.data.PendingPaymentsResponse> {
        return try {
            Logger.auth("PAYMENT_SERVICE", "Obteniendo pagos confirmados del vendedor: $sellerId, página: $page")

            val result = paymentApiClient.getConfirmedPayments(sellerId, page, size, token)

            result.fold(
                onSuccess = { response ->
                    Logger.auth("PAYMENT_SERVICE", "Pagos confirmados obtenidos: ${response.data.payments.size} pagos en página ${response.data.pagination.currentPage}")
                    Result.success(response)
                },
                onFailure = { error ->
                    Logger.auth("PAYMENT_SERVICE", "Error obteniendo pagos confirmados: ${error.message}")
                    Result.failure(error)
                }
            )
        } catch (e: Exception) {
            Logger.auth("PAYMENT_SERVICE", "Error obteniendo pagos confirmados: ${e.message}")
            Result.failure(e)
        }
    }
}