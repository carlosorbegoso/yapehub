package org.sysarp.project.service

import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.sysarp.project.data.*

class AdminApiService : BaseHttpService() {
    
    // Generar código de afiliación
    suspend fun generateAffiliationCode(
        adminId: Int,
        branchId: Int,
        expirationHours: Int,
        maxUses: Int,
        notes: String? = null,
        accessToken: String
    ): Result<GenerateAffiliationCodeResponse> = withContext(Dispatchers.IO) {
        try {
            println("🌐 [ADMIN_API] Iniciando generación de código de afiliación")
            println("📤 [ADMIN_API] AdminId: $adminId")
            println("📤 [ADMIN_API] BranchId: $branchId")
            println("📤 [ADMIN_API] ExpirationHours: $expirationHours")
            println("📤 [ADMIN_API] MaxUses: $maxUses")
            println("📤 [ADMIN_API] Notes: $notes")
            
            val url = "$baseUrl/generate-affiliation-code-protected?adminId=$adminId&branchId=$branchId&expirationHours=$expirationHours&maxUses=$maxUses"
            val finalUrl = if (notes != null) "$url&notes=$notes" else url
            
            val response = httpClient.post(finalUrl) {
                contentType(ContentType.Application.Json)
                header("Authorization", "Bearer $accessToken")
                setBody("{}")
            }
            
            println("📥 [ADMIN_API] Respuesta de generación recibida - Status: ${response.status}")
            
            when (response.status) {
                HttpStatusCode.OK, HttpStatusCode.Created -> {
                    println("✅ [ADMIN_API] Código generado exitosamente")
                    val result = response.body<GenerateAffiliationCodeResponse>()
                    Result.success(result)
                }
                HttpStatusCode.Unauthorized -> {
                    println("❌ [ADMIN_API] Error de autenticación - Status: ${response.status}")
                    Result.failure(handleErrorResponse(response))
                }
                HttpStatusCode.BadRequest -> {
                    println("❌ [ADMIN_API] Error de validación - Status: ${response.status}")
                    Result.failure(handleErrorResponse(response))
                }
                else -> {
                    println("❌ [ADMIN_API] Error inesperado - Status: ${response.status}")
                    Result.failure(handleErrorResponse(response))
                }
            }
        } catch (e: Exception) {
            Result.failure(handleNetworkException(e))
        }
    }
    
    // Obtener vendedores del administrador
    suspend fun getMySellers(
        adminId: Int,
        accessToken: String,
        page: Int = 1,
        limit: Int = 3
    ): Result<MySellersResponse> = withContext(Dispatchers.IO) {
        try {
            println("🌐 [ADMIN_API] Iniciando obtención de vendedores del administrador")
            println("📤 [ADMIN_API] AdminId: $adminId")
            println("📤 [ADMIN_API] Page: $page")
            println("📤 [ADMIN_API] Limit: $limit")
            
            val response = httpClient.get("$baseUrl/admin/sellers/my-sellers") {
                contentType(ContentType.Application.Json)
                header("Authorization", "Bearer $accessToken")
                url {
                    parameters.append("adminId", adminId.toString())
                    parameters.append("page", page.toString())
                    parameters.append("limit", limit.toString())
                }
            }
            
            println("📥 [ADMIN_API] Respuesta de vendedores recibida - Status: ${response.status}")
            
            when (response.status) {
                HttpStatusCode.OK -> {
                    println("✅ [ADMIN_API] Vendedores obtenidos exitosamente")
                    val result = response.body<MySellersResponse>()
                    Result.success(result)
                }
                HttpStatusCode.Unauthorized -> {
                    println("❌ [ADMIN_API] Error de autenticación - Status: ${response.status}")
                    Result.failure(handleErrorResponse(response))
                }
                HttpStatusCode.BadRequest -> {
                    println("❌ [ADMIN_API] Error de validación - Status: ${response.status}")
                    Result.failure(handleErrorResponse(response))
                }
                else -> {
                    println("❌ [ADMIN_API] Error inesperado - Status: ${response.status}")
                    Result.failure(handleErrorResponse(response))
                }
            }
        } catch (e: Exception) {
            Result.failure(handleNetworkException(e))
        }
    }
    
