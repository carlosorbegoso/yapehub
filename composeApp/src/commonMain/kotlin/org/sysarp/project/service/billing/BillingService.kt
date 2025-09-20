package org.sysarp.project.service.billing

import org.sysarp.project.data.*
import org.sysarp.project.service.auth.AuthService
import org.sysarp.project.service.http.billing.BillingApiClient
import org.sysarp.project.utils.Logger
import org.sysarp.project.utils.ErrorManager
import org.sysarp.project.utils.ErrorInfo

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
    
    // =============================================================================
    // MÉTODOS DE PAGO
    // =============================================================================
    
    /**
     * Genera un código de pago para suscribirse a un plan
     * API: POST /api/billing/operations?action=generate-code&adminId={adminId}
     */
    suspend fun generateSubscriptionPayment(planId: Int): Result<PaymentCode> {
        val userProfile = authService.userProfile.value
        val accessToken = authService.accessToken.value
        
        Logger.auth("BILLING_SERVICE", "💳 Generando pago de suscripción para plan ID: $planId")
        Logger.auth("BILLING_SERVICE", "👤 Admin ID: ${userProfile?.adminId}")
        Logger.auth("BILLING_SERVICE", "🔑 Token disponible: ${accessToken != null}")
        
        if (userProfile?.adminId == null || accessToken == null) {
            Logger.auth("BILLING_SERVICE", "❌ Usuario no autenticado")
            return Result.failure(Exception("Usuario no autenticado"))
        }
        
        val request = GeneratePaymentRequest(
            planId = planId,
            paymentMethod = "yape"
        )
        
        Logger.auth("BILLING_SERVICE", "📤 Request: planId=$planId, paymentMethod=yape")
        
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
    
    // =============================================================================
    // MÉTODOS DE OPERACIONES DIRECTAS (APIs PÚBLICAS QUE GENERAN INGRESOS)
    // =============================================================================
    
    /**
     * Suscribirse directamente a un plan (genera ingreso inmediato)
     * API: POST /api/billing/operations?action=subscribe&adminId={adminId}
     */
    suspend fun subscribeToPlan(planId: Int): Result<Boolean> {
        val userProfile = authService.userProfile.value
        val accessToken = authService.accessToken.value
        
        Logger.auth("BILLING_SERVICE", "🔄 Suscribiéndose directamente al plan ID: $planId")
        
        if (userProfile?.adminId == null || accessToken == null) {
            return Result.failure(Exception("Usuario no autenticado"))
        }
        
        return billingApiClient.subscribeToPlan(userProfile.adminId.toInt(), accessToken, planId)
    }
    
    /**
     * Upgrade de plan (genera ingreso por diferencia)
     * API: POST /api/billing/operations?action=upgrade&adminId={adminId}
     */
    suspend fun upgradePlan(planId: Int): Result<Boolean> {
        val userProfile = authService.userProfile.value
        val accessToken = authService.accessToken.value
        
        Logger.auth("BILLING_SERVICE", "⬆️ Upgrade al plan ID: $planId")
        
        if (userProfile?.adminId == null || accessToken == null) {
            return Result.failure(Exception("Usuario no autenticado"))
        }
        
        return billingApiClient.upgradePlan(userProfile.adminId.toInt(), accessToken, planId)
    }
    
    /**
     * Comprar tokens directamente (genera ingreso inmediato)
     * API: POST /api/billing/operations?action=purchase&adminId={adminId}
     */
    suspend fun purchaseTokens(tokensPackage: String): Result<Boolean> {
        val userProfile = authService.userProfile.value
        val accessToken = authService.accessToken.value
        
        Logger.auth("BILLING_SERVICE", "🛒 Comprando tokens directamente: $tokensPackage")
        
        if (userProfile?.adminId == null || accessToken == null) {
            return Result.failure(Exception("Usuario no autenticado"))
        }
        
        return billingApiClient.purchaseTokens(userProfile.adminId.toInt(), accessToken, tokensPackage)
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
            Result.success(tokenStatus.tokensAvailable >= requiredTokens)
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
     * Obtiene todos los planes disponibles (fallback local)
     */
    fun getAvailablePlansLocal(): List<SubscriptionPlan> {
        Logger.auth("BILLING_SERVICE", "📋 Cargando planes locales: ${BillingConfigs.AVAILABLE_PLANS.size} planes")
        BillingConfigs.AVAILABLE_PLANS.forEach { plan ->
            Logger.auth("BILLING_SERVICE", "📋 Plan: ${plan.name} - S/ ${plan.price}")
        }
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
                tokenStatus.tokensUsed > 2000 -> BillingConfigs.AVAILABLE_PLANS.find { it.id == 4 } // Empresarial
                tokenStatus.tokensUsed > 500 -> BillingConfigs.AVAILABLE_PLANS.find { it.id == 3 } // Profesional
                tokenStatus.tokensUsed > 100 -> BillingConfigs.AVAILABLE_PLANS.find { it.id == 2 } // Básico
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
    
    // =============================================================================
    // MÉTODOS CON MANEJO ELEGANTE DE ERRORES
    // =============================================================================
    
    /**
     * Obtiene planes disponibles con manejo elegante de errores
     */
    suspend fun getAvailablePlansWithErrorHandling(): Pair<List<SubscriptionPlan>?, ErrorInfo?> {
        return try {
            Logger.auth("BILLING_SERVICE", "📋 Obteniendo planes disponibles con manejo de errores")
            val result = getAvailablePlans()
            result.fold(
                onSuccess = { plans ->
                    Logger.auth("BILLING_SERVICE", "✅ Planes obtenidos exitosamente: ${plans.size}")
                    Pair(plans, null)
                },
                onFailure = { exception ->
                    Logger.auth("BILLING_SERVICE", "❌ Error obteniendo planes: ${exception.message}")
                    val errorInfo = ErrorManager.parseException(exception)
                    Pair(null, errorInfo)
                }
            )
        } catch (e: Exception) {
            Logger.auth("BILLING_SERVICE", "❌ Error inesperado: ${e.message}")
            val errorInfo = ErrorManager.parseException(e)
            Pair(null, errorInfo)
        }
    }
    
    /**
     * Obtiene paquetes de tokens con manejo elegante de errores
     */
    suspend fun getAvailableTokenPackagesWithErrorHandling(): Pair<List<TokenPackage>?, ErrorInfo?> {
        return try {
            Logger.auth("BILLING_SERVICE", "🪙 Obteniendo paquetes de tokens con manejo de errores")
            val result = getAvailableTokenPackages()
            result.fold(
                onSuccess = { packages ->
                    Logger.auth("BILLING_SERVICE", "✅ Paquetes obtenidos exitosamente: ${packages.size}")
                    Pair(packages, null)
                },
                onFailure = { exception ->
                    Logger.auth("BILLING_SERVICE", "❌ Error obteniendo paquetes: ${exception.message}")
                    val errorInfo = ErrorManager.parseException(exception)
                    Pair(null, errorInfo)
                }
            )
        } catch (e: Exception) {
            Logger.auth("BILLING_SERVICE", "❌ Error inesperado: ${e.message}")
            val errorInfo = ErrorManager.parseException(e)
            Pair(null, errorInfo)
        }
    }
    
    /**
     * Obtiene dashboard de facturación con manejo elegante de errores
     */
    suspend fun getCurrentBillingDashboardWithErrorHandling(): Pair<BillingDashboard?, ErrorInfo?> {
        return try {
            Logger.auth("BILLING_SERVICE", "📊 Obteniendo dashboard con manejo de errores")
            val result = getCurrentBillingDashboard()
            result.fold(
                onSuccess = { dashboard ->
                    Logger.auth("BILLING_SERVICE", "✅ Dashboard obtenido exitosamente")
                    Pair(dashboard, null)
                },
                onFailure = { exception ->
                    Logger.auth("BILLING_SERVICE", "❌ Error obteniendo dashboard: ${exception.message}")
                    val errorInfo = ErrorManager.parseException(exception)
                    Pair(null, errorInfo)
                }
            )
        } catch (e: Exception) {
            Logger.auth("BILLING_SERVICE", "❌ Error inesperado: ${e.message}")
            val errorInfo = ErrorManager.parseException(e)
            Pair(null, errorInfo)
        }
    }
    
    /**
     * Genera pago de suscripción con manejo elegante de errores
     */
    suspend fun generateSubscriptionPaymentWithErrorHandling(planId: Int): Pair<PaymentCode?, ErrorInfo?> {
        return try {
            Logger.auth("BILLING_SERVICE", "💳 Generando pago de suscripción con manejo de errores")
            val result = generateSubscriptionPayment(planId)
            result.fold(
                onSuccess = { paymentCode ->
                    Logger.auth("BILLING_SERVICE", "✅ Pago generado exitosamente: ${paymentCode.paymentCode}")
                    Pair(paymentCode, null)
                },
                onFailure = { exception ->
                    Logger.auth("BILLING_SERVICE", "❌ Error generando pago: ${exception.message}")
                    val errorInfo = ErrorManager.parseException(exception)
                    Pair(null, errorInfo)
                }
            )
        } catch (e: Exception) {
            Logger.auth("BILLING_SERVICE", "❌ Error inesperado: ${e.message}")
            val errorInfo = ErrorManager.parseException(e)
            Pair(null, errorInfo)
        }
    }
    
    /**
     * Genera pago de tokens con manejo elegante de errores
     */
    suspend fun generateTokenPurchasePaymentWithErrorHandling(tokensPackage: String): Pair<PaymentCode?, ErrorInfo?> {
        return try {
            Logger.auth("BILLING_SERVICE", "🪙 Generando pago de tokens con manejo de errores")
            val result = generateTokenPurchasePayment(tokensPackage)
            result.fold(
                onSuccess = { paymentCode ->
                    Logger.auth("BILLING_SERVICE", "✅ Pago de tokens generado exitosamente: ${paymentCode.paymentCode}")
                    Pair(paymentCode, null)
                },
                onFailure = { exception ->
                    Logger.auth("BILLING_SERVICE", "❌ Error generando pago de tokens: ${exception.message}")
                    val errorInfo = ErrorManager.parseException(exception)
                    Pair(null, errorInfo)
                }
            )
        } catch (e: Exception) {
            Logger.auth("BILLING_SERVICE", "❌ Error inesperado: ${e.message}")
            val errorInfo = ErrorManager.parseException(e)
            Pair(null, errorInfo)
        }
    }
    
}
