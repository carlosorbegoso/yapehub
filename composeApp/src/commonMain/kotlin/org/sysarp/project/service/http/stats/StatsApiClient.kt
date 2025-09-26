package org.sysarp.project.service.http

import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.parameter
import kotlinx.serialization.json.Json
import org.sysarp.project.data.AdminStatsResponse
import org.sysarp.project.data.AnalyticsParams
import org.sysarp.project.data.AnalyticsResponse
import org.sysarp.project.data.FinancialAnalysisParams
import org.sysarp.project.data.FinancialAnalysisResponse
import org.sysarp.project.data.PaymentTransparencyParams
import org.sysarp.project.data.PaymentTransparencyResponse
import org.sysarp.project.data.QuickSummaryResponse
import org.sysarp.project.data.SellerFinancialAnalysisParams
import org.sysarp.project.data.SellerFinancialAnalysisResponse
import org.sysarp.project.data.SellerStatsResponse

/**
 * Cliente HTTP para estadísticas
 */
class StatsApiClient : BaseApiClient() {

    suspend fun getAdminStatsSummary(
        adminId: Int,
        startDate: String? = null,
        endDate: String? = null,
        token: String
    ): Result<AdminStatsResponse> {
        return try {
            logInfo("STATS_API", "Obteniendo resumen de estadísticas del admin: $adminId")

            val response = client.get("$baseUrl/api/stats/admin/summary") {
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
        startDate: String? = null,
        endDate: String? = null,
        token: String
    ): Result<SellerStatsResponse> {
        return try {
            logInfo("STATS_API", "Obteniendo resumen de estadísticas del vendedor: $sellerId")

            val response = client.get("$baseUrl/api/stats/seller/summary") {
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
    suspend fun getAdminDashboard(
        adminId: Int,
        startDate: String? = null,
        endDate: String? = null,
        token: String
    ): Result<QuickSummaryResponse> {
        return try {
            logInfo("STATS_API", "Obteniendo dashboard para admin: $adminId")
            logInfo("STATS_API", "Dashboard Params: adminId=$adminId, startDate=$startDate, endDate=$endDate")

            val response = client.get("$baseUrl/api/stats/admin/dashboard") {
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
                    val dashboardResponse = Json.decodeFromString<QuickSummaryResponse>(responseBody)
                    logInfo("STATS_API", "Dashboard de admin obtenido exitosamente")
                    Result.success(dashboardResponse)
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

                val finalErrorMessage = "Error obteniendo dashboard de admin: ${response.status} - $errorMessage"
                logError("STATS_API", finalErrorMessage)
                Result.failure(Exception(finalErrorMessage))
            }
        } catch (e: Exception) {
            logError("STATS_API", "Error obteniendo dashboard de admin: ${e.message}")
            Result.failure(e)
        }
    }

    suspend fun getAnalytics(
        adminId: Int,
        startDate: String? = null,
        endDate: String? = null,
        include: String? = null, // "trends,forecast" o "trends" o "forecast"
        period: String? = null, // "daily", "weekly", "monthly", "yearly"
        metric: String? = null, // "sales", "transactions", "performance", "all"
        confidence: Double? = null, // 0.90, 0.95, 0.99
        days: Int? = null, // días para forecast
        token: String
    ): Result<AnalyticsResponse> {
        return try {
            logInfo("STATS_API", "Obteniendo analytics completos para admin: $adminId con parámetros avanzados")

            val response = client.get("$baseUrl/api/stats/admin/analytics") {
                parameter("adminId", adminId)
                startDate?.let { parameter("startDate", it) }
                endDate?.let { parameter("endDate", it) }
                include?.let { parameter("include", it) }
                period?.let { parameter("period", it) }
                metric?.let { parameter("metric", it) }
                confidence?.let { parameter("confidence", it) }
                days?.let { parameter("days", it) }
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
        include: String? = null, // "trends,forecast" o "trends" o "forecast"
        period: String? = null, // "daily", "weekly", "monthly", "yearly"
        metric: String? = null, // "sales", "transactions", "performance", "all"
        confidence: Double? = null, // 0.90, 0.95, 0.99
        days: Int? = null, // días para forecast
        token: String
    ): Result<AnalyticsResponse> {
        return try {
            logInfo("STATS_API", "Obteniendo analytics completos para vendedor: $sellerId con parámetros avanzados")

            val response = client.get("$baseUrl/api/stats/seller/analytics") {
                parameter("sellerId", sellerId)
                startDate?.let { parameter("startDate", it) }
                endDate?.let { parameter("endDate", it) }
                include?.let { parameter("include", it) }
                period?.let { parameter("period", it) }
                metric?.let { parameter("metric", it) }
                confidence?.let { parameter("confidence", it) }
                days?.let { parameter("days", it) }
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

    suspend fun getAnalytics(
        adminId: Int,
        startDate: String? = null,
        endDate: String? = null,
        analyticsParams: AnalyticsParams,
        token: String
    ): Result<AnalyticsResponse> {
        return getAnalytics(
            adminId = adminId,
            startDate = startDate,
            endDate = endDate,
            include = analyticsParams.include,
            period = analyticsParams.period,
            metric = analyticsParams.metric,
            confidence = analyticsParams.confidence,
            days = analyticsParams.days,
            token = token
        )
    }

    suspend fun getSellerAnalytics(
        sellerId: Int,
        startDate: String? = null,
        endDate: String? = null,
        analyticsParams: AnalyticsParams,
        token: String
    ): Result<AnalyticsResponse> {
        return getSellerAnalytics(
            sellerId = sellerId,
            startDate = startDate,
            endDate = endDate,
            include = analyticsParams.include,
            period = analyticsParams.period,
            metric = analyticsParams.metric,
            confidence = analyticsParams.confidence,
            days = analyticsParams.days,
            token = token
        )
    }

    // API de Análisis Financiero
    suspend fun getFinancialAnalysis(
        adminId: Int,
        startDate: String? = null,
        endDate: String? = null,
        include: String? = null,
        currency: String? = null,
        taxRate: Double? = null,
        token: String
    ): Result<FinancialAnalysisResponse> {
        return try {
            logInfo("STATS_API", "Obteniendo análisis financiero para admin: $adminId")

            val response = client.get("$baseUrl/api/stats/admin/financial") {
                parameter("adminId", adminId)
                startDate?.let { parameter("startDate", it) }
                endDate?.let { parameter("endDate", it) }
                include?.let { parameter("include", it) }
                currency?.let { parameter("currency", it) }
                taxRate?.let { parameter("taxRate", it) }
                header("Authorization", "Bearer $token")
                header("accept", "application/json")
            }

            if (response.status.value in 200..299) {
                try {
                    val responseBody = response.body<String>()
                    val json = Json { ignoreUnknownKeys = true }
                    val financialData = json.decodeFromString<FinancialAnalysisResponse>(responseBody)
                    logInfo("STATS_API", "Análisis financiero obtenido exitosamente")
                    Result.success(financialData)
                } catch (e: Exception) {
                    logError("STATS_API", "Error deserializando análisis financiero: ${e.message}")
                    Result.failure(Exception("Error procesando respuesta del servidor: ${e.message}"))
                }
            } else {
                val errorBody = response.body<String>()
                logError("STATS_API", "Error HTTP ${response.status.value}: $errorBody")
                Result.failure(Exception("Error del servidor: ${response.status.value}"))
            }
        } catch (e: Exception) {
            logError("STATS_API", "Error obteniendo análisis financiero: ${e.message}")
            Result.failure(e)
        }
    }

    // API de Transparencia de Pagos
    suspend fun getPaymentTransparency(
        adminId: Int,
        startDate: String? = null,
        endDate: String? = null,
        includeFees: Boolean? = null,
        includeTaxes: Boolean? = null,
        includeCommissions: Boolean? = null,
        token: String
    ): Result<PaymentTransparencyResponse> {
        return try {
            logInfo("STATS_API", "Obteniendo transparencia de pagos para admin: $adminId")

            val response = client.get("$baseUrl/api/stats/admin/payment-transparency") {
                parameter("adminId", adminId)
                startDate?.let { parameter("startDate", it) }
                endDate?.let { parameter("endDate", it) }
                includeFees?.let { parameter("includeFees", it) }
                includeTaxes?.let { parameter("includeTaxes", it) }
                includeCommissions?.let { parameter("includeCommissions", it) }
                header("Authorization", "Bearer $token")
                header("accept", "application/json")
            }

            if (response.status.value in 200..299) {
                try {
                    val responseBody = response.body<String>()
                    val json = Json { ignoreUnknownKeys = true }
                    val transparencyData = json.decodeFromString<PaymentTransparencyResponse>(responseBody)
                    logInfo("STATS_API", "Transparencia de pagos obtenida exitosamente")
                    Result.success(transparencyData)
                } catch (e: Exception) {
                    logError("STATS_API", "Error deserializando transparencia de pagos: ${e.message}")
                    Result.failure(Exception("Error procesando respuesta del servidor: ${e.message}"))
                }
            } else {
                val errorBody = response.body<String>()
                logError("STATS_API", "Error HTTP ${response.status.value}: $errorBody")
                Result.failure(Exception("Error del servidor: ${response.status.value}"))
            }
        } catch (e: Exception) {
            logError("STATS_API", "Error obteniendo transparencia de pagos: ${e.message}")
            Result.failure(e)
        }
    }

    suspend fun getFinancialAnalysis(
        adminId: Int,
        startDate: String? = null,
        endDate: String? = null,
        financialParams: FinancialAnalysisParams,
        token: String
    ): Result<FinancialAnalysisResponse> {
        return getFinancialAnalysis(
            adminId = adminId,
            startDate = startDate,
            endDate = endDate,
            include = financialParams.include,
            currency = financialParams.currency,
            taxRate = financialParams.taxRate,
            token = token
        )
    }

    suspend fun getPaymentTransparency(
        adminId: Int,
        startDate: String? = null,
        endDate: String? = null,
        transparencyParams: PaymentTransparencyParams,
        token: String
    ): Result<PaymentTransparencyResponse> {
        return getPaymentTransparency(
            adminId = adminId,
            startDate = startDate,
            endDate = endDate,
            includeFees = transparencyParams.includeFees,
            includeTaxes = transparencyParams.includeTaxes,
            includeCommissions = transparencyParams.includeCommissions,
            token = token
        )
    }

    // API de Análisis Financiero de Vendedores
    suspend fun getSellerFinancialAnalysis(
        sellerId: Int,
        startDate: String? = null,
        endDate: String? = null,
        include: String? = null,
        currency: String? = null,
        commissionRate: Double? = null,
        token: String
    ): Result<SellerFinancialAnalysisResponse> {
        return try {
            logInfo("STATS_API", "Obteniendo análisis financiero para vendedor: $sellerId")

            val response = client.get("$baseUrl/api/stats/seller/financial") {
                parameter("sellerId", sellerId)
                startDate?.let { parameter("startDate", it) }
                endDate?.let { parameter("endDate", it) }
                include?.let { parameter("include", it) }
                currency?.let { parameter("currency", it) }
                commissionRate?.let { parameter("commissionRate", it) }
                header("Authorization", "Bearer $token")
                header("accept", "application/json")
            }

            if (response.status.value in 200..299) {
                try {
                    val responseBody = response.body<String>()
                    val json = Json { ignoreUnknownKeys = true }
                    val financialData = json.decodeFromString<SellerFinancialAnalysisResponse>(responseBody)
                    logInfo("STATS_API", "Análisis financiero de vendedor obtenido exitosamente")
                    Result.success(financialData)
                } catch (e: Exception) {
                    logError("STATS_API", "Error deserializando análisis financiero de vendedor: ${e.message}")
                    Result.failure(Exception("Error procesando respuesta del servidor: ${e.message}"))
                }
            } else {
                val errorBody = response.body<String>()
                logError("STATS_API", "Error HTTP ${response.status.value}: $errorBody")
                Result.failure(Exception("Error del servidor: ${response.status.value}"))
            }
        } catch (e: Exception) {
            logError("STATS_API", "Error obteniendo análisis financiero de vendedor: ${e.message}")
            Result.failure(e)
        }
    }

    suspend fun getSellerFinancialAnalysis(
        sellerId: Int,
        startDate: String? = null,
        endDate: String? = null,
        sellerFinancialParams: SellerFinancialAnalysisParams,
        token: String
    ): Result<SellerFinancialAnalysisResponse> {
        return getSellerFinancialAnalysis(
            sellerId = sellerId,
            startDate = startDate,
            endDate = endDate,
            include = sellerFinancialParams.include,
            currency = sellerFinancialParams.currency,
            commissionRate = sellerFinancialParams.commissionRate,
            token = token
        )
    }
}
