package org.sysarp.project.utils

import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

object SuccessHandler {
    
    /**
     * Muestra un mensaje de éxito que se desvanece automáticamente
     */
    @Composable
    fun ShowSuccessMessage(
        message: String,
        snackbarHostState: SnackbarHostState,
        duration: Long = 3000L // 3 segundos por defecto
    ) {
        val coroutineScope = rememberCoroutineScope()
        
        LaunchedEffect(message) {
            if (message.isNotEmpty()) {
                coroutineScope.launch {
                    snackbarHostState.showSnackbar(
                        message = message,
                        duration = androidx.compose.material3.SnackbarDuration.Short
                    )
                }
            }
        }
    }
    
    /**
     * Muestra un mensaje de éxito con acción personalizada
     */
    @Composable
    fun ShowSuccessMessageWithAction(
        message: String,
        actionLabel: String = "OK",
        snackbarHostState: SnackbarHostState,
        onActionClick: () -> Unit = {},
        duration: Long = 5000L // 5 segundos para mensajes con acción
    ) {
        val coroutineScope = rememberCoroutineScope()
        
        LaunchedEffect(message) {
            if (message.isNotEmpty()) {
                coroutineScope.launch {
                    val result = snackbarHostState.showSnackbar(
                        message = message,
                        actionLabel = actionLabel,
                        duration = androidx.compose.material3.SnackbarDuration.Long
                    )
                    
                    if (result == SnackbarResult.ActionPerformed) {
                        onActionClick()
                    }
                }
            }
        }
    }
    
    /**
     * Mensajes de éxito predefinidos para diferentes acciones
     */
    object Messages {
        const val ADMIN_REGISTERED = "✅ Administrador registrado exitosamente"
        const val SELLER_AFFILIATED = "✅ Vendedor afiliado correctamente"
        const val PROFILE_UPDATED = "✅ Perfil actualizado exitosamente"
        const val PASSWORD_CHANGED = "✅ Contraseña cambiada exitosamente"
        const val QR_GENERATED = "✅ Código QR generado exitosamente"
        const val SELLER_STATUS_CHANGED = "✅ Estado del vendedor actualizado"
        const val SELLER_DELETED = "✅ Vendedor eliminado exitosamente"
        const val DEACTIVATION_REQUESTED = "✅ Solicitud de baja enviada"
        const val DEACTIVATION_APPROVED = "✅ Solicitud de baja aprobada"
        const val DEACTIVATION_REJECTED = "✅ Solicitud de baja rechazada"
        const val LOGIN_SUCCESS = "✅ Inicio de sesión exitoso"
        const val LOGOUT_SUCCESS = "✅ Sesión cerrada exitosamente"
        const val DATA_EXPORTED = "✅ Datos exportados exitosamente"
        const val SETTINGS_SAVED = "✅ Configuración guardada exitosamente"
    }
}
