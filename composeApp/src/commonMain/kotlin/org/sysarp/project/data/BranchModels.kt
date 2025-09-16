package org.sysarp.project.data

import kotlinx.serialization.Serializable

// Modelos para crear sucursal
@Serializable
data class CreateBranchRequest(
    val adminId: Int,
    val name: String,
    val code: String,
    val address: String
)

// Modelos para actualizar sucursal
@Serializable
data class UpdateBranchRequest(
    val adminId: Int,
    val name: String,
    val code: String,
    val address: String,
    val isActive: Boolean
)

// Modelos para respuesta de sucursales
@Serializable
data class BranchesResponse(
    val success: Boolean,
    val message: String,
    val data: BranchesData?,
    val error: Boolean
)

@Serializable
data class BranchesData(
    val branches: List<BranchInfo>,
    val pagination: BranchPagination
)

@Serializable
data class BranchInfo(
    val branchId: Int,
    val name: String,
    val code: String,
    val address: String,
    val isActive: Boolean,
    val sellersCount: Int,
    val createdAt: String,
    val updatedAt: String
)

@Serializable
data class BranchPagination(
    val currentPage: Int,
    val totalPages: Int,
    val totalItems: Int,
    val itemsPerPage: Int
)

// Modelos para respuesta de sucursal individual
@Serializable
data class BranchResponse(
    val success: Boolean,
    val message: String,
    val data: BranchData?,
    val error: Boolean
)

@Serializable
data class BranchData(
    val branchId: Int,
    val name: String,
    val code: String,
    val address: String,
    val isActive: Boolean,
    val sellersCount: Int,
    val createdAt: String,
    val updatedAt: String
)

// Modelos para vendedores por sucursal
@Serializable
data class BranchSellersResponse(
    val success: Boolean,
    val message: String,
    val data: BranchSellersData?,
    val error: Boolean
)

@Serializable
data class BranchSellersData(
    val branch: BranchInfo,
    val sellers: List<BranchSellerInfo>,
    val pagination: BranchPagination
)

@Serializable
data class BranchSellerInfo(
    val sellerId: Int,
    val sellerName: String,
    val email: String,
    val phone: String,
    val isActive: Boolean,
    val createdAt: String
)
