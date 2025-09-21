package org.sysarp.project.service.http.billing

import io.ktor.client.call.*
import io.ktor.client.request.*
import kotlinx.serialization.json.Json
import org.sysarp.project.data.*
import org.sysarp.project.service.http.BaseApiClient

class BillingApiClient : BaseApiClient() {
    
    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
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
    suspend fun getBillingDashboard(adminId: Int, token: String): Result<BillingDashboard> {
        return try {
            
            val response = client.get("$baseUrl/api/billing") {
                parameter("type", "dashboard")
                parameter("adminId", adminId)
                parameter("period", "monthly")
                parameter("include", "forecast")
                header("Authorization", "Bearer $token")
            }
            
            val responseBody = response.body<String>()
            
            // Intentar parsear como respuesta flexible primero
            try {
                val flexibleResponse = json.decodeFromString<FlexibleBillingResponse<BillingDashboard>>(responseBody)
                
                if (flexibleResponse.success == true && flexibleResponse.data != null) {
                    Result.success(flexibleResponse.data)
                } else {
                    Result.failure(Exception(flexibleResponse.message))
                }
            } catch (e: Exception) {
                // Si falla, intentar como respuesta estándar
                try {
                    val dashboardResponse = json.decodeFromString<BillingDashboardResponse>(responseBody)
                    if (dashboardResponse.success) {
                        Result.success(dashboardResponse.data)
                    } else {
                        Result.failure(Exception(dashboardResponse.message))
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
    
    /**
     * Comprar tokens directamente (genera ingreso inmediato)
     * API: POST /api/billing/operations?action=purchase&adminId={adminId}
     */
    suspend fun purchaseTokens(adminId: Int, token: String, tokensPackage: String): Result<Boolean> {
        return try {
            
            val response = client.post("$baseUrl/api/billing/operations") {
                parameter("adminId", adminId)
                parameter("action", "purchase")
                header("Authorization", "Bearer $token")
                header("Content-Type", "application/json")
                setBody(mapOf("tokensPackage" to tokensPackage))
            }
            
            val responseBody = response.body<String>()
            
            val purchaseResponse = json.decodeFromString<BillingResponse<Any>>(responseBody)
            
            if (purchaseResponse.success) {
                Result.success(true)
            } else {
                Result.failure(Exception(purchaseResponse.message))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
}
