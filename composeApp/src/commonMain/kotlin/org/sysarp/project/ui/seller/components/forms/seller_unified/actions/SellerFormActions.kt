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
     * Procesa registro y login automáticamente usando el servicio unificado
     * @param coroutineScope Scope de corrutinas
     * @param sellerService Servicio de vendedores
     * @param authService Servicio de autenticación
     * @param affiliationCode Código de afiliación
     * @param sellerName Nombre del vendedor
     * @param phone Teléfono
     * @param isExistingSeller Si es un vendedor existente (deprecated, se maneja automáticamente)
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
        isExistingSeller: Boolean, // Deprecated, se maneja automáticamente
        onLoadingChange: (Boolean) -> Unit,
        onError: (String) -> Unit,
        onSuccess: (String) -> Unit
    ) {
        coroutineScope.launch {
            onLoadingChange(true)
            try {
                // Usar el nuevo servicio unificado que maneja automáticamente login/registro
                val authResult = sellerService.authenticateSellerUnified(
                    phone = phone,
                    affiliationCode = affiliationCode,
                    sellerName = sellerName
                )
                
                authResult.fold(
                    onSuccess = { result: org.sysarp.project.service.seller.SellerAuthUnifiedService.SellerAuthResult ->
                        when (result) {
                            is org.sysarp.project.service.seller.SellerAuthUnifiedService.SellerAuthResult.Success -> {
                                val message = if (result.isNewUser) {
                                    "¡Registro exitoso! Bienvenido a YapeHub"
                                } else {
                                    "¡Bienvenido de vuelta!"
                                }
                                onSuccess(message)
                            }
                            is org.sysarp.project.service.seller.SellerAuthUnifiedService.SellerAuthResult.Error -> {
                                onError(result.message)
                            }
                        }
                        onLoadingChange(false)
                    },
                    onFailure = { error: Throwable ->
                        onError("Error de conexión: ${error.message}")
                        onLoadingChange(false)
                    }
                )
            } catch (e: Exception) {
                onError("Error inesperado: ${e.message}")
                onLoadingChange(false)
            }
        }
    }
    
    /**
     * Procesa registro y login automáticamente usando el servicio unificado (versión simplificada)
     * @param coroutineScope Scope de corrutinas
     * @param sellerService Servicio de vendedores
     * @param affiliationCode Código de afiliación
     * @param sellerName Nombre del vendedor
     * @param phone Teléfono
     * @param onLoadingChange Callback para cambiar estado de carga
     * @param onError Callback para manejar errores
     * @param onSuccess Callback para manejar éxito
     */
    fun processSellerActionUnified(
        coroutineScope: CoroutineScope,
        sellerService: SellerService,
        affiliationCode: String,
        sellerName: String,
        phone: String,
        onLoadingChange: (Boolean) -> Unit,
        onError: (String) -> Unit,
        onSuccess: (String) -> Unit
    ) {
        coroutineScope.launch {
            onLoadingChange(true)
            try {
                // Usar el nuevo servicio unificado que maneja automáticamente login/registro
                val authResult = sellerService.authenticateSellerUnified(
                    phone = phone,
                    affiliationCode = affiliationCode,
                    sellerName = sellerName
                )
                
                authResult.fold(
                    onSuccess = { result: org.sysarp.project.service.seller.SellerAuthUnifiedService.SellerAuthResult ->
                        when (result) {
                            is org.sysarp.project.service.seller.SellerAuthUnifiedService.SellerAuthResult.Success -> {
                                val message = if (result.isNewUser) {
                                    "¡Registro exitoso! Bienvenido a YapeHub"
                                } else {
                                    "¡Bienvenido de vuelta!"
                                }
                                onSuccess(message)
                            }
                            is org.sysarp.project.service.seller.SellerAuthUnifiedService.SellerAuthResult.Error -> {
                                onError(result.message)
                            }
                        }
                        onLoadingChange(false)
                    },
                    onFailure = { error: Throwable ->
                        onError("Error de conexión: ${error.message}")
                        onLoadingChange(false)
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
