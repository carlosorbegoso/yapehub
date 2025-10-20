package org.sysarp.project.service.payment

import org.sysarp.project.service.http.payment.PaymentApiClient

class PaymentService(
    private val paymentApiClient: PaymentApiClient
) {
    
    /**
     * Obtener pagos con filtro dinámico según el rol del usuario
     */
    suspend fun getPayments(
        sellerId: Int? = null,
        status: String,
        page: Int = 0,
        size: Int = 20,
        startDate: String? = null,
        endDate: String? = null,
        token: String
    ): Result<org.sysarp.project.data.PendingPaymentsResponse> {
        return try {
            val result = paymentApiClient.getPayments(sellerId, status, page, size, startDate, endDate, token)

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
     * Obtener pagos pendientes de un vendedor (método de conveniencia)
     */
    suspend fun getPendingPayments(
        sellerId: Int, 
        page: Int = 0, 
        limit: Int = 20,
        startDate: String? = null,
        endDate: String? = null,
        token: String
    ): Result<org.sysarp.project.data.PendingPaymentsResponse> {
        return getPayments(sellerId, "PENDING", page, limit, startDate, endDate, token)
    }

    /**
     * Obtener pagos confirmados de un vendedor (método de conveniencia)
     */
    suspend fun getConfirmedPayments(
        sellerId: Int,
        page: Int = 0,
        size: Int = 20,
        startDate: String? = null,
        endDate: String? = null,
        token: String
    ): Result<org.sysarp.project.data.PendingPaymentsResponse> {
        return getPayments(sellerId, "CLAIMED", page, size, startDate, endDate, token)
    }

    /**
     * Obtener pagos con múltiples estados (método de conveniencia)
     * @param sellerId ID del vendedor
     * @param statuses Lista de estados: ["PENDING", "CLAIMED", "REJECTED"]
     * @param page Número de página
     * @param size Tamaño de página
     * @param startDate Fecha de inicio (opcional)
     * @param endDate Fecha de fin (opcional)
     * @param token Token de autenticación
     */
    suspend fun getPaymentsWithMultipleStatuses(
        sellerId: Int? = null,
        statuses: List<String>,
        page: Int = 0,
        size: Int = 20,
        startDate: String? = null,
        endDate: String? = null,
        token: String
    ): Result<org.sysarp.project.data.PendingPaymentsResponse> {
        val statusString = statuses.joinToString(",")
        return getPayments(sellerId, statusString, page, size, startDate, endDate, token)
    }

    /**
     * Obtener todos los pagos de un vendedor (PENDING + CLAIMED + REJECTED)
     */
    suspend fun getAllPayments(
        sellerId: Int? = null,
        page: Int = 0,
        size: Int = 20,
        startDate: String? = null,
        endDate: String? = null,
        token: String
    ): Result<org.sysarp.project.data.PendingPaymentsResponse> {
        return getPaymentsWithMultipleStatuses(
            sellerId, 
            listOf("PENDING", "CLAIMED", "REJECTED"), 
            page, 
            size, 
            startDate, 
            endDate, 
            token
        )
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
        startDate: String? = null,
        endDate: String? = null,
        token: String
    ): Result<org.sysarp.project.data.AdminPaymentManagementResponse> {
        return try {

            val result = paymentApiClient.getAdminPaymentManagement(adminId, page, size, status, startDate, endDate, token)

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
        startDate: String? = null,
        endDate: String? = null,
        token: String
    ): Result<org.sysarp.project.data.PendingPaymentsResponse> {
        return try {

            val result = paymentApiClient.getSellerPendingPaymentsForAdmin(sellerId, adminId, page, size, startDate, endDate, token)

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