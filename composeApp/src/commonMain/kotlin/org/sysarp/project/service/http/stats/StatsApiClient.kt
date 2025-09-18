package org.sysarp.project.service.http

import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.client.call.*
import kotlinx.serialization.json.Json
import org.sysarp.project.data.AdminStatsResponse
import org.sysarp.project.data.SellerStatsResponse
import org.sysarp.project.data.QuickSummaryResponse
import org.sysarp.project.data.AnalyticsResponse

/**
 * Cliente HTTP para estadísticas
 */
class StatsApiClient : BaseApiClient() {

    suspend fun getAdminStatsSummary(
        adminId: Int,
        token: String
    ): Result<AdminStatsResponse> {
        return try {
            logInfo("STATS_API", "Obteniendo resumen de estadísticas del admin: $adminId")

            val response = client.get("$baseUrl/api/stats/admin/summary") {
                parameter("adminId", adminId)
                header("Authorization", "Bearer $token")
                header("accept", "application/json")
            }

            if (response.status.value in 200..299) {
                try {
                    // Usar deserialización manual directamente ya que Kotlin reflection no está disponible
                    val responseBody = response.body<String>()
                    logInfo("STATS_API", "Respuesta del servidor: $responseBody")
                    val adminStatsResponse = kotlinx.serialization.json.Json.decodeFromString<AdminStatsResponse>(responseBody)
                    logInfo("STATS_API", "Estadísticas de admin obtenidas exitosamente")
                    Result.success(adminStatsResponse)
                } catch (e: Exception) {
                    logError("STATS_API", "Error deserializando respuesta: ${e.message}")
                    Result.failure(Exception("Error deserializando respuesta del servidor: ${e.message}"))
                }
            } else {
                val errorMessage = try {
                    val errorBody = response.body<String>()
                    logError("STATS_API", "Error body: $errorBody")
                    errorBody
                } catch (e: Exception) {
                    "Error desconocido: ${e.message}"
                }

                val finalErrorMessage = "Error obteniendo estadísticas de admin: ${response.status} - $errorMessage"
                logError("STATS_API", finalErrorMessage)
                Result.failure(Exception(finalErrorMessage))
            }
        } catch (e: Exception) {
            logError("STATS_API", "Error obteniendo estadísticas de admin: ${e.message}")
            Result.failure(e)
        }
    }

    suspend fun getAdminStats(
        adminId: Int,
        startDate: String,
        endDate: String,
        token: String
    ): Result<AdminStatsResponse> {
        return try {
            logInfo("STATS_API", "Obteniendo estadísticas del admin: $adminId desde $startDate hasta $endDate")

            val response = client.get("$baseUrl/api/stats/admin") {
                parameter("adminId", adminId)
                parameter("startDate", startDate)
                parameter("endDate", endDate)
                header("Authorization", "Bearer $token")
                header("accept", "application/json")
            }

            if (response.status.value in 200..299) {
                try {
                    // Usar deserialización manual directamente ya que Kotlin reflection no está disponible
                    val responseBody = response.body<String>()
                    logInfo("STATS_API", "Respuesta del servidor: $responseBody")
                    val adminStatsResponse = kotlinx.serialization.json.Json.decodeFromString<AdminStatsResponse>(responseBody)
                    logInfo("STATS_API", "Estadísticas de admin obtenidas exitosamente")
                    Result.success(adminStatsResponse)
                } catch (e: Exception) {
                    logError("STATS_API", "Error deserializando respuesta: ${e.message}")
                    Result.failure(Exception("Error deserializando respuesta del servidor: ${e.message}"))
                }
            } else {
                val errorMessage = try {
                    val errorBody = response.body<String>()
                    logError("STATS_API", "Error body: $errorBody")
                    errorBody
                } catch (e: Exception) {
                    "Error desconocido: ${e.message}"
                }

                val finalErrorMessage = "Error obteniendo estadísticas de admin: ${response.status} - $errorMessage"
                logError("STATS_API", finalErrorMessage)
                Result.failure(Exception(finalErrorMessage))
            }
        } catch (e: Exception) {
            logError("STATS_API", "Error obteniendo estadísticas de admin: ${e.message}")
            Result.failure(e)
        }
    }

    suspend fun getSellerStatsSummary(
        sellerId: Int,
        token: String
    ): Result<SellerStatsResponse> {
        return try {
            logInfo("STATS_API", "Obteniendo resumen de estadísticas del vendedor: $sellerId")

            val response = client.get("$baseUrl/api/stats/seller/summary") {
                parameter("sellerId", sellerId)
                header("Authorization", "Bearer $token")
                header("accept", "application/json")
            }

            if (response.status.value in 200..299) {
                try {
                    // Usar deserialización manual directamente ya que Kotlin reflection no está disponible
                    val responseBody = response.body<String>()
                    logInfo("STATS_API", "Respuesta del servidor: $responseBody")
                    val sellerStatsResponse = kotlinx.serialization.json.Json.decodeFromString<SellerStatsResponse>(responseBody)
                    logInfo("STATS_API", "Estadísticas de vendedor obtenidas exitosamente")
                    Result.success(sellerStatsResponse)
                } catch (e: Exception) {
                    logError("STATS_API", "Error deserializando respuesta: ${e.message}")
                    Result.failure(Exception("Error deserializando respuesta del servidor: ${e.message}"))
                }
            } else {
                val errorMessage = try {
                    val errorBody = response.body<String>()
                    logError("STATS_API", "Error body: $errorBody")
                    errorBody
                } catch (e: Exception) {
                    "Error desconocido: ${e.message}"
                }

                val finalErrorMessage = "Error obteniendo estadísticas de vendedor: ${response.status} - $errorMessage"
                logError("STATS_API", finalErrorMessage)
                Result.failure(Exception(finalErrorMessage))
            }
        } catch (e: Exception) {
            logError("STATS_API", "Error obteniendo estadísticas de vendedor: ${e.message}")
            Result.failure(e)
        }
    }