    // Listar todos los vendedores del sistema
    suspend fun getSellers(
        accessToken: String,
        page: Int = 1,
        limit: Int = 20,
        branchId: Int? = null,
        status: String = "all"
    ): Result<SellersResponse> = withContext(Dispatchers.IO) {
        try {
            println("🌐 [ADMIN_API] Iniciando obtención de todos los vendedores")
            println("📤 [ADMIN_API] Page: $page")
            println("📤 [ADMIN_API] Limit: $limit")
            println("📤 [ADMIN_API] BranchId: $branchId")
            println("📤 [ADMIN_API] Status: $status")
            
            val response = httpClient.get("$baseUrl/admin/sellers") {
                contentType(ContentType.Application.Json)
                header("Authorization", "Bearer $accessToken")
                url {
                    parameters.append("page", page.toString())
                    parameters.append("limit", limit.toString())
                    branchId?.let { parameters.append("branchId", it.toString()) }
                    parameters.append("status", status)
                }
            }
            
            println("📥 [ADMIN_API] Respuesta de vendedores recibida - Status: ${response.status}")
            
            when (response.status) {
                HttpStatusCode.OK -> {
                    println("✅ [ADMIN_API] Vendedores obtenidos exitosamente")
                    val result = response.body<SellersResponse>()
                    Result.success(result)
                }
                HttpStatusCode.Unauthorized -> {
                    println("❌ [ADMIN_API] Error de autenticación - Status: ${response.status}")
                    Result.failure(handleErrorResponse(response))
                }
                HttpStatusCode.BadRequest -> {
                    println("❌ [ADMIN_API] Error de validación - Status: ${response.status}")
                    Result.failure(handleErrorResponse(response))
                }
                else -> {
                    println("❌ [ADMIN_API] Error inesperado - Status: ${response.status}")
                    Result.failure(handleErrorResponse(response))
                }
            }
        } catch (e: Exception) {
            Result.failure(handleNetworkException(e))
        }
    }
    
    // Actualizar vendedor
    suspend fun updateSeller(
        sellerId: Int,
        adminId: Int,
        accessToken: String,
        name: String? = null,
        phone: String? = null,
        isActive: Boolean? = null
    ): Result<UpdateSellerResponse> = withContext(Dispatchers.IO) {
        try {
            println("🌐 [ADMIN_API] Iniciando actualización de vendedor")
            println("📤 [ADMIN_API] SellerId: $sellerId")
            println("📤 [ADMIN_API] AdminId: $adminId")
            println("📤 [ADMIN_API] Name: $name")
            println("📤 [ADMIN_API] Phone: $phone")
            println("📤 [ADMIN_API] IsActive: $isActive")
            
            val response = httpClient.put("$baseUrl/admin/sellers/$sellerId") {
                contentType(ContentType.Application.Json)
                header("Authorization", "Bearer $accessToken")
                url {
                    parameters.append("adminId", adminId.toString())
                    name?.let { parameters.append("name", it) }
                    phone?.let { parameters.append("phone", it) }
                    isActive?.let { parameters.append("isActive", it.toString()) }
                }
                setBody("{}")
            }
            
            println("📥 [ADMIN_API] Respuesta de actualización recibida - Status: ${response.status}")
            
            when (response.status) {
                HttpStatusCode.OK, HttpStatusCode.Created -> {
                    println("✅ [ADMIN_API] Vendedor actualizado exitosamente")
                    val result = response.body<UpdateSellerResponse>()
                    Result.success(result)
                }
                HttpStatusCode.Unauthorized -> {
                    println("❌ [ADMIN_API] Error de autenticación - Status: ${response.status}")
                    Result.failure(handleErrorResponse(response))
                }
                HttpStatusCode.BadRequest -> {
                    println("❌ [ADMIN_API] Error de validación - Status: ${response.status}")
                    Result.failure(handleErrorResponse(response))
                }
                HttpStatusCode.NotFound -> {
                    println("❌ [ADMIN_API] Vendedor no encontrado - Status: ${response.status}")
                    Result.failure(handleErrorResponse(response))
                }
                else -> {
                    println("❌ [ADMIN_API] Error inesperado - Status: ${response.status}")
                    Result.failure(handleErrorResponse(response))
                }
            }
        } catch (e: Exception) {
            Result.failure(handleNetworkException(e))
        }
    }
    
