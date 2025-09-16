package org.sysarp.project.data

import kotlinx.serialization.Serializable

// Modelos para vendedores
@Serializable
data class Seller(
    val id: Int,
    val name: String,
    val email: String,
    val phone: String,
    val branchId: Int?,
    val branchName: String?,
    val adminId: Int,
    val adminName: String?,
    val isActive: Boolean,
    val isOnline: Boolean,
    val totalPayments: Int,
    val totalAmount: Double,
    val lastPayment: String?,
    val affiliationDate: String?
)

// Modelo específico para la respuesta de "mis vendedores"
@Serializable
data class MySeller(
    val sellerId: Int,
    val name: String,
    val email: String,
    val phone: String,
    val branchId: Int,
    val branchName: String,
    val isActive: Boolean,
    val isOnline: Boolean,
    val totalPayments: Int,
    val totalAmount: Double,
    val lastPayment: String?,
    val affiliationDate: String?
)

@Serializable
data class MySellersResponse(
    val success: Boolean,
    val message: String,
    val data: MySellersData? = null,
    val error: Boolean = false
)

@Serializable
data class MySellersData(
    val sellers: List<MySeller>,
    val pagination: PaginationData
)

// Modelos para actualización de vendedores
@Serializable
data class UpdateSellerRequest(
    val name: String? = null,
    val phone: String? = null,
    val isActive: Boolean? = null
)

@Serializable
data class UpdateSellerResponse(
    val success: Boolean,
    val message: String,
    val data: UpdateSellerData? = null,
    val error: Boolean = false
)

@Serializable
data class UpdateSellerData(
    val sellerId: Int,
    val name: String,
    val phone: String,
    val isActive: Boolean,
    val updatedAt: String
)

// Modelos para eliminación/pausa de vendedores
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
    val action: String,  // "pause", "delete", "activate"
    val status: String,
    val updatedAt: String
)

@Serializable
data class SellersResponse(
    val success: Boolean,
    val message: String,
    val data: SellersData? = null,
    val error: Boolean = false
)

@Serializable
data class SellersData(
    val sellers: List<MySeller>,
    val pagination: PaginationData
)

@Serializable
data class PaginationData(
    val currentPage: Int,
    val totalPages: Int,
    val totalItems: Int,
    val itemsPerPage: Int
)

// Modelos para registro de vendedores
@Serializable
data class SellerRegistrationRequest(
    val affiliationCode: String,
    val sellerName: String,
    val phone: String
)

@Serializable
data class SellerRegistrationResponse(
    val success: Boolean,
    val message: String,
    val data: SellerRegistrationData? = null,
    val error: Boolean = false
)

@Serializable
data class SellerRegistrationData(
    val sellerId: Int,
    val affiliationCode: String,
    val sellerName: String,
    val phone: String,
    val branchId: Int,
    val branchName: String,
    val adminId: Int,
    val adminName: String,
    val registeredAt: String
)

// Modelos para login de vendedores con código de afiliación
@Serializable
data class SellerLoginByPhoneResponse(
    val success: Boolean,
    val message: String,
    val data: SellerLoginData? = null,
    val error: Boolean = false
)

@Serializable
data class SellerLoginData(
    val sellerId: Int,
    val sellerName: String,
    val email: String,
    val phone: String,
    val branchId: Int,
    val branchName: String,
    val branchCode: String,
    val affiliationCode: String,
    val accessToken: String
)

// Modelos legacy para compatibilidad
@Serializable
data class SellerLoginDataLegacy(
    val user: SellerUserData,
    val accessToken: String,
    val refreshToken: String,
    val expiresIn: Int
)

@Serializable
data class SellerUserData(
    val id: Int? = null,
    val name: String? = null,
    val phone: String? = null,
    val email: String? = null,
    val role: String? = null,
    val branchId: Int? = null,
    val branchName: String? = null,
    val branchCode: String? = null,
    val adminId: Int? = null,
    val adminName: String? = null,
    val isActive: Boolean? = null,
    val isVerified: Boolean = false,
    val sellerId: Int? = null,
    val affiliationCode: String? = null
)

// Modelos para validación de códigos de afiliación
@Serializable
data class ValidateAffiliationCodeRequest(
    val affiliationCode: String
)

@Serializable
data class ValidateAffiliationCodeResponse(
    val success: Boolean,
    val message: String,
    val data: ValidateAffiliationCodeData? = null,
    val error: Boolean = false
)

@Serializable
data class ValidateAffiliationCodeData(
    val isValid: Boolean,
    val affiliationCode: String,
    val branchId: Int?,
    val branchName: String?,
    val adminId: Int?
)

// Alias para compatibilidad
typealias SellerData = Seller
