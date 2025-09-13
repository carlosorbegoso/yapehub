package org.sysarp.project.utils

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue

object GenericErrorHandler {
    
    /**
     * Maneja errores de API de forma genérica y muestra un diálogo de error
     */
    @Composable
    fun ShowErrorDialog(
        error: Throwable?,
        onDismiss: () -> Unit = {},
        onRetry: (() -> Unit)? = null,
        title: String = "Error",
        showRetryButton: Boolean = true
    ) {
        if (error != null) {
            var showDialog by remember { mutableStateOf(true) }
            
            if (showDialog) {
                AlertDialog(
                    onDismissRequest = {
                        showDialog = false
                        onDismiss()
                    },
                    title = {
                        Text(
                            text = title,
                            style = MaterialTheme.typography.headlineSmall,
                            color = MaterialTheme.colorScheme.error
                        )
                    },
                    text = {
                        Text(
                            text = getFriendlyErrorMessage(error),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    },
                    confirmButton = {
                        if (showRetryButton && onRetry != null) {
                            Button(
                                onClick = {
                                    showDialog = false
                                    onRetry()
                                },
                                colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.primary
                                )
                            ) {
                                Text("Reintentar")
                            }
                        }
                    },
                    dismissButton = {
                        TextButton(
                            onClick = {
                                showDialog = false
                                onDismiss()
                            },
                            colors = androidx.compose.material3.ButtonDefaults.textButtonColors(
                                contentColor = MaterialTheme.colorScheme.onSurface
                            )
                        ) {
                            Text("Cerrar")
                        }
                    },
                    containerColor = MaterialTheme.colorScheme.surface
                )
            }
        }
    }
    
    /**
     * Convierte errores técnicos en mensajes amigables para el usuario
     */
    fun getFriendlyErrorMessage(error: Throwable?): String {
        if (error == null) return "Error desconocido"
        
        return when {
            // Errores de conectividad
            error.message?.contains("EPREM") == true -> 
                "Error de conectividad: No se puede conectar al servidor. Verifica tu conexión a internet."
            
            error.message?.contains("Connection refused") == true -> 
                "Conexión rechazada: El servidor no está disponible en este momento."
            
            error.message?.contains("timeout") == true -> 
                "Timeout: El servidor tardó demasiado en responder. Intenta nuevamente."
            
            error.message?.contains("Network is unreachable") == true -> 
                "Red inalcanzable: Verifica tu conexión a internet."
            
            error.message?.contains("Socket") == true -> 
                "Error de conexión: Problema de conectividad de red."
            
            error.message?.contains("UnknownHostException") == true -> 
                "Servidor no encontrado: No se puede resolver la dirección del servidor."
            
            error.message?.contains("Permission denied") == true -> 
                "Permisos insuficientes: La aplicación no tiene permisos para acceder a internet."
            
            // Errores de serialización JSON
            error.message?.contains("JsonConvertException") == true -> 
                "Error de datos: El servidor devolvió información en formato incorrecto. Contacta al soporte técnico."
            
            error.message?.contains("Illegal input") == true -> 
                "Error de datos: La información recibida del servidor no es válida. Intenta nuevamente."
            
            error.message?.contains("Fields") == true && error.message?.contains("are required") == true -> 
                "Error de datos: Faltan campos requeridos en la respuesta del servidor."
            
            // Errores de validación
            error.message?.contains("Validation failed") == true -> 
                "Error de validación: Los datos ingresados no son válidos. Revisa la información."
            
            error.message?.contains("already exists") == true -> 
                "Datos duplicados: La información ingresada ya existe en el sistema."
            
            error.message?.contains("Invalid email format") == true -> 
                "Email inválido: El formato del email no es correcto."
            
            error.message?.contains("Password must be") == true -> 
                "Contraseña inválida: La contraseña no cumple con los requisitos de seguridad."
            
            error.message?.contains("RUC must be") == true -> 
                "RUC inválido: El RUC debe tener el formato correcto."
            
            // Errores de autenticación
            error.message?.contains("Unauthorized") == true -> 
                "No autorizado: Tu sesión ha expirado. Inicia sesión nuevamente."
            
            error.message?.contains("Forbidden") == true -> 
                "Acceso denegado: No tienes permisos para realizar esta acción."
            
            error.message?.contains("Not Found") == true -> 
                "Recurso no encontrado: La información solicitada no existe."
            
            // Errores del servidor
            error.message?.contains("Internal Server Error") == true -> 
                "Error del servidor: Hay un problema temporal en el servidor. Intenta más tarde."
            
            error.message?.contains("Service Unavailable") == true -> 
                "Servicio no disponible: El servidor está temporalmente fuera de servicio."
            
            // Error genérico
            else -> error.message ?: "Error desconocido. Intenta nuevamente o contacta al soporte técnico."
        }
    }
    
    /**
     * Maneja errores de forma silenciosa (solo logging)
     */
    fun logError(error: Throwable?, context: String = "") {
        if (error != null) {
            println("🚨 [ERROR] $context: ${error.javaClass.simpleName}")
            println("🚨 [ERROR] Mensaje: ${error.message}")
            println("🚨 [ERROR] Stack trace: ${error.stackTrace.take(3).joinToString("\n")}")
        }
    }
}
