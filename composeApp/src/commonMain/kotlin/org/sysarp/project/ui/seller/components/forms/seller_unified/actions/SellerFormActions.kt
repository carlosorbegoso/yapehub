package org.sysarp.project.ui.components.seller_unified.actions

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import org.sysarp.project.service.SellerService
import org.sysarp.project.service.auth.AuthService

/**
 * Acciones y lógica de procesamiento para el formulario de vendedor
 */
object SellerFormActions {
    
    /**
     * Verifica si el código de afiliación existe
     * @param coroutineScope Scope de corrutinas
     * @param sellerService Servicio de vendedores
     * @param affiliationCode Código a verificar
     * @param onLoadingChange Callback para cambiar estado de carga
     * @param onError Callback para manejar errores
     * @param onShowFields Callback para mostrar campos adicionales
     */
    fun checkAffiliationCode(
        coroutineScope: CoroutineScope,
        sellerService: SellerService,
        affiliationCode: String,
        onLoadingChange: (Boolean) -> Unit,
        onError: (String) -> Unit,
        onShowFields: (Boolean) -> Unit
    ) {
        coroutineScope.launch {
            onLoadingChange(true)
            try {
                // Simular verificación del código
                // En una implementación real, aquí harías una llamada a la API
                kotlinx.coroutines.delay(1000) // Simular delay de red
                
                // Por ahora, asumimos que si el código tiene más de 3 caracteres, existe
                val codeExists = affiliationCode.length > 3
                
                onShowFields(codeExists)
                onLoadingChange(false)
            } catch (e: Exception) {
                onError("Error verificando código: ${e.message}")
                onLoadingChange(false)
            }
        }
    }
    
    /**
     * Procesa registro y login automáticamente
     * @param coroutineScope Scope de corrutinas
     * @param sellerService Servicio de vendedores
     * @param authService Servicio de autenticación
     * @param affiliationCode Código de afiliación
     * @param sellerName Nombre del vendedor
     * @param phone Teléfono
     * @param isExistingSeller Si es un vendedor existente
     * @param onLoadingChange Callback para cambiar estado de carga
     * @param onError Callback para manejar errores
     * @param onSuccess Callback para manejar éxito
     */
    fun processSellerAction(
        coroutineScope: CoroutineScope,
        sellerService: SellerService,
        authService: AuthService,
        affiliationCode: String,
        sellerName: String,
        phone: String,
        isExistingSeller: Boolean,
        onLoadingChange: (Boolean) -> Unit,
        onError: (String) -> Unit,
        onSuccess: (String) -> Unit
    ) {
        coroutineScope.launch {
            onLoadingChange(true)
            try {
                // Intentar registro primero (tu lógica es correcta)
                val registerResult = sellerService.registerSeller(
                    affiliationCode = affiliationCode,
                    sellerName = sellerName,
                    phone = phone
                )
                
                registerResult.fold(
                    onSuccess = { registrationResponse ->
                        // Registro exitoso, el servidor ya devuelve el token directamente
                        onSuccess("¡Registro exitoso! Bienvenido a YapeChamo")
                        onLoadingChange(false)
                    },
                    onFailure = { registerError ->
                        // Verificar si el error es porque el vendedor ya existe
                        val isSellerAlreadyExists = registerError.message?.let { message ->
                            message.contains("already exists", ignoreCase = true) ||
                            message.contains("duplicate", ignoreCase = true) ||
                            message.contains("phone already registered", ignoreCase = true) ||
                            message.contains("seller already exists", ignoreCase = true) ||
                            message.contains("ya está registrado", ignoreCase = true)
                        } ?: false
                        
                        if (isSellerAlreadyExists) {
                            // El vendedor ya existe, intentar login directamente
                            val loginResult = sellerService.loginSellerByPhone(phone, affiliationCode)
                            
                            loginResult.fold(
                                onSuccess = { loginResponse ->
                                    onSuccess("¡Bienvenido de vuelta!")
                                    onLoadingChange(false)
                                },
                                onFailure = { loginError ->
                                    onError("Error de acceso: ${loginError.message}")
                                    onLoadingChange(false)
                                }
                            )
                        } else {
                            // Mostrar directamente el mensaje del servidor
                            onError("Error en el registro: ${registerError.message}")
                            onLoadingChange(false)
                        }
                    }
                )
            } catch (e: Exception) {
                onError("Error inesperado: ${e.message}")
                onLoadingChange(false)
            }
        }
    }
    
    /**
     * Refresca los datos del vendedor
     * @param coroutineScope Scope de corrutinas
     * @param statsService Servicio de estadísticas
     */
    fun refreshSellerData(
        coroutineScope: CoroutineScope,
        statsService: org.sysarp.project.service.stats.StatsService
    ) {
        coroutineScope.launch {
            // Aquí se pueden agregar llamadas para refrescar estadísticas del vendedor
            // Por ejemplo: statsService.getSellerSummary(), etc.
        }
    }
    
    /**
     * Maneja el cambio de valor en un campo con limpieza de errores
     * @param newValue Nuevo valor
     * @param onValueChange Callback para cambiar el valor
     * @param onErrorClear Callback para limpiar errores
     */
    fun handleFieldValueChange(
        newValue: String,
        onValueChange: (String) -> Unit,
        onErrorClear: () -> Unit
    ) {
        onValueChange(newValue)
        onErrorClear()
    }
    
    /**
     * Sanitiza y procesa los datos del formulario antes del envío
     * @param affiliationCode Código de afiliación
     * @param sellerName Nombre del vendedor
     * @param phone Teléfono
     * @return Triple con los datos sanitizados
     */
    fun sanitizeFormData(
        affiliationCode: String,
        sellerName: String,
        phone: String
    ): Triple<String, String, String> {
        val sanitizedAffiliationCode = affiliationCode.trim().uppercase()
        val sanitizedSellerName = sellerName.trim().replace(Regex("\\s+"), " ")
        val sanitizedPhone = phone.trim()
        
        return Triple(sanitizedAffiliationCode, sanitizedSellerName, sanitizedPhone)
    }
}
