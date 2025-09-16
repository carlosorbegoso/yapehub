package org.sysarp.project.service.http

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import kotlinx.serialization.json.Json
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
            logInfo("PAYMENT_API", "Obteniendo pagos pendientes del vendedor: $sellerId, página: $page, límite: $limit")

            val response = httpClient.get("$baseUrl/api/payments/pending") {
                parameter("sellerId", sellerId)
                parameter("page", page)
                parameter("limit", limit)
                header("Authorization", "Bearer $token")
                header("accept", "application/json")
            }

            if (response.status.value in 200..299) {
                try {
                    // Usar deserialización manual directamente ya que Kotlin reflection no está disponible
                    val responseBody = response.body<String>()
                    logInfo("PAYMENT_API", "Respuesta del servidor: $responseBody")
                    val pendingPaymentsResponse = kotlinx.serialization.json.Json.decodeFromString<org.sysarp.project.data.PendingPaymentsResponse>(responseBody)
                    logInfo("PAYMENT_API", "Pagos pendientes obtenidos exitosamente: ${pendingPaymentsResponse.data.payments.size} pagos en página ${pendingPaymentsResponse.data.pagination.currentPage}")
                    Result.success(pendingPaymentsResponse)
                } catch (e: Exception) {
                    logError("PAYMENT_API", "Error deserializando respuesta: ${e.message}")
                    Result.failure(Exception("Error deserializando respuesta del servidor: ${e.message}"))
                }
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
            logInfo("PAYMENT_API", "Confirmando pago: $paymentId para vendedor: $sellerId con token: ${token.take(20)}...")

            val request = org.sysarp.project.data.ClaimPaymentRequest(
                sellerId = sellerId,
                paymentId = paymentId
            )

            val response = httpClient.post("$baseUrl/api/payments/claim") {
                header("Authorization", "Bearer $token")
                header("accept", "application/json")
                header("Content-Type", "application/json")
                setBody(Json.encodeToString(org.sysarp.project.data.ClaimPaymentRequest.serializer(), request))
            }

            if (response.status.value in 200..299) {
                try {
                    val responseBody = response.body<String>()
                    logInfo("PAYMENT_API", "Respuesta de claim: $responseBody")
                    val claimResponse = kotlinx.serialization.json.Json.decodeFromString<org.sysarp.project.data.ClaimPaymentResponse>(responseBody)
                    logInfo("PAYMENT_API", "Pago confirmado exitosamente: $paymentId")
                    Result.success(claimResponse)
                } catch (e: Exception) {
                    logError("PAYMENT_API", "Error deserializando respuesta de claim: ${e.message}")
                    Result.failure(Exception("Error deserializando respuesta del servidor: ${e.message}"))
                }
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
            logInfo("PAYMENT_API", "Rechazando pago: $paymentId para vendedor: $sellerId con token: ${token.take(20)}...")

            val request = org.sysarp.project.data.RejectPaymentRequest(
                sellerId = sellerId,
                paymentId = paymentId,
                reason = reason
            )

            val response = httpClient.post("$baseUrl/api/payments/reject") {
                header("Authorization", "Bearer $token")
                header("accept", "application/json")
                header("Content-Type", "application/json")
                setBody(Json.encodeToString(org.sysarp.project.data.RejectPaymentRequest.serializer(), request))
            }

            if (response.status.value in 200..299) {
                try {
                    val responseBody = response.body<String>()
                    logInfo("PAYMENT_API", "Respuesta de reject: $responseBody")
                    val rejectResponse = kotlinx.serialization.json.Json.decodeFromString<org.sysarp.project.data.RejectPaymentResponse>(responseBody)
                    logInfo("PAYMENT_API", "Pago rechazado exitosamente: $paymentId")
                    Result.success(rejectResponse)
                } catch (e: Exception) {
                    logError("PAYMENT_API", "Error deserializando respuesta de reject: ${e.message}")
                    Result.failure(Exception("Error deserializando respuesta del servidor: ${e.message}"))
                }
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

    suspend fun getAdminPaymentManagement(
        adminId: Int,
        page: Int = 0,
        size: Int = 20,
        status: String? = null,
        token: String
    ): Result<org.sysarp.project.data.AdminPaymentManagementResponse> {
        return try {
            logInfo("PAYMENT_API", "Obteniendo gestión de pagos para admin: $adminId, página: $page, tamaño: $size, estado: $status")

            val response = httpClient.get("$baseUrl/api/payments/admin/management") {
                parameter("adminId", adminId)
                parameter("page", page)
                parameter("size", size)
                status?.let { parameter("status", it) }
                header("Authorization", "Bearer $token")
                header("accept", "application/json")
            }

            if (response.status.value in 200..299) {
                try {
                    // Usar deserialización manual directamente ya que Kotlin reflection no está disponible
                    val responseBody = response.body<String>()
                    logInfo("PAYMENT_API", "Respuesta del servidor: $responseBody")
                    val adminPaymentResponse = kotlinx.serialization.json.Json.decodeFromString<org.sysarp.project.data.AdminPaymentManagementResponse>(responseBody)
                    logInfo("PAYMENT_API", "Gestión de pagos obtenida exitosamente: ${adminPaymentResponse.data.payments.size} pagos en página ${adminPaymentResponse.data.pagination.currentPage}")
                    Result.success(adminPaymentResponse)
                } catch (e: Exception) {
                    logError("PAYMENT_API", "Error deserializando respuesta: ${e.message}")
                    Result.failure(Exception("Error deserializando respuesta del servidor: ${e.message}"))
                }
            } else {
                val errorMessage = try {
                    val errorBody = response.body<String>()
                    logError("PAYMENT_API", "Error body: $errorBody")
                    errorBody
                } catch (e: Exception) {
                    "Error desconocido: ${e.message}"
                }

                val finalErrorMessage = "Error obteniendo gestión de pagos: ${response.status} - $errorMessage"
                logError("PAYMENT_API", finalErrorMessage)
                Result.failure(Exception(finalErrorMessage))
            }
        } catch (e: Exception) {
            logError("PAYMENT_API", "Error obteniendo gestión de pagos: ${e.message}")
            Result.failure(e)
        }
    }
}