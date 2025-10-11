package org.sysarp.project.utils

import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
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
     * Mensajes de éxito predefinidos para diferentes acciones
     */
    object Messages {
        const val ADMIN_REGISTERED = "✅ Administrador registrado exitosamente"

        const val LOGIN_SUCCESS = "✅ Inicio de sesión exitoso"
    }
}
