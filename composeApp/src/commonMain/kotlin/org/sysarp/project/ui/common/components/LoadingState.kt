package org.sysarp.project.ui.common.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * Estado global para manejar loading en toda la aplicación
 */
class LoadingState {
    private var _isLoading by mutableStateOf(false)
    private var _message by mutableStateOf("")
    private var _canDismiss by mutableStateOf(false)
    
    val isLoading: Boolean get() = _isLoading
    val message: String get() = _message
    val canDismiss: Boolean get() = _canDismiss
    
    /**
     * Muestra el loading con un mensaje
     */
    fun show(
        message: String = LoadingMessages.LOADING,
        canDismiss: Boolean = false
    ) {
        _message = message
        _canDismiss = canDismiss
        _isLoading = true
    }
    
    /**
     * Oculta el loading
     */
    fun hide() {
        _isLoading = false
        _message = ""
        _canDismiss = false
    }
    
    /**
     * Actualiza solo el mensaje sin cambiar el estado de loading
     */
    fun updateMessage(message: String) {
        _message = message
    }
    
    /**
     * Ejecuta una operación con loading automático
     */
    suspend fun <T> withLoading(
        message: String = LoadingMessages.LOADING,
        canDismiss: Boolean = false,
        minDuration: Long = 500L, // Duración mínima para evitar flicker
        operation: suspend () -> T
    ): T {
        show(message, canDismiss)
        val startTime = System.currentTimeMillis()
        
        return try {
            val result = operation()
            
            // Asegurar duración mínima
            val elapsed = System.currentTimeMillis() - startTime
            if (elapsed < minDuration) {
                delay(minDuration - elapsed)
            }
            
            result
        } finally {
            hide()
        }
    }
}

/**
 * Hook para usar el estado de loading
 */
@Composable
fun rememberLoadingState(): LoadingState {
    return remember { LoadingState() }
}

/**
 * Extensión para CoroutineScope para usar loading fácilmente
 */
fun CoroutineScope.launchWithLoading(
    loadingState: LoadingState,
    message: String = LoadingMessages.LOADING,
    canDismiss: Boolean = false,
    minDuration: Long = 500L,
    onError: (Throwable) -> Unit = {},
    block: suspend CoroutineScope.() -> Unit
) {
    launch {
        try {
            loadingState.withLoading(message, canDismiss, minDuration) {
                block()
            }
        } catch (e: Exception) {
            onError(e)
        }
    }
}

/**
 * Composable que maneja el loading overlay automáticamente
 */
@Composable
fun LoadingHandler(
    loadingState: LoadingState,
    onDismiss: () -> Unit = { loadingState.hide() },
    content: @Composable () -> Unit
) {
    content()
    
    LoadingOverlay(
        isVisible = loadingState.isLoading,
        message = loadingState.message,
        canDismiss = loadingState.canDismiss,
        onDismiss = onDismiss
    )
}