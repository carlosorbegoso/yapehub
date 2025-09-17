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

// Modelos para obtener vendedores del administrador
@Serializable
data class GetSellersResponse(
    val success: Boolean,
    val message: String,
    val data: SellersDataForAdmin? = null,
    val error: Boolean = false
)

@Serializable
data class SellersDataForAdmin(
    val sellers: List<SellerInfo>,
    val pagination: PaginationInfo // Replaced PaginationData with PaginationInfo
)

@Serializable
data class SellerInfo(
    val sellerId: Int,
    val name: String,
    val email: String,
    val phone: String,
    val branchId: Int,
    val branchName: String?,
    val isActive: Boolean,
    val isOnline: Boolean,
    val totalPayments: Int,
    val totalAmount: Double,
    val lastPayment: String?,
    val affiliationDate: String?
)

// Modelos para códigos de afiliación generados por Admin
@Serializable
data class GenerateAffiliationCodeRequest(
    val adminId: Int,
    val branchId: Int,
    val expirationHours: Int,
    val maxUses: Int,
    val notes: String? = null
)

// Modelos para vendedores conectados
@Serializable
data class ConnectedSellersResponse(
    val success: Boolean,
    val message: String,
    val data: ConnectedSellersData? = null,
    val error: Boolean = false
)

@Serializable
data class ConnectedSellersData(
    val adminId: Int,
    val connectedSellers: List<ConnectedSellerInfo>,
    val totalConnected: Int,
    val timestamp: String
)

@Serializable
data class ConnectedSellerInfo(
    val sellerId: Int,
    val sellerName: String,
    val email: String,
    val phone: String,
    val branchId: Int,
    val branchName: String,
    val isConnected: Boolean,
    val lastSeen: String
)

// Modelos para estado de vendedores
@Serializable
data class SellersStatusResponse(
    val success: Boolean,
    val message: String,
    val data: SellersStatusData? = null,
    val error: Boolean = false
)

@Serializable
data class SellersStatusData(
    val adminId: Int,
    val totalSellers: Int,
    val connectedCount: Int,
    val disconnectedCount: Int,
    val sellers: List<SellerStatusInfo>
)

@Serializable
data class SellerStatusInfo(
    val sellerId: Int,
    val sellerName: String,
    val email: String,
    val phone: String,
    val branchId: Int,
    val branchName: String,
    val isConnected: Boolean,
    val lastSeen: String,
    val totalPayments: Int,
    val totalAmount: Double
)

@Serializable
data class GenerateAffiliationCodeResponse(
    val success: Boolean,
    val message: String,
    val data: AffiliationCodeData? = null, // AffiliationCodeData will be in AffiliationModels.kt
    val error: Boolean = false
)

@Serializable
data class DeactivationRequest(
    val id: Int,
    val sellerId: Int,
    val reason: String,
    val status: String,
    val requestedAt: String,
    val processedAt: String? = null
)

// Modelos para perfil de administrador
@Serializable
data class AdminProfileResponse(
    val success: Boolean,
    val message: String,
    val data: AdminProfileData? = null,
    val error: Boolean = false
)

@Serializable
data class AdminProfileData(
    val userId: Int,
    val email: String? = null,
    val businessName: String,
    val businessType: String? = null,
    val ruc: String? = null,
    val phone: String,
    val address: String,
    val contactName: String,
    val isVerified: Boolean,
    val createdAt: String,
    val updatedAt: String
)

@Serializable
data class UpdateAdminProfileRequest(
    val businessName: String? = null,
    val businessType: String? = null,
    val phone: String? = null,
    val address: String? = null,
    val contactName: String? = null
)

// Modelos para gestión de vendedores con filtros
@Serializable
data class SellersWithFiltersResponse(
    val success: Boolean,
    val message: String,
    val data: SellersWithFiltersData? = null,
    val error: Boolean = false
)

@Serializable
data class SellersWithFiltersData(
    val sellers: List<SellerInfo>,
    val pagination: PaginationInfo,
    val filters: FilterInfo
)

@Serializable
data class FilterInfo(
    val totalCount: Int,
    val activeCount: Int,
    val inactiveCount: Int,
    val onlineCount: Int,
    val offlineCount: Int
)

// Modelos para actualizar vendedor
@Serializable
data class UpdateSellerRequest(
    val name: String? = null,
    val email: String? = null,
    val phone: String? = null,
    val branchId: Int? = null,
    val isActive: Boolean? = null
)

@Serializable
data class UpdateSellerResponse(
    val success: Boolean,
    val message: String,
    val data: SellerInfo? = null,
    val error: Boolean = false
)

// Modelos para eliminar vendedor
@Serializable
data class DeleteSellerResponse(
    val success: Boolean,
    val message: String,
    val data: DeleteSellerData? = null,
    val error: Boolean = false
)

@Serializable
data class DeleteSellerData(
    val sellerId: Int,
    val sellerName: String,
    val deletedAt: String
)

// Modelos para estadísticas de notificaciones
@Serializable
data class NotificationStatsResponse(
    val success: Boolean,
    val message: String,
    val data: NotificationStatsData? = null,
    val error: Boolean = false
)

@Serializable
data class NotificationStatsData(
    val adminId: Int,
    val totalNotifications: Int,
    val successfulNotifications: Int,
    val failedNotifications: Int,
    val pendingNotifications: Int,
    val todayNotifications: Int,
    val weeklyNotifications: Int,
    val monthlyNotifications: Int,
    val averageResponseTime: Double,
    val lastNotificationAt: String? = null
)
