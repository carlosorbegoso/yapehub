package org.sysarp.project.data

import kotlinx.serialization.Serializable

@Serializable
data class UserProfile(
    val id: String,
    val name: String,
    val email: String,
    val role: UserRole,
    val assignedStores: List<String> = emptyList(), // Para vendedores, lista de tiendas asignadas
    val isActive: Boolean = true,
    // Campos adicionales para compatibilidad con AuthService
    val businessId: Int? = null,
    val businessName: String? = null,
    val isVerified: Boolean = false,
    val deviceId: String = "",
    val adminId: String? = null,
    val sellerId: String? = null,
    val sellerName: String? = null,
    val branchCode: String? = null,
    val branchName: String? = null,
    val affiliationCode: String? = null,
    val permissions: List<String> = emptyList(),
    val subscriptionPlan: String? = null,
    val subscriptionStatus: String? = null
)

@Serializable
enum class UserRole {
    ADMIN,      // Puede ver todas las transacciones y gestionar tiendas
    VENDOR      // Solo puede ver transacciones de sus tiendas asignadas
}

@Serializable
data class Store(
    val id: String,
    val name: String,
    val address: String? = null,
    val isActive: Boolean = true
)
