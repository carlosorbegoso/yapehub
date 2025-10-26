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
            name = email,
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
        branchCode: String? = null,
        isVerified: Boolean,
        sellerId: Int?,
        affiliationCode: String? = null
    ): UserProfile {
        return UserProfile(
            id = id?.toString() ?: "0",
            name = name ?: email ?: "Usuario",
            email = email ?: "",
            role = if (role == "ADMIN") UserRole.ADMIN else UserRole.VENDOR,
            businessId = branchId,
            businessName = branchName,
            branchCode = branchCode,
            isVerified = isVerified,
            deviceId = id?.toString() ?: "0",
            adminId = id?.toString(),
            sellerId = sellerId?.toString(),
            affiliationCode = affiliationCode,
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
