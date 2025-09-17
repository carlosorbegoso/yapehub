package org.sysarp.project.service.branch

import org.sysarp.project.data.*
import org.sysarp.project.service.http.BranchApiClient

class BranchService(
    private val branchApiClient: BranchApiClient
) {
    
    suspend fun createBranch(
        adminId: Int,
        name: String,
        code: String,
        address: String,
        accessToken: String
    ): Result<BranchData> {
        return try {
            branchApiClient.createBranch(
                adminId = adminId,
                name = name,
                code = code,
                address = address,
                accessToken = accessToken
            )
        } catch (e: Exception) {
            println("❌ [BRANCH_SERVICE] Error creando sucursal: ${e.message}")
            Result.failure(e)
        }
    }
    
    suspend fun getBranches(
        adminId: Int,
        accessToken: String,
        status: String? = null,
        page: Int = 0,
        size: Int = 20
    ): Result<BranchesData> {
        return try {
            branchApiClient.getBranches(
                adminId = adminId,
                accessToken = accessToken,
                status = status,
                page = page,
                size = size
            )
        } catch (e: Exception) {
            println("❌ [BRANCH_SERVICE] Error obteniendo sucursales: ${e.message}")
            Result.failure(e)
        }
    }
    
    suspend fun getBranchDetails(
        branchId: Int,
        adminId: Int,
        accessToken: String
    ): Result<BranchData> {
        return try {
            branchApiClient.getBranchDetails(
                branchId = branchId,
                adminId = adminId,
                accessToken = accessToken
            )
        } catch (e: Exception) {
            println("❌ [BRANCH_SERVICE] Error obteniendo detalles: ${e.message}")
            Result.failure(e)
        }
    }
    
    suspend fun updateBranch(
        branchId: Int,
        adminId: Int,
        name: String,
        code: String,
        address: String,
        isActive: Boolean,
        accessToken: String
    ): Result<BranchData> {
        return try {
            branchApiClient.updateBranch(
                branchId = branchId,
                adminId = adminId,
                name = name,
                code = code,
                address = address,
                isActive = isActive,
                accessToken = accessToken
            )
        } catch (e: Exception) {
            println("❌ [BRANCH_SERVICE] Error actualizando sucursal: ${e.message}")
            Result.failure(e)
        }
    }
    
    suspend fun deleteBranch(
        branchId: Int,
        adminId: Int,
        accessToken: String
    ): Result<Boolean> {
        return try {
            branchApiClient.deleteBranch(
                branchId = branchId,
                adminId = adminId,
                accessToken = accessToken
            )
        } catch (e: Exception) {
            println("❌ [BRANCH_SERVICE] Error eliminando sucursal: ${e.message}")
            Result.failure(e)
        }
    }
    
    suspend fun getBranchSellers(
        branchId: Int,
        adminId: Int,
        accessToken: String,
        page: Int = 0,
        size: Int = 20
    ): Result<BranchSellersData> {
        return try {
            branchApiClient.getBranchSellers(
                branchId = branchId,
                adminId = adminId,
                accessToken = accessToken,
                page = page,
                size = size
            )
        } catch (e: Exception) {
            println("❌ [BRANCH_SERVICE] Error obteniendo vendedores: ${e.message}")
            Result.failure(e)
        }
    }
}
