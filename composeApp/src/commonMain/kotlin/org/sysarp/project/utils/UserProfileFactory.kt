package org.sysarp.project.utils

import org.sysarp.project.data.UserProfile
import org.sysarp.project.data.UserRole

/**
 * Factory para crear UserProfile de manera consistente
 * Elimina duplicación en la creación de perfiles de usuario
 */
object UserProfileFactory {
    
    /**
     * Crea un UserProfile para administrador
     */
    fun createAdminProfile(
        id: Int,
        email: String,
        businessId: Int?,
        businessName: String?,
        isVerified: Boolean
    ): UserProfile {
        return UserProfile(
            id = id.toString(),
            name = email, // Usar email como name temporalmente
            email = email,
            role = UserRole.ADMIN,
            businessId = businessId,
            businessName = businessName,
            isVerified = isVerified,
            deviceId = id.toString(),
            adminId = id.toString(),
            sellerId = null, // Admin no tiene sellerId
            permissions = getAdminPermissions(),
            subscriptionPlan = "PROFESSIONAL",
            subscriptionStatus = "ACTIVE"
        )
    }
    
    /**
     * Crea un UserProfile para vendedor
     */
    fun createSellerProfile(
        id: Int?,
        name: String?,
        email: String?,
        role: String,
        branchId: Int?,
        branchName: String?,
        isVerified: Boolean,
        sellerId: Int?
    ): UserProfile {
        return UserProfile(
            id = id?.toString() ?: "0",
            name = name ?: email ?: "Usuario",
            email = email ?: "",
            role = if (role == "ADMIN") UserRole.ADMIN else UserRole.VENDOR,
            businessId = branchId,
            businessName = branchName,
            isVerified = isVerified,
            deviceId = id?.toString() ?: "0",
            adminId = id?.toString(),
            sellerId = sellerId?.toString(),
            permissions = getSellerPermissions(),
            subscriptionPlan = "BASIC",
            subscriptionStatus = "ACTIVE"
        )
    }
    
    /**
     * Crea un UserProfile mock para pruebas
     */
    fun createMockSellerProfile(
        sellerName: String,
        deviceId: String,
        adminId: String,
        branchCode: String
    ): UserProfile {
        return UserProfile(
            id = "0", // Mock ID
            name = sellerName,
            email = "seller@mock.com", // Mock email
            role = UserRole.VENDOR,
            businessId = null,
            businessName = null,
            isVerified = false,
            deviceId = deviceId,
            adminId = adminId,
            sellerName = sellerName,
            branchCode = branchCode,
            permissions = getSellerPermissions(),
            subscriptionPlan = "BASIC",
            subscriptionStatus = "ACTIVE"
        )
    }
    
    /**
     * Obtiene permisos para administradores
     */
    private fun getAdminPermissions(): List<String> {
        return listOf(
            Constants.Permissions.RECEIVE_YAPE_NOTIFICATIONS,
            Constants.Permissions.SEND_PAYMENT_ALERTS,
            Constants.Permissions.MANAGE_SELLERS,
            Constants.Permissions.VIEW_ANALYTICS
        )
    }
    
    /**
     * Obtiene permisos para vendedores
     */
    private fun getSellerPermissions(): List<String> {
        return listOf(
            Constants.Permissions.RECEIVE_YAPE_NOTIFICATIONS,
            Constants.Permissions.SEND_PAYMENT_ALERTS
        )
    }
}
