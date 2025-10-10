package org.sysarp.project.data

import kotlinx.serialization.Serializable

/**
 * Modelos de datos consolidados y unificados
 * Elimina duplicaciones y centraliza estructuras comunes
 */

// ===== MODELOS BASE COMUNES =====

/**
 * Respuesta base para todas las APIs
 */
@Serializable
data class BaseApiResponse<T>(
    val success: Boolean,
    val message: String,
    val data: T? = null,
    val error: Boolean = false
)

/**
 * Información de paginación común
 */
// PaginationInfo ya existe en CommonModels.kt - usar esa definición

/**
 * Período de tiempo común
 */
// StatsPeriod ya existe en StatsModels.kt - usar esa definición

// ===== MODELOS DE AUTENTICACIÓN UNIFICADOS =====

/**
 * Datos de usuario unificados
 */
@Serializable
data class UnifiedUserData(
    val id: Int,
    val email: String,
    val name: String? = null,
    val role: String,
    val businessId: Int? = null,
    val businessName: String? = null,
    val branchId: Int? = null,
    val branchName: String? = null,
    val branchCode: String? = null,
    val sellerId: Int? = null,
    val affiliationCode: String? = null,
    val isVerified: Boolean,
    val phone: String? = null,
    val address: String? = null
)

/**
 * Respuesta de login unificada
 */
@Serializable
data class UnifiedLoginResponse(
    val success: Boolean,
    val message: String,
    val data: UnifiedLoginData? = null,
    val error: Boolean = false
)

@Serializable
data class UnifiedLoginData(
    val accessToken: String,
    val refreshToken: String,
    val expiresIn: Int,
    val user: UnifiedUserData
)

// ===== MODELOS DE ESTADÍSTICAS UNIFICADOS =====

/**
 * Resumen de estadísticas unificado
 */
@Serializable
data class UnifiedStatsSummary(
    val totalSales: Double,
    val totalTransactions: Int,
    val averageTransactionValue: Double,
    val pendingPayments: Int,
    val confirmedPayments: Int,
    val rejectedPayments: Int,
    val claimRate: Double? = null,
    val averageConfirmationTime: Double? = null
)

/**
 * Estadísticas diarias unificadas
 */
@Serializable
data class UnifiedDailyStats(
    val date: String,
    val totalSales: Double,
    val transactionCount: Int,
    val averageValue: Double,
    val pendingCount: Int? = null,
    val confirmedCount: Int? = null
)

/**
 * Respuesta de estadísticas unificada
 */
@Serializable
data class UnifiedStatsResponse(
    val success: Boolean,
    val message: String,
    val data: UnifiedStatsData,
    val error: Boolean = false
)

@Serializable
data class UnifiedStatsData(
    val period: StatsPeriod, // Usar StatsPeriod existente
    val summary: UnifiedStatsSummary,
    val dailyStats: List<UnifiedDailyStats>,
    val sellerStats: List<UnifiedSellerStats>? = null,
    val performanceMetrics: UnifiedPerformanceMetrics? = null
)

/**
 * Estadísticas de vendedor unificadas
 */
@Serializable
data class UnifiedSellerStats(
    val sellerId: Int,
    val sellerName: String,
    val totalSales: Double,
    val transactionCount: Int,
    val averageValue: Double,
    val pendingCount: Int
)

/**
 * Métricas de rendimiento unificadas
 */
@Serializable
data class UnifiedPerformanceMetrics(
    val conversionRate: Double,
    val averageTransactionTime: Double,
    val customerSatisfaction: Double,
    val repeatCustomerRate: Double
)

// ===== MODELOS DE PAGOS UNIFICADOS =====

/**
 * Información de pago unificada
 */
@Serializable
data class UnifiedPaymentInfo(
    val id: Int,
    val amount: Double,
    val currency: String,
    val status: String,
    val sellerId: Int,
    val sellerName: String? = null,
    val branchId: Int? = null,
    val branchName: String? = null,
    val transactionId: String? = null,
    val sender: String? = null,
    val createdAt: String,
    val confirmedAt: String? = null,
    val rejectedAt: String? = null,
    val rejectionReason: String? = null
)

/**
 * Respuesta de pagos unificada
 */
@Serializable
data class UnifiedPaymentsResponse(
    val success: Boolean,
    val message: String,
    val data: UnifiedPaymentsData,
    val error: Boolean = false
)

@Serializable
data class UnifiedPaymentsData(
    val payments: List<UnifiedPaymentInfo>,
    val pagination: PaginationInfo,
    val summary: UnifiedPaymentSummary? = null
)

/**
 * Resumen de pagos unificado
 */
@Serializable
data class UnifiedPaymentSummary(
    val totalAmount: Double,
    val totalCount: Int,
    val pendingAmount: Double,
    val pendingCount: Int,
    val confirmedAmount: Double,
    val confirmedCount: Int,
    val rejectedAmount: Double,
    val rejectedCount: Int
)

// ===== MODELOS DE NOTIFICACIONES UNIFICADOS =====

/**
 * Notificación unificada
 */
@Serializable
data class UnifiedNotification(
    val id: Int,
    val type: String, // "PAYMENT", "SYSTEM", "ALERT", "YAPE"
    val title: String,
    val message: String,
    val isRead: Boolean,
    val createdAt: String,
    val readAt: String? = null,
    val data: Map<String, String>? = null
)

/**
 * Respuesta de notificaciones unificada
 */
@Serializable
data class UnifiedNotificationsResponse(
    val success: Boolean,
    val message: String,
    val data: UnifiedNotificationsData,
    val error: Boolean = false
)

@Serializable
data class UnifiedNotificationsData(
    val notifications: List<UnifiedNotification>,
    val pagination: PaginationInfo,
    val unreadCount: Int
)

// ===== MODELOS DE ERRORES UNIFICADOS =====

/**
 * Error de validación unificado
 */
@Serializable
data class UnifiedValidationError(
    val message: String,
    val code: String,
    val field: String? = null,
    val invalidValue: String? = null,
    val timestamp: String
)

/**
 * Respuesta de error unificada
 */
@Serializable
data class UnifiedErrorResponse(
    val success: Boolean = false,
    val message: String,
    val code: String,
    val details: Map<String, String>? = null,
    val timestamp: String
)
