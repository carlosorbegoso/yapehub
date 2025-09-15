package org.sysarp.project.service.http

import io.ktor.client.call.body
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import org.sysarp.project.data.*

/**
 * Cliente API especializado para pagos
 */
class PaymentApiClient : BaseApiClient() {
    
    /**
     * Obtener pagos pendientes
     */
    suspend fun getPendingPayments(
        sellerId: Int,
        accessToken: String
    ): Result<ApiResponse<List<PendingPayment>>> {
        return try {
            logInfo("PAYMENT_API", "Obteniendo pagos pendientes para vendedor: $sellerId")
            
            val response = client.get("$baseUrl/api/payments/pending") {
                header("Authorization", "Bearer $accessToken")
                parameter("sellerId", sellerId)
            }
            
            if (response.status.isSuccess()) {
                val paymentsResponse = response.body<ApiResponse<List<PendingPayment>>>()
                logInfo("PAYMENT_API", "Pagos pendientes obtenidos exitosamente")
                Result.success(paymentsResponse)
            } else {
                val errorMessage = "Error obteniendo pagos pendientes: ${response.status}"
                logError("PAYMENT_API", errorMessage)
                Result.failure(Exception(errorMessage))
            }
        } catch (e: Exception) {
            logError("PAYMENT_API", "Error obteniendo pagos pendientes: ${e.message}")
            Result.failure(e)
        }
    }
    
    /**
     * Confirmar pago
     */
    suspend fun confirmPayment(
        paymentId: Int,
        accessToken: String
    ): Result<ApiResponse<ClaimPaymentData>> {
        return try {
            logInfo("PAYMENT_API", "Confirmando pago: $paymentId")
            
            val response = client.post("$baseUrl/api/payments/$paymentId/confirm") {
                contentType(ContentType.Application.Json)
                header("Authorization", "Bearer $accessToken")
            }
            
            if (response.status.isSuccess()) {
                val confirmResponse = response.body<ApiResponse<ClaimPaymentData>>()
                logInfo("PAYMENT_API", "Pago confirmado exitosamente")
                Result.success(confirmResponse)
            } else {
                val errorMessage = "Error confirmando pago: ${response.status}"
                logError("PAYMENT_API", errorMessage)
                Result.failure(Exception(errorMessage))
            }
        } catch (e: Exception) {
            logError("PAYMENT_API", "Error confirmando pago: ${e.message}")
            Result.failure(e)
        }
    }
    
    /**
     * Procesar Yape como pago
     */
    suspend fun processYapeAsPayment(
        adminId: Int,
        yapeNotification: YapeNotification,
        accessToken: String
    ): Result<ApiResponse<YapePaymentResponse>> {
        return try {
            logInfo("PAYMENT_API", "Procesando Yape como pago para admin: $adminId")
            
            val response = client.post("$baseUrl/api/payments/yape-process") {
                contentType(ContentType.Application.Json)
                header("Authorization", "Bearer $accessToken")
                setBody(yapeNotification)
            }
            
            if (response.status.isSuccess()) {
                val yapeResponse = response.body<ApiResponse<YapePaymentResponse>>()
                logInfo("PAYMENT_API", "Yape procesado como pago exitosamente")
                Result.success(yapeResponse)
            } else {
                val errorMessage = "Error procesando Yape como pago: ${response.status}"
                logError("PAYMENT_API", errorMessage)
                Result.failure(Exception(errorMessage))
            }
        } catch (e: Exception) {
            logError("PAYMENT_API", "Error procesando Yape como pago: ${e.message}")
            Result.failure(e)
        }
    }
}

