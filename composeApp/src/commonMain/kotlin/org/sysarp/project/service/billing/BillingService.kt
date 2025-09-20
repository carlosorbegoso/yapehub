package org.sysarp.project.service.billing

import org.sysarp.project.data.*
import org.sysarp.project.service.auth.AuthService
import org.sysarp.project.service.http.billing.BillingApiClient
import org.sysarp.project.utils.Logger

class BillingService(
    private val billingApiClient: BillingApiClient,
    private val authService: AuthService
) {
    
    // =============================================================================
    // MÉTODOS DE CONVENIENCIA PARA SUSCRIPCIONES
    // =============================================================================
    
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
    suspend fun getCurrentTokenStatus(): Result<TokenStatus> {
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
    
    // =============================================================================
    // MÉTODOS DE PAGO
    // =============================================================================
    
    /**
     * Genera un código de pago para suscribirse a un plan
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
    
    // =============================================================================
    // MÉTODOS DE VERIFICACIÓN Y LÍMITES
    // =============================================================================
    
    
    // =============================================================================
    // MÉTODOS DE CONVENIENCIA PARA PLANES ESPECÍFICOS
    // =============================================================================
    
    /**
     * Suscribirse al plan básico
     */
    suspend fun subscribeToBasicPlan(): Result<PaymentCode> {
        return generateSubscriptionPayment(2) // Plan Básico
    }
    
    /**
     * Suscribirse al plan profesional
     */
    suspend fun subscribeToProfessionalPlan(): Result<PaymentCode> {
        return generateSubscriptionPayment(3) // Plan Profesional
    }
    
    /**
     * Suscribirse al plan empresarial
     */
    suspend fun subscribeToEnterprisePlan(): Result<PaymentCode> {
        return generateSubscriptionPayment(4) // Plan Empresarial
    }
    
    // =============================================================================
    // MÉTODOS DE CONVENIENCIA PARA COMPRA DE TOKENS
    // =============================================================================
    
    /**
     * Comprar paquete de 100 tokens
     */
    suspend fun purchase100Tokens(): Result<PaymentCode> {
        return generateTokenPurchasePayment("100")
    }
    
    /**
     * Comprar paquete de 500 tokens
     */
    suspend fun purchase500Tokens(): Result<PaymentCode> {
        return generateTokenPurchasePayment("500")
    }
    
    /**
     * Comprar paquete de 1000 tokens
     */
    suspend fun purchase1000Tokens(): Result<PaymentCode> {
        return generateTokenPurchasePayment("1000")
    }
    
    /**
     * Comprar paquete de 5000 tokens
     */
    suspend fun purchase5000Tokens(): Result<PaymentCode> {
        return generateTokenPurchasePayment("5000")
    }
    
    // =============================================================================
    // MÉTODOS DE UTILIDAD
    // =============================================================================
    
    /**
     * Verifica si el usuario tiene tokens suficientes para una operación
     */
    suspend fun hasEnoughTokens(requiredTokens: Int = 1): Result<Boolean> {
        return try {
            val tokenStatus = getCurrentTokenStatus().getOrThrow()
            Result.success(tokenStatus.remainingTokens >= requiredTokens)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Obtiene el precio de un paquete de tokens
     */
    fun getTokenPackagePrice(tokensPackage: String): Double? {
        return BillingConfigs.TOKEN_PACKAGES[tokensPackage]
    }
    
    /**
     * Obtiene todos los paquetes de tokens disponibles desde la API
     */
    suspend fun getAvailableTokenPackages(): Result<List<TokenPackage>> {
        return try {
            val userProfile = authService.userProfile.value
            val accessToken = authService.accessToken.value
            
            if (userProfile?.adminId == null || accessToken == null) {
                Logger.auth("BILLING_SERVICE", "❌ Perfil de usuario o token de autenticación no disponible")
                return Result.failure(Exception("Perfil de usuario o token de autenticación no disponible"))
            }
            
            billingApiClient.getTokenPackages(userProfile.adminId.toInt(), accessToken)
        } catch (e: Exception) {
            Logger.auth("BILLING_SERVICE", "❌ Error obteniendo paquetes de tokens: ${e.message}")
            Result.failure(e)
        }
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
     * Obtiene el número de Yape para pagos
     */
    fun getYapeNumber(): String {
        return BillingConfigs.YAPE_NUMBER
    }
    
    /**
     * Verifica si el usuario está en plan gratuito
     */
    suspend fun isOnFreePlan(): Result<Boolean> {
        return try {
            val subscription = getCurrentSubscription().getOrThrow()
            Result.success(subscription.status == "free")
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Obtiene el plan recomendado basado en el uso actual
     */
    suspend fun getRecommendedPlan(): Result<SubscriptionPlan?> {
        return try {
            val tokenStatus = getCurrentTokenStatus().getOrThrow()
            
            // Lógica simple de recomendación basada en uso de tokens
            val recommendedPlan = when {
                tokenStatus.usedTokens > 2000 -> BillingConfigs.AVAILABLE_PLANS.find { it.id == 4 } // Empresarial
                tokenStatus.usedTokens > 500 -> BillingConfigs.AVAILABLE_PLANS.find { it.id == 3 } // Profesional
                tokenStatus.usedTokens > 100 -> BillingConfigs.AVAILABLE_PLANS.find { it.id == 2 } // Básico
                else -> null // Mantener plan gratuito
            }
            
            Result.success(recommendedPlan)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Sube una imagen de comprobante de pago en formato base64
     */
    suspend fun uploadPaymentProofBase64(
        paymentCode: String, 
        imageBase64: String, 
        notes: String? = null
    ): Result<Boolean> {
        val userProfile = authService.userProfile.value
        val accessToken = authService.accessToken.value
        
        if (userProfile?.adminId == null || accessToken == null) {
            return Result.failure(Exception("Usuario no autenticado"))
        }
        
        val request = PaymentUploadBase64Request(
            imageBase64 = imageBase64,
            notes = notes
        )
        
        return billingApiClient.uploadPaymentProofBase64(userProfile.adminId.toInt(), accessToken, paymentCode, request)
    }
    
    
    /**
     * Verifica el estado de un pago específico
     */
    suspend fun getPaymentStatus(paymentCode: String): Result<PaymentStatus> {
        val accessToken = authService.accessToken.value
        
        if (accessToken == null) {
            return Result.failure(Exception("Usuario no autenticado"))
        }
        
        return billingApiClient.getPaymentStatus(paymentCode, accessToken)
    }
    
}
