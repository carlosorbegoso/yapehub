package org.sysarp.project.data

import kotlinx.serialization.Serializable

// =============================================================================
// MODELOS DE DATOS PARA SISTEMA DE FACTURACIÓN
// =============================================================================

@Serializable
data class SubscriptionPlan(
    val id: Int,
    val name: String,
    val description: String,
    val price: Double,
    val currency: String,
    val billingCycle: String, // " monthly", "yearly"
    val maxSellers: Int,
    val tokensIncluded: Int,
    val features: List<String> = emptyList(),
    val isPopular: Boolean = false,
    val isActive: Boolean = true
)

@Serializable
data class SubscriptionStatus(
    val subscriptionId: Int? = null,
    val status: String, // "free", "active", "expired", "cancelled"
    val planName: String,
    val description: String,
    val price: Double,
    val currency: String,
    val billingCycle: String,
    val maxSellers: Int,
    val tokensIncluded: Int? = null,
    val startDate: String? = null,
    val endDate: String? = null,
    val isActive: Boolean,
    val message: String
)

@Serializable
data class TokenStatus(
    val totalTokens: Int,
    val usedTokens: Int,
    val remainingTokens: Int,
    val tokensIncluded: Int,
    val tokensPurchased: Int,
    val lastResetDate: String? = null,
    val nextResetDate: String? = null,
    val usageByOperation: Map<String, Int> = emptyMap()
)

@Serializable
data class PaymentCode(
    val paymentCode: String,
    val yapeNumber: String,
    val amount: Double,
    val currency: String,
    val expiresAt: String,
    val instructions: String
)

@Serializable
data class PaymentStatus(
    val paymentCode: String,
    val status: String, // "pending", "approved", "rejected", "expired"
    val amount: Double,
    val currency: String,
    val expiresAt: String,
    val isExpired: Boolean,
    val message: String,
    val reviewNotes: String? = null,
    val processedAt: String? = null
)

@Serializable
data class BillingDashboard(
    val adminId: Int,
    val tokenStatus: TokenStatusResponse? = null,
    val subscriptionStatus: SubscriptionStatus,
    val recentPayments: List<PaymentStatus> = emptyList(),
    val billingSummary: BillingSummary,
    val lastUpdated: String
)

@Serializable
data class TokenStatusResponse(
    val tokensAvailable: Int,
    val tokensUsed: Int,
    val tokensPurchased: Int,
    val daysUntilReset: Int,
    val usagePercentage: Double
)

@Serializable
data class MonthlyUsage(
    val tokensUsed: Int,
    val tokensRemaining: Int,
    val operationsCount: Int,
    val mostUsedOperation: String
)

@Serializable
data class BillingSummary(
    val totalSpent: Double,
    val currency: String,
    val nextBillingDate: String? = null,
    val autoRenewal: Boolean,
    val paymentMethod: String
)

@Serializable
data class PaymentUploadRequest(
    val paymentCode: String,
    val imageUrl: String,
    val notes: String? = null
)

@Serializable
data class PaymentUploadBase64Request(
    val imageBase64: String,
    val notes: String? = null
)

@Serializable
data class GeneratePaymentRequest(
    val planId: Int? = null,
    val tokensPackage: String? = null,
    val paymentMethod: String = "yape"
)

@Serializable
data class TokenPackage(
    val id: String,
    val name: String,
    val description: String,
    val tokens: Int,
    val price: Double,
    val currency: String = "PEN",
    val discount: Double = 0.0,
    val isPopular: Boolean = false,
    val features: List<String> = emptyList(),
    val discountedPrice: Double = 0.0
)

// =============================================================================
// PARÁMETROS PARA APIs DE FACTURACIÓN
// =============================================================================

@Serializable
data class BillingParams(
    val type: String, // "tokens", "subscription", "payments", "dashboard", "plans"
    val adminId: Int,
    val period: String = "current", // "current", "monthly", "yearly", "custom"
    val include: String = "details", // "details", "history", "forecast", "recommendations"
    val startDate: String? = null,
    val endDate: String? = null
)

