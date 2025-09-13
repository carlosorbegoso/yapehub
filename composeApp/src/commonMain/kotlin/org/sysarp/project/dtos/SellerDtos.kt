package org.sysarp.project.dtos

import kotlinx.serialization.Serializable

// DTOs para el sistema de vendedores

@Serializable
data class SellerInfo(
    val id: Int,
    val name: String,
    val phone: String,
    val email: String? = null,
    val isActive: Boolean,
    val branchId: Int? = null,
    val branchName: String? = null,
    val adminId: Int,
    val adminName: String? = null,
    val registeredAt: String,
    val lastActivityAt: String? = null,
    val totalTransactions: Int = 0,
    val totalAmount: Double = 0.0
)

@Serializable
data class SellerRegistrationData(
    val sellerId: Int,
    val sellerName: String,
    val phone: String,
    val branchId: Int,
    val branchName: String,
    val adminId: Int,
    val adminName: String,
    val registeredAt: String
)

@Serializable
data class SellerLoginData(
    val user: SellerUserData,
    val accessToken: String,
    val refreshToken: String,
    val expiresIn: Int
)

@Serializable
data class SellerUserData(
    val id: Int,
    val name: String,
    val phone: String,
    val email: String? = null,
    val role: String,
    val branchId: Int? = null,
    val branchName: String? = null,
    val adminId: Int,
    val adminName: String? = null,
    val isActive: Boolean,
    val isVerified: Boolean = false
)
