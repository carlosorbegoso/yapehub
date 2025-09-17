package org.sysarp.project.service.http

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import kotlinx.serialization.json.Json
import org.sysarp.project.data.*
import org.sysarp.project.utils.Constants

class BranchApiClient(private val httpClient: HttpClient) {
    
    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
    }
    
    // Crear sucursal
    suspend fun createBranch(
        adminId: Int,
        name: String,
        code: String,
        address: String,
        accessToken: String
    ): Result<BranchData> {
        return try {
            val request = CreateBranchRequest(
                adminId = adminId,
                name = name,
                code = code,
                address = address
            )
            
            val response: HttpResponse = httpClient.post("${Constants.BASE_URL}/api/admin/branches") {
                header(HttpHeaders.Authorization, "Bearer $accessToken")
                header(HttpHeaders.ContentType, "application/json")
                setBody(request)
            }
            
            val responseBody = response.bodyAsText()
            
            if (response.status.isSuccess()) {
                val branchResponse = json.decodeFromString<BranchResponse>(responseBody)
                Result.success(branchResponse.data!!)
            } else {
                val errorMessage = "Error ${response.status.value}: ${response.status.description}"
                Result.failure(Exception(errorMessage))
            }
            
        } catch (e: Exception) {
            println("❌ [BRANCH_API] Error creando sucursal: ${e.message}")
            Result.failure(e)
        }
    }
    
    // Listar sucursales
    suspend fun getBranches(
        adminId: Int,
        accessToken: String,
        status: String? = null,
        page: Int = 0,
        size: Int = 20
    ): Result<BranchesData> {
        return try {
            val response: HttpResponse = httpClient.get("${Constants.BASE_URL}/api/admin/branches") {
                header(HttpHeaders.Authorization, "Bearer $accessToken")
                parameter("adminId", adminId)
                status?.let { parameter("status", it) }
                parameter("page", page)
                parameter("size", size)
            }
            
            val responseBody = response.bodyAsText()
            
            if (response.status.isSuccess()) {
                val branchesResponse = json.decodeFromString<BranchesResponse>(responseBody)
                Result.success(branchesResponse.data!!)
            } else {
                val errorMessage = "Error ${response.status.value}: ${response.status.description}"
                Result.failure(Exception(errorMessage))
            }
            
        } catch (e: Exception) {
            println("❌ [BRANCH_API] Error obteniendo sucursales: ${e.message}")
            Result.failure(e)
        }
    }
    
    // Obtener detalles de sucursal
    suspend fun getBranchDetails(
        branchId: Int,
        adminId: Int,
        accessToken: String
    ): Result<BranchData> {
        return try {
            val response: HttpResponse = httpClient.get("${Constants.BASE_URL}/api/admin/branches/$branchId") {
                header(HttpHeaders.Authorization, "Bearer $accessToken")
                parameter("adminId", adminId)
            }
            
            val responseBody = response.bodyAsText()
            
            if (response.status.isSuccess()) {
                val branchResponse = json.decodeFromString<BranchResponse>(responseBody)
                Result.success(branchResponse.data!!)
            } else {
                val errorMessage = "Error ${response.status.value}: ${response.status.description}"
                Result.failure(Exception(errorMessage))
            }
            
        } catch (e: Exception) {
            println("❌ [BRANCH_API] Error obteniendo detalles: ${e.message}")
            Result.failure(e)
        }
    }
    
    // Actualizar sucursal
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
            val request = UpdateBranchRequest(
                adminId = adminId,
                name = name,
                code = code,
                address = address,
                isActive = isActive
            )
            
            val response: HttpResponse = httpClient.put("${Constants.BASE_URL}/api/admin/branches/$branchId") {
                header(HttpHeaders.Authorization, "Bearer $accessToken")
                header(HttpHeaders.ContentType, "application/json")
                setBody(request)
            }
            
            val responseBody = response.bodyAsText()
            
            if (response.status.isSuccess()) {
                val branchResponse = json.decodeFromString<BranchResponse>(responseBody)
                Result.success(branchResponse.data!!)
            } else {
                val errorMessage = "Error ${response.status.value}: ${response.status.description}"
                Result.failure(Exception(errorMessage))
            }
            
        } catch (e: Exception) {
            println("❌ [BRANCH_API] Error actualizando sucursal: ${e.message}")
            Result.failure(e)
        }
    }
    
    // Eliminar sucursal
    suspend fun deleteBranch(
        branchId: Int,
        adminId: Int,
        accessToken: String
    ): Result<Boolean> {
        return try {
            val response: HttpResponse = httpClient.delete("${Constants.BASE_URL}/api/admin/branches/$branchId") {
                header(HttpHeaders.Authorization, "Bearer $accessToken")
                parameter("adminId", adminId)
            }
            
            if (response.status.isSuccess()) {
                Result.success(true)
            } else {
                val errorMessage = "Error ${response.status.value}: ${response.status.description}"
                Result.failure(Exception(errorMessage))
            }
            
        } catch (e: Exception) {
            println("❌ [BRANCH_API] Error eliminando sucursal: ${e.message}")
            Result.failure(e)
        }
    }
    
    // Obtener vendedores por sucursal
    suspend fun getBranchSellers(
        branchId: Int,
        adminId: Int,
        accessToken: String,
        page: Int = 0,
        size: Int = 20
    ): Result<BranchSellersData> {
        return try {
            val response: HttpResponse = httpClient.get("${Constants.BASE_URL}/api/admin/branches/$branchId/sellers") {
                header(HttpHeaders.Authorization, "Bearer $accessToken")
                parameter("adminId", adminId)
                parameter("page", page)
                parameter("size", size)
            }
            
            val responseBody = response.bodyAsText()
            
            if (response.status.isSuccess()) {
                val sellersResponse = json.decodeFromString<BranchSellersResponse>(responseBody)
                Result.success(sellersResponse.data!!)
            } else {
                val errorMessage = "Error ${response.status.value}: ${response.status.description}"
                Result.failure(Exception(errorMessage))
            }
            
        } catch (e: Exception) {
            println("❌ [BRANCH_API] Error obteniendo vendedores: ${e.message}")
            Result.failure(e)
        }
    }
}
