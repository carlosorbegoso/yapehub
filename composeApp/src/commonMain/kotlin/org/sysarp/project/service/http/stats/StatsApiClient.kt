package org.sysarp.project.service.http

import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.parameter
import kotlinx.serialization.json.Json
import org.sysarp.project.data.UnifiedStatsResponse
import org.sysarp.project.utils.Constants

/**
 * Cliente HTTP para estadísticas - Solo endpoint unificado
 * Backend ha deprecado todas las APIs antiguas, solo queda /api/stats/summary
 */
class StatsApiClient : BaseApiClient() {

    /**
     * Obtiene estadísticas unificadas para admin o seller
     * Único endpoint disponible después de la deprecación del backend
     */
    suspend fun getUnifiedStatsSummary(
        adminId: Int? = null,
        sellerId: Int? = null,
        startDate: String? = null,
        endDate: String? = null,
        token: String
    ): Result<UnifiedStatsResponse> {
        return try {
            val role = if (adminId != null) "ADMIN" else "SELLER"
            val id = adminId ?: sellerId ?: 0
            // logInfo("STATS_API", "Obteniendo estadísticas unificadas para $role: $id")

            val response = client.get("$baseUrl/api/stats/summary") {
                adminId?.let { parameter("adminId", it) }
                sellerId?.let { parameter("sellerId", it) }
                startDate?.let { parameter("startDate", it) }
                endDate?.let { parameter("endDate", it) }
                header("Authorization", "Bearer $token")
                header("accept", "application/json")
            }

            if (response.status.value in 200..299) {
                try {
                    val responseBody = response.body<String>()
                    // logInfo("STATS_API", "Respuesta del servidor: $responseBody")
                    val unifiedStatsResponse = kotlinx.serialization.json.Json.decodeFromString<UnifiedStatsResponse>(responseBody)
                    // logInfo("STATS_API", "Estadísticas unificadas obtenidas exitosamente para $role: $id")
                    Result.success(unifiedStatsResponse)
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

                val finalErrorMessage = "Error obteniendo estadísticas unificadas para $role: ${response.status} - $errorMessage"
                logError("STATS_API", finalErrorMessage)
                Result.failure(Exception(finalErrorMessage))
            }
        } catch (e: Exception) {
            logError("STATS_API", "Error obteniendo estadísticas unificadas: ${e.message}")
            Result.failure(e)
        }
    }
    
    /**
     * Obtiene datos de analytics específicos usando las URLs proporcionadas por el endpoint unificado
     */
    suspend fun getAnalyticsFromUrl(
        url: String,
        token: String
    ): Result<String> {
        return try {
            // Construir URL completa basada en si es relativa o absoluta
            val processedUrl = when {
                url.startsWith("http://") || url.startsWith("https://") -> {
                    // URL absoluta - reemplazar localhost con la configuración base
                    url.replace("http://localhost:8080", Constants.BASE_URL)
                        .replace("https://localhost:8080", Constants.BASE_URL)
                        .replace("http://192.168.1.100:8080", Constants.BASE_URL)
                        .replace("https://192.168.1.100:8080", Constants.BASE_URL)
                        .replace("http://10.0.2.2:8080", Constants.BASE_URL)
                        .replace("https://10.0.2.2:8080", Constants.BASE_URL)
                }
                url.startsWith("/") -> {
                    // URL relativa - agregar base URL
                    "${Constants.BASE_URL}$url"
                }
                else -> {
                    // URL sin slash inicial - agregar base URL y slash
                    "${Constants.BASE_URL}/$url"
                }
            }
            // logInfo("STATS_API", "Obteniendo analytics desde URL original: $url")
            // logInfo("STATS_API", "URL procesada: $processedUrl")
            
            val response = client.get(processedUrl) {
                header("Authorization", "Bearer $token")
                header("accept", "application/json")
            }

            if (response.status.value in 200..299) {
                val responseBody = response.body<String>()
                // logInfo("STATS_API", "Analytics obtenidos exitosamente desde: $url")
                Result.success(responseBody)
            } else {
                val errorMessage = try {
                    val errorBody = response.body<String>()
                    logError("STATS_API", "Error body: $errorBody")
                    errorBody
                } catch (e: Exception) {
                    "Error desconocido: ${e.message}"
                }

                val finalErrorMessage = "Error obteniendo analytics desde $url: ${response.status} - $errorMessage"
                logError("STATS_API", finalErrorMessage)
                Result.failure(Exception(finalErrorMessage))
            }
        } catch (e: Exception) {
            logError("STATS_API", "Error obteniendo analytics desde $url: ${e.message}")
            Result.failure(e)
        }
    }
}