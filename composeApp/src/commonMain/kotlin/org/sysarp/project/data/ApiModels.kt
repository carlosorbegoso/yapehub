package org.sysarp.project.data

import kotlinx.serialization.Serializable

// Modelos para la API de registro de administrador
@Serializable
data class AdminRegistrationRequest(
    val businessName: String,
    val businessType: String,
    val ruc: String,
    val email: String,
    val password: String,
    val phone: String,
    val address: String,
    val contactName: String
)

@Serializable
data class AdminRegistrationResponse(
    val success: Boolean,
    val message: String,
    val data: AdminRegistrationData? = null,
    val error: Boolean = false
)

@Serializable
data class AdminRegistrationData(
    val accessToken: String,
    val refreshToken: String,
    val expiresIn: Int,
    val user: AdminUserData
)

@Serializable
data class AdminUserData(
    val id: Int,
    val email: String,
    val role: String,
    val businessId: Int,
    val businessName: String,
    val isVerified: Boolean
)

// Modelos para login
@Serializable
data class LoginRequest(
    val email: String,
    val password: String,
    val deviceFingerprint: String,
    val role: String
)

@Serializable
data class LoginResponse(
    val success: Boolean,
    val message: String,
    val data: LoginData? = null,
    val error: Boolean = false
)

@Serializable
data class LoginData(
    val accessToken: String,
    val refreshToken: String,
    val expiresIn: Int,
    val user: LoginUserData
)

@Serializable
data class LoginUserData(
    val id: Int,
    val email: String,
    val role: String,
    val businessId: Int,
    val businessName: String,
    val isVerified: Boolean
)

// Modelos para refresh token
@Serializable
data class RefreshTokenRequest(
    val refreshToken: String
)

@Serializable
data class RefreshTokenResponse(
    val success: Boolean,
    val message: String,
    val data: RefreshTokenData? = null,
    val error: Boolean = false
)

@Serializable
data class RefreshTokenData(
    val accessToken: String,
    val refreshToken: String,
    val expiresIn: Int,
    val user: RefreshUserData
)

@Serializable
data class RefreshUserData(
    val id: Int,
    val email: String,
    val role: String,
    val businessId: Int,
    val businessName: String,
    val isVerified: Boolean
)

// Modelos para logout
@Serializable
data class LogoutRequest(
    val userId: String
)

@Serializable
data class LogoutResponse(
    val success: Boolean,
    val message: String,
    val error: Boolean = false
)

// Modelos para transacciones
@Serializable
data class Transaction(
    val id: String,
    val securityCode: String,
    val amount: String,
    val timestamp: String,
    val description: String? = null,
    val type: String,
    val businessName: String? = null,
    val branchId: String? = null,
    val branchName: String? = null,
    val sellerId: String? = null,
    val sellerName: String? = null,
    val isProcessed: Boolean,
    val paymentMethod: String? = null,
    val customerPhone: String? = null
)

@Serializable
data class TransactionsResponse(
    val success: Boolean,
    val data: TransactionsData? = null
)

@Serializable
data class TransactionsData(
    val transactions: List<Transaction>,
    val pagination: Pagination
)

@Serializable
data class Pagination(
    val currentPage: Int,
    val totalPages: Int,
    val totalItems: Int,
    val itemsPerPage: Int
)

// Modelos para dashboard
@Serializable
data class DashboardResponse(
    val success: Boolean,
    val data: DashboardData? = null
)

@Serializable
data class DashboardData(
    val summary: DashboardSummary,
    val dailyStats: List<DailyStat>,
    val topSellers: List<TopSeller>,
    val branchStats: List<BranchStat>
)

@Serializable
data class DashboardSummary(
    val totalTransactions: Int,
    val totalAmount: String,
    val pendingTransactions: Int,
    val activeSellers: Int,
    val totalBranches: Int
)

@Serializable
data class DailyStat(
    val date: String,
    val transactions: Int,
    val amount: String
)

@Serializable
data class TopSeller(
    val sellerId: String,
    val sellerName: String,
    val transactions: Int,
    val amount: String
)

@Serializable
data class BranchStat(
    val branchId: String,
    val branchName: String,
    val transactions: Int,
    val amount: String
)

// Modelos para vendedores
@Serializable
data class Seller(
    val id: String,
    val name: String,
    val email: String,
    val phone: String,
    val branchId: String,
    val branchName: String,
    val isActive: Boolean,
    val isOnline: Boolean,
    val totalPayments: Int,
    val totalAmount: String,
    val lastPayment: String,
    val affiliationDate: String
)

@Serializable
data class SellersResponse(
    val success: Boolean,
    val data: SellersData? = null
)

@Serializable
data class SellersData(
    val sellers: List<Seller>,
    val pagination: Pagination
)

// Modelos para códigos QR y afiliación
@Serializable
data class QRGenerationRequest(
    val type: String = "affiliation",
    val expirationHours: Int = 24,
    val maxUses: Int = 10,
    val branchId: String? = null
)

@Serializable
data class QRGenerationResponse(
    val success: Boolean,
    val data: QRData? = null
)

@Serializable
data class QRData(
    val qrId: String,
    val qrCode: String,
    val qrImageUrl: String,
    val expiresAt: String,
    val maxUses: Int,
    val remainingUses: Int,
    val branchId: String? = null
)

@Serializable
data class AffiliationCodeRequest(
    val expirationHours: Int = 72,
    val maxUses: Int = 1,
    val branchId: String? = null,
    val notes: String? = null
)

@Serializable
data class AffiliationCodeResponse(
    val success: Boolean,
    val data: AffiliationCodeData? = null
)

@Serializable
data class AffiliationCodeData(
    val affiliationCode: String,
    val expiresAt: String,
    val maxUses: Int,
    val remainingUses: Int,
    val branchId: String? = null
)

// Modelos para afiliación de vendedores
@Serializable
data class SellerAffiliationRequest(
    val sellerName: String,
    val email: String,
    val phone: String,
    val branchId: String,
    val affiliationCode: String
)

@Serializable
data class SellerAffiliationResponse(
    val success: Boolean,
    val message: String,
    val data: SellerAffiliationData? = null
)

@Serializable
data class SellerAffiliationData(
    val sellerId: String,
    val name: String,
    val email: String,
    val branchId: String,
    val branchName: String,
    val isActive: Boolean,
    val affiliationDate: String
)

// Modelos para confirmar transacciones
@Serializable
data class ConfirmTransactionRequest(
    val sellerId: String? = null,
    val notes: String? = null
)

@Serializable
data class ConfirmTransactionResponse(
    val success: Boolean,
    val message: String,
    val data: ConfirmTransactionData? = null
)

@Serializable
data class ConfirmTransactionData(
    val transactionId: String,
    val isProcessed: Boolean,
    val processedAt: String
)

// Modelos para notificaciones
@Serializable
data class NotificationRequest(
    val targetType: String, // "seller", "admin", "all"
    val targetId: String? = null,
    val title: String,
    val message: String,
    val type: String,
    val data: Map<String, String> = emptyMap()
)

@Serializable
data class NotificationResponse(
    val success: Boolean,
    val sentCount: Int,
    val failedCount: Int
)

// Modelos para errores de API
@Serializable
data class ApiError(
    val message: String,
    val code: String,
    val details: ErrorDetails? = null,
    val timestamp: String
)

@Serializable
data class ErrorDetails(
    val validationErrors: Map<String, ValidationError>? = null
)

@Serializable
data class ValidationError(
    val invalidValue: String? = null,
    val message: String
)
