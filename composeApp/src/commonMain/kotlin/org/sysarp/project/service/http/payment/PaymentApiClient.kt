package org.sysarp.project.service.http

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.parameter
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import kotlinx.serialization.json.Json
import org.sysarp.project.data.SellerConnectionStatusResponse

class PaymentApiClient(
    private val httpClient: HttpClient
) : BaseApiClient() {

    /**
     * Obtener pagos con filtro dinámico según el rol del usuario
     * @param sellerId ID del vendedor (requerido para SELLER, opcional para ADMIN)
     * @param status Estados del pago: PENDING, CLAIMED, REJECTED, ALL o múltiples separados por coma (ej: "PENDING,CLAIMED")
     * @param page Número de página
     * @param size Tamaño de página
     * @param startDate Fecha de inicio (opcional)
     * @param endDate Fecha de fin (opcional)
     * @param token Token de autenticación
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
            logInfo("PAYMENT_API", "Obteniendo pagos - sellerId: $sellerId, status: $status, página: $page, límite: $size, fechas: $startDate - $endDate")

            val response = httpClient.get("$baseUrl/api/payments") {
                sellerId?.let { parameter("sellerId", it) }
                parameter("page", page)
                parameter("size", size)
                parameter("status", status)
                startDate?.let { parameter("startDate", it) }
                endDate?.let { parameter("endDate", it) }
                header("Authorization", "Bearer $token")
                header("accept", "application/json")
            }

            if (response.status.value in 200..299) {
                try {
                    val responseBody = response.body<String>()
                    logInfo("PAYMENT_API", "Respuesta del servidor: $responseBody")
                    val paymentsResponse = kotlinx.serialization.json.Json.decodeFromString<org.sysarp.project.data.PendingPaymentsResponse>(responseBody)
                    logInfo("PAYMENT_API", "Pagos obtenidos exitosamente: ${paymentsResponse.data.payments.size} pagos en página ${paymentsResponse.data.pagination.currentPage} (status: $status)")
                    Result.success(paymentsResponse)
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

                val finalErrorMessage = "Error obteniendo pagos (status: $status): ${response.status} - $errorMessage"
                logError("PAYMENT_API", finalErrorMessage)
                Result.failure(Exception(finalErrorMessage))
            }
        } catch (e: Exception) {
            logError("PAYMENT_API", "Error obteniendo pagos (status: $status): ${e.message}")
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
                    
                    // Intentar parsear el error del servidor
                    try {
                        val serverError = kotlinx.serialization.json.Json.decodeFromString<org.sysarp.project.data.ServerErrorResponse>(errorBody)
                        logError("PAYMENT_API", "Error parseado del servidor: ${serverError.message} - ${serverError.details.reason}")
                        "${serverError.message} - ${serverError.details.reason}"
                    } catch (parseError: Exception) {
                        logError("PAYMENT_API", "Error parseando respuesta de error: ${parseError.message}")
                        errorBody
                    }
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
                    
                    // Intentar parsear el error del servidor
                    try {
                        val serverError = kotlinx.serialization.json.Json.decodeFromString<org.sysarp.project.data.ServerErrorResponse>(errorBody)
                        logError("PAYMENT_API", "Error parseado del servidor: ${serverError.message} - ${serverError.details.reason}")
                        "${serverError.message} - ${serverError.details.reason}"
                    } catch (parseError: Exception) {
                        logError("PAYMENT_API", "Error parseando respuesta de error: ${parseError.message}")
                        errorBody
                    }
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
        startDate: String? = null,
        endDate: String? = null,
        token: String
    ): Result<org.sysarp.project.data.AdminPaymentManagementResponse> {
        return try {
            logInfo("PAYMENT_API", "Obteniendo gestión de pagos para admin: $adminId, página: $page, tamaño: $size, estado: $status, fechas: $startDate - $endDate")

            val response = httpClient.get("$baseUrl/api/payments") {
                parameter("adminId", adminId)
                parameter("page", page)
                parameter("size", size)
                status?.let { parameter("status", it) }
                startDate?.let { parameter("startDate", it) }
                endDate?.let { parameter("endDate", it) }
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
    
    /**
     * Obtener estado de conexión del vendedor
     */
    suspend fun getSellerConnectionStatus(
        sellerId: Int,
        token: String
    ): Result<SellerConnectionStatusResponse> {
        return try {
            logInfo("PAYMENT_API", "Obteniendo estado de conexión del vendedor: $sellerId")

            val response = httpClient.get("$baseUrl/api/payments/status/$sellerId") {
                header("Authorization", "Bearer $token")
                header("accept", "application/json")
            }

            if (response.status.value in 200..299) {
                try {
                    val responseBody = response.body<String>()
                    logInfo("PAYMENT_API", "Respuesta del servidor: $responseBody")
                    val statusResponse = kotlinx.serialization.json.Json.decodeFromString<SellerConnectionStatusResponse>(responseBody)
                    logInfo("PAYMENT_API", "Estado de conexión obtenido exitosamente")
                    Result.success(statusResponse)
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

                val finalErrorMessage = "Error obteniendo estado de conexión: ${response.status} - $errorMessage"
                logError("PAYMENT_API", finalErrorMessage)
                Result.failure(Exception(finalErrorMessage))
            }
        } catch (e: Exception) {
            logError("PAYMENT_API", "Error obteniendo estado de conexión: ${e.message}")
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
            logInfo("PAYMENT_API", "Obteniendo pagos pendientes del vendedor $sellerId para admin $adminId, página: $page, tamaño: $size, fechas: $startDate - $endDate")

            val response = httpClient.get("$baseUrl/api/payments/pending") {
                parameter("sellerId", sellerId)
                parameter("adminId", adminId)
                parameter("page", page)
                parameter("size", size)
                startDate?.let { parameter("startDate", it) }
                endDate?.let { parameter("endDate", it) }
                header("Authorization", "Bearer $token")
                header("accept", "application/json")
            }

            if (response.status.value in 200..299) {
                try {
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

}