    suspend fun getQuickSummary(
        adminId: Int,
        startDate: String? = null,
        endDate: String? = null,
        token: String
    ): Result<QuickSummaryResponse> {
        return try {
            logInfo("STATS_API", "Obteniendo resumen rápido para admin: $adminId")

            val response = client.get("$baseUrl/api/stats/quick-summary") {
                parameter("adminId", adminId)
                startDate?.let { parameter("startDate", it) }
                endDate?.let { parameter("endDate", it) }
                header("Authorization", "Bearer $token")
                header("accept", "application/json")
            }

            if (response.status.value in 200..299) {
                try {
                    // Usar deserialización manual directamente ya que Kotlin reflection no está disponible
                    val responseBody = response.body<String>()
                    logInfo("STATS_API", "Respuesta raw: $responseBody")
                    val quickSummaryResponse = Json.decodeFromString<QuickSummaryResponse>(responseBody)
                    logInfo("STATS_API", "Resumen rápido obtenido exitosamente")
                    Result.success(quickSummaryResponse)
                } catch (e: Exception) {
                    logError("STATS_API", "Error deserializando respuesta: ${e.message}")
                    Result.failure(Exception("Error deserializando respuesta del servidor: ${e.message}"))
                }
            } else {
                val errorMessage = try {
                    val errorBody = response.body<String>()
                    logError("STATS_API", "Error body: $errorBody")
                    errorBody
                } catch (e: Exception) {
                    "Error desconocido: ${e.message}"
                }

                val finalErrorMessage = "Error obteniendo resumen rápido: ${response.status} - $errorMessage"
                logError("STATS_API", finalErrorMessage)
                Result.failure(Exception(finalErrorMessage))
            }
        } catch (e: Exception) {
            logError("STATS_API", "Error obteniendo resumen rápido: ${e.message}")
            Result.failure(e)
        }
    }

    suspend fun getAnalytics(
        adminId: Int,
        startDate: String? = null,
        endDate: String? = null,
        token: String
    ): Result<AnalyticsResponse> {
        return try {
            logInfo("STATS_API", "Obteniendo analytics completos para admin: $adminId")

            val response = client.get("$baseUrl/api/stats/analytics") {
                parameter("adminId", adminId)
                startDate?.let { parameter("startDate", it) }
                endDate?.let { parameter("endDate", it) }
                header("Authorization", "Bearer $token")
                header("accept", "application/json")
            }

            if (response.status.value in 200..299) {
                try {
                    // Usar deserialización manual directamente ya que Kotlin reflection no está disponible
                    val responseBody = response.body<String>()
                    logInfo("STATS_API", "Respuesta raw: $responseBody")
                    val analyticsResponse = Json.decodeFromString<AnalyticsResponse>(responseBody)
                    logInfo("STATS_API", "Analytics obtenidos exitosamente")
                    Result.success(analyticsResponse)
                } catch (e: Exception) {
                    logError("STATS_API", "Error deserializando respuesta: ${e.message}")
                    Result.failure(Exception("Error deserializando respuesta del servidor: ${e.message}"))
                }
            } else {
                val errorMessage = try {
                    val errorBody = response.body<String>()
                    logError("STATS_API", "Error body: $errorBody")
                    errorBody
                } catch (e: Exception) {
                    "Error desconocido: ${e.message}"
                }

                val finalErrorMessage = "Error obteniendo analytics: ${response.status} - $errorMessage"
                logError("STATS_API", finalErrorMessage)
                Result.failure(Exception(finalErrorMessage))
            }
        } catch (e: Exception) {
            logError("STATS_API", "Error obteniendo analytics: ${e.message}")
            Result.failure(e)
        }
    }

    suspend fun getSellerAnalytics(
        sellerId: Int,
        startDate: String? = null,
        endDate: String? = null,
        token: String
    ): Result<AnalyticsResponse> {
        return try {
            logInfo("STATS_API", "Obteniendo analytics completos para vendedor: $sellerId")

            val response = client.get("$baseUrl/api/stats/seller/analytics") {
                parameter("sellerId", sellerId)
                startDate?.let { parameter("startDate", it) }
                endDate?.let { parameter("endDate", it) }
                header("Authorization", "Bearer $token")
                header("accept", "application/json")
            }

            if (response.status.value in 200..299) {
                try {
                    // Usar deserialización manual directamente ya que Kotlin reflection no está disponible
                    val responseBody = response.body<String>()
                    logInfo("STATS_API", "Respuesta raw: $responseBody")
                    val analyticsResponse = Json.decodeFromString<AnalyticsResponse>(responseBody)
                    logInfo("STATS_API", "Analytics de vendedor obtenidos exitosamente")
                    Result.success(analyticsResponse)
                } catch (e: Exception) {
                    logError("STATS_API", "Error deserializando respuesta: ${e.message}")
                    Result.failure(Exception("Error deserializando respuesta del servidor: ${e.message}"))
                }
            } else {
                val errorMessage = try {
                    val errorBody = response.body<String>()
                    logError("STATS_API", "Error body: $errorBody")
                    errorBody
                } catch (e: Exception) {
                    "Error desconocido: ${e.message}"
                }

                val finalErrorMessage = "Error obteniendo analytics de vendedor: ${response.status} - $errorMessage"
                logError("STATS_API", finalErrorMessage)
                Result.failure(Exception(finalErrorMessage))
            }
        } catch (e: Exception) {
            logError("STATS_API", "Error obteniendo analytics de vendedor: ${e.message}")
            Result.failure(e)
        }
    }
}