    // Eliminar/Pausar vendedor
    suspend fun deleteSeller(
        sellerId: Int,
        adminId: Int,
        accessToken: String,
        action: String = "pause"  // "pause", "delete", "activate"
    ): Result<DeleteSellerResponse> = withContext(Dispatchers.IO) {
        try {
            println("🌐 [ADMIN_API] Iniciando eliminación/pausa de vendedor")
            println("📤 [ADMIN_API] SellerId: $sellerId")
            println("📤 [ADMIN_API] AdminId: $adminId")
            println("📤 [ADMIN_API] Action: $action")
            
            val response = httpClient.delete("$baseUrl/admin/sellers/$sellerId") {
                contentType(ContentType.Application.Json)
                header("Authorization", "Bearer $accessToken")
                url {
                    parameters.append("action", action)
                    parameters.append("adminId", adminId.toString())
                }
            }
            
            println("📥 [ADMIN_API] Respuesta de eliminación recibida - Status: ${response.status}")
            
            when (response.status) {
                HttpStatusCode.OK, HttpStatusCode.NoContent -> {
                    println("✅ [ADMIN_API] Vendedor ${action} exitosamente")
                    val result = response.body<DeleteSellerResponse>()
                    Result.success(result)
                }
                HttpStatusCode.Unauthorized -> {
                    println("❌ [ADMIN_API] Error de autenticación - Status: ${response.status}")
                    Result.failure(handleErrorResponse(response))
                }
                HttpStatusCode.BadRequest -> {
                    println("❌ [ADMIN_API] Error de validación - Status: ${response.status}")
                    Result.failure(handleErrorResponse(response))
                }
                HttpStatusCode.NotFound -> {
                    println("❌ [ADMIN_API] Vendedor no encontrado - Status: ${response.status}")
                    Result.failure(handleErrorResponse(response))
                }
                else -> {
                    println("❌ [ADMIN_API] Error inesperado - Status: ${response.status}")
                    Result.failure(handleErrorResponse(response))
                }
            }
        } catch (e: Exception) {
            Result.failure(handleNetworkException(e))
        }
    }
    
    // Dashboard del administrador
    suspend fun getAdminDashboard(accessToken: String): Result<Map<String, Any>> = withContext(Dispatchers.IO) {
        try {
            println("🌐 [ADMIN_API] Iniciando obtención de dashboard del administrador")
            
            val response = httpClient.get("$baseUrl/admin/dashboard") {
                contentType(ContentType.Application.Json)
                header("Authorization", "Bearer $accessToken")
            }
            
            println("📥 [ADMIN_API] Respuesta de dashboard recibida - Status: ${response.status}")
            
            when (response.status) {
                HttpStatusCode.OK -> {
                    println("✅ [ADMIN_API] Dashboard obtenido exitosamente")
                    val result = response.body<Map<String, Any>>()
                    Result.success(result)
                }
                HttpStatusCode.Unauthorized -> {
                    println("❌ [ADMIN_API] Error de autenticación - Status: ${response.status}")
                    Result.failure(handleErrorResponse(response))
                }
                else -> {
                    println("❌ [ADMIN_API] Error inesperado - Status: ${response.status}")
                    Result.failure(handleErrorResponse(response))
                }
            }
        } catch (e: Exception) {
            Result.failure(handleNetworkException(e))
        }
    }
    
    // Analytics del administrador
    suspend fun getAdminAnalytics(accessToken: String): Result<Map<String, Any>> = withContext(Dispatchers.IO) {
        try {
            println("🌐 [ADMIN_API] Iniciando obtención de analytics del administrador")
            
            val response = httpClient.get("$baseUrl/admin/analytics/reports") {
                contentType(ContentType.Application.Json)
                header("Authorization", "Bearer $accessToken")
            }
            
            println("📥 [ADMIN_API] Respuesta de analytics recibida - Status: ${response.status}")
            
            when (response.status) {
                HttpStatusCode.OK -> {
                    println("✅ [ADMIN_API] Analytics obtenidos exitosamente")
                    val result = response.body<Map<String, Any>>()
                    Result.success(result)
                }
                HttpStatusCode.Unauthorized -> {
                    println("❌ [ADMIN_API] Error de autenticación - Status: ${response.status}")
                    Result.failure(handleErrorResponse(response))
                }
                else -> {
                    println("❌ [ADMIN_API] Error inesperado - Status: ${response.status}")
                    Result.failure(handleErrorResponse(response))
                }
            }
        } catch (e: Exception) {
            Result.failure(handleNetworkException(e))
        }
    }
}
