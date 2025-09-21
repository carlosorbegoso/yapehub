package org.sysarp.project.service.payment

import org.sysarp.project.service.http.PaymentApiClient

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

            val result = paymentApiClient.getPendingPayments(sellerId, page, limit, token)

            result.fold(
                onSuccess = { response ->
                    Result.success(response)
                },
                onFailure = { error ->
                    Result.failure(error)
                }
            )
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun claimPayment(
        sellerId: Int,
        paymentId: Int,
        token: String
    ): Result<org.sysarp.project.data.ClaimPaymentResponse> {
        return try {

            val result = paymentApiClient.claimPayment(sellerId, paymentId, token)

            result.fold(
                onSuccess = { response ->
                    Result.success(response)
                },
                onFailure = { error ->
                    Result.failure(error)
                }
            )
        } catch (e: Exception) {
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

            val result = paymentApiClient.rejectPayment(sellerId, paymentId, reason, token)

            result.fold(
                onSuccess = { response ->
                    Result.success(response)
                },
                onFailure = { error ->
                    Result.failure(error)
                }
            )
        } catch (e: Exception) {
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

            val result = paymentApiClient.getAdminPaymentManagement(adminId, page, size, status, token)

            result.fold(
                onSuccess = { response ->
                    Result.success(response)
                },
                onFailure = { error ->
                    Result.failure(error)
                }
            )
        } catch (e: Exception) {
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

            val result = paymentApiClient.getSellerConnectionStatus(sellerId, token)

            result.fold(
                onSuccess = { response ->
                    Result.success(response)
                },
                onFailure = { error ->
                    Result.failure(error)
                }
            )
        } catch (e: Exception) {
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

            val result = paymentApiClient.getSellerPendingPaymentsForAdmin(sellerId, adminId, page, size, token)

            result.fold(
                onSuccess = { response ->
                    Result.success(response)
                },
                onFailure = { error ->
                    Result.failure(error)
                }
            )
        } catch (e: Exception) {
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

            val result = paymentApiClient.getConfirmedPayments(sellerId, page, size, token)

            result.fold(
                onSuccess = { response ->
                    Result.success(response)
                },
                onFailure = { error ->
                    Result.failure(error)
                }
            )
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}