@Serializable
data class BillingOperationParams(
    val adminId: Int,
    val action: String, // "generate-code", "upload", "subscribe", "upgrade", "cancel", "purchase", "check", "simulate"
    val validate: Boolean = true
)

// =============================================================================
// RESPUESTAS DE APIs
// =============================================================================

@Serializable
data class BillingResponse<T>(
    val success: Boolean,
    val message: String,
    val data: T,
    val error: Boolean = false
)

@Serializable
data class BillingErrorResponse(
    val message: String,
    val code: String,
    val details: Map<String, String> = emptyMap(),
    val timestamp: String
)

@Serializable
data class FlexibleBillingResponse<T>(
    val success: Boolean? = null,
    val message: String,
    val data: T? = null,
    val error: Boolean? = null,
    val code: String? = null,
    val details: Map<String, String>? = null,
    val timestamp: String? = null
)

@Serializable
data class SubscriptionResponse(
    val success: Boolean,
    val message: String,
    val data: SubscriptionStatus,
    val error: Boolean = false
)

@Serializable
data class TokenResponse(
    val success: Boolean,
    val message: String,
    val data: TokenStatusResponse,
    val error: Boolean = false
)

@Serializable
data class PaymentCodeResponse(
    val success: Boolean,
    val message: String,
    val data: PaymentCode,
    val error: Boolean = false
)

@Serializable
data class PaymentStatusResponse(
    val success: Boolean,
    val message: String,
    val data: PaymentStatus,
    val error: Boolean = false
)

@Serializable
data class BillingDashboardResponse(
    val success: Boolean,
    val message: String,
    val data: BillingDashboard,
    val error: Boolean = false
)

// =============================================================================
// CONFIGURACIONES PREDEFINIDAS
// =============================================================================

object BillingConfigs {
    
    // Planes disponibles
    val AVAILABLE_PLANS = listOf(
        SubscriptionPlan(
            id = 1,
            name = "Plan Gratuito",
            description = "Plan básico con funcionalidades limitadas",
            price = 0.0,
            currency = "PEN",
            billingCycle = "monthly",
            maxSellers = 1,
            tokensIncluded = 100,
            features = listOf("Dashboard básico", "1 vendedor", "100 tokens/mes")
        ),
        SubscriptionPlan(
            id = 2,
            name = "Plan Básico",
            description = "Perfecto para pequeños negocios",
            price = 29.0,
            currency = "PEN",
            billingCycle = "monthly",
            maxSellers = 5,
            tokensIncluded = 500,
            features = listOf("Dashboard completo", "5 vendedores", "500 tokens/mes", "Soporte por email"),
            isPopular = true
        ),
        SubscriptionPlan(
            id = 3,
            name = "Plan Profesional",
            description = "Para negocios en crecimiento",
            price = 79.0,
            currency = "PEN",
            billingCycle = "monthly",
            maxSellers = 15,
            tokensIncluded = 2000,
            features = listOf("Analytics avanzados", "15 vendedores", "2000 tokens/mes", "Soporte prioritario", "Reportes personalizados")
        ),
        SubscriptionPlan(
            id = 4,
            name = "Plan Empresarial",
            description = "Para empresas grandes",
            price = 149.0,
            currency = "PEN",
            billingCycle = "monthly",
            maxSellers = 50,
            tokensIncluded = 10000,
            features = listOf("Todas las funcionalidades", "50 vendedores", "10000 tokens/mes", "Soporte 24/7", "API personalizada", "Integraciones")
        )
    )
    
    // Paquetes de tokens adicionales
    val TOKEN_PACKAGES = mapOf(
        "100" to 15.0,
        "500" to 65.0,
        "1000" to 120.0,
        "5000" to 550.0
    )
    
    // Número de Yape para pagos
    const val YAPE_NUMBER = "977737772"
    
    // Configuraciones de límites
    object Limits {
        const val FREE_MAX_SELLERS = 1
        const val FREE_TOKENS_PER_MONTH = 100
        const val PAYMENT_CODE_EXPIRY_HOURS = 24
    }
}
