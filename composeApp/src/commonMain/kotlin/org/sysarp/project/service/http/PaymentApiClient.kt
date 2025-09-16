package org.sysarp.project.service.http

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import org.sysarp.project.service.http.BaseApiClient

class PaymentApiClient(
    private val httpClient: HttpClient
) : BaseApiClient() {

    suspend fun getPendingPayments(
        sellerId: Int, 
        page: Int = 0, 
        limit: Int = 20, 
        token: String
    ): Result<org.sysarp.project.data.PendingPaymentsResponse> {
        return try {
            logInfo("PAYMENT_API", "Obteniendo pagos pendientes del vendedor: $sellerId, página: $page")

            val response = httpClient.get("$baseUrl/api/payments/pending") {
                parameter("sellerId", sellerId)
                parameter("page", page)
                parameter("limit", limit)
                header("Authorization", "Bearer $token")
                header("accept", "application/json")
            }

            if (response.status.value in 200..299) {
                val pendingPaymentsResponse = response.body<org.sysarp.project.data.PendingPaymentsResponse>()
                logInfo("PAYMENT_API", "Pagos pendientes obtenidos exitosamente: ${pendingPaymentsResponse.data.payments.size} pagos en página ${pendingPaymentsResponse.data.pagination.currentPage}")
                Result.success(pendingPaymentsResponse)
            } else {
                val errorMessage = try {
                    val errorBody = response.body<String>()
                    logError("PAYMENT_API", "Error body: $errorBody")
                    errorBody
                } catch (e: Exception) {
                    "Error desconocido: ${e.message}"
                }

                val finalErrorMessage = "Error obteniendo pagos pendientes: ${response.status} - $errorMessage"
                logError("PAYMENT_API", finalErrorMessage)
                Result.failure(Exception(finalErrorMessage))
            }
        } catch (e: Exception) {
            logError("PAYMENT_API", "Error obteniendo pagos pendientes: ${e.message}")
            Result.failure(e)
        }
    }

    suspend fun claimPayment(
        sellerId: Int,
        paymentId: Int,
        token: String
    ): Result<org.sysarp.project.data.ClaimPaymentResponse> {
        return try {
            logInfo("PAYMENT_API", "Confirmando pago: $paymentId para vendedor: $sellerId")

            val request = org.sysarp.project.data.ClaimPaymentRequest(
                sellerId = sellerId,
                paymentId = paymentId
            )

            val response = httpClient.post("$baseUrl/api/payments/claim") {
                header("Authorization", "Bearer $token")
                header("accept", "application/json")
                header("Content-Type", "application/json")
                setBody(request)
            }

            if (response.status.value in 200..299) {
                val claimResponse = response.body<org.sysarp.project.data.ClaimPaymentResponse>()
                logInfo("PAYMENT_API", "Pago confirmado exitosamente: $paymentId")
                Result.success(claimResponse)
            } else {
                val errorMessage = try {
                    val errorBody = response.body<String>()
                    logError("PAYMENT_API", "Error body: $errorBody")
                    errorBody
                } catch (e: Exception) {
                    "Error desconocido: ${e.message}"
                }

                val finalErrorMessage = "Error confirmando pago: ${response.status} - $errorMessage"
                logError("PAYMENT_API", finalErrorMessage)
                Result.failure(Exception(finalErrorMessage))
            }
        } catch (e: Exception) {
            logError("PAYMENT_API", "Error confirmando pago: ${e.message}")
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
            logInfo("PAYMENT_API", "Rechazando pago: $paymentId para vendedor: $sellerId")

            val request = org.sysarp.project.data.RejectPaymentRequest(
                sellerId = sellerId,
                paymentId = paymentId,
                reason = reason
            )

            val response = httpClient.post("$baseUrl/api/payments/reject") {
                header("Authorization", "Bearer $token")
                header("accept", "application/json")
                header("Content-Type", "application/json")
                setBody(request)
            }

            if (response.status.value in 200..299) {
                val rejectResponse = response.body<org.sysarp.project.data.RejectPaymentResponse>()
                logInfo("PAYMENT_API", "Pago rechazado exitosamente: $paymentId")
                Result.success(rejectResponse)
            } else {
                val errorMessage = try {
                    val errorBody = response.body<String>()
                    logError("PAYMENT_API", "Error body: $errorBody")
                    errorBody
                } catch (e: Exception) {
                    "Error desconocido: ${e.message}"
                }

                val finalErrorMessage = "Error rechazando pago: ${response.status} - $errorMessage"
                logError("PAYMENT_API", finalErrorMessage)
                Result.failure(Exception(finalErrorMessage))
            }
        } catch (e: Exception) {
            logError("PAYMENT_API", "Error rechazando pago: ${e.message}")
            Result.failure(e)
        }
    }
}