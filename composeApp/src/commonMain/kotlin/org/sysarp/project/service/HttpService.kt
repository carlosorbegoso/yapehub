package org.sysarp.project.service

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.auth.*
import io.ktor.client.plugins.auth.providers.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.plugins.logging.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import org.sysarp.project.config.ServerConfig
import org.sysarp.project.data.*
import org.sysarp.project.utils.ErrorHandler

class HttpService {
    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
    }
    
    private val baseUrl = ServerConfig.BASE_URL
    
    private val httpClient = HttpClient {
        install(ContentNegotiation) {
            json(json)
        }
        install(Logging) {
            level = LogLevel.INFO
        }
        install(Auth) {
            bearer {
                loadTokens {
                    val token = TokenManager.getAccessToken()
                    if (token != null) {
                        BearerTokens(token, "")
                    } else {
                        null
                    }
                }
            }
        }
        install(HttpTimeout) {
            requestTimeoutMillis = 30000
            connectTimeoutMillis = 10000
            socketTimeoutMillis = 30000
        }
    }
    
    // Registro de administrador
    suspend fun registerAdmin(request: AdminRegistrationRequest): Result<AdminRegistrationResponse> = withContext(Dispatchers.IO) {
        try {
            println("🌐 [HTTP] Iniciando petición a: $baseUrl/auth/admin/register")
            println("📤 [HTTP] Datos enviados: ${request}")
            
            val response = httpClient.post("$baseUrl/auth/admin/register") {
                contentType(ContentType.Application.Json)
                setBody(request)
            }
            
            println("📥 [HTTP] Respuesta recibida - Status: ${response.status}")
            println("📥 [HTTP] Headers: ${response.headers}")
            
            when (response.status) {
                HttpStatusCode.OK, HttpStatusCode.Created -> {
                    println("✅ [HTTP] Registro exitoso - Status: ${response.status}")
                    val result = response.body<AdminRegistrationResponse>()
                    println("📋 [HTTP] Respuesta del servidor: ${result}")
                    Result.success(result)
                }
                HttpStatusCode.BadRequest, HttpStatusCode.UnprocessableEntity -> {
                    println("❌ [HTTP] Error de validación - Status: ${response.status}")
                    val error = response.body<ApiError>()
                    println("📋 [HTTP] Error del servidor: ${error}")
                    val friendlyMessage = ErrorHandler.getFriendlyErrorMessage(error)
                    Result.failure(Exception(friendlyMessage))
                }
                HttpStatusCode.Conflict -> {
                    println("❌ [HTTP] Conflicto (email duplicado) - Status: ${response.status}")
                    val error = response.body<ApiError>()
                    println("📋 [HTTP] Error del servidor: ${error}")
                    val friendlyMessage = ErrorHandler.getFriendlyErrorMessage(error)
                    Result.failure(Exception(friendlyMessage))
                }
                else -> {
                    println("❌ [HTTP] Error inesperado - Status: ${response.status}")
                    val error = response.body<ApiError>()
                    println("📋 [HTTP] Error del servidor: ${error}")
                    val friendlyMessage = ErrorHandler.getFriendlyErrorMessage(error)
                    Result.failure(Exception(friendlyMessage))
                }
            }
        } catch (e: Exception) {
            println("💥 [HTTP] Excepción capturada: ${e.javaClass.simpleName}")
            println("💥 [HTTP] Mensaje de error: ${e.message}")
            println("💥 [HTTP] Stack trace: ${e.stackTrace.take(5).joinToString("\n")}")
            
            val errorMessage = when {
                e.message?.contains("EPREM") == true -> "Error de conectividad: No se puede conectar al servidor. Verifica la IP y que el servidor esté corriendo."
                e.message?.contains("Connection refused") == true -> "Conexión rechazada: El servidor no está corriendo o no es accesible."
                e.message?.contains("timeout") == true -> "Timeout: El servidor tardó demasiado en responder."
                e.message?.contains("Network is unreachable") == true -> "Red inalcanzable: Verifica tu conexión a internet."
                e.message?.contains("Socket") == true -> "Error de socket: Problema de conectividad de red."
                e.message?.contains("UnknownHostException") == true -> "Host desconocido: No se puede resolver la dirección del servidor."
                else -> "Error de red: ${e.message}"
            }
            
            println("📝 [HTTP] Mensaje de error amigable: $errorMessage")
            Result.failure(Exception(errorMessage))
        }
    }
    
    // Login
    suspend fun login(request: LoginRequest): Result<LoginResponse> = withContext(Dispatchers.IO) {
        try {
            println("🌐 [HTTP] Iniciando login a: $baseUrl/auth/login")
            println("📤 [HTTP] Datos de login enviados: ${request}")
            
            val response = httpClient.post("$baseUrl/auth/login") {
                contentType(ContentType.Application.Json)
                setBody(request)
            }
            
            println("📥 [HTTP] Respuesta de login recibida - Status: ${response.status}")
            println("📥 [HTTP] Headers: ${response.headers}")
            
            when (response.status) {
                HttpStatusCode.OK -> {
                    println("✅ [HTTP] Login exitoso - Status: ${response.status}")
                    val result = response.body<LoginResponse>()
                    println("📋 [HTTP] Respuesta del servidor: ${result}")
                    Result.success(result)
                }
                HttpStatusCode.Unauthorized -> {
                    println("❌ [HTTP] Error de autenticación - Status: ${response.status}")
                    val error = response.body<ApiError>()
                    println("📋 [HTTP] Error del servidor: ${error}")
                    val friendlyMessage = ErrorHandler.getFriendlyErrorMessage(error)
                    Result.failure(Exception(friendlyMessage))
                }
                HttpStatusCode.BadRequest -> {
                    println("❌ [HTTP] Error de validación - Status: ${response.status}")
                    val error = response.body<ApiError>()
                    println("📋 [HTTP] Error del servidor: ${error}")
                    val friendlyMessage = ErrorHandler.getFriendlyErrorMessage(error)
                    Result.failure(Exception(friendlyMessage))
                }
                else -> {
                    println("❌ [HTTP] Error inesperado - Status: ${response.status}")
                    val error = response.body<ApiError>()
                    println("📋 [HTTP] Error del servidor: ${error}")
                    val friendlyMessage = ErrorHandler.getFriendlyErrorMessage(error)
                    Result.failure(Exception(friendlyMessage))
                }
            }
        } catch (e: Exception) {
            println("💥 [HTTP] Excepción en login: ${e.javaClass.simpleName}")
            println("💥 [HTTP] Mensaje de error: ${e.message}")
            println("💥 [HTTP] Stack trace: ${e.stackTrace.take(5).joinToString("\n")}")
            
            val errorMessage = when {
                e.message?.contains("EPREM") == true -> "Error de conectividad: No se puede conectar al servidor. Verifica la IP y que el servidor esté corriendo."
                e.message?.contains("Connection refused") == true -> "Conexión rechazada: El servidor no está corriendo o no es accesible."
                e.message?.contains("timeout") == true -> "Timeout: El servidor tardó demasiado en responder."
                e.message?.contains("Network is unreachable") == true -> "Red inalcanzable: Verifica tu conexión a internet."
                e.message?.contains("Socket") == true -> "Error de socket: Problema de conectividad de red."
                e.message?.contains("UnknownHostException") == true -> "Host desconocido: No se puede resolver la dirección del servidor."
                else -> "Error de red: ${e.message}"
            }
            
            println("📝 [HTTP] Mensaje de error amigable: $errorMessage")
            Result.failure(Exception(errorMessage))
        }
    }
    
    // Refresh token
    suspend fun refreshToken(refreshToken: String): Result<RefreshTokenResponse> = withContext(Dispatchers.IO) {
        try {
            println("🌐 [HTTP] Iniciando refresh token a: $baseUrl/auth/refresh")
            println("📤 [HTTP] RefreshToken: ${refreshToken.take(20)}...")
            
            val response = httpClient.post("$baseUrl/auth/refresh") {
                contentType(ContentType.Application.Json)
                header("X-Auth-Token", refreshToken)
                setBody("{}") // Body vacío como en el curl
            }
            
            println("📥 [HTTP] Respuesta de refresh recibida - Status: ${response.status}")
            println("📥 [HTTP] Headers: ${response.headers}")
            
            when (response.status) {
                HttpStatusCode.OK -> {
                    println("✅ [HTTP] Refresh exitoso - Status: ${response.status}")
                    val result = response.body<RefreshTokenResponse>()
                    println("📋 [HTTP] Respuesta del servidor: ${result}")
                    Result.success(result)
                }
                HttpStatusCode.Unauthorized -> {
                    println("❌ [HTTP] Error de autenticación - Status: ${response.status}")
                    val error = response.body<ApiError>()
                    println("📋 [HTTP] Error del servidor: ${error}")
                    val friendlyMessage = ErrorHandler.getFriendlyErrorMessage(error)
                    Result.failure(Exception(friendlyMessage))
                }
                HttpStatusCode.BadRequest -> {
                    println("❌ [HTTP] Error de validación - Status: ${response.status}")
                    val error = response.body<ApiError>()
                    println("📋 [HTTP] Error del servidor: ${error}")
                    val friendlyMessage = ErrorHandler.getFriendlyErrorMessage(error)
                    Result.failure(Exception(friendlyMessage))
                }
                else -> {
                    println("❌ [HTTP] Error inesperado - Status: ${response.status}")
                    val error = response.body<ApiError>()
                    println("📋 [HTTP] Error del servidor: ${error}")
                    val friendlyMessage = ErrorHandler.getFriendlyErrorMessage(error)
                    Result.failure(Exception(friendlyMessage))
                }
            }
        } catch (e: Exception) {
            println("💥 [HTTP] Excepción en refresh: ${e.javaClass.simpleName}")
            println("💥 [HTTP] Mensaje de error: ${e.message}")
            println("💥 [HTTP] Stack trace: ${e.stackTrace.take(5).joinToString("\n")}")
            
            val errorMessage = when {
                e.message?.contains("EPREM") == true -> "Error de conectividad: No se puede conectar al servidor. Verifica la IP y que el servidor esté corriendo."
                e.message?.contains("Connection refused") == true -> "Conexión rechazada: El servidor no está corriendo o no es accesible."
                e.message?.contains("timeout") == true -> "Timeout: El servidor tardó demasiado en responder."
                e.message?.contains("Network is unreachable") == true -> "Red inalcanzable: Verifica tu conexión a internet."
                e.message?.contains("Socket") == true -> "Error de socket: Problema de conectividad de red."
                e.message?.contains("UnknownHostException") == true -> "Host desconocido: No se puede resolver la dirección del servidor."
                else -> "Error de red: ${e.message}"
            }
            
            println("📝 [HTTP] Mensaje de error amigable: $errorMessage")
            Result.failure(Exception(errorMessage))
        }
    }
    
    // Logout
    suspend fun logout(accessToken: String): Result<LogoutResponse> = withContext(Dispatchers.IO) {
        try {
            println("🌐 [HTTP] Iniciando logout a: $baseUrl/auth/logout")
            println("📤 [HTTP] AccessToken: ${accessToken.take(20)}...")
            
            val response = httpClient.post("$baseUrl/auth/logout") {
                contentType(ContentType.Application.Json)
                header("X-Auth-Token", accessToken)
                setBody("{}") // Body vacío como en el curl
            }
            
            println("📥 [HTTP] Respuesta de logout recibida - Status: ${response.status}")
            println("📥 [HTTP] Headers: ${response.headers}")
            
            when (response.status) {
                HttpStatusCode.OK -> {
                    println("✅ [HTTP] Logout exitoso - Status: ${response.status}")
                    val result = response.body<LogoutResponse>()
                    println("📋 [HTTP] Respuesta del servidor: ${result}")
                    Result.success(result)
                }
                HttpStatusCode.Unauthorized -> {
                    println("❌ [HTTP] Error de autenticación - Status: ${response.status}")
                    val error = response.body<ApiError>()
                    println("📋 [HTTP] Error del servidor: ${error}")
                    val friendlyMessage = ErrorHandler.getFriendlyErrorMessage(error)
                    Result.failure(Exception(friendlyMessage))
                }
                HttpStatusCode.BadRequest -> {
                    println("❌ [HTTP] Error de validación - Status: ${response.status}")
                    val error = response.body<ApiError>()
                    println("📋 [HTTP] Error del servidor: ${error}")
                    val friendlyMessage = ErrorHandler.getFriendlyErrorMessage(error)
                    Result.failure(Exception(friendlyMessage))
                }
                else -> {
                    println("❌ [HTTP] Error inesperado - Status: ${response.status}")
                    val error = response.body<ApiError>()
                    println("📋 [HTTP] Error del servidor: ${error}")
                    val friendlyMessage = ErrorHandler.getFriendlyErrorMessage(error)
                    Result.failure(Exception(friendlyMessage))
                }
            }
        } catch (e: Exception) {
            println("💥 [HTTP] Excepción en logout: ${e.javaClass.simpleName}")
            println("💥 [HTTP] Mensaje de error: ${e.message}")
            println("💥 [HTTP] Stack trace: ${e.stackTrace.take(5).joinToString("\n")}")
            
            val errorMessage = when {
                e.message?.contains("EPREM") == true -> "Error de conectividad: No se puede conectar al servidor. Verifica la IP y que el servidor esté corriendo."
                e.message?.contains("Connection refused") == true -> "Conexión rechazada: El servidor no está corriendo o no es accesible."
                e.message?.contains("timeout") == true -> "Timeout: El servidor tardó demasiado en responder."
                e.message?.contains("Network is unreachable") == true -> "Red inalcanzable: Verifica tu conexión a internet."
                e.message?.contains("Socket") == true -> "Error de socket: Problema de conectividad de red."
                e.message?.contains("UnknownHostException") == true -> "Host desconocido: No se puede resolver la dirección del servidor."
                else -> "Error de red: ${e.message}"
            }
            
            println("📝 [HTTP] Mensaje de error amigable: $errorMessage")
            Result.failure(Exception(errorMessage))
        }
    }
    
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
            println("🌐 [HTTP] Iniciando generación de código de afiliación")
            println("📤 [HTTP] AdminId: $adminId, BranchId: $branchId")
            println("📤 [HTTP] ExpirationHours: $expirationHours, MaxUses: $maxUses")
            println("📤 [HTTP] AccessToken: ${accessToken.take(20)}...")
            
            val url = "$baseUrl/generate-affiliation-code-protected?" +
                    "adminId=$adminId&" +
                    "branchId=$branchId&" +
                    "expirationHours=$expirationHours&" +
                    "maxUses=$maxUses" +
                    if (notes != null) "&notes=$notes" else ""
            
            val response = httpClient.post(url) {
                contentType(ContentType.Application.Json)
                header("Authorization", "Bearer $accessToken")
                setBody("{}") // Body vacío como en el curl
            }
            
            println("📥 [HTTP] Respuesta de generación recibida - Status: ${response.status}")
            println("📥 [HTTP] Headers: ${response.headers}")
            
            when (response.status) {
                HttpStatusCode.OK -> {
                    println("✅ [HTTP] Código generado exitosamente - Status: ${response.status}")
                    val result = response.body<GenerateAffiliationCodeResponse>()
                    println("📋 [HTTP] Respuesta del servidor: ${result}")
                    Result.success(result)
                }
                HttpStatusCode.Unauthorized -> {
                    println("❌ [HTTP] Error de autenticación - Status: ${response.status}")
                    val error = response.body<ApiError>()
                    println("📋 [HTTP] Error del servidor: ${error}")
                    val friendlyMessage = ErrorHandler.getFriendlyErrorMessage(error)
                    Result.failure(Exception(friendlyMessage))
                }
                HttpStatusCode.BadRequest -> {
                    println("❌ [HTTP] Error de validación - Status: ${response.status}")
                    val error = response.body<ApiError>()
                    println("📋 [HTTP] Error del servidor: ${error}")
                    val friendlyMessage = ErrorHandler.getFriendlyErrorMessage(error)
                    Result.failure(Exception(friendlyMessage))
                }
                else -> {
                    println("❌ [HTTP] Error inesperado - Status: ${response.status}")
                    val error = response.body<ApiError>()
                    println("📋 [HTTP] Error del servidor: ${error}")
                    val friendlyMessage = ErrorHandler.getFriendlyErrorMessage(error)
                    Result.failure(Exception(friendlyMessage))
                }
            }
        } catch (e: Exception) {
            println("💥 [HTTP] Excepción en generación de código: ${e.javaClass.simpleName}")
            println("💥 [HTTP] Mensaje de error: ${e.message}")
            println("💥 [HTTP] Stack trace: ${e.stackTrace.take(5).joinToString("\n")}")
            
            val errorMessage = when {
                e.message?.contains("EPREM") == true -> "Error de conectividad: No se puede conectar al servidor. Verifica la IP y que el servidor esté corriendo."
                e.message?.contains("Connection refused") == true -> "Conexión rechazada: El servidor no está corriendo o no es accesible."
                e.message?.contains("timeout") == true -> "Timeout: El servidor tardó demasiado en responder."
                e.message?.contains("Network is unreachable") == true -> "Red inalcanzable: Verifica tu conexión a internet."
                e.message?.contains("Socket") == true -> "Error de socket: Problema de conectividad de red."
                e.message?.contains("UnknownHostException") == true -> "Host desconocido: No se puede resolver la dirección del servidor."
                else -> "Error de red: ${e.message}"
            }
            
            println("📝 [HTTP] Mensaje de error amigable: $errorMessage")
            Result.failure(Exception(errorMessage))
        }
    }
    
    // Registrar vendedor con código de afiliación
    suspend fun registerSeller(
        affiliationCode: String,
        sellerName: String,
        phone: String
    ): Result<SellerRegistrationResponse> = withContext(Dispatchers.IO) {
        try {
            println("🌐 [HTTP] Iniciando registro de vendedor")
            println("📤 [HTTP] AffiliationCode: $affiliationCode")
            println("📤 [HTTP] SellerName: $sellerName")
            println("📤 [HTTP] Phone: $phone")
            
            val request = SellerRegistrationRequest(
                affiliationCode = affiliationCode,
                sellerName = sellerName,
                phone = phone
            )
            
            val response = httpClient.post("$baseUrl/seller/register") {
                contentType(ContentType.Application.Json)
                setBody(request)
            }
            
            println("📥 [HTTP] Respuesta de registro recibida - Status: ${response.status}")
            println("📥 [HTTP] Headers: ${response.headers}")
            
            when (response.status) {
                HttpStatusCode.OK -> {
                    println("✅ [HTTP] Vendedor registrado exitosamente - Status: ${response.status}")
                    val result = response.body<SellerRegistrationResponse>()
                    println("📋 [HTTP] Respuesta del servidor: ${result}")
                    Result.success(result)
                }
                HttpStatusCode.BadRequest -> {
                    println("❌ [HTTP] Error de validación - Status: ${response.status}")
                    try {
                        val error = response.body<ApiError>()
                        println("📋 [HTTP] Error estructurado del servidor: ${error}")
                        val friendlyMessage = ErrorHandler.getFriendlyErrorMessage(error)
                        Result.failure(Exception(friendlyMessage))
                    } catch (e: Exception) {
                        // Si no se puede deserializar como ApiError, intentar como respuesta simple
                        try {
                            val simpleError = response.body<Map<String, Any>>()
                            val message = simpleError["message"] as? String ?: "Error de validación"
                            println("📋 [HTTP] Error simple del servidor: ${simpleError}")
                            val friendlyMessage = ErrorHandler.getFriendlyErrorMessage(message)
                            Result.failure(Exception(friendlyMessage))
                        } catch (e2: Exception) {
                            println("📋 [HTTP] No se pudo deserializar error: ${e2.message}")
                            Result.failure(Exception("Error de validación"))
                        }
                    }
                }
                HttpStatusCode.Conflict -> {
                    println("❌ [HTTP] Error de conflicto - Status: ${response.status}")
                    try {
                        val error = response.body<ApiError>()
                        println("📋 [HTTP] Error del servidor: ${error}")
                        val friendlyMessage = ErrorHandler.getFriendlyErrorMessage(error)
                        Result.failure(Exception(friendlyMessage))
                    } catch (e: Exception) {
                        try {
                            val simpleError = response.body<Map<String, Any>>()
                            val message = simpleError["message"] as? String ?: "Error de conflicto"
                            println("📋 [HTTP] Error simple del servidor: ${simpleError}")
                            Result.failure(Exception(message))
                        } catch (e2: Exception) {
                            println("📋 [HTTP] No se pudo deserializar error: ${e2.message}")
                            Result.failure(Exception("Error de conflicto"))
                        }
                    }
                }
                else -> {
                    println("❌ [HTTP] Error inesperado - Status: ${response.status}")
                    try {
                        val error = response.body<ApiError>()
                        println("📋 [HTTP] Error del servidor: ${error}")
                        val friendlyMessage = ErrorHandler.getFriendlyErrorMessage(error)
                        Result.failure(Exception(friendlyMessage))
                    } catch (e: Exception) {
                        try {
                            val simpleError = response.body<Map<String, Any>>()
                            val message = simpleError["message"] as? String ?: "Error inesperado"
                            println("📋 [HTTP] Error simple del servidor: ${simpleError}")
                            Result.failure(Exception(message))
                        } catch (e2: Exception) {
                            println("📋 [HTTP] No se pudo deserializar error: ${e2.message}")
                            Result.failure(Exception("Error inesperado"))
                        }
                    }
                }
            }
        } catch (e: Exception) {
            println("💥 [HTTP] Excepción en registro de vendedor: ${e.javaClass.simpleName}")
            println("💥 [HTTP] Mensaje de error: ${e.message}")
            println("💥 [HTTP] Stack trace: ${e.stackTrace.take(5).joinToString("\n")}")
            
            val errorMessage = when {
                e.message?.contains("EPREM") == true -> "Error de conectividad: No se puede conectar al servidor. Verifica la IP y que el servidor esté corriendo."
                e.message?.contains("Connection refused") == true -> "Conexión rechazada: El servidor no está corriendo o no es accesible."
                e.message?.contains("timeout") == true -> "Timeout: El servidor tardó demasiado en responder."
                e.message?.contains("Network is unreachable") == true -> "Red inalcanzable: Verifica tu conexión a internet."
                e.message?.contains("Socket") == true -> "Error de socket: Problema de conectividad de red."
                e.message?.contains("UnknownHostException") == true -> "Host desconocido: No se puede resolver la dirección del servidor."
                else -> "Error de red: ${e.message}"
            }
            
            println("📝 [HTTP] Mensaje de error amigable: $errorMessage")
            Result.failure(Exception(errorMessage))
        }
    }
    
    // Login de vendedor por teléfono
    suspend fun sellerLoginByPhone(phone: String): Result<SellerLoginByPhoneResponse> = withContext(Dispatchers.IO) {
        try {
            println("🌐 [HTTP] Iniciando login de vendedor por teléfono")
            println("📤 [HTTP] Phone: $phone")
            
            // Generar identificador único del teléfono
            val phoneFingerprint = generatePhoneFingerprint(phone)
            println("📤 [HTTP] Phone Fingerprint: $phoneFingerprint")
            
            val response = httpClient.post("$baseUrl/auth/seller/login-by-phone?phone=$phone") {
                contentType(ContentType.Application.Json)
                header("X-Phone-Fingerprint", phoneFingerprint)
                setBody("{}") // Body vacío como en el curl
            }
            
            println("📥 [HTTP] Respuesta de login recibida - Status: ${response.status}")
            println("📥 [HTTP] Headers: ${response.headers}")
            
            when (response.status) {
                HttpStatusCode.OK -> {
                    println("✅ [HTTP] Login de vendedor exitoso - Status: ${response.status}")
                    val result = response.body<SellerLoginByPhoneResponse>()
                    println("📋 [HTTP] Respuesta del servidor: ${result}")
                    Result.success(result)
                }
                HttpStatusCode.Unauthorized -> {
                    println("❌ [HTTP] Error de autenticación - Status: ${response.status}")
                    val error = response.body<ApiError>()
                    println("📋 [HTTP] Error del servidor: ${error}")
                    val friendlyMessage = ErrorHandler.getFriendlyErrorMessage(error)
                    Result.failure(Exception(friendlyMessage))
                }
                HttpStatusCode.BadRequest -> {
                    println("❌ [HTTP] Error de validación - Status: ${response.status}")
                    val error = response.body<ApiError>()
                    println("📋 [HTTP] Error del servidor: ${error}")
                    val friendlyMessage = ErrorHandler.getFriendlyErrorMessage(error)
                    Result.failure(Exception(friendlyMessage))
                }
                else -> {
                    println("❌ [HTTP] Error inesperado - Status: ${response.status}")
                    val error = response.body<ApiError>()
                    println("📋 [HTTP] Error del servidor: ${error}")
                    val friendlyMessage = ErrorHandler.getFriendlyErrorMessage(error)
                    Result.failure(Exception(friendlyMessage))
                }
            }
        } catch (e: Exception) {
            println("💥 [HTTP] Excepción en login de vendedor: ${e.javaClass.simpleName}")
            println("💥 [HTTP] Mensaje de error: ${e.message}")
            println("💥 [HTTP] Stack trace: ${e.stackTrace.take(5).joinToString("\n")}")
            
            val errorMessage = when {
                e.message?.contains("EPREM") == true -> "Error de conectividad: No se puede conectar al servidor. Verifica la IP y que el servidor esté corriendo."
                e.message?.contains("Connection refused") == true -> "Conexión rechazada: El servidor no está corriendo o no es accesible."
                e.message?.contains("timeout") == true -> "Timeout: El servidor tardó demasiado en responder."
                e.message?.contains("Network is unreachable") == true -> "Red inalcanzable: Verifica tu conexión a internet."
                e.message?.contains("Socket") == true -> "Error de socket: Problema de conectividad de red."
                e.message?.contains("UnknownHostException") == true -> "Host desconocido: No se puede resolver la dirección del servidor."
                else -> "Error de red: ${e.message}"
            }
            
            println("📝 [HTTP] Mensaje de error amigable: $errorMessage")
            Result.failure(Exception(errorMessage))
        }
    }
    
    // Validar código de afiliación
    suspend fun validateAffiliationCode(affiliationCode: String): Result<ValidateAffiliationCodeResponse> = withContext(Dispatchers.IO) {
        try {
            println("🌐 [HTTP] Iniciando validación de código de afiliación")
            println("📤 [HTTP] AffiliationCode: $affiliationCode")
            
            val request = ValidateAffiliationCodeRequest(affiliationCode = affiliationCode)
            
            val response = httpClient.post("$baseUrl/auth/validate-affiliation-code") {
                contentType(ContentType.Application.Json)
                setBody(request)
            }
            
            println("📥 [HTTP] Respuesta de validación recibida - Status: ${response.status}")
            println("📥 [HTTP] Headers: ${response.headers}")
            
            when (response.status) {
                HttpStatusCode.OK -> {
                    println("✅ [HTTP] Código validado exitosamente - Status: ${response.status}")
                    val result = response.body<ValidateAffiliationCodeResponse>()
                    println("📋 [HTTP] Respuesta del servidor: ${result}")
                    Result.success(result)
                }
                HttpStatusCode.BadRequest -> {
                    println("❌ [HTTP] Error de validación - Status: ${response.status}")
                    val error = response.body<ApiError>()
                    println("📋 [HTTP] Error del servidor: ${error}")
                    val friendlyMessage = ErrorHandler.getFriendlyErrorMessage(error)
                    Result.failure(Exception(friendlyMessage))
                }
                else -> {
                    println("❌ [HTTP] Error inesperado - Status: ${response.status}")
                    val error = response.body<ApiError>()
                    println("📋 [HTTP] Error del servidor: ${error}")
                    val friendlyMessage = ErrorHandler.getFriendlyErrorMessage(error)
                    Result.failure(Exception(friendlyMessage))
                }
            }
        } catch (e: Exception) {
            println("💥 [HTTP] Excepción en validación de código: ${e.javaClass.simpleName}")
            println("💥 [HTTP] Mensaje de error: ${e.message}")
            println("💥 [HTTP] Stack trace: ${e.stackTrace.take(5).joinToString("\n")}")
            
            val errorMessage = when {
                e.message?.contains("EPREM") == true -> "Error de conectividad: No se puede conectar al servidor. Verifica la IP y que el servidor esté corriendo."
                e.message?.contains("Connection refused") == true -> "Conexión rechazada: El servidor no está corriendo o no es accesible."
                e.message?.contains("timeout") == true -> "Timeout: El servidor tardó demasiado en responder."
                e.message?.contains("Network is unreachable") == true -> "Red inalcanzable: Verifica tu conexión a internet."
                e.message?.contains("Socket") == true -> "Error de socket: Problema de conectividad de red."
                e.message?.contains("UnknownHostException") == true -> "Host desconocido: No se puede resolver la dirección del servidor."
                else -> "Error de red: ${e.message}"
            }
            
            println("📝 [HTTP] Mensaje de error amigable: $errorMessage")
            Result.failure(Exception(errorMessage))
        }
    }
    
    // Obtener vendedores del administrador
    suspend fun getAdminSellers(adminId: Int): Result<GetSellersResponse> = withContext(Dispatchers.IO) {
        try {
            println("🌐 [HTTP] Iniciando obtención de vendedores del administrador")
            println("📤 [HTTP] AdminId: $adminId")
            
            val response = httpClient.get("$baseUrl/admin/sellers/my-sellers?adminId=$adminId") {
                contentType(ContentType.Application.Json)
            }
            
            println("📥 [HTTP] Respuesta de vendedores recibida - Status: ${response.status}")
            println("📥 [HTTP] Headers: ${response.headers}")
            
            when (response.status) {
                HttpStatusCode.OK -> {
                    println("✅ [HTTP] Vendedores obtenidos exitosamente - Status: ${response.status}")
                    val result = response.body<GetSellersResponse>()
                    println("📋 [HTTP] Respuesta del servidor: ${result}")
                    Result.success(result)
                }
                HttpStatusCode.Unauthorized -> {
                    println("❌ [HTTP] Error de autenticación - Status: ${response.status}")
                    val error = response.body<ApiError>()
                    println("📋 [HTTP] Error del servidor: ${error}")
                    val friendlyMessage = ErrorHandler.getFriendlyErrorMessage(error)
                    Result.failure(Exception(friendlyMessage))
                }
                HttpStatusCode.BadRequest -> {
                    println("❌ [HTTP] Error de validación - Status: ${response.status}")
                    val error = response.body<ApiError>()
                    println("📋 [HTTP] Error del servidor: ${error}")
                    val friendlyMessage = ErrorHandler.getFriendlyErrorMessage(error)
                    Result.failure(Exception(friendlyMessage))
                }
                else -> {
                    println("❌ [HTTP] Error inesperado - Status: ${response.status}")
                    val error = response.body<ApiError>()
                    println("📋 [HTTP] Error del servidor: ${error}")
                    val friendlyMessage = ErrorHandler.getFriendlyErrorMessage(error)
                    Result.failure(Exception(friendlyMessage))
                }
            }
        } catch (e: Exception) {
            println("💥 [HTTP] Excepción en obtención de vendedores: ${e.javaClass.simpleName}")
            println("💥 [HTTP] Mensaje de error: ${e.message}")
            println("💥 [HTTP] Stack trace: ${e.stackTrace.take(5).joinToString("\n")}")
            
            val errorMessage = when {
                e.message?.contains("EPREM") == true -> "Error de conectividad: No se puede conectar al servidor. Verifica la IP y que el servidor esté corriendo."
                e.message?.contains("Connection refused") == true -> "Conexión rechazada: El servidor no está corriendo o no es accesible."
                e.message?.contains("timeout") == true -> "Timeout: El servidor tardó demasiado en responder."
                e.message?.contains("Network is unreachable") == true -> "Red inalcanzable: Verifica tu conexión a internet."
                e.message?.contains("Socket") == true -> "Error de socket: Problema de conectividad de red."
                e.message?.contains("UnknownHostException") == true -> "Host desconocido: No se puede resolver la dirección del servidor."
                else -> "Error de red: ${e.message}"
            }
            
            println("📝 [HTTP] Mensaje de error amigable: $errorMessage")
            Result.failure(Exception(errorMessage))
        }
    }
    
    // Obtener transacciones
    suspend fun getTransactions(
        page: Int = 1,
        limit: Int = 50,
        startDate: String? = null,
        endDate: String? = null,
        branchId: String? = null,
        sellerId: String? = null,
        status: String = "all"
    ): Result<TransactionsResponse> = withContext(Dispatchers.IO) {
        try {
            val response = httpClient.get("$baseUrl/transactions") {
                parameter("page", page)
                parameter("limit", limit)
                startDate?.let { parameter("startDate", it) }
                endDate?.let { parameter("endDate", it) }
                branchId?.let { parameter("branchId", it) }
                sellerId?.let { parameter("sellerId", it) }
                parameter("status", status)
            }
            
            when (response.status) {
                HttpStatusCode.OK -> {
                    val result = response.body<TransactionsResponse>()
                    Result.success(result)
                }
                else -> {
                    val error = response.body<ApiError>()
                    Result.failure(Exception(error.message))
                }
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    // Confirmar transacción
    suspend fun confirmTransaction(transactionId: String, request: ConfirmTransactionRequest): Result<ConfirmTransactionResponse> = withContext(Dispatchers.IO) {
        try {
            val response = httpClient.post("$baseUrl/transactions/$transactionId/confirm") {
                contentType(ContentType.Application.Json)
                setBody(request)
            }
            
            when (response.status) {
                HttpStatusCode.OK -> {
                    val result = response.body<ConfirmTransactionResponse>()
                    Result.success(result)
                }
                else -> {
                    val error = response.body<ApiError>()
                    Result.failure(Exception(error.message))
                }
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    // Dashboard de administrador
    suspend fun getAdminDashboard(period: String = "today", branchId: String? = null): Result<DashboardResponse> = withContext(Dispatchers.IO) {
        try {
            val response = httpClient.get("$baseUrl/admin/dashboard") {
                parameter("period", period)
                branchId?.let { parameter("branchId", it) }
            }
            
            when (response.status) {
                HttpStatusCode.OK -> {
                    val result = response.body<DashboardResponse>()
                    Result.success(result)
                }
                else -> {
                    val error = response.body<ApiError>()
                    Result.failure(Exception(error.message))
                }
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    // Dashboard de vendedor
    suspend fun getSellerDashboard(period: String = "today"): Result<DashboardResponse> = withContext(Dispatchers.IO) {
        try {
            val response = httpClient.get("$baseUrl/sellers/dashboard") {
                parameter("period", period)
            }
            
            when (response.status) {
                HttpStatusCode.OK -> {
                    val result = response.body<DashboardResponse>()
                    Result.success(result)
                }
                else -> {
                    val error = response.body<ApiError>()
                    Result.failure(Exception(error.message))
                }
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    // Listar vendedores
    suspend fun getSellers(
        page: Int = 1,
        limit: Int = 20,
        branchId: String? = null,
        status: String = "all"
    ): Result<SellersResponse> = withContext(Dispatchers.IO) {
        try {
            val response = httpClient.get("$baseUrl/admin/sellers") {
                parameter("page", page)
                parameter("limit", limit)
                branchId?.let { parameter("branchId", it) }
                parameter("status", status)
            }
            
            when (response.status) {
                HttpStatusCode.OK -> {
                    val result = response.body<SellersResponse>()
                    Result.success(result)
                }
                else -> {
                    val error = response.body<ApiError>()
                    Result.failure(Exception(error.message))
                }
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    // Generar código QR
    suspend fun generateQR(request: QRGenerationRequest): Result<QRGenerationResponse> = withContext(Dispatchers.IO) {
        try {
            val response = httpClient.post("$baseUrl/admin/qr/generate") {
                contentType(ContentType.Application.Json)
                setBody(request)
            }
            
            when (response.status) {
                HttpStatusCode.OK, HttpStatusCode.Created -> {
                    val result = response.body<QRGenerationResponse>()
                    Result.success(result)
                }
                else -> {
                    val error = response.body<ApiError>()
                    Result.failure(Exception(error.message))
                }
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    // Generar código de afiliación
    suspend fun generateAffiliationCode(request: AffiliationCodeRequest): Result<AffiliationCodeResponse> = withContext(Dispatchers.IO) {
        try {
            val response = httpClient.post("$baseUrl/admin/affiliation-codes/generate") {
                contentType(ContentType.Application.Json)
                setBody(request)
            }
            
            when (response.status) {
                HttpStatusCode.OK, HttpStatusCode.Created -> {
                    val result = response.body<AffiliationCodeResponse>()
                    Result.success(result)
                }
                else -> {
                    val error = response.body<ApiError>()
                    Result.failure(Exception(error.message))
                }
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    // Enviar notificación
    suspend fun sendNotification(request: NotificationRequest): Result<NotificationResponse> = withContext(Dispatchers.IO) {
        try {
            val response = httpClient.post("$baseUrl/notifications/send") {
                contentType(ContentType.Application.Json)
                setBody(request)
            }
            
            when (response.status) {
                HttpStatusCode.OK -> {
                    val result = response.body<NotificationResponse>()
                    Result.success(result)
                }
                else -> {
                    val error = response.body<ApiError>()
                    Result.failure(Exception(error.message))
                }
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    fun close() {
        httpClient.close()
    }
    
    // Generar identificador único del teléfono
    private fun generatePhoneFingerprint(phone: String): String {
        // Combinar el teléfono con información del dispositivo y timestamp
        val deviceInfo = "android_device_${System.currentTimeMillis()}"
        val combined = "${phone}_${deviceInfo}_${System.currentTimeMillis()}"
        
        // Generar hash simple (en producción usar algoritmo más robusto)
        return "phone_fp_${combined.hashCode().toString().replace("-", "")}"
    }
}

// Singleton para manejo de tokens
object TokenManager {
    private var accessToken: String? = null
    private var refreshToken: String? = null
    
    fun saveTokens(accessToken: String, refreshToken: String) {
        this.accessToken = accessToken
        this.refreshToken = refreshToken
        // TODO: Implementar almacenamiento seguro (Android Keystore, iOS Keychain)
    }
    
    fun getAccessToken(): String? = accessToken
    
    fun getRefreshToken(): String? = refreshToken
    
    fun clearTokens() {
        accessToken = null
        refreshToken = null
        // TODO: Limpiar almacenamiento seguro
    }
}
