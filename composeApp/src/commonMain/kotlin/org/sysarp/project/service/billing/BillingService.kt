package org.sysarp.project.service.billing

import org.sysarp.project.data.BillingConfigs
import org.sysarp.project.data.BillingDashboard
import org.sysarp.project.data.GeneratePaymentRequest
import org.sysarp.project.data.PaymentCode
import org.sysarp.project.data.PaymentStatus
import org.sysarp.project.data.PaymentUploadRequest
import org.sysarp.project.data.SubscriptionPlan
import org.sysarp.project.data.SubscriptionStatus
import org.sysarp.project.data.TokenPackage
import org.sysarp.project.data.TokenStatusResponse
import org.sysarp.project.service.auth.AuthService
import org.sysarp.project.service.http.billing.BillingApiClient
import org.sysarp.project.utils.ErrorInfo
import org.sysarp.project.utils.ErrorManager

class BillingService(
    private val billingApiClient: BillingApiClient,
    private val authService: AuthService
) {
    
    
    /**
     * Obtiene el estado actual de la suscripción del usuario autenticado
     */
    suspend fun getCurrentSubscription(): Result<SubscriptionStatus> {
        val userProfile = authService.userProfile.value
        val accessToken = authService.accessToken.value
        
        if (userProfile?.adminId == null || accessToken == null) {
            return Result.failure(Exception("Usuario no autenticado"))
        }
        
        return billingApiClient.getSubscriptionStatus(userProfile.adminId.toInt(), accessToken)
    }
    
    /**
     * Obtiene el estado de tokens del usuario autenticado
     */
    suspend fun getCurrentTokenStatus(): Result<TokenStatusResponse> {
        val userProfile = authService.userProfile.value
        val accessToken = authService.accessToken.value
        
        if (userProfile?.adminId == null || accessToken == null) {
            return Result.failure(Exception("Usuario no autenticado"))
        }
        
        return billingApiClient.getTokenStatus(userProfile.adminId.toInt(), accessToken)
    }
    
    /**
     * Obtiene el dashboard completo de facturación del usuario autenticado
     */
    suspend fun getCurrentBillingDashboard(): Result<BillingDashboard> {
        val userProfile = authService.userProfile.value
        val accessToken = authService.accessToken.value
        
        if (userProfile?.adminId == null || accessToken == null) {
            return Result.failure(Exception("Usuario no autenticado"))
        }
        
        return billingApiClient.getBillingDashboard(userProfile.adminId.toInt(), accessToken)
    }
    
    /**
     * Obtiene los planes disponibles
     */
    suspend fun getAvailablePlans(): Result<List<SubscriptionPlan>> {
        val accessToken = authService.accessToken.value
        
        if (accessToken == null) {
            return Result.failure(Exception("Usuario no autenticado"))
        }
        
        return billingApiClient.getAvailablePlans(accessToken)
    }
    
    
    /**
     * Genera un código de pago para suscribirse a un plan
     * API: POST /api/billing/operations?action=generate-code&adminId={adminId}
     */
    suspend fun generateSubscriptionPayment(planId: Int): Result<PaymentCode> {
        val userProfile = authService.userProfile.value
        val accessToken = authService.accessToken.value
        
        
        if (userProfile?.adminId == null || accessToken == null) {
            return Result.failure(Exception("Usuario no autenticado"))
        }
        
        val request = GeneratePaymentRequest(
            planId = planId,
            paymentMethod = "yape"
        )
        
        
        return billingApiClient.generatePaymentCode(userProfile.adminId.toInt(), accessToken, request)
    }
    
    /**
     * Genera un código de pago para comprar tokens adicionales
     * API: POST /api/billing/operations?action=generate-code&adminId={adminId}
     */
    suspend fun generateTokenPurchasePayment(tokensPackage: String): Result<PaymentCode> {
        val userProfile = authService.userProfile.value
        val accessToken = authService.accessToken.value
        
        if (userProfile?.adminId == null || accessToken == null) {
            return Result.failure(Exception("Usuario no autenticado"))
        }
        
        val request = GeneratePaymentRequest(
            tokensPackage = tokensPackage,
            paymentMethod = "yape"
        )
        
        return billingApiClient.generatePaymentCode(userProfile.adminId.toInt(), accessToken, request)
    }
    
    /**
     * Sube un comprobante de pago
     * API: POST /api/billing/operations?action=upload&adminId={adminId}
     */
    suspend fun uploadPaymentProof(paymentCode: String, imageUrl: String, notes: String? = null): Result<Boolean> {
        val userProfile = authService.userProfile.value
        val accessToken = authService.accessToken.value
        
        if (userProfile?.adminId == null || accessToken == null) {
            return Result.failure(Exception("Usuario no autenticado"))
        }
        
        val request = PaymentUploadRequest(
            paymentCode = paymentCode,
            imageUrl = imageUrl,
            notes = notes
        )
        
        return billingApiClient.uploadPaymentProof(userProfile.adminId.toInt(), accessToken, request)
    }
    
    /**
     * Verifica el estado de un pago
     */
    suspend fun checkPaymentStatus(paymentCode: String): Result<PaymentStatus> {
        val accessToken = authService.accessToken.value
        
        if (accessToken == null) {
            return Result.failure(Exception("Usuario no autenticado"))
        }
        
        return billingApiClient.getPaymentStatus(paymentCode, accessToken)
    }
    
    
    /**
     * Obtiene todos los paquetes de tokens disponibles desde la API
     */
    suspend fun getAvailableTokenPackages(): Result<List<TokenPackage>> {
        return try {
            val userProfile = authService.userProfile.value
            val accessToken = authService.accessToken.value
            
            if (userProfile?.adminId == null || accessToken == null) {
                return Result.failure(Exception("Perfil de usuario o token de autenticación no disponible"))
            }
            
            billingApiClient.getTokenPackages(userProfile.adminId.toInt(), accessToken)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Obtiene todos los planes disponibles (fallback local)
     */
    fun getAvailablePlansLocal(): List<SubscriptionPlan> {
        return BillingConfigs.AVAILABLE_PLANS
    }
    
    /**
     * Obtiene todos los paquetes de tokens disponibles (fallback local)
     */
    fun getAvailableTokenPackagesLocal(): List<TokenPackage> {
        return BillingConfigs.TOKEN_PACKAGES.map { (tokens, price) ->
            TokenPackage(
                id = "tokens_$tokens",
                name = when (tokens) {
                    "100" -> "Paquete Básico"
                    "500" -> "Paquete Estándar"
                    "1000" -> "Paquete Profesional"
                    "5000" -> "Paquete Empresarial"
                    else -> "Paquete de $tokens tokens"
                },
                description = when (tokens) {
                    "100" -> "100 tokens para operaciones básicas"
                    "500" -> "500 tokens para uso moderado"
                    "1000" -> "1000 tokens para uso intensivo"
                    "5000" -> "5000 tokens para uso empresarial"
                    else -> "$tokens tokens para tus operaciones"
                },
                tokens = tokens.toInt(),
                price = price,
                currency = "PEN",
                discount = when (tokens) {
                    "100" -> 0.0
                    "500" -> 0.13
                    "1000" -> 0.20
                    "5000" -> 0.27
                    else -> 0.0
                },
                isPopular = tokens == "500",
                features = when (tokens) {
                    "100" -> listOf("Procesamiento de pagos", "Generación de QR", "Reportes básicos")
                    "500" -> listOf("Procesamiento de pagos", "Generación de QR", "Reportes avanzados", "Soporte prioritario")
                    "1000" -> listOf("Procesamiento de pagos", "Generación de QR", "Reportes avanzados", "Soporte prioritario", "API avanzada")
                    "5000" -> listOf("Procesamiento de pagos", "Generación de QR", "Reportes avanzados", "Soporte prioritario", "API avanzada", "Integraciones personalizadas")
                    else -> emptyList()
                },
                discountedPrice = price * (1 - when (tokens) {
                    "100" -> 0.0
                    "500" -> 0.13
                    "1000" -> 0.20
                    "5000" -> 0.27
                    else -> 0.0
                })
            )
        }
    }
    
    
    
    
    
    
    
    /**
     * Obtiene planes disponibles con manejo elegante de errores
     */
    suspend fun getAvailablePlansWithErrorHandling(): Pair<List<SubscriptionPlan>?, ErrorInfo?> {
        return try {
            val result = getAvailablePlans()
            result.fold(
                onSuccess = { plans ->
                    Pair(plans, null)
                },
                onFailure = { exception ->
                    val errorInfo = ErrorManager.parseException(exception)
                    Pair(null, errorInfo)
                }
            )
        } catch (e: Exception) {
            val errorInfo = ErrorManager.parseException(e)
            Pair(null, errorInfo)
        }
    }
    
    
    
    
    
}
