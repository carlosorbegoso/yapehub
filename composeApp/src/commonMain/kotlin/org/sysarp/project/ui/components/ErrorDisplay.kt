package org.sysarp.project.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.sysarp.project.data.ApiError
import org.sysarp.project.utils.ErrorHandler
import kotlinx.serialization.json.Json

/**
 * Componente reutilizable para mostrar errores de manera amigable
 */
@Composable
fun ErrorDisplay(
    errorMessage: String,
    modifier: Modifier = Modifier,
    showIcon: Boolean = true,
    isValidationError: Boolean = true
) {
    if (errorMessage.isNotEmpty()) {
        Card(
            modifier = modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.errorContainer
            ),
            shape = RoundedCornerShape(12.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (showIcon) {
                    Icon(
                        imageVector = Icons.Filled.Error,
                        contentDescription = "Error",
                        tint = MaterialTheme.colorScheme.onErrorContainer,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                }
                
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = "Error de validación",
                        color = MaterialTheme.colorScheme.onErrorContainer,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                    
                    Spacer(modifier = Modifier.height(4.dp))
                    
                    Text(
                        text = if (isValidationError) {
                            parseApiError(errorMessage)
                        } else {
                            errorMessage
                        },
                        color = MaterialTheme.colorScheme.onErrorContainer,
                        fontSize = 13.sp,
                        lineHeight = 18.sp
                    )
                }
            }
        }
    }
}

/**
 * Componente para mostrar errores de validación específicos con iconos
 */
@Composable
fun ValidationErrorDisplay(
    errorMessage: String,
    modifier: Modifier = Modifier
) {
    if (errorMessage.isNotEmpty()) {
        Card(
            modifier = modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.errorContainer
            ),
            shape = RoundedCornerShape(12.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Filled.Warning,
                        contentDescription = "Advertencia",
                        tint = MaterialTheme.colorScheme.onErrorContainer,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Revisa los siguientes campos:",
                        color = MaterialTheme.colorScheme.onErrorContainer,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Text(
                    text = parseApiError(errorMessage),
                    color = MaterialTheme.colorScheme.onErrorContainer,
                    fontSize = 13.sp,
                    lineHeight = 18.sp
                )
            }
        }
    }
}

/**
 * Componente para mostrar errores de conexión
 */
@Composable
fun ConnectionErrorDisplay(
    errorMessage: String,
    modifier: Modifier = Modifier,
    onRetry: (() -> Unit)? = null
) {
    if (errorMessage.isNotEmpty()) {
        Card(
            modifier = modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.errorContainer
            ),
            shape = RoundedCornerShape(12.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    imageVector = Icons.Filled.WifiOff,
                    contentDescription = "Sin conexión",
                    tint = MaterialTheme.colorScheme.onErrorContainer,
                    modifier = Modifier.size(32.dp)
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Text(
                    text = "Error de conexión",
                    color = MaterialTheme.colorScheme.onErrorContainer,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )
                
                Spacer(modifier = Modifier.height(4.dp))
                
                Text(
                    text = "Verifica tu conexión a internet e intenta nuevamente",
                    color = MaterialTheme.colorScheme.onErrorContainer,
                    fontSize = 13.sp,
                    textAlign = TextAlign.Center
                )
                
                if (onRetry != null) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Button(
                        onClick = onRetry,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.error,
                            contentColor = MaterialTheme.colorScheme.onError
                        ),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Refresh,
                            contentDescription = "Reintentar",
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Reintentar")
                    }
                }
            }
        }
    }
}

/**
 * Función auxiliar para parsear errores de API y convertirlos a mensajes amigables
 */
private fun parseApiError(errorMessage: String): String {
    return try {
        // Intentar parsear como ApiError
        val apiError = Json.decodeFromString<ApiError>(errorMessage)
        
        // Si hay errores de validación específicos, mostrarlos
        val validationErrors = apiError.details?.validationErrors
        if (validationErrors != null && validationErrors.isNotEmpty()) {
            val friendlyErrors = validationErrors.map { (field, error) ->
                val fieldName = getFieldDisplayName(field)
                "$fieldName: ${getFriendlyMessage(error.message)}"
            }
            friendlyErrors.joinToString("; ")
        } else {
            // Usar el mensaje general del error
            apiError.message
        }
    } catch (e: Exception) {
        // Si no se puede parsear como ApiError, usar el mensaje original
        errorMessage
    }
}

/**
 * Convierte nombres de campos técnicos a nombres amigables
 */
private fun getFieldDisplayName(field: String): String {
    return when {
        field.contains("phone", ignoreCase = true) -> "Teléfono"
        field.contains("email", ignoreCase = true) -> "Email"
        field.contains("name", ignoreCase = true) -> "Nombre"
        field.contains("password", ignoreCase = true) -> "Contraseña"
        field.contains("affiliation", ignoreCase = true) -> "Código de afiliación"
        field.contains("business", ignoreCase = true) -> "Negocio"
        field.contains("ruc", ignoreCase = true) -> "RUC"
        field.contains("address", ignoreCase = true) -> "Dirección"
        else -> "Campo"
    }
}

/**
 * Convierte mensajes técnicos a mensajes amigables
 */
private fun getFriendlyMessage(message: String): String {
    return when {
        message.contains("Invalid phone number format", ignoreCase = true) -> 
            "El formato del teléfono no es válido. Debe contener solo números"
        message.contains("Invalid email format", ignoreCase = true) -> 
            "El formato del email no es válido"
        message.contains("Required field", ignoreCase = true) -> 
            "Este campo es obligatorio"
        message.contains("Too short", ignoreCase = true) -> 
            "El texto es muy corto"
        message.contains("Too long", ignoreCase = true) -> 
            "El texto es muy largo"
        message.contains("Invalid format", ignoreCase = true) -> 
            "El formato no es válido"
        else -> message
    }
}
