package org.sysarp.project.service.http.billing

import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.parameter
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.decodeFromJsonElement
import org.sysarp.project.data.BillingDashboard
import org.sysarp.project.data.BillingDashboardResponse
import org.sysarp.project.data.BillingResponse
import org.sysarp.project.data.FlexibleBillingResponse
import org.sysarp.project.data.GeneratePaymentRequest
import org.sysarp.project.data.PaymentCode
import org.sysarp.project.data.PaymentCodeResponse
import org.sysarp.project.data.PaymentStatus
import org.sysarp.project.data.PaymentStatusResponse
import org.sysarp.project.data.PaymentUploadBase64Request
import org.sysarp.project.data.PaymentUploadRequest
import org.sysarp.project.data.SubscriptionPlan
import org.sysarp.project.data.SubscriptionResponse
import org.sysarp.project.data.SubscriptionStatus
import org.sysarp.project.data.TokenPackage
import org.sysarp.project.data.TokenStatusResponse
import org.sysarp.project.service.http.BaseApiClient

class BillingApiClient : BaseApiClient() {
    
    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
        coerceInputValues = true
        encodeDefaults = true
        allowStructuredMapKeys = true
        explicitNulls = false
    }
    
    /**
     * Función auxiliar para limpiar la respuesta JSON
     */
    private fun cleanJsonResponse(responseBody: String): String {
        return responseBody
            .trim()
            .replace(Regex("\\r\\n|\\r|\\n"), " ") // Reemplazar saltos de línea
            .replace(Regex("\\s+"), " ") // Normalizar espacios
            .replace(Regex("[\\x00-\\x1F\\x7F]"), "") // Remover caracteres de control
    }
    
    /**
     * Función auxiliar para parsear respuestas de facturación de manera robusta
     */
    private inline fun <reified T> parseBillingResponse(responseBody: String): Result<T> {
        val cleanedResponse = cleanJsonResponse(responseBody)
        return try {
            // Intentar parsear como respuesta flexible primero
            val flexibleResponse = json.decodeFromString<FlexibleBillingResponse<T>>(cleanedResponse)
            
            if (flexibleResponse.success == true && flexibleResponse.data != null) {
                Result.success(flexibleResponse.data)
            } else {
                Result.failure(Exception(flexibleResponse.message))
            }
        } catch (e: Exception) {
            println("BILLING_API: Error parsing flexible response: ${e.message}")
            // Si falla, intentar como respuesta estándar
            try {
                val standardResponse = json.decodeFromString<BillingResponse<T>>(cleanedResponse)
                if (standardResponse.success) {
                    Result.success(standardResponse.data)
                } else {
                    Result.failure(Exception(standardResponse.message))
                }
            } catch (e2: Exception) {
                println("BILLING_API: Error parsing standard response: ${e2.message}")
                // Intentar parsear directamente como el tipo T
                try {
                    val directResponse = json.decodeFromString<T>(cleanedResponse)
                    Result.success(directResponse)
                } catch (e3: Exception) {
                    println("BILLING_API: Error parsing direct response: ${e3.message}")
                    // Intentar con la respuesta original como último recurso
                    try {
                        val originalResponse = json.decodeFromString<T>(responseBody)
                        Result.success(originalResponse)
                    } catch (e4: Exception) {
                        println("BILLING_API: Error parsing original response: ${e4.message}")
                        Result.failure(Exception("Error parsing response at offset 96: ${e4.message}. Original: $responseBody"))
                    }
                }
            }
        }
    }
    
    // =============================================================================
    // APIs PÚBLICAS PARA USUARIOS
    // =============================================================================
    
    /**
     * Obtiene el estado de tokens del administrador
     */
    suspend fun getTokenStatus(adminId: Int, token: String): Result<TokenStatusResponse> {
        return try {
            
            val response = client.get("$baseUrl/api/billing") {
                parameter("type", "tokens")
                parameter("adminId", adminId)
                header("Authorization", "Bearer $token")
            }
            
            val responseBody = response.body<String>()
            
            // Intentar parsear como respuesta flexible primero
            try {
                val flexibleResponse = json.decodeFromString<FlexibleBillingResponse<TokenStatusResponse>>(responseBody)
                
                if (flexibleResponse.success == true && flexibleResponse.data != null) {
                    Result.success(flexibleResponse.data)
                } else {
                    Result.failure(Exception(flexibleResponse.message))
                }
            } catch (e: Exception) {
                // Si falla, intentar como respuesta estándar
                try {
                    val billingResponse = json.decodeFromString<BillingResponse<TokenStatusResponse>>(responseBody)
                    if (billingResponse.success) {
                        Result.success(billingResponse.data)
                    } else {
                        Result.failure(Exception(billingResponse.message))
                    }
                } catch (e2: Exception) {
                    Result.failure(Exception("Error parsing response: ${e.message}"))
                }
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Obtiene el estado de suscripción del administrador
     */
    suspend fun getSubscriptionStatus(adminId: Int, token: String): Result<SubscriptionStatus> {
        return try {
            
            val response = client.get("$baseUrl/api/billing") {
                parameter("type", "subscription")
                parameter("adminId", adminId)
                header("Authorization", "Bearer $token")
            }
            
            val responseBody = response.body<String>()
            
            // Intentar parsear como respuesta flexible primero
            try {
                val flexibleResponse = json.decodeFromString<FlexibleBillingResponse<SubscriptionStatus>>(responseBody)
                
                if (flexibleResponse.success == true && flexibleResponse.data != null) {
                    Result.success(flexibleResponse.data)
                } else {
                    Result.failure(Exception(flexibleResponse.message))
                }
            } catch (e: Exception) {
                // Si falla, intentar como respuesta estándar
                try {
                    val subscriptionResponse = json.decodeFromString<SubscriptionResponse>(responseBody)
                    if (subscriptionResponse.success) {
                        Result.success(subscriptionResponse.data)
                    } else {
                        Result.failure(Exception(subscriptionResponse.message))
                    }
                } catch (e2: Exception) {
                    Result.failure(Exception("Error parsing response: ${e.message}"))
                }
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Obtiene el dashboard completo de facturación
     */
    suspend fun getBillingDashboard(
        adminId: Int, 
        startDate: String? = null,
        endDate: String? = null,
        token: String
    ): Result<BillingDashboard> {
        return try {
            
            val response = client.get("$baseUrl/api/billing") {
                parameter("type", "dashboard")
                parameter("adminId", adminId)
                parameter("period", "monthly")
                parameter("include", "forecast")
                startDate?.let { parameter("startDate", it) }
                endDate?.let { parameter("endDate", it) }
                header("Authorization", "Bearer $token")
            }
            
            val responseBody = response.body<String>()
            
            // Log de la respuesta para debugging
            println("BILLING_API: Respuesta del servidor: $responseBody")
            
            // Parsear directamente con kotlinx.serialization
            try {
                val billingResponse = json.decodeFromString<BillingDashboardResponse>(responseBody)
                if (billingResponse.success) {
                    Result.success(billingResponse.data)
                } else {
                    Result.failure(Exception(billingResponse.message))
                }
            } catch (e: Exception) {
                println("BILLING_API: Error en parsing: ${e.message}")
                Result.failure(e)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Obtiene los paquetes de tokens disponibles
     */
    suspend fun getTokenPackages(adminId: Int, token: String): Result<List<TokenPackage>> {
        return try {
            
            val response = client.get("$baseUrl/api/billing") {
                parameter("type", "token-packages")
                parameter("adminId", adminId.toString())
                parameter("include", "details")
                header("Authorization", "Bearer $token")
            }
            
            val responseBody = response.body<String>()
            
            // Intentar parsear como respuesta flexible primero
            try {
                val flexibleResponse = json.decodeFromString<FlexibleBillingResponse<List<TokenPackage>>>(responseBody)
                
                if (flexibleResponse.success == true && flexibleResponse.data != null) {
                    Result.success(flexibleResponse.data)
                } else {
                    Result.failure(Exception(flexibleResponse.message))
                }
            } catch (e: Exception) {
                // Si falla, intentar como respuesta estándar
                try {
                    val packagesResponse = json.decodeFromString<BillingResponse<List<TokenPackage>>>(responseBody)
                    if (packagesResponse.success) {
                        Result.success(packagesResponse.data)
                    } else {
                        Result.failure(Exception(packagesResponse.message))
                    }
                } catch (e2: Exception) {
                    Result.failure(Exception("Error parsing response: ${e.message}"))
                }
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Obtiene los planes disponibles
     */
    suspend fun getAvailablePlans(token: String): Result<List<SubscriptionPlan>> {
        return try {
            
            val response = client.get("$baseUrl/api/billing") {
                parameter("type", "plans")
                parameter("include", "details")
                header("Authorization", "Bearer $token")
            }
            
            val responseBody = response.body<String>()
            
            // Intentar parsear como respuesta flexible primero
            try {
                val flexibleResponse = json.decodeFromString<FlexibleBillingResponse<List<SubscriptionPlan>>>(responseBody)
                
                if (flexibleResponse.success == true && flexibleResponse.data != null) {
                    Result.success(flexibleResponse.data)
                } else {
                    Result.failure(Exception(flexibleResponse.message))
                }
            } catch (e: Exception) {
                // Si falla, intentar como respuesta estándar
                try {
                    val plansResponse = json.decodeFromString<BillingResponse<List<SubscriptionPlan>>>(responseBody)
                    if (plansResponse.success) {
                        Result.success(plansResponse.data)
                    } else {
                        Result.failure(Exception(plansResponse.message))
                    }
                } catch (e2: Exception) {
                    Result.failure(Exception("Error parsing response: ${e.message}"))
                }
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Genera un código de pago para suscripción o tokens
     */
    suspend fun generatePaymentCode(
        adminId: Int, 
        token: String, 
        request: GeneratePaymentRequest
    ): Result<PaymentCode> {
        return try {
            
            val response = client.post("$baseUrl/api/billing/operations") {
                parameter("adminId", adminId)
                parameter("action", "generate-code")
                parameter("validate", "true")
                header("Authorization", "Bearer $token")
                header("Content-Type", "application/json")
                setBody(request)
            }
            
            val responseBody = response.body<String>()
            
            val paymentResponse = json.decodeFromString<PaymentCodeResponse>(responseBody)
            
            if (paymentResponse.success) {
                Result.success(paymentResponse.data)
            } else {
                Result.failure(Exception(paymentResponse.message))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Sube una imagen de comprobante de pago
     */
    suspend fun uploadPaymentProof(
        adminId: Int, 
        token: String, 
        request: PaymentUploadRequest
    ): Result<Boolean> {
        return try {
            
            val response = client.post("$baseUrl/api/billing/payments/upload") {
                parameter("adminId", adminId)
                parameter("paymentCode", request.paymentCode)
                header("Authorization", "Bearer $token")
                header("Content-Type", "application/json")
                setBody(request)
            }
            
            val responseBody = response.body<String>()
            
            val uploadResponse = json.decodeFromString<BillingResponse<Any>>(responseBody)
            
            if (uploadResponse.success) {
                Result.success(true)
            } else {
                Result.failure(Exception(uploadResponse.message))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Sube una imagen de comprobante de pago en formato base64
     */
    suspend fun uploadPaymentProofBase64(
        adminId: Int, 
        token: String, 
        paymentCode: String,
        request: PaymentUploadBase64Request
    ): Result<Boolean> {
        return try {
            
            val response = client.post("$baseUrl/api/billing/payments/upload") {
                parameter("adminId", adminId)
                parameter("paymentCode", paymentCode)
                header("Authorization", "Bearer $token")
                header("Content-Type", "application/json")
                setBody(request)
            }
            
            val responseBody = response.body<String>()
            
            val uploadResponse = json.decodeFromString<BillingResponse<Any>>(responseBody)
            
            if (uploadResponse.success) {
                Result.success(true)
            } else {
                Result.failure(Exception(uploadResponse.message))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Verifica el estado de un pago
     */
    suspend fun getPaymentStatus(paymentCode: String, token: String): Result<PaymentStatus> {
        return try {
            
            val response = client.get("$baseUrl/api/billing/payments/status/$paymentCode") {
                header("Authorization", "Bearer $token")
            }
            
            val responseBody = response.body<String>()
            
            val statusResponse = json.decodeFromString<PaymentStatusResponse>(responseBody)
            
            if (statusResponse.success) {
                Result.success(statusResponse.data)
            } else {
                Result.failure(Exception(statusResponse.message))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    // =============================================================================
    // APIs PÚBLICAS QUE GENERAN INGRESOS DIRECTOS
    // =============================================================================
    
    /**
     * Suscribirse directamente a un plan (genera ingreso inmediato)
     * API: POST /api/billing/operations?action=subscribe&adminId={adminId}
     */
    suspend fun subscribeToPlan(adminId: Int, token: String, planId: Int): Result<Boolean> {
        return try {
            
            val response = client.post("$baseUrl/api/billing/operations") {
                parameter("adminId", adminId)
                parameter("action", "subscribe")
                header("Authorization", "Bearer $token")
                header("Content-Type", "application/json")
                setBody(mapOf("planId" to planId))
            }
            
            val responseBody = response.body<String>()
            
            val subscribeResponse = json.decodeFromString<BillingResponse<Any>>(responseBody)
            
            if (subscribeResponse.success) {
                Result.success(true)
            } else {
                Result.failure(Exception(subscribeResponse.message))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Upgrade de plan (genera ingreso por diferencia)
     * API: POST /api/billing/operations?action=upgrade&adminId={adminId}
     */
    suspend fun upgradePlan(adminId: Int, token: String, planId: Int): Result<Boolean> {
        return try {
            
            val response = client.post("$baseUrl/api/billing/operations") {
                parameter("adminId", adminId)
                parameter("action", "upgrade")
                header("Authorization", "Bearer $token")
                header("Content-Type", "application/json")
                setBody(mapOf("planId" to planId))
            }
            
            val responseBody = response.body<String>()
            
            val upgradeResponse = json.decodeFromString<BillingResponse<Any>>(responseBody)
            
            if (upgradeResponse.success) {
                Result.success(true)
            } else {
                Result.failure(Exception(upgradeResponse.message))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    
}
