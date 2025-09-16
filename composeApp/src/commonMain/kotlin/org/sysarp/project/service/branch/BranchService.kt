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
            println("🔐 [BRANCH_SERVICE] Creando sucursal: $name")
            
            val result = branchApiClient.createBranch(
                adminId = adminId,
                name = name,
                code = code,
                address = address,
                accessToken = accessToken
            )
            
            result.onSuccess { branchData ->
                println("🔐 [BRANCH_SERVICE] Sucursal creada exitosamente: ${branchData.name}")
            }.onFailure { error ->
                println("🔐 [BRANCH_SERVICE] Error creando sucursal: ${error.message}")
            }
            
            result
            
        } catch (e: Exception) {
            println("🔐 [BRANCH_SERVICE] ERROR: Error en servicio de sucursales: ${e.message}")
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
            println("🔐 [BRANCH_SERVICE] Obteniendo sucursales para admin: $adminId")
            
            val result = branchApiClient.getBranches(
                adminId = adminId,
                accessToken = accessToken,
                status = status,
                page = page,
                size = size
            )
            
            result.onSuccess { branchesData ->
                println("🔐 [BRANCH_SERVICE] Sucursales obtenidas: ${branchesData.branches.size}")
            }.onFailure { error ->
                println("🔐 [BRANCH_SERVICE] Error obteniendo sucursales: ${error.message}")
            }
            
            result
            
        } catch (e: Exception) {
            println("🔐 [BRANCH_SERVICE] ERROR: Error en servicio de sucursales: ${e.message}")
            Result.failure(e)
        }
    }
    
    suspend fun getBranchDetails(
        branchId: Int,
        adminId: Int,
        accessToken: String
    ): Result<BranchData> {
        return try {
            println("🔐 [BRANCH_SERVICE] Obteniendo detalles de sucursal: $branchId")
            
            val result = branchApiClient.getBranchDetails(
                branchId = branchId,
                adminId = adminId,
                accessToken = accessToken
            )
            
            result.onSuccess { branchData ->
                println("🔐 [BRANCH_SERVICE] Detalles obtenidos: ${branchData.name}")
            }.onFailure { error ->
                println("🔐 [BRANCH_SERVICE] Error obteniendo detalles: ${error.message}")
            }
            
            result
            
        } catch (e: Exception) {
            println("🔐 [BRANCH_SERVICE] ERROR: Error en servicio de sucursales: ${e.message}")
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
            println("🔐 [BRANCH_SERVICE] Actualizando sucursal: $branchId")
            
            val result = branchApiClient.updateBranch(
                branchId = branchId,
                adminId = adminId,
                name = name,
                code = code,
                address = address,
                isActive = isActive,
                accessToken = accessToken
            )
            
            result.onSuccess { branchData ->
                println("🔐 [BRANCH_SERVICE] Sucursal actualizada: ${branchData.name}")
            }.onFailure { error ->
                println("🔐 [BRANCH_SERVICE] Error actualizando sucursal: ${error.message}")
            }
            
            result
            
        } catch (e: Exception) {
            println("🔐 [BRANCH_SERVICE] ERROR: Error en servicio de sucursales: ${e.message}")
            Result.failure(e)
        }
    }
    
    suspend fun deleteBranch(
        branchId: Int,
        adminId: Int,
        accessToken: String
    ): Result<Boolean> {
        return try {
            println("🔐 [BRANCH_SERVICE] Eliminando sucursal: $branchId")
            
            val result = branchApiClient.deleteBranch(
                branchId = branchId,
                adminId = adminId,
                accessToken = accessToken
            )
            
            result.onSuccess {
                println("🔐 [BRANCH_SERVICE] Sucursal eliminada exitosamente")
            }.onFailure { error ->
                println("🔐 [BRANCH_SERVICE] Error eliminando sucursal: ${error.message}")
            }
            
            result
            
        } catch (e: Exception) {
            println("🔐 [BRANCH_SERVICE] ERROR: Error en servicio de sucursales: ${e.message}")
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
            println("🔐 [BRANCH_SERVICE] Obteniendo vendedores de sucursal: $branchId")
            
            val result = branchApiClient.getBranchSellers(
                branchId = branchId,
                adminId = adminId,
                accessToken = accessToken,
                page = page,
                size = size
            )
            
            result.onSuccess { sellersData ->
                println("🔐 [BRANCH_SERVICE] Vendedores obtenidos: ${sellersData.sellers.size}")
            }.onFailure { error ->
                println("🔐 [BRANCH_SERVICE] Error obteniendo vendedores: ${error.message}")
            }
            
            result
            
        } catch (e: Exception) {
            println("🔐 [BRANCH_SERVICE] ERROR: Error en servicio de sucursales: ${e.message}")
            Result.failure(e)
        }
    }
